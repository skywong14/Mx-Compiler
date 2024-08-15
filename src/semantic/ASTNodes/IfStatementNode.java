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
