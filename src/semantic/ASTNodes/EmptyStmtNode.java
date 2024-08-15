package semantic.ASTNodes;

public class EmptyStmtNode extends StatementNode {
    @Override
    public boolean hasReturnStatement() {
        return false;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
