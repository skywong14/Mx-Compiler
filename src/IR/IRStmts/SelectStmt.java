package IR.IRStmts;

public class SelectStmt extends IRStmt {
    public String dest;
    public String cond;
    public String trueBranch;
    public String falseBranch;
    public BasicIRType type;

    public SelectStmt(BasicIRType type, String cond, String trueBranch, String falseBranch, String dest) {
        this.dest = dest;
        this.cond = cond;
        this.trueBranch = trueBranch;
        this.falseBranch = falseBranch;
        this.type = type;
    }

    @Override
    public String toString() {
        return dest + " = select i1 " + cond + ", " + type.toString() + " " + trueBranch + ", " + type.toString() + " " + falseBranch;
    }

    @Override
    public int getSpSize() { return 1; }
}
