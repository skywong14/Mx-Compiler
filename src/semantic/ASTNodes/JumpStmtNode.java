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

    public ExpressionNode getExpression() { return expression; }
    public void setExpression(ExpressionNode expression) { this.expression = expression; }

    public void notifyParent() {
        if (expression != null) expression.setParent(this);
    }

    @Override
    public boolean hasReturnStatement() {
        return jumpType.equals("return");
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
