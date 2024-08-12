package semantic.ASTNodes;

public class NewExprNode extends PrimaryExpressionNode{
    private String identifier; // if class
    private TypeNode type; // if array

    public NewExprNode(TypeNode type_, String identifier_) {
        this.type = type_;
        this.identifier = identifier_;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
