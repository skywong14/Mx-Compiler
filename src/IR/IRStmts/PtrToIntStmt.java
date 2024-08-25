package IR.IRStmts;

// %size = ptrtoint ptr getelementptr (%classType, ptr null, i32 1) to i32
public class PtrToIntStmt extends IRStmt{
    public String dest;
    public String classType;
    public String ptr;

    public PtrToIntStmt(String classType, String ptr, String dest) {
        this.dest = dest;
        this.classType = classType;
        this.ptr = ptr;
    }

    @Override
    public String toString() {
        return dest + " = ptrtoint ptr getelementptr (" + classType + ", ptr " + ptr + ", i32 1) to i32";
    }
}
