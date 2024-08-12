package semantic.ASTNodes;

import semantic.Type;

import java.util.ArrayList;

public class VariableNode extends StatementNode{
    private ArrayList<String> names;
    private TypeNode type;
    private ArrayList<ExpressionNode> values;

    public VariableNode(TypeNode type_) {
        this.names = new ArrayList<>();
        this.values = new ArrayList<>();
        this.type = type_;
    }
    public void addName(String name) {
        names.add(name);
    }
    public void addValue(ExpressionNode value) {
        values.add(value);
    }

    public ArrayList<String> getNames() {
        return names;
    }
    public ArrayList<ExpressionNode> getValues() {
        return values;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
