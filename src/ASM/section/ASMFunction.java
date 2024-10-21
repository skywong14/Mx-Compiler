package ASM.section;

import ASM.inst.ASMInst;
import ASM.operand.PhysicalReg;

import java.util.ArrayList;
import java.util.HashMap;

public class ASMFunction {
    public String name;
    public ArrayList<ASMBlock> blocks;
    public int spOffset;
    public int curSpOffset;

    public ASMBlock curBlock;

    public PhysicalReg physicalReg;

    public String blockHead = null;

    public HashMap<String, Integer> virtualRegMap;

    // can use memory: [ 0(sp) , spOffsetMax(sp) )
    public ASMFunction(String name, int spOffset) {
        this.name = name;
        this.blocks = new ArrayList<>();
        this.spOffset = ((spOffset - 1) / 16 + 1 ) * 16; // 16 字节对齐
        this.physicalReg = new PhysicalReg();
        this.curSpOffset = 0; // 从底开始分配
        this.virtualRegMap = new HashMap<>();
        curBlock = new ASMBlock(name, true, 2);
        this.blocks.add(curBlock);
    }

    public int allocMemory(int size) {
        if (size % 4 != 0) throw new RuntimeException("size not aligned");
        curSpOffset += size;
        if (curSpOffset > spOffset) throw new RuntimeException("stack overflow");
        return curSpOffset - size;
    }

    public void putVirtualReg(String regName, int offset) {
        virtualRegMap.put(regName, offset);
    }

    public int getVirtualReg(String regName) {
        if (!virtualRegMap.containsKey(regName)) {
            int offset = allocMemory(4);
            virtualRegMap.put(regName, offset);
        }
        return virtualRegMap.get(regName);
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

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(".type ").append(name).append(", @function\n");
        for (ASMBlock block : blocks) {
            sb.append(block.toString()).append("\n");
        }
        return sb.toString();
    }
}
