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
        // todo
        Type leftType = left.deduceType(scopeManager);
        Type rightType = right.deduceType(scopeManager);

        if (!leftType.equals(rightType))
            throw new RuntimeException("Binary expression type mismatch, left: " + leftType.toString() + ", right: " + rightType.toString());

        if (Objects.equals(operator, "+") || Objects.equals(operator, "-") ||
                Objects.equals(operator, "*") || Objects.equals(operator, "/") || Objects.equals(operator, "%")) {
            if (!leftType.equals("int"))
                throw new RuntimeException("Binary expression(+-*/%) type should be int, but received: " + leftType.toString());
            return leftType;
        }
        if (Objects.equals(operator, "&") || Objects.equals(operator, "^") || Objects.equals(operator, "|")) {
            if (!leftType.equals("int"))
                throw new RuntimeException("Binary expression(+-*/%) type should be int, but received: " + leftType.toString());
            return leftType;
        }
        if (Objects.equals(operator, "==") || Objects.equals(operator, "!=")){
            return new Type("bool");
        }
        if (Objects.equals(operator, "&&") || Objects.equals(operator, "||")){
            if (!leftType.equals("bool"))
                throw new RuntimeException("Binary expression(&&,||) type should be bool, but received: " + leftType.toString());
            return new Type("bool");
        }
        if (Objects.equals(operator, "<") || Objects.equals(operator, ">")
                || Objects.equals(operator, "<=") || Objects.equals(operator, ">=")) {
            if (!leftType.equals("int"))
                throw new RuntimeException("Binary expression(<,>,<=,>=) type should be int, but received: " + leftType.toString());
            return new Type("bool");
        }
        if (Objects.equals(operator, "<<") || Objects.equals(operator, ">>")) {
            if (!leftType.equals("int"))
                throw new RuntimeException("Binary expression(<<,>>) type should be int, but received: " + leftType.toString());
            return leftType;
        }
        if (Objects.equals(operator, "=")) {
            return leftType;
        }
        throw new RuntimeException("Unknown operator at BinaryExpr: " + operator);
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
