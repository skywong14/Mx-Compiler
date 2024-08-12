package semantic.ASTNodes;

public class UnaryExprNode extends ExpressionNode{
    private String operator;
    private ExpressionNode expression;
    private boolean LeftOp;

    public UnaryExprNode(String operator_, ExpressionNode expression_, boolean LeftOp_) {
        this.operator = operator_;
        this.expression = expression_;
        this.LeftOp = LeftOp_;
    }

    public String getOperator() {
        return operator;
    }

    public ExpressionNode getExpression() {
        return expression;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
