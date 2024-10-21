package optimize;

import ASM.inst.ASMInst;
import ASM.section.ASMBlock;
import ASM.section.ASMFunction;

import java.util.ArrayList;

public class ASMFunc {
    public String name, blockHead;
    public ArrayList<ASMBlock> blocks;
    public ASMBlock curBlock;
    public int spOffset;

    public ArrayList<ASMInst> inits, ends;

    public ASMFunc(String name) {
        this.name = name;
        this.blockHead = name + "_head.";
        blocks = new ArrayList<>();
        inits = new ArrayList<>();
        ends = new ArrayList<>();
        curBlock = new ASMBlock(name, true, 2);
        blocks.add(curBlock);
    }

    public void newBlock(String label) {
        curBlock = new ASMBlock(label);
        blocks.add(curBlock);
    }

    public void addInst(ASMInst inst) {
        curBlock.addInst(inst);
    }
    public void addInst(ArrayList<ASMInst> inst) {
        curBlock.addInst(inst);
    }

    @Override
    public String toString() {
        // todo
        return "";
    }
}
