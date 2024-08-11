package semantic.ASTNodes;

import java.util.ArrayList;

public class ProgramNode extends ASTNode {
    private ArrayList<ClassNode> classes;
    private ArrayList<FunctionNode> functions;
    private ArrayList<VariableNode> variables;

    public ProgramNode() {
        this.classes = new ArrayList<>();
        this.functions = new ArrayList<>();
        this.variables = new ArrayList<>();
    }
    public void addFunction(FunctionNode function) {
        functions.add(function);
    }
    public void addClass(ClassNode class_) {
        classes.add(class_);
    }
    public void addVariable(VariableNode variable_) {
        variables.add(variable_);
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
