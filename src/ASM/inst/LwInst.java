package ASM.inst;

public class LwInst extends ASMInst {
    public int size;
    public String dest;
    public String base;
    public int offset;

    public LwInst(String dest, int offset, String base) {
        this.dest = dest;
        this.base = base;
        this.offset = offset;
    }

    @Override
    public String toString() {
        return "lw " + dest + ", " + offset + "(" + base + ")";
    }
}
