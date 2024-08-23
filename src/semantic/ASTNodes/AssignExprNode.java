package semantic.ASTNodes;

import semantic.ScopeManager;
import semantic.Type;

import java.util.Objects;

public class AssignExprNode extends ExpressionNode {
    private ExpressionNode left;
    private ExpressionNode right;

    Type deduceType = null;

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
    public void setLeft(ExpressionNode left) { this.left = left; }
    public void setRight(ExpressionNode right) { this.right = right; }

    public void notifyParent() {
        left.setParent(this);
        right.setParent(this);
    }

    @Override
    public Type deduceType(ScopeManager scopeManager) {
        if (deduceType != null) return deduceType;
        if (scopeManager == null) throw new RuntimeException("ScopeManager is null");

        // leftValue check is in SemanticChecker
        deduceType = left.deduceType(scopeManager);
        return deduceType;
    }

    @Override
    public boolean isLeftValue() {
        return left.isLeftValue();
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
