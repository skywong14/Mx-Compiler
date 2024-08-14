package semantic.ASTNodes;

import java.util.ArrayList;

public class FieldDeclarationNode extends ASTNode{
    private TypeNode type;
    private ArrayList<String> names;

    public FieldDeclarationNode(TypeNode type_){
        this.type = type_;
        this.names = new ArrayList<>();
    }

    public void addName(String name) {
        names.add(name);
    }
    public ArrayList<String> getNames() {
        return names;
    }

    public TypeNode getType() {
        return type;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
