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

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
