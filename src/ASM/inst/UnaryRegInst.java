package ASM.inst;

public class UnaryRegInst extends ASMInst {
    public String op;
    public String rd, rs;

    public UnaryRegInst(String op, String rd, String rs) {
        this.op = op;
        this.rd = rd;
        this.rs = rs;
    }

    @Override
    public String toString() {
        return op + " " + rd + ", " + rs;
    }
}
