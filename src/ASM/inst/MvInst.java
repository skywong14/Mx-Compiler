package ASM.inst;

public class MvInst extends ASMInst {
    String src, dest;
    public MvInst(String dest, String src) {
        this.src = src;
        this.dest = dest;
    }

    @Override
    public String toString() {
        return "mv " + dest + ", " + src;
    }
}
