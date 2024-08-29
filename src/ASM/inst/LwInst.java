package ASM.inst;

public class LwInst extends ASMInst {
    public int size;
    public String dest;
    public String base;
    public int offset;

    public LwInst(int size, String dest, String base, int offset) {
        this.size = size;
        this.dest = dest;
        this.base = base;
        this.offset = offset;
    }

    @Override
    public String toString() {
        return "lw " + dest + ", " + offset + "(" + base + ")";
    }
}
