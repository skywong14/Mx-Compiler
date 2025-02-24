package IR.IRStmts;

public class AssignStmt extends IRStmt {
    public String dest;
    public String src;

    public AssignStmt(String dest, String src) {
        this.dest = dest;
        this.src = src;
    }

    @Override
    public String getDest() { return dest; }

    @Override
    public String toString() {
        return dest + " = " + src;
    }
}
