package semantic.ASTNodes;

import java.util.ArrayList;

public class ProgramNode extends ASTNode {
    private ArrayList<ClassDeclarationNode> classes;
    private ArrayList<FunctionDeclarationNode> functions;
    private ArrayList<VariableDeclarationNode> variables;

    public ProgramNode() {
        this.classes = new ArrayList<>();
        this.functions = new ArrayList<>();
        this.variables = new ArrayList<>();
    }
    public void addFunction(FunctionDeclarationNode function) {
        functions.add(function);
    }
    public void addClass(ClassDeclarationNode class_) {
        classes.add(class_);
    }
    public void addVariable(VariableDeclarationNode variable_) {
        variables.add(variable_);
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
