package semantic.ASTNodes;

public class ParameterNode extends ASTNode{
    private String name;
    private TypeNode type;
    private ExpressionNode value;

    public ParameterNode(String name_, TypeNode type_) {
        this.name = name_;
        this.type = type_;
        this.value = null;
    }

    public String getName() {
        return name;
    }
    public TypeNode getType() {
        return type;
    }
    public void setValue(ExpressionNode value_) {
        this.value = value_;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
