package semantic.ASTNodes;

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
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
