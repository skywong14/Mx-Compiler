package IR.IRStmts;

// %ptr = bitcast ptr %ret to ptr
public class BitCastStmt extends IRStmt{
    public String dest;
    public String ptr;

    public BitCastStmt(String ptr, String dest) {
        this.dest = dest;
        this.ptr = ptr;
    }

    @Override
    public String toString() {
        return dest + " = bitcast ptr " + ptr + " to ptr";
    }
}
