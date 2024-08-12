package semantic.ASTNodes;

public class IdentifierNode extends PrimaryExpressionNode{
    private String identifier; // might be 'this'

    public IdentifierNode(String identifier_) {
        this.identifier = identifier_;
    }

    public String getIdentifier() {
        return identifier;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
