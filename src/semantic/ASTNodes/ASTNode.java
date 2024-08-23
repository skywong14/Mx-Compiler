package semantic.ASTNodes;

public abstract class ASTNode {
    ASTNode parent;

    public ASTNode() {
        this.parent = null;
    }

    public void setParent(ASTNode parent) {
        this.parent = parent;
    }

    public ASTNode getParent() {
        return parent;
    }

    public abstract void accept(ASTVisitor visitor);
}
