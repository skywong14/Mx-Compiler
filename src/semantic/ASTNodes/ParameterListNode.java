package semantic.ASTNodes;

import java.util.ArrayList;

public class ParameterListNode extends ASTNode{
    private ArrayList<ParameterNode> parameters;

    public ParameterListNode() {
        parameters = new ArrayList<>();
    }

    public void addParameter(ParameterNode parameter) {
        parameters.add(parameter);
    }

    public int size() {
        return parameters.size();
    }
    public ArrayList<ParameterNode> getParameters() {
        return parameters;
    }

    public void notifyParent() {
        for (ParameterNode node : parameters)
            node.setParent(this);
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
