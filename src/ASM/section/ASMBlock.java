package ASM.section;

import ASM.inst.ASMInst;
import ASM.inst.JInst;

import java.util.ArrayList;
import java.util.HashMap;

public class ASMBlock{
    // attributes
    public boolean isGlobal;
    public int alignSize;
    public String label;
    // instructions
    public ArrayList<ASMInst> insts;

    public HashMap<String, ASMBlock> blocksForJump = new HashMap<>();

    public ASMBlock(String label) {
        this.label = label;
        this.insts = new ArrayList<>();
        this.isGlobal = false;
        this.alignSize = 0;
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

    public void addInst(ArrayList<ASMInst> inst) {
        insts.addAll(inst);
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

        // add jump block
        for (ASMBlock block : blocksForJump.values()) {
            sb.append(block.toString());
        }

        return sb.toString();
    }

    public String getBlockForJump(String blockName) {
        if (blockName.equals(label)) return label;
        if (blocksForJump.containsKey(blockName)) return blocksForJump.get(blockName).label;
        // add new block
        ASMBlock newBlock = new ASMBlock(label + "2" + blockName);
        newBlock.addInst(new JInst(blockName));
        blocksForJump.put(blockName, newBlock);
        return newBlock.label;
    }
}
