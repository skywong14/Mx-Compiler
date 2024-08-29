package ASM.inst;

public class JInst extends ASMInst {
    public String label;

    public JInst(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return "j " + label;
    }
}
