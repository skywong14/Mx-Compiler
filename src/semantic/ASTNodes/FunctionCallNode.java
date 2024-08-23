package semantic.ASTNodes;

import semantic.ScopeManager;
import semantic.Type;

public class FunctionCallNode extends ExpressionNode{
    private String identifier;
    private ArgListNode argListNode;

    Type deduceType = null;

    public FunctionCallNode(String identifier_, ArgListNode argListNode_) {
        this.identifier = identifier_;
        this.argListNode = argListNode_;
    }

    public String getIdentifier() { return identifier; }
    public ArgListNode getArgListNode() { return argListNode; }
    public void setIdentifier(String identifier) { this.identifier = identifier; }
    public void setArgListNode(ArgListNode argListNode) { this.argListNode = argListNode; }

    public void notifyParent() { argListNode.setParent(this); }

    @Override
    public Type deduceType(ScopeManager scopeManager) {
        if (deduceType != null) return deduceType;
        if (scopeManager == null) throw new RuntimeException("ScopeManager is null");

        FunctionNode functionNode = scopeManager.resolveFunction(identifier);
        deduceType = functionNode.getReturnType();
        return deduceType;
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
