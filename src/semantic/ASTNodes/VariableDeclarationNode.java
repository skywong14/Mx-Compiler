package semantic.ASTNodes;

import semantic.Type;

import java.util.ArrayList;

public class VariableDeclarationNode extends StatementNode{
    private ArrayList<String> names;
    private TypeNode type;
    private ArrayList<ExpressionNode> values;

    private ArrayList<VariableNode> variableNodes;

    public boolean isGlobal = false;

    public VariableDeclarationNode(TypeNode type_) {
        this.variableNodes = new ArrayList<>();
        this.type = type_;
    }
    public Type getType() { return type.getType(); }
    public TypeNode getTypeNode() { return type; }
    public int getSize() { return names.size(); }
    public void addVariable(String name, ExpressionNode value) {
        variableNodes.add(new VariableNode(type, name, value));
    }

    public ArrayList<VariableNode> getVariableNodes() {
        return variableNodes;
    }
    public ArrayList<String> getNames() { return names; }

    public void notifyParent() {
        type.setParent(this);
        for (VariableNode variable : variableNodes)
            variable.setParent(this);
    }

    @Override
    public boolean hasReturnStatement() {
        return false;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
