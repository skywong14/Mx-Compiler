package IR.IRStmts;

public class AllocaStmt extends IRStmt {
    public String dest;
    public IRType type;

    public AllocaStmt(IRType type, String dest) {
        this.type = type;
        this.dest = dest;
    }

    @Override
    public String toString() {
        return dest + " = alloca " + type.toString();
    }

    @Override
    public int getSpSize() { return 1; }
}
