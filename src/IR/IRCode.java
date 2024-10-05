package IR;

import IR.IRStmts.*;
import optimize.IRFunction;

import java.util.ArrayList;

// blocks for optimization
public class IRCode{
    private static void debug(String msg) {
//        System.out.println("IRCode: " + msg);
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

    public void optimize() {
        // mem2reg
        for (IRFunction func : funcStmts) {
            debug(func.name);
            func.mem2reg();
        }
        debug("mem2reg finished");
    }
}
