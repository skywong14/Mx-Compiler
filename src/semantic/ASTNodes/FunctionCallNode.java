package semantic.ASTNodes;

public class FunctionCallNode extends PrimaryExpressionNode{
    private String identifier;
    private ArgListNode argListNode; // might be nullptr

    public FunctionCallNode(String identifier_, ArgListNode argListNode_) {
        this.identifier = identifier_;
        this.argListNode = argListNode_;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
