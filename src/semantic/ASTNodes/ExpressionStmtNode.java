package semantic.ASTNodes;

public class ExpressionStmtNode extends StatementNode{
    private ExpressionNode expression;

    public ExpressionStmtNode(ExpressionNode expression_) {
        this.expression = expression_;
    }

    public ExpressionNode getExpression() {
        return expression;
    }

    @Override
    public boolean hasReturnStatement() {
        return false;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
