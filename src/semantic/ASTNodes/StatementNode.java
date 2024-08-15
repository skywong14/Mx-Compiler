package semantic.ASTNodes;

public abstract class StatementNode extends ASTNode{
    public abstract boolean hasReturnStatement();
    public StatementNode() {
        super();
    }
}
