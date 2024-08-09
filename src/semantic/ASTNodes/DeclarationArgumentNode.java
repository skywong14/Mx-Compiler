package semantic.ASTNodes;

import semantic.Type;

public class DeclarationArgumentNode extends ASTNode{
    private String name;
    private Type type;

    public DeclarationArgumentNode(String name_, Type type_) {
        this.name = name_;
        this.type = type_;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
