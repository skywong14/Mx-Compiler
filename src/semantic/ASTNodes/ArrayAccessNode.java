package semantic.ASTNodes;

import semantic.ScopeManager;
import semantic.Type;

import java.util.ArrayList;

public class ArrayAccessNode extends ExpressionNode {
    private ExpressionNode expression;
    private ArrayList<ExpressionNode> expressions;

    Type deduceType = null;

    public ArrayAccessNode(ExpressionNode primaryExpression_) {
        this.expression = primaryExpression_;
        expressions = new ArrayList<>();
    }

    public void setHeadExpression(ExpressionNode expression) { this.expression = expression; }
    public void addExpression(ExpressionNode expression) { expressions.add(expression); }
    public ExpressionNode getHeadExpression() { return expression; }
    public ArrayList<ExpressionNode> getExpressions() { return expressions; }

    public void notifyParent() {
        expression.setParent(this);
        for (ExpressionNode e : expressions)
            e.setParent(this);
    }

    @Override
    public Type deduceType(ScopeManager scopeManager) {
        if (deduceType != null) return deduceType;
        if (scopeManager == null) throw new RuntimeException("ScopeManager is null");

        int sz = expressions.size();
        deduceType = expression.deduceType(scopeManager).arrayDereference(sz);
        return deduceType;
    }

    @Override
    public boolean isLeftValue() {
        return expression.isLeftValue();
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
