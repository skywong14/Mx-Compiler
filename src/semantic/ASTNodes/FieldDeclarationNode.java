package semantic.ASTNodes;

import semantic.Type;

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
    public int getSize() { return names.size(); }
    public ArrayList<String> getNames() { return names;  }
    public TypeNode getTypeNode() { return type; }
    public Type getType() { return type.getType(); }
    public void notifyParent() { type.setParent(this); }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
