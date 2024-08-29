package ASM.inst;

public class MvInst extends ASMInst {
    String rd, rs;

    public MvInst(String rd, String rs) {
        this.rd = rd;
        this.rs = rs;
    }

    @Override
    public String toString() {
        return "mv " + rd + ", " + rs;
    }
}
