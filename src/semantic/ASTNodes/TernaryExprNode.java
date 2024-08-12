package semantic.ASTNodes;

public class TernaryExprNode extends ExpressionNode{
    private ExpressionNode condition;
    private ExpressionNode trueExpr;
    private ExpressionNode falseExpr;

    public TernaryExprNode(ExpressionNode condition_, ExpressionNode trueExpr_, ExpressionNode falseExpr_) {
        this.condition = condition_;
        this.trueExpr = trueExpr_;
        this.falseExpr = falseExpr_;
    }

    public ExpressionNode getCondition() {
        return condition;
    }

    public ExpressionNode getTrueExpr() {
        return trueExpr;
    }

    public ExpressionNode getFalseExpr() {
        return falseExpr;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
