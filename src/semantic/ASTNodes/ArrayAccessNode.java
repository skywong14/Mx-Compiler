package semantic.ASTNodes;

import semantic.ScopeManager;
import semantic.Type;

import java.util.ArrayList;

public class ArrayAccessNode extends ExpressionNode {
    private ExpressionNode expression;
    private ArrayList<ExpressionNode> expressions;

    public ArrayAccessNode(ExpressionNode primaryExpression_) {
        this.expression = primaryExpression_;
        expressions = new ArrayList<>();
    }

    public void addExpression(ExpressionNode expression) {
        expressions.add(expression);
    }

    public ExpressionNode getPrimaryExpression() { return expression; }
    public ArrayList<ExpressionNode> getExpressions() { return expressions; }

    @Override
    public Type deduceType(ScopeManager scopeManager) {
        int sz = expressions.size();
        return expression.deduceType(scopeManager).arrayDereference(sz);
    }

    @Override
    public boolean isLeftValue() {
        return true;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
