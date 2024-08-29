package ASM.section;

import ASM.inst.ASMInst;

import java.util.ArrayList;

public class ASMBlock{
    // attributes
    public boolean isGlobal;
    public int alignSize;
    // label
    public String label;
    // instructions
    public ArrayList<ASMInst> insts;

    public ASMBlock(String label) {
        this.label = label;
        this.insts = new ArrayList<>();
        this.isGlobal = false;
        this.alignSize = 1;
    }

    public ASMBlock(String label, boolean isGlobal_, int alignSize_) {
        this.label = label;
        this.insts = new ArrayList<>();
        this.isGlobal = isGlobal_;
        this.alignSize = alignSize_;
    }


    public void addInst(ASMInst inst) {
        insts.add(inst);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (alignSize != 0) {
            sb.append(".p2align ").append(alignSize).append("\n");
        }
        if (isGlobal) {
            sb.append(".globl ").append(label).append("\n");
        }
        sb.append(label).append(":\n");
        for (ASMInst inst : insts) {
            sb.append("\t").append(inst.toString()).append("\n");
        }
        return sb.toString();
    }
}
