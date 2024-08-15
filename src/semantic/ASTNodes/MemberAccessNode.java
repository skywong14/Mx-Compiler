package semantic.ASTNodes;

import semantic.ScopeManager;
import semantic.Type;

public class MemberAccessNode extends ExpressionNode {
    private ExpressionNode expression;
    private String identifier;
    private boolean isMethod;
    private ArgListNode argListNode;

    public MemberAccessNode(ExpressionNode primaryExpression_, String identifier_,
                            boolean isMethod_, ArgListNode argListNode_) {
        this.expression = primaryExpression_;
        this.identifier = identifier_;
        this.isMethod = isMethod_;
        this.argListNode = argListNode_;
    }

    public ExpressionNode getPrimaryExpression() {
        return expression;
    }

    public String getIdentifier() {
        return identifier;
    }
    public ArgListNode getArgListNode() { return argListNode; }
    public boolean isMethod() {  return isMethod; }

    @Override
    public Type deduceType(ScopeManager scopeManager) {
        Type primaryType = expression.deduceType(scopeManager);
        if (isMethod) {
            if (primaryType.isArray()){
                if (identifier.equals("size")) {
                    if (!argListNode.getArgList().isEmpty())
                        throw new RuntimeException("Array size does not have arguments");
                    return new Type("int", false, 0);
                } else {
                    throw new RuntimeException("Array type does not have member access: " + identifier);
                }
            }
            ClassNode classNode = scopeManager.resolveClass(expression.deduceType(scopeManager).getBasicType());
            FunctionNode functionNode = classNode.getMethod(identifier);
            return functionNode.getReturnType();
        } else {
            if (primaryType.isArray())
                throw new RuntimeException("Array type does not have field access");
            ClassNode classNode = scopeManager.resolveClass(expression.deduceType(scopeManager).getBasicType());
            VariableNode variableNode = classNode.getField(identifier);
            return variableNode.getType();
        }
    }

    @Override
    public boolean isLeftValue() {
        return !isMethod;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
