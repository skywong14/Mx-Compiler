package semantic.ASTNodes;

import semantic.ScopeManager;
import semantic.Type;

import java.util.ArrayList;

public class ConstantNode extends PrimaryExpressionNode {
    public String literal_value;
    public boolean isArray;
    public ArrayList<ConstantNode> constants;

    Type type;

    public ConstantNode(String value_, boolean isArray_) {
        this.literal_value = value_;
        this.isArray = isArray_;
        this.constants = new ArrayList<>(); // init: null
        type = null;
    }

    public void addConstant(ConstantNode constant) {
        constants.add(constant);
    }

    public void notifyParent() {
        for (ConstantNode c : constants) {
            c.setParent(this);
        }
    }

    @Override
    public Type deduceType(ScopeManager scopeManager) {
        if (type != null) return type;

        if (isArray) {
            if (constants.isEmpty()) {
                type = new Type("null").arrayReference();
                return type;
            }
            // according to the first element
            Type atom_type = constants.get(0).deduceType(scopeManager);
            Type tmp_type;
            for (int i = 1; i < constants.size(); i++) {
                tmp_type = constants.get(i).deduceType(scopeManager);
                if (!atom_type.equals(tmp_type)) {
                    throw new RuntimeException("Array elements must have the same type");
                }
                if (atom_type.getBaseType().equals("null")) {
                    atom_type = tmp_type;
                    // to deal with something like {{},{1,2},{true}}
                }
            }
            type = atom_type.arrayReference();
        } else {
            // according to the literal value
            switch (literal_value) {
                case "true", "false" -> type = new Type("bool");
                case "null" -> type = new Type("null");
                case "this" -> type = new Type("this");
                case "void" -> type = new Type("void");
                default -> {
                    try {
                        Integer.parseInt(literal_value);
                        type = new Type("int");
                    } catch (NumberFormatException e) {
                        type = new Type("string");
                    }
                }
            }
        }
        return type;
    }

    @Override
    public boolean isLeftValue() {
        return false;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
