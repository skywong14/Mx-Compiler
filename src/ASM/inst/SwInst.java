package ASM.inst;

public class SwInst extends ASMInst {
    public String src;
    public String dest;
    public int offset;
    public SwInst(String src, int offset, String dest) {
        this.src = src;
        this.dest = dest;
        this.offset = offset;
    }

    @Override
    public String toString() {
        return "sw " + src + ", " + offset + "(" + dest + ")";
    }
}
