package semantic.ASTNodes;

import IR.IRStmts.BasicIRType;
import IR.IRStmts.IRType;
import semantic.Type;

import java.util.ArrayList;

public class TypeNode extends ASTNode {
    private Type type;
    private ArrayList<ExpressionNode> expressions; // size of array

    public TypeNode(Type type_) {
        this.type = type_;
        this.expressions = new ArrayList<>();
    }

    public void addExpression(ExpressionNode expression) {
        expressions.add(expression);
    }

    public Type getType() { return type; }
    public ArrayList<ExpressionNode> getExpressions() { return expressions; }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
