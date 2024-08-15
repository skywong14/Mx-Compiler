package semantic.ASTNodes;

import semantic.ScopeManager;
import semantic.Type;

public class NewExprNode extends PrimaryExpressionNode{
    private String identifier; // if class
    private TypeNode type; // if array
    private ConstantNode arrayConstant; // if array

    public NewExprNode(TypeNode type_, String identifier_, ConstantNode arrayConstant_) {
        this.type = type_;
        this.identifier = identifier_;
        this.arrayConstant = arrayConstant_;
    }

    public Type getType() { return type.getType(); }
    public TypeNode getTypeNode() { return type; }
    public String getIdentifier() { return identifier; }
    public ConstantNode getArrayConstant() { return arrayConstant; }

    @Override
    public Type deduceType(ScopeManager scopeManager) {
        if (identifier == null) {
            // new Array
            return type.getType();
        } else {
            // new Class
            return new Type(identifier);
        }
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
