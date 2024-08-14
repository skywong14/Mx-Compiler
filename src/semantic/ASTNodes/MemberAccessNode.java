package semantic.ASTNodes;

import semantic.ScopeManager;
import semantic.Type;

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
    public Type deduceType(ScopeManager scopeManager) {
        if (isMethod) {
            Type primaryType = primaryExpression.deduceType(scopeManager);
            if (primaryType.isArray())
                throw new RuntimeException("Array type does not have member access");
            ClassNode classNode = scopeManager.resolveClass(primaryExpression.deduceType(scopeManager).getBasicType());
            FunctionNode functionNode = classNode.getMethod(identifier);
            return functionNode.getReturnType();
        } else {
            Type primaryType = primaryExpression.deduceType(scopeManager);
            if (primaryType.isArray())
                throw new RuntimeException("Array type does not have member access");
            ClassNode classNode = scopeManager.resolveClass(primaryExpression.deduceType(scopeManager).getBasicType());
            VariableNode variableNode = classNode.getField(identifier);
            return variableNode.getType();
        }
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
