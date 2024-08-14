package semantic.ASTNodes;

import semantic.ScopeManager;
import semantic.Type;

public class IdentifierNode extends PrimaryExpressionNode{
    private String identifier; // can not be 'this'

    public IdentifierNode(String identifier_) {
        this.identifier = identifier_;
    }

    public String getIdentifier() {
        return identifier;
    }

    @Override
    public Type deduceType(ScopeManager scopeManager) {
        return scopeManager.resolveVariable(identifier).getType();
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
