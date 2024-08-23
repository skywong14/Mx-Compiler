package semantic.ASTNodes;

public class IfStatementNode extends StatementNode{
    private ExpressionNode condition;
    private StatementNode ifStatement;
    private StatementNode elseStatement;

    public IfStatementNode(ExpressionNode condition_, StatementNode ifStatement_, StatementNode elseStatement_) {
        this.condition = condition_;
        this.ifStatement = ifStatement_;
        this.elseStatement = elseStatement_;
    }

    public ExpressionNode getCondition() { return condition; }
    public StatementNode getIfStatement() { return ifStatement; }
    public StatementNode getElseStatement() { return elseStatement; }
    public void setCondition(ExpressionNode condition) { this.condition = condition; }

    public void notifyParent() {
        condition.setParent(this);
        ifStatement.setParent(this);
        if (elseStatement != null)
            elseStatement.setParent(this);
    }

    @Override
    public boolean hasReturnStatement() {
        if (elseStatement != null) {
            return ifStatement.hasReturnStatement() && elseStatement.hasReturnStatement();
        }
        return ifStatement.hasReturnStatement();
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
