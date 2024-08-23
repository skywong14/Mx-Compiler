package semantic;

public class Type {
    // int, boolean, string, void, class, array
    // string, class, array type are all reference type in fact

    public String baseType;
    public boolean is_array;
    public int dimension;

    public Type(String basicType_) {
        this.baseType = basicType_;
        this.is_array = false;
        this.dimension = 0;
    }
    public Type(String basicType_, boolean is_array_, int dimension_) {
        this.baseType = basicType_;
        this.is_array = is_array_;
        this.dimension = dimension_;
    }

    public boolean isReferenceType() {
        return !baseType.equals("int") && !baseType.equals("bool") && !baseType.equals("void");
    }
    public boolean isArray() { return is_array; }
    public String getBaseType() { return baseType; }
    public int getDimension() { return dimension; }
    public Type arrayReference() {
        return new Type(baseType, true, dimension + 1);
    }
    public Type arrayDereference(int n) {
        if (n > dimension) {
            throw new RuntimeException("[Dimension Out Of Bound] Array dereference error");
        }
        if (n == dimension) {
            return new Type(baseType, false, 0);
        }
        return new Type(baseType, is_array, dimension - n);
    }

    public String toString() {
        String res = baseType;
        if (is_array) {
            res += "{}".repeat(dimension);
        }
        return res;
    }
    public boolean equals(Type otherType) {
        if (this.equals("null")) {
            if (otherType.is_array) return true;
            if (otherType.baseType.equals("int") || otherType.baseType.equals("bool") || otherType.baseType.equals("void")) return false;
            return true;
        }
        if (otherType.equals("null")) {
            if (this.is_array) return true;
            if (this.baseType.equals("int") || this.baseType.equals("bool") || this.baseType.equals("void")) return false;
            return true;
        }
        // not null
        if (this.is_array != otherType.is_array) return false;
        if (!this.is_array) return this.baseType.equals(otherType.baseType);
        // is_array
        if (this.baseType.equals("void") || otherType.baseType.equals("void")) return false;
        if (!this.baseType.equals("null") && !otherType.baseType.equals("null")) {
            // null : something like {{},{}}
            if (!this.baseType.equals(otherType.baseType)) return false;
        }
        // dimension check
        return this.dimension == otherType.dimension;
    }
    public boolean equals(String basicType) {
        return this.baseType.equals(basicType) && !this.is_array;
    }
}