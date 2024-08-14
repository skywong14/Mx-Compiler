package semantic.ASTNodes;

import semantic.ScopeManager;
import semantic.Type;

import java.util.ArrayList;

public class ArrayAccessNode extends PrimaryExpressionNode {
    private PrimaryExpressionNode primaryExpression;
    private ArrayList<ExpressionNode> expressions;

    public ArrayAccessNode(PrimaryExpressionNode primaryExpression_) {
        this.primaryExpression = primaryExpression_;
        expressions = new ArrayList<>();
    }

    public void addExpression(ExpressionNode expression) {
        expressions.add(expression);
    }

    @Override
    public Type deduceType(ScopeManager scopeManager) {
        int sz = expressions.size();
        for (int i = 0; i < sz; i++) {
            if (!expressions.get(i).deduceType(scopeManager).equals("int")) {
                throw new RuntimeException("Array access index must be int");
            }
        }
        return primaryExpression.deduceType(scopeManager).arrayDereference(sz);
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
