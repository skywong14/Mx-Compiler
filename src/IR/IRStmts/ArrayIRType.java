package IR.IRStmts;

import IR.IRBuilder;

public class ArrayIRType extends IRType {
    IRType baseType;
    int size;

    ArrayIRType(IRType baseType, int size) {
        this.baseType = baseType;
        this.size = size;
    }

    @Override
    public String toString() {
        return "[" + size + " x " + baseType.toString() + "]";
    }
}
