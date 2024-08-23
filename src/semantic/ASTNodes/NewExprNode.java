package semantic.ASTNodes;

import semantic.ScopeManager;
import semantic.Type;

public class NewExprNode extends PrimaryExpressionNode{
    private String identifier; // if class
    private TypeNode type; // if array
    private ConstantNode arrayConstant; // if array

    Type deduceType = null;

    public NewExprNode(TypeNode type_, String identifier_, ConstantNode arrayConstant_) {
        this.type = type_;
        this.identifier = identifier_;
        this.arrayConstant = arrayConstant_;
    }

    public Type getType() { return type.getType(); }
    public TypeNode getTypeNode() { return type; }
    public String getIdentifier() { return identifier; }
    public ConstantNode getArrayConstant() { return arrayConstant; }

    public void setTypeNode(TypeNode type) { this.type = type; }
    public void setIdentifier(String identifier) { this.identifier = identifier; }
    public void setArrayConstant(ConstantNode arrayConstant) { this.arrayConstant = arrayConstant; }

    public void notifyParent() {
        if (type != null) type.setParent(this);
        if (arrayConstant != null) arrayConstant.setParent(this);
    }

    @Override
    public Type deduceType(ScopeManager scopeManager) {
        if (deduceType != null) return deduceType;

        if (identifier == null) {
            // new Array
            deduceType = type.getType();
            return deduceType;
        } else {
            // new Class
            deduceType = new Type(identifier);
            return deduceType;
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
