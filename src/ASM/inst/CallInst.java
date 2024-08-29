package ASM.inst;

public class CallInst extends ASMInst {
    public String func;
    public CallInst(String func) {
        this.func = func;
    }

    @Override
    public String toString() {
        return "call " + func;
    }
}
