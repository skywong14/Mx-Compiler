package IR.IRStmts;

public class ConstantStmt extends IRStmt{
    // type, value, dest
    public String type;
    public String value;
    public String dest;

    public ConstantStmt(String type, String value, String dest) {
        this.type = type;
        this.value = value;
        this.dest = dest;
    }

    @Override
    public String toString() {
        return "todo: ConstantStmt";
    }
}
