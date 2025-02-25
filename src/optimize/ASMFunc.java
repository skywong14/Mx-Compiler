package optimize;

import ASM.inst.ASMInst;
import ASM.inst.RetInst;
import ASM.section.ASMBlock;
import optimize.utils.TemplateInst;

import java.util.ArrayList;

public class ASMFunc {
    public String name, blockHead;
    public ArrayList<ASMBlock> blocks;
    public ASMBlock curBlock;
    public int spOffset;
    public TemplateInst epilogue;
    public TemplateInst prologue;
    public boolean needPrologueBlock = false;

    public ArrayList<ASMInst> inits, ends;

    public ASMFunc(String name, int funcCnt) {
        this.name = name;
        this.blockHead = "B" + funcCnt + ".";
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

    public void setEpilogue(ArrayList<ASMInst> epilogue) {
        this.epilogue = new TemplateInst(epilogue);
    }

    public void setPrologue(ArrayList<ASMInst> prologue) {
        this.prologue = new TemplateInst(prologue);
    }

    public void addInst(ASMInst inst) {
        curBlock.addInst(inst);
    }
    public void addInst(ArrayList<ASMInst> inst) {
        curBlock.addInst(inst);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (ASMBlock block : blocks) {
            sb.append(block.toString());
        }
        return sb.toString();
    }
}
