package IR.IRStmts;

import semantic.ASTNodes.ClassNode;
import semantic.ASTNodes.FunctionNode;
import semantic.ASTNodes.ParameterNode;

import java.util.ArrayList;

public class FunctionDeclarationStmt extends IRStmt {
    public String name;
    public BasicIRType returnType;
    public ArrayList<BasicIRType> argTypes;
    public ArrayList<String> argNames;

    public FunctionDeclarationStmt(String name) {
        this.name = name;
        this.returnType = new BasicIRType("void");
        this.argTypes = new ArrayList<>();
        this.argNames = new ArrayList<>();
    }

    public FunctionDeclarationStmt(FunctionNode function) {
        this.name = function.getName();
        this.returnType = new BasicIRType(function.getReturnType());
        this.argTypes = new ArrayList<>();
        this.argNames = new ArrayList<>();
        for (ParameterNode arg : function.getParameters()) {
            this.argTypes.add(new BasicIRType(arg.getType()));
            this.argNames.add(arg.getName());
        }
    }

    public FunctionDeclarationStmt(ClassNode classNode, FunctionNode function) {
        this.name = classNode.name + "." + function.getName();
        this.returnType = new BasicIRType(function.getReturnType());
        this.argTypes = new ArrayList<>();
        this.argNames = new ArrayList<>();
        this.argTypes.add(new BasicIRType("ptr"));
        this.argNames.add("this");
        for (ParameterNode arg : function.getParameters()) {
            this.argTypes.add(new BasicIRType(arg.getType()));
            this.argNames.add(arg.getName());
        }
    }

    @Override
    public String getDest() { return null; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("declare ").append(returnType.toString()).append(" @").append(name).append("(");
        for (int i = 0; i < argTypes.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(argTypes.get(i).toString()).append(" %").append(i);
        }
        sb.append(")");
        return sb.toString();
    }
}
