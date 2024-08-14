package semantic.ASTNodes;

import java.util.ArrayList;

public class ConstructorNode extends ASTNode {
    private String name;
    private CompoundStmtNode body;

    public ConstructorNode(String name_, CompoundStmtNode body_) {
        this.name = name_;
        this.body = body_;
    }

    public String getName() {
        return name;
    }
    public CompoundStmtNode getBody() { return body; }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
