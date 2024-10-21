package IR.IRStmts;

public class AllocaStmt extends IRStmt {
    public String dest;
    public BasicIRType type;

    public AllocaStmt(BasicIRType type, String dest) {
        this.type = type;
        this.dest = dest;
    }

    @Override
    public String toString() {
        return dest + " = alloca " + type.toString();
    }
}
