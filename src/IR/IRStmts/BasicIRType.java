package IR.IRStmts;

import semantic.Type;

public class BasicIRType extends IRType {
    // can only be int, bool, void, ptr
    public String typeName;

    public BasicIRType() {
        typeName = "ptr";
    }

    public BasicIRType(Type type) {
        if (type.is_array) {
            this.typeName = "ptr";
        } else {
            switch (type.baseType) {
                case "int" -> this.typeName = "i32";
                case "bool" -> this.typeName = "i1";
                case "void" -> this.typeName = "void";
                default -> this.typeName = "ptr";
            }
        }
    }

    public BasicIRType(String typeName) {
        this.typeName = typeName;
    }

    public int elementSize() {
        switch (typeName) {
            case "bool" -> {
                return 1;
            }
            case "void" -> {
                return 0;
            }
            default -> {
                return 32;
            }
        }
    }

    @Override
    public String toString() {
        return this.typeName;
    }
}
