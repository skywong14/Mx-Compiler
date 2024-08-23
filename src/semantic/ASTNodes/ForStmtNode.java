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

    public void setInit(StatementNode init) { this.init = init; }
    public void setCondition(ExpressionNode condition) { this.condition = condition; }
    public void setStep(ExpressionNode step) { this.step = step; }
    public StatementNode getInit() { return init; }
    public ExpressionNode getCondition() { return condition; }
    public ExpressionNode getStep() { return step; }
    public StatementNode getBody() { return body; }

    public void notifyParent() {
        if (init != null) init.setParent(this);
        if (condition != null) condition.setParent(this);
        if (step != null) step.setParent(this);
        body.setParent(this);
    }

    @Override
    public boolean hasReturnStatement() {
        return body.hasReturnStatement();
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
