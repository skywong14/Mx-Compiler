package semantic.ASTNodes;

import java.util.ArrayList;

public class ConstantNode extends PrimaryExpressionNode {
    private String literal_value;
    private boolean isArray;
    private ArrayList<ConstantNode> constants;

    public ConstantNode(String value_, boolean isArray_) {
        this.literal_value = value_;
        this.isArray = isArray_;
        this.constants = new ArrayList<>(); // init: null
    }

    public void addConstant(ConstantNode constant) {
        constants.add(constant);
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
