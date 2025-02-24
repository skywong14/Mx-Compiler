package optimize;

import IR.IRBuilder;
import IR.IRStmts.*;
import optimize.earlyOptimization.EarlyOptim;
import optimize.redundancyElimination.CommonSubexpr;

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

    public IRCode() {
    }

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

    void inlineInitCall() {
        IRFunction initCallFunc = null;
        IRFunction mainFunc = null;
        for (IRFunction func : funcStmts)
            if (func.name.equals("__Mx_global_var_init__"))
                initCallFunc = func;
            else if (func.name.equals("main"))
                mainFunc = func;
        if (initCallFunc == null || mainFunc == null) throw new RuntimeException("No main function / __Mx_global_var_init__ function found");
        if (initCallFunc.blocks.size() == 1 && initCallFunc.blocks.get(0).stmts.size() == 1) {
            funcStmts.remove(initCallFunc);
            for (IRStmt stmt : mainFunc.blocks.get(0).stmts)
                if (stmt instanceof CallStmt callStmt && callStmt.funcName.equals("__Mx_global_var_init__")) {
                    mainFunc.blocks.get(0).stmts.remove(stmt);
                    break;
            }
            return;
        }
    }

    public void optimize() {
        // mem2reg in IR, add phi_stmts
        for (IRFunction func : funcStmts) {
            func.mem2reg();
            func.addPhi();
        }

        // early optimize
        new EarlyOptim().optimize(this);

        // CommonSubexpr in IR
        new CommonSubexpr().optimize(this);

        inlineInitCall(); // only inline __Mx_global_var_init__ function to main function

        // dead code elimination in IR
        for (IRFunction func : funcStmts) {
            func.DCE();
        }

        // aggressive dead code elimination
        for (IRFunction func : funcStmts) {
            func.aggressiveDCE();
        }

        // global2local in IR
        for (IRFunction func : funcStmts) {
            func.global2local();
        }
    }

    public void erasePhi() {
        for (IRFunction func : funcStmts) {
            func.erasePhi();
        }
    }
}
