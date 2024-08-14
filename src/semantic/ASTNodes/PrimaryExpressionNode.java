package semantic.ASTNodes;

import semantic.ScopeManager;
import semantic.Type;

public abstract class PrimaryExpressionNode extends ExpressionNode{
    public abstract Type deduceType(ScopeManager scopeManager);

    public PrimaryExpressionNode() {
        super();
    }
}
