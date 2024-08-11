package semantic.ASTNodes;

import semantic.Type;

import java.util.ArrayList;

public class TypeNode extends ASTNode {
    private Type type;
    private ArrayList<ExpressionNode> expressions;

    public TypeNode(Type type_) {
        this.type = type_;
        this.expressions = new ArrayList<>();
    }

    public void addExpression(ExpressionNode expression) {
        expressions.add(expression);
    }

    public Type getType() {
        return type;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
