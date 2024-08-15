package semantic.ASTNodes;

import semantic.ScopeManager;
import semantic.Type;

public class IdentifierNode extends PrimaryExpressionNode{
    private String identifier; // might  be 'this'

    public IdentifierNode(String identifier_) {
        this.identifier = identifier_;
    }

    public String getName() {
        return identifier;
    }

    @Override
    public Type deduceType(ScopeManager scopeManager) {
        if (identifier.equals("this")) {
            ClassNode currentClass = scopeManager.getCurrentClass();
            if (currentClass == null)
                throw new RuntimeException("this should be used in class scope");
            return new Type(currentClass.getName(), false, 0);
        }
        return scopeManager.resolveVariable(identifier).getType();
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
