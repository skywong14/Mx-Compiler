package IR.IRStmts;

public class LabelStmt extends IRStmt {
    public String label;

    public LabelStmt(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label + ":";
    }

    @Override
    public int getSpSize() { return 0; }
}
