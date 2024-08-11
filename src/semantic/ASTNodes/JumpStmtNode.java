package semantic.ASTNodes;

public class JumpStmtNode extends StatementNode{
    private String jumpType;
    private ExpressionNode expression;

    public JumpStmtNode(String jumpType_, ExpressionNode expression_) {
        this.jumpType = jumpType_;
        this.expression = expression_;
    }

    public String getJumpType() {
        return jumpType;
    }

    public ExpressionNode getExpression() {
        return expression;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
