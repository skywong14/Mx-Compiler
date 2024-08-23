package semantic.ASTNodes;

import semantic.ScopeManager;
import semantic.Type;

public class TernaryExprNode extends ExpressionNode{
    private ExpressionNode condition;
    private ExpressionNode trueExpr;
    private ExpressionNode falseExpr;

    Type deduceType = null;

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
    public void setCondition(ExpressionNode condition) { this.condition = condition; }
    public void setTrueExpr(ExpressionNode trueExpr) { this.trueExpr = trueExpr; }
    public void setFalseExpr(ExpressionNode falseExpr) { this.falseExpr = falseExpr; }

    public void notifyParent() {
        condition.setParent(this);
        trueExpr.setParent(this);
        falseExpr.setParent(this);
    }

    @Override
    public Type deduceType(ScopeManager scopeManager) {
        if (deduceType != null) return deduceType;
        if (scopeManager == null) throw new RuntimeException("ScopeManager is null");

        Type conditionType = condition.deduceType(scopeManager);
        Type trueType = trueExpr.deduceType(scopeManager);
        Type falseType = falseExpr.deduceType(scopeManager);
        if (conditionType.equals("boolean")) {
            throw new RuntimeException("Ternary condition must be of type boolean");
        }
        if (!trueType.equals(falseType)) {
            throw new RuntimeException("Ternary true and false expressions must have the same type, got [" + trueType + "] and [" + falseType + "]");
        }
        deduceType = trueType;
        return deduceType;
    }

    @Override
    public boolean isLeftValue() {
        return false;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
