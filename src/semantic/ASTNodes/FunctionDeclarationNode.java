package semantic.ASTNodes;

import java.util.ArrayList;

public class FunctionDeclarationNode extends ASTNode{
    private String name;
    private ArrayList<DeclarationArgumentNode> arguments;
    private TypeNode returnType;
    private CompoundStatementNode body;

    public FunctionDeclarationNode(String name_) {
        this.name = name_;
        arguments = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
