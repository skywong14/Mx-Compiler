package semantic.ASTNodes;

import semantic.Type;

import java.util.ArrayList;

public class FunctionNode extends ASTNode{
    private String name;
    private ArrayList<ParameterNode> parameters;
    private TypeNode returnType;
    private CompoundStmtNode body;

    public FunctionNode(TypeNode returnType_, String name_,
                        ParameterListNode arguments_, CompoundStmtNode body_) {
        this.returnType = returnType_;
        this.name = name_;
        this.parameters = arguments_.getParameters();
        this.body = body_;
    }

    public String getName() {
        return name;
    }
    public int getParameterSize() { return parameters.size(); }
    public ArrayList<ParameterNode> getParameters() { return parameters; }
    public Type getReturnType() {
        assert(returnType != null);
        return returnType.getType();
    }
    public CompoundStmtNode getBody() {
        return body;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
