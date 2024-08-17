package semantic.ASTNodes;

import semantic.ScopeManager;
import semantic.Type;

import java.util.Objects;

public class BinaryExprNode extends ExpressionNode{
    private String operator;
    private ExpressionNode left;
    private ExpressionNode right;

    public BinaryExprNode(String operator_, ExpressionNode left_, ExpressionNode right_) {
        this.operator = operator_;
        this.left = left_;
        this.right = right_;
    }

    public String getOperator() {
        return operator;
    }

    public ExpressionNode getLeft() {
        return left;
    }

    public ExpressionNode getRight() {
        return right;
    }

    @Override
    public Type deduceType(ScopeManager scopeManager) {
        Type leftType = left.deduceType(scopeManager);

        if (Objects.equals(operator, "=")) {
            throw new RuntimeException("[Runtime Error] Assignment operator should not be here");
        }

        if (Objects.equals(operator, "+")) {
            return leftType;
        }

        if (Objects.equals(operator, "==") || Objects.equals(operator, "!=")
            || Objects.equals(operator, "&&") || Objects.equals(operator, "||")
            || Objects.equals(operator, "<") || Objects.equals(operator, ">")
                || Objects.equals(operator, "<=") || Objects.equals(operator, ">=")) {
            return new Type("bool");
        }

        if (Objects.equals(operator, "-") || Objects.equals(operator, "*")
                || Objects.equals(operator, "/") || Objects.equals(operator, "%")
                || Objects.equals(operator, "<<") || Objects.equals(operator, ">>")
                || Objects.equals(operator, "&") || Objects.equals(operator, "^") || Objects.equals(operator, "|")) {
              return new Type("int");
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
