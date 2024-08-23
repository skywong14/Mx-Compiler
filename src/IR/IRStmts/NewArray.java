package IR.IRStmts;

public class NewArray extends IRStmt{
    public String dest;
    public BasicIRType type;
    public int size;

    public NewArray(String dest, int size, BasicIRType type) {
        this.dest = dest;
        this.size = size;
        this.type = type;
    }

    @Override
    public String toString() {
        return dest + " = call ptr @__allocateArray__(i32 " + size + ", i32 "+ type.elementSize() +")\n";
    }
}
