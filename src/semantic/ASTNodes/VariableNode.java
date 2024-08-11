package semantic.ASTNodes;

import semantic.Type;

public class VariableNode extends ASTNode{
    private String name;
    private TypeNode type;
    private ExpressionNode value;

    public VariableNode(String name_, TypeNode type_, ExpressionNode value_) {
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

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
