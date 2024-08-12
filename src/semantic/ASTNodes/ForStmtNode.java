package semantic.ASTNodes;

public class ForStmtNode extends StatementNode{
    private StatementNode init;
    private ExpressionNode condition;
    private ExpressionNode step;
    private StatementNode body;

    public ForStmtNode(StatementNode init_, ExpressionNode condition_, ExpressionNode step_, StatementNode body_) {
        this.init = init_;
        this.condition = condition_;
        this.step = step_;
        this.body = body_;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
