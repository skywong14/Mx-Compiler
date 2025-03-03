package optimize;

import IR.IRStmts.*;
import optimize.optimizations.*;

import java.util.ArrayList;

// blocks for optimization
public class IRCode{
    private static void debug(String msg) {
        System.out.println("; [IRCode]: " + msg);
    }

    DeclarationStmt declarationStmt;
    public ArrayList<IRFunction> funcStmts = new ArrayList<>();
    public ArrayList<StringDeclareStmt> stringDeclarations = new ArrayList<>();
    public ArrayList<ClassTypeDefineStmt> classTypeDefineStmts = new ArrayList<>();
    public ArrayList<GlobalVariableDeclareStmt> globalVariables = new ArrayList<>();

    public IRCode() {}

    public void initDeclaration(DeclarationStmt declaration) {
        declarationStmt = declaration;
    }
    public void addFunction(FunctionImplementStmt func) {
        funcStmts.add(new IRFunction(func));
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        // Declaration
        sb.append(declarationStmt.toString()).append("\n");

        // StringConstants
        for (IRStmt constantStmt : stringDeclarations) {
            sb.append(constantStmt.toString()).append("\n");
        }

        // GlobalVariables
        for (GlobalVariableDeclareStmt globalVariableDeclareStmt : globalVariables) {
            sb.append(globalVariableDeclareStmt.toString()).append("\n");
        }

        // ClassTypeDefineStmt
        for (ClassTypeDefineStmt classTypeDefineStmt : classTypeDefineStmts) {
            sb.append(classTypeDefineStmt.toString()).append("\n");
        }

        // Functions, Methods
        for (IRFunction func : funcStmts) {
            sb.append(func.toString()).append("\n");
        }

        return sb.toString();
    }

    public void mem2reg() {
        // mem2reg in IR, add phi_stmts
        for (IRFunction func : funcStmts) {
            func.mem2reg();
            func.addPhi();
        }
    }

    public void optimize() {
        // Constant Folding
        new ConstantFolding().optimize(this);

        // CommonSubexpr Elimination
        new CommonSubexpr().optimize(this);

        // Function Inline
        new FunctionInline().optimize(this);

        // Dead Code Elimination
        new DCE().optimize(this);

        // Sparse Conditional Constant Propagation (SCCP)
        new ConstantPropagation().optimize(this); // need SSA form

        // Dead Code Elimination
        new DCE().optimize(this);

        // Global2Local
        new Global2Local().optimize(this); // ATTENTION: G2L will change the SSA form

        System.out.println(commentType(toString()));
    }

    public void erasePhi() {
        for (IRFunction func : funcStmts) {
            func.erasePhi();
        }
    }

    public static String commentType(String s) {
        StringBuilder sb = new StringBuilder();
        String[] lines = s.split("\n");
        for (String line : lines) {
            sb.append("# ").append(line).append("\n");
        }
        return sb.toString();
    } // for debug
}
