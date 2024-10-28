package optimize.utils;

import ASM.inst.ASMInst;

import java.util.ArrayList;

public class TemplateInst extends ASMInst {
    ArrayList<ASMInst> insts;

    public TemplateInst(ArrayList<ASMInst> insts) {
        this.insts = insts;
    }

    public void add(ASMInst inst) {
        insts.add(inst);
    }
    public void add(ArrayList<ASMInst> insts) {
        this.insts.addAll(insts);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (ASMInst inst : insts) {
            sb.append("\t").append(inst).append("\n");
        }
        return sb.toString();
    }
}
