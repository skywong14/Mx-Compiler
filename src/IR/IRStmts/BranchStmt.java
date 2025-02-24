package IR.IRStmts;

public class BranchStmt extends IRStmt {
    public String condition;
    public String trueLabel;
    public String falseLabel;

    // br i1 <cond>, label <iftrue>, label <iffalse> ; Conditional branch
    public BranchStmt(String condition, String trueLabel, String falseLabel) {
        this.condition = condition;
        this.trueLabel = trueLabel;
        this.falseLabel = falseLabel;
    }

    // br label <dest> ; Unconditional branch
    public BranchStmt(String dest) {
        this.condition = null;
        this.trueLabel = dest;
        this.falseLabel = null;
    }

    @Override
    public String getDest() { return null; }

    @Override
    public String toString() {
        if (condition == null) {
            return "br label %" + trueLabel;
        } else {
            return "br i1 " + condition + ", label %" + trueLabel + ", label %" + falseLabel;
        }
    }
}
