package semantic.ASTNodes;

public class VariableDeclarationNode extends ASTNode{
    private String variable_name;
    private TypeNode typeNode;
    private ExpressionNode valueNode;

    public VariableDeclarationNode(String name_, TypeNode type_, ExpressionNode value_) {
        this.variable_name = name_;
        this.typeNode = type_;
        this.valueNode = value_;
    }

    public String getName() {
        return variable_name;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
