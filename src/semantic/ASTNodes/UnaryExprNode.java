package semantic.ASTNodes;

import semantic.ScopeManager;
import semantic.Type;

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
    public Type deduceType(ScopeManager scopeManager) {
        // todo : if ! and ~ must be boolean
        if (operator.equals("--") || operator.equals("++") || operator.equals("-") || operator.equals("+")) {
            if (!expression.deduceType(scopeManager).equals("int"))
                throw new RuntimeException("Type error: " + operator + " cannot be applied to " + expression.deduceType(scopeManager));
        }
        if (operator.equals("!")) {
            if (!expression.deduceType(scopeManager).equals("boolean"))
                throw new RuntimeException("Type error: " + operator + " cannot be applied to " + expression.deduceType(scopeManager));
        }
        return expression.deduceType(scopeManager);
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
