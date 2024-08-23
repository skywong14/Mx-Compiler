package semantic.ASTNodes;

import semantic.ScopeManager;
import semantic.Type;

import java.util.Objects;

public class BinaryExprNode extends ExpressionNode{
    private String operator;
    private ExpressionNode left;
    private ExpressionNode right;

    Type deduceType;

    public BinaryExprNode(String operator_, ExpressionNode left_, ExpressionNode right_) {
        this.operator = operator_;
        this.left = left_;
        this.right = right_;
        this.deduceType = null;
    }

    public String getOperator() {
        return operator;
    }
    public ExpressionNode getLeft() {
        return left;
    }
    public ExpressionNode getRight() {  return right; }
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

        Type leftType = left.deduceType(scopeManager);

        if (Objects.equals(operator, "=")) {
            throw new RuntimeException("[Runtime Error] Assignment operator should not be here");
        }

        if (Objects.equals(operator, "+")) {
            deduceType = leftType;
            return deduceType;
        }

        if (Objects.equals(operator, "==") || Objects.equals(operator, "!=")
            || Objects.equals(operator, "&&") || Objects.equals(operator, "||")
            || Objects.equals(operator, "<") || Objects.equals(operator, ">")
                || Objects.equals(operator, "<=") || Objects.equals(operator, ">=")) {
            deduceType = new Type("bool");
            return deduceType;
        }

        if (Objects.equals(operator, "-") || Objects.equals(operator, "*")
                || Objects.equals(operator, "/") || Objects.equals(operator, "%")
                || Objects.equals(operator, "<<") || Objects.equals(operator, ">>")
                || Objects.equals(operator, "&") || Objects.equals(operator, "^") || Objects.equals(operator, "|")) {
            deduceType = new Type("int");
            return deduceType;
        }

        throw new RuntimeException("Unknown operator at BinaryExpr: " + operator);
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
