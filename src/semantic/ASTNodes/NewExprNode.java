package semantic.ASTNodes;

import semantic.ScopeManager;
import semantic.Type;

public class NewExprNode extends PrimaryExpressionNode{
    private String identifier; // if class
    private TypeNode type; // if array

    public NewExprNode(TypeNode type_, String identifier_) {
        this.type = type_;
        this.identifier = identifier_;
    }

    @Override
    public Type deduceType(ScopeManager scopeManager) {
        if (type != null) {
            // new Array
            return type.getType();
        } else {
            // new Class
            return new Type(identifier);
        }
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
