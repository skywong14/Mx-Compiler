package semantic.ASTNodes;

import semantic.ScopeManager;
import semantic.Type;

public class UnaryExprNode extends ExpressionNode{
    private String operator;
    private ExpressionNode expression;
    private boolean LeftOp;

    Type deduceType = null;

    public UnaryExprNode(String operator_, ExpressionNode expression_, boolean LeftOp_) {
        this.operator = operator_;
        this.expression = expression_;
        this.LeftOp = LeftOp_;
    }

    public String getOperator() {
        return operator;
    }

    public boolean isLeftOp() { return LeftOp; }

    public ExpressionNode getExpression() {
        return expression;
    }

    public void setExpression(ExpressionNode expression) { this.expression = expression; }

    public void notifyParent() { expression.setParent(this); }

    @Override
    public Type deduceType(ScopeManager scopeManager) {
        if (deduceType != null) return deduceType;
        if (scopeManager == null) throw new RuntimeException("ScopeManager is null");

        deduceType = expression.deduceType(scopeManager);
        return deduceType;
    }

    @Override
    public boolean isLeftValue() {
        return LeftOp && (operator.equals("++") || operator.equals("--")) && expression.isLeftValue();
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
