package semantic.ASTNodes;

import semantic.Type;

public class VariableNode extends ASTNode{
    private String name;
    private TypeNode type;
    private ExpressionNode value;

//    private boolean isGlobal;
//    private int multiplier;

    public VariableNode(TypeNode type_, String name_) {
        this.name = name_;
        this.type = type_;
        this.value = null;

//        this.isGlobal = false;
//        this.multiplier = 1;
    }
    public VariableNode(TypeNode type_, String name_, ExpressionNode value_) {
        this.name = name_;
        this.type = type_;
        this.value = value_;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type.getType();
    }
    public TypeNode getTypeNode() {
        return type;
    }

    public ExpressionNode getValue() {
        return value;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
