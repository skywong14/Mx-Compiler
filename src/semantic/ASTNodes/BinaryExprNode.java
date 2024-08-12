package semantic.ASTNodes;

public class BinaryExprNode extends ExpressionNode{
    private String operator;
    private ExpressionNode left;
    private ExpressionNode right;

    public BinaryExprNode(String operator_, ExpressionNode left_, ExpressionNode right_) {
        this.operator = operator_;
        this.left = left_;
        this.right = right_;
    }

    public String getOperator() {
        return operator;
    }

    public ExpressionNode getLeft() {
        return left;
    }

    public ExpressionNode getRight() {
        return right;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
