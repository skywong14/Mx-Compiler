package semantic.ASTNodes;

import java.util.ArrayList;

public class CompoundStmtNode extends StatementNode{
    private ArrayList<StatementNode> statements;

    public CompoundStmtNode() {
        this.statements = new ArrayList<>();
    }

    public void addStatement(StatementNode statement) {
        statements.add(statement);
    }

    public ArrayList<StatementNode> getStatement() {
        return statements;
    }

    @Override
    public boolean hasReturnStatement() {
        for (StatementNode statement : statements) {
            if (statement.hasReturnStatement())
                return true;
        }
        return false;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
