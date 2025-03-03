package IR.IRStmts;

public class StoreStmt extends IRStmt {
    public String val;
    public String dest;
    public BasicIRType type;

    // store <type> val, ptr dest
    public StoreStmt(BasicIRType type, String val, String dest) {
        this.val = val;
        this.dest = dest;
        this.type = type;
    }

    @Override
    public String getDest() { return null; }

    @Override
    public String toString() {
        return "store " + type.toString() + " " + val + ", ptr " + dest;
    }
}
