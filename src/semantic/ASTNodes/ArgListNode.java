package semantic.ASTNodes;

import java.util.ArrayList;

public class ArgListNode extends ASTNode{
    private ArrayList<ParameterNode> parameters;
    private ArrayList<ExpressionNode> values;

    public ArgListNode() {
        parameters = new ArrayList<>();
        values = new ArrayList<>();
    }

    public void addParameter(ParameterListNode parameters_) {
        parameters.addAll(parameters_.getParameters());
    }
    public void addValue(ExpressionNode value) {
        values.add(value);
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
