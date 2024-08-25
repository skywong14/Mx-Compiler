package IR.IRStmts;

public class StoreStmt extends IRStmt {
    public String src;
    public String dest;
    public BasicIRType type;

    // store <type> src(val), ptr dest
    public StoreStmt(BasicIRType type, String src, String dest) {
        this.src = src;
        this.dest = dest;
        this.type = type;
    }

    @Override
    public String toString() {
        return "store " + type.toString() + " " + src + ", ptr " + dest;
    }
}
