package IR.IRStmts;

// call void @llvm.memset.p0.p0.i32(ptr %ptr, i8 0, i32 %size, i1 false)
public class MemsetStmt extends IRStmt {
    public String ptr;
    public String value;
    public String size;
    public String isVolatile;

    public MemsetStmt(String ptr, String value, String size, String isVolatile) {
        this.ptr = ptr;
        this.value = value;
        this.size = size;
        this.isVolatile = isVolatile;
    }

    @Override
    public String toString() {
        return "call void @llvm.memset.p0.p0.i32(ptr " + ptr + ", i32 " + value + ", i32 " + size + ", i1 " + isVolatile + ")";
    }
}
