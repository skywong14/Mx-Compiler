package ASM.inst;

public class SwInst extends ASMInst {
    public String src;
    public String dest;
    public int offset;
    public SwInst(String src, String dest, int offset) {
        this.src = src;
        this.dest = dest;
        this.offset = offset;
    }

    @Override
    public String toString() {
        return "sw " + src + ", " + offset + "(" + dest + ")";
    }
}
