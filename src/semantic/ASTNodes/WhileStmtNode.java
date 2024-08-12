package semantic.ASTNodes;

public class WhileStmtNode extends StatementNode{
    private ExpressionNode condition;
    private StatementNode body;

    public WhileStmtNode(ExpressionNode condition_, StatementNode body_) {
        this.condition = condition_;
        this.body = body_;
    }

    public ExpressionNode getCondition() {
        return condition;
    }

    public StatementNode getBody() {
        return body;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
