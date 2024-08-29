package ASM.inst;

public class JalInst extends ASMInst {
    public String label;
    public JalInst(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return "jal " + label;
    }
}
