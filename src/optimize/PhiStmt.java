package optimize;

import IR.IRStmts.IRStmt;

public class PhiStmt extends IRStmt {
    public String dest;
    public String src1;
    public String src2;
    public String label1;
    public String label2;

    public PhiStmt(String dest, String src1, String src2, String label1, String label2) {
        this.dest = dest;
        this.src1 = src1;
        this.src2 = src2;
        this.label1 = label1;
        this.label2 = label2;
    }

    @Override
    public String toString() {
        return dest + " = phi i32 [" + src1 + ", %" + label1 + "], [" + src2 + ", %" + label2 + "]";
    }

    @Override
    public int getSpSize() {
        return 1;
    }
}
