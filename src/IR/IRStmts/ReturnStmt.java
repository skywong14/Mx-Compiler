package IR.IRStmts;

public class ReturnStmt extends IRStmt {
    public BasicIRType type;
    public String src;

    public ReturnStmt(BasicIRType type, String src) {
        this.type = type;
        this.src = src;
    }

    @Override
    public String toString() {
        return "ret " + type.toString() + " " + src;
    }
}
