package ASM.inst;

public class LiInst extends ASMInst {
    public int val;
    public LiInst(int val) {
        this.val = val;
    }

    @Override
    public String toString() {
        return "li " + val;
    }
}
