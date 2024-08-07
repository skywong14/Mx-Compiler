package semantic;

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
    public Type(TypeEnum basicType_, boolean is_array = false, int dimension = 0, String className = "") {
        this.basicType = basicType_;
        if (is_array) type = TypeEnum.ARRAY;
        else type = basicType_;
        this.dimension = dimension; // 0: {}, any dimention is ok
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
}