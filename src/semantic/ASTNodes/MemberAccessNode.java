package semantic.ASTNodes;

public class MemberAccessNode extends PrimaryExpressionNode {
    private PrimaryExpressionNode primaryExpression;
    private String identifier;
    private boolean isMethod;
    private ArgListNode argListNode;

    public MemberAccessNode(PrimaryExpressionNode primaryExpression_, String identifier_,
                            boolean isMethod_, ArgListNode argListNode_) {
        this.primaryExpression = primaryExpression_;
        this.identifier = identifier_;
        this.isMethod = isMethod_;
        this.argListNode = argListNode_;
    }

    public PrimaryExpressionNode getPrimaryExpression() {
        return primaryExpression;
    }

    public String getIdentifier() {
        return identifier;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
