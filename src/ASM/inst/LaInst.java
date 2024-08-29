package ASM.inst;

public class LaInst extends ASMInst {
    public String rd;
    public String label;

    public LaInst(String rd, String label) {
        this.rd = rd;
        this.label = label;
    }

    @Override
    public String toString() {
        return "la " + rd + ", " + label;
    }
}
