package semantic.ASTNodes;

public class ExpressionStmtNode extends StatementNode{
    private ExpressionNode expression;

    public ExpressionStmtNode(ExpressionNode expression_) {
        this.expression = expression_;
    }

    public void setExpression(ExpressionNode expression) { this.expression = expression; }
    public ExpressionNode getExpression() {
        return expression;
    }

    public void notifyParent() { expression.setParent(this); }

    @Override
    public boolean hasReturnStatement() {
        return false;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
