package semantic.ASTNodes;

public class PrimaryExpressionNode extends ExpressionNode{

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
