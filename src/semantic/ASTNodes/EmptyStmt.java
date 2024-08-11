package semantic.ASTNodes;

public class EmptyStmt extends StatementNode {
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
