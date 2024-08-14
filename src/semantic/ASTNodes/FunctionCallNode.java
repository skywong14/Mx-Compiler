package semantic.ASTNodes;

import semantic.ScopeManager;
import semantic.Type;

public class FunctionCallNode extends PrimaryExpressionNode{
    private String identifier;
    private ArgListNode argListNode;

    public FunctionCallNode(String identifier_, ArgListNode argListNode_) {
        this.identifier = identifier_;
        this.argListNode = argListNode_;
    }

    public String getIdentifier() { return identifier; }
    public ArgListNode getArgListNode() { return argListNode; }

    @Override
    public Type deduceType(ScopeManager scopeManager) {
        FunctionNode functionNode = scopeManager.resolveFunction(identifier);
        return functionNode.getReturnType();
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
