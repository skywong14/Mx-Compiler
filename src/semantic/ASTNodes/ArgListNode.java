package semantic.ASTNodes;

import java.util.ArrayList;

public class ArgListNode extends ASTNode{
    private ArrayList<ExpressionNode> values;

    public ArgListNode() {
        values = new ArrayList<>();
    }

    public void addValue(ExpressionNode value) {
        values.add(value);
    }

    public ArrayList<ExpressionNode> getValues() {
        return values;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
