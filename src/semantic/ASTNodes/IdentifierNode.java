package semantic.ASTNodes;

import semantic.ScopeManager;
import semantic.Type;

public class IdentifierNode extends PrimaryExpressionNode{
    private String identifier; // might  be 'this'

    Type deduceType = null;

    public IdentifierNode(String identifier_) {
        this.identifier = identifier_;
    }

    public String getName() {
        return identifier;
    }

    @Override
    public Type deduceType(ScopeManager scopeManager) {
        if (deduceType != null) return deduceType;
        if (scopeManager == null) throw new RuntimeException("ScopeManager is null");

        if (identifier.equals("%this")) {
            ClassNode currentClass = scopeManager.getCurrentClass();
            if (currentClass == null)
                throw new RuntimeException("this should be used in class scope");
            deduceType = new Type(currentClass.getName(), false, 0);
            return deduceType;
        }
        deduceType = scopeManager.resolveVariable(identifier).getType();
        return deduceType;
    }

    @Override
    public boolean isLeftValue() {
        if (identifier.equals("this")) {
            return false;
        }
        return true;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
