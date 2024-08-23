package IR.IRStmts;

public class BranchStmt extends IRStmt {
    public String label;

    public BranchStmt(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return "br label %" + label;
    }
}
