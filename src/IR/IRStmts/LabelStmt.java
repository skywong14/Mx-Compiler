package IR.IRStmts;

public class LabelStmt extends IRStmt {
    public String label;

    public LabelStmt(String label) {
        this.label = label;
    }

    @Override
    public String getDest() { return null; }

    @Override
    public String toString() {
        return label + ":";
    }
}
