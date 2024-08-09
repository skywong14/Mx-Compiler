package semantic.ASTNodes;

import java.util.ArrayList;

public class CompoundStatementNode extends ASTNode{
    private ArrayList<StatementNode> statements;

    public CompoundStatementNode() {
        this.statements = new ArrayList<>();
    }

    public void addStatement(StatementNode statement) {
        statements.add(statement);
    }

    public ArrayList<StatementNode> getStatement() {
        return statements;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
