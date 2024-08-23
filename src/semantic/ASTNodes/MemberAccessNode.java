package semantic.ASTNodes;

import semantic.ScopeManager;
import semantic.Type;

public class MemberAccessNode extends ExpressionNode {
    private ExpressionNode expression;
    private String identifier;
    private boolean isMethod;
    private ArgListNode argListNode;

    Type deduceType = null;

    public MemberAccessNode(ExpressionNode primaryExpression_, String identifier_,
                            boolean isMethod_, ArgListNode argListNode_) {
        this.expression = primaryExpression_;
        this.identifier = identifier_;
        this.isMethod = isMethod_;
        this.argListNode = argListNode_;
    }

    public ExpressionNode getExpression() { return expression; }
    public String getIdentifier() {  return identifier; }
    public ArgListNode getArgListNode() { return argListNode; }
    public boolean isMethod() {  return isMethod; }
    public void setExpression(ExpressionNode expression) { this.expression = expression; }

    public void notifyParent() {
        if (expression != null) expression.setParent(this);
        if (argListNode != null) argListNode.setParent(this);
    }

    @Override
    public Type deduceType(ScopeManager scopeManager) {
        if (deduceType != null) return deduceType;
        if (scopeManager == null) throw new RuntimeException("ScopeManager is null");

        Type primaryType = expression.deduceType(scopeManager);
        if (isMethod) {
            if (primaryType.isArray()){
                if (identifier.equals("size")) {
                    if (!argListNode.getArgList().isEmpty())
                        throw new RuntimeException("Array size does not have arguments");
                    deduceType = new Type("int", false, 0);
                    return deduceType;
                } else {
                    throw new RuntimeException("Array type does not have member access: " + identifier);
                }
            }
            ClassNode classNode = scopeManager.resolveClass(expression.deduceType(scopeManager).getBaseType());
            FunctionNode functionNode = classNode.getMethod(identifier);
            deduceType = functionNode.getReturnType();
            return deduceType;
        } else {
            if (primaryType.isArray())
                throw new RuntimeException("Array type does not have field access");
            ClassNode classNode = scopeManager.resolveClass(expression.deduceType(scopeManager).getBaseType());
            VariableNode variableNode = classNode.getField(identifier);
            deduceType = variableNode.getType();
            return deduceType;
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
