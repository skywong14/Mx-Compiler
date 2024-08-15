package semantic.ASTNodes;

import semantic.Type;

import java.util.ArrayList;

public class ConstructorNode extends FunctionNode {
    private String self_name;
    private CompoundStmtNode self_body;

    public ConstructorNode(String name_, CompoundStmtNode body_) {
        super(new TypeNode(new Type("void")), name_,
                new ParameterListNode(), body_);
        this.self_name = name_;
        this.self_body = body_;
    }

    public String getName() {
        return self_name;
    }
    public CompoundStmtNode getBody() { return self_body; }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
