package IR.IRStmts;

public class SelectStmt extends IRStmt {
    public String dest;
    public String cond;
    public String trueVal;
    public String falseVal;
    public BasicIRType type;

    public SelectStmt(BasicIRType type, String cond, String trueVal, String falseVal, String dest) {
        this.dest = dest;
        this.cond = cond;
        this.trueVal = trueVal;
        this.falseVal = falseVal;
        this.type = type;
    }

    @Override
    public String getDest() { return dest; }

    @Override
    public String toString() {
        return dest + " = select i1 " + cond + ", " + type.toString() + " " + trueVal + ", " + type.toString() + " " + falseVal;
    }
}
