package semantic.ASTNodes;

import semantic.ScopeManager;
import semantic.Type;

import java.util.Objects;

public class AssignExprNode extends ExpressionNode {
    private ExpressionNode left;
    private ExpressionNode right;

    public AssignExprNode(ExpressionNode left_, ExpressionNode right_) {
        this.left = left_;
        this.right = right_;
    }

    public ExpressionNode getLeft() {
        return left;
    }

    public ExpressionNode getRight() {
        return right;
    }

    @Override
    public Type deduceType(ScopeManager scopeManager) {
        // leftValue check is in SemanticChecker
        return left.deduceType(scopeManager);
    }

    @Override
    public boolean isLeftValue() {
        return left.isLeftValue() && right.isLeftValue();
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
