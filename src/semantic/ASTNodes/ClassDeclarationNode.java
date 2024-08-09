package semantic.ASTNodes;

import java.util.ArrayList;

public class ClassDeclarationNode extends ASTNode {
    private String name;
    private ArrayList<VariableDeclarationNode> fields;
    private ArrayList<FunctionDeclarationNode> methods;

    public ClassDeclarationNode(String name_, ArrayList<VariableDeclarationNode> fields_, ArrayList<FunctionDeclarationNode> methods_) {
        this.name = name_;
        this.fields = fields_;
        this.methods = methods_;
    }

    public String getName() {
        return name;
    }

    public ArrayList<VariableDeclarationNode> getFields() {
        return fields;
    }

    public ArrayList<FunctionDeclarationNode> getMethods() {
        return methods;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
