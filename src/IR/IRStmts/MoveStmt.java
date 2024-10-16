package IR.IRStmts;

public class MoveStmt extends IRStmt {
    public String dest;
    public String src;

    public MoveStmt(String dest, String src) {
        this.dest = dest;
        this.src = src;
    }

    @Override
    public String toString() {
        return dest + " = " + src;
    }

    @Override
    public int getSpSize() {
        return 0;
    }
}
