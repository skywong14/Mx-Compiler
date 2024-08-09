package semantic;

import java.util.Objects;

public class Type {
    // int, boolean, string, class, array
    // string, class, array type are all reference type in fact
    public enum TypeEnum {
        INT, BOOL, STRING, CLASS, ARRAY, UNKNOWN
    }
    String className;
    TypeEnum type, basicType;
    int dimension;
    public Type() {
        type = TypeEnum.UNKNOWN;
    }
    public Type(TypeEnum basicType_, boolean is_array, int dimension, String className_) {
        this.basicType = basicType_;
        this.className = className_;
        if (is_array) type = TypeEnum.ARRAY;
        else type = basicType_;
        this.dimension = dimension; // 0: {}, any dimention is ok
    }
    public Type(TypeEnum basicType_) {
        this(basicType_, false, 0, "");
    }
    public Type(Type otherType) {
        this.basicType = otherType.basicType;
        this.type = otherType.type;
        this.dimension = otherType.dimension;
        this.className = otherType.className;
    }

    public String toString() {
        if (type == TypeEnum.ARRAY) {
            return "Type{type='" + type + "', basicType='" + basicType +
                    "', dimension='" + dimension + "', className='" + className + "'}";
        } else {
            return "Type{type='" + type + "}";
        }
    }
    public boolean equals(Type otherType) {
        if (this.type != otherType.type) return false;
        if (this.type == TypeEnum.ARRAY) {
            if (this.dimension != otherType.dimension) return false;
            if (this.basicType != otherType.basicType) return false;
            if (this.basicType == TypeEnum.CLASS) {
                return this.className.equals(otherType.className);
            }
        }
        if (this.type == TypeEnum.CLASS) {
            return this.className.equals(otherType.className);
        }
        return true;
    }
}