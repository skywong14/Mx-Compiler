package semantic.ASTNodes;

import semantic.ScopeManager;
import semantic.Type;

public abstract class ExpressionNode extends ASTNode{
    public abstract Type deduceType(ScopeManager scopeManager);
    public abstract boolean isLeftValue();
    public ExpressionNode() {
        super();
    }
}
