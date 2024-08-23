package semantic.ASTNodes;

import semantic.Type;

public class ParameterNode extends ASTNode{
    private String name;
    private TypeNode type;

    public ParameterNode(String name_, TypeNode type_) {
        this.name = name_;
        this.type = type_;
    }

    public String getName() { return name; }
    public Type getType() { return type.getType(); }
    public TypeNode getTypeNode() { return type; }
    public void notifyParent() { type.setParent(this); }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
