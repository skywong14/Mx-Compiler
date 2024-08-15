package semantic;

import java.util.Objects;

public class Type {
    // int, boolean, string, void, class, array
    // string, class, array type are all reference type in fact

    String basicType;
    boolean is_array;
    int dimension;

    public Type(String basicType_) {
        this.basicType = basicType_;
        this.is_array = false;
        this.dimension = 0;
    }
    public Type(String basicType_, boolean is_array_, int dimension_) {
        this.basicType = basicType_;
        this.is_array = is_array_;
        this.dimension = dimension_;
    }

    public boolean isReferenceType() {
        return !basicType.equals("int") && !basicType.equals("bool") && !basicType.equals("void");
    }
    public boolean isArray() { return is_array; }
    public String getBasicType() { return basicType; }
    public int getDimension() { return dimension; }
    public Type arrayReference() {
        return new Type(basicType, true, dimension + 1);
    }
    public Type arrayDereference(int n) {
        if (n > dimension) {
            throw new RuntimeException("Array dereference error");
        }
        if (n == dimension) {
            return new Type(basicType, false, 0);
        }
        return new Type(basicType, is_array, dimension - n);
    }

    public String toString() {
        String res = basicType;
        if (is_array) {
            res += "{}".repeat(dimension);
        }
        return res;
    }
    public boolean equals(Type otherType) {
        if (this.equals("null")) {
            if (otherType.is_array) return true;
            if (otherType.basicType.equals("int") || otherType.basicType.equals("boolean") || otherType.basicType.equals("void")) return false;
            return true;
        }
        if (otherType.equals("null")) {
            if (this.is_array) return true;
            if (this.basicType.equals("int") || this.basicType.equals("boolean") || this.basicType.equals("void")) return false;
            return true;
        }
        // not null
        if (this.is_array != otherType.is_array) return false;
        if (!this.is_array) return this.basicType.equals(otherType.basicType);
        // is_array
        if (this.basicType.equals("void") || otherType.basicType.equals("void")) return false;
        if (!this.basicType.equals("null") && !otherType.basicType.equals("null")) {
            // null : something like {{},{}}
            if (!this.basicType.equals(otherType.basicType)) return false;
        }
        return this.dimension == otherType.dimension;
    }
    public boolean equals(String basicType) {
        return this.basicType.equals(basicType) && !this.is_array;
    }
}