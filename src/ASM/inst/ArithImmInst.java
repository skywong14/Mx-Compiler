package ASM.inst;

public class ArithImmInst extends ASMInst {
    public String op;
    public String rd;
    public String rs;
    public String imm;
    public ArithImmInst(String op, String rd, String rs, String imm) {
        this.op = op;
        this.rd = rd;
        this.rs = rs;
        this.imm = imm;
    }

    @Override
    public String toString() {
        return op + " " + rd + ", " + rs + ", " + imm;
    }
}
