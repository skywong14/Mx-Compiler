package semantic;

import semantic.ASTNodes.ExpressionNode;

import java.util.ArrayList;
import java.util.Objects;

public class Type {
    // int, boolean, string, class, array
    // string, class, array type are all reference type in fact

    String basicType;
    boolean is_array;
    int dimension; // 0: {}, any dimention is ok

    public Type(String basicType_, boolean is_array_, int dimension_) {
        this.basicType = basicType_;
        this.is_array = is_array_;
        this.dimension = dimension_;
    }

    public String toString() {
        String res = basicType;
        if (is_array) {
            if (dimension == 0)  res += "{}";
            else res += "[]".repeat(dimension);
        }
        return res;
    }
    public boolean equals(Type otherType) {
        if (this.is_array != otherType.is_array) return false;
        if (!this.is_array) return this.basicType.equals(otherType.basicType);
        // is_array
        if (!this.basicType.equals(otherType.basicType)) return false;
        if (this.dimension == 0 || otherType.dimension == 0) return true;
        return this.dimension == otherType.dimension;
    }
}