package semantic.ASTNodes;

import java.util.ArrayList;

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

    public StatementNode getInit() { return init; }
    public ExpressionNode getCondition() { return condition; }
    public ExpressionNode getStep() { return step; }
    public StatementNode getBody() { return body; }

    @Override
    public boolean hasReturnStatement() {
        return body.hasReturnStatement();
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
