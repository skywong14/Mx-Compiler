package semantic.ASTNodes;

import java.util.ArrayList;

public class ArgListNode extends ASTNode{
    private ArrayList<ExpressionNode> values;

    public ArgListNode() {
        values = new ArrayList<>();
    }

    public void addExpression(ExpressionNode value) {
        values.add(value);
    }

    public int getSize() { return values.size(); }
    public ArrayList<ExpressionNode> getArgList() { return values; }

    public void notifyParent() { for (ExpressionNode e : values) e.setParent(this); }



    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
