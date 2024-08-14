package semantic.ASTNodes;

import semantic.ScopeManager;
import semantic.Type;

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
    public Type deduceType(ScopeManager scopeManager) {
        Type conditionType = condition.deduceType(scopeManager);
        Type trueType = trueExpr.deduceType(scopeManager);
        Type falseType = falseExpr.deduceType(scopeManager);
        if (conditionType.equals("boolean")) {
            throw new RuntimeException("Ternary condition must be of type boolean");
        }
        if (trueType != falseType) {
            throw new RuntimeException("Ternary true and false expressions must have the same type");
        }
        return trueType;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
