package ASM.inst;

public class MvInst extends ASMInst {
    String src, dest;
    public MvInst(String src, String dest) {
        this.src = src;
        this.dest = dest;
    }

    @Override
    public String toString() {
        return "mv " + src + " " + dest;
    }
}
