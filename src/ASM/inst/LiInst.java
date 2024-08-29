package ASM.inst;

public class LiInst extends ASMInst {
    public int val;
    public String rd;

    public LiInst(String rd, int val) {
        this.val = val;
        this.rd = rd;
    }

    @Override
    public String toString() {
        return "li " + rd + ", " + val;
    }
}
