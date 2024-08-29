package ASM.inst;

public class BranchIfInst extends ASMInst {
    public String op;
    public String rs1;
    public String rs2;
    public String label;
    public BranchIfInst(String op, String rs1, String rs2, String label) {
        this.op = op;
        this.rs1 = rs1;
        this.rs2 = rs2;
        this.label = label;
    }

    @Override
    public String toString() {
        if (rs2 == null) return op + " " + rs1 + ", " + label;
        return op + " " + rs1 + ", " + rs2 + ", " + label;
    }
}
