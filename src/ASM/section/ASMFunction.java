package ASM.section;

import ASM.inst.ASMInst;
import ASM.inst.CallInst;
import ASM.operand.PhysicalReg;

import java.util.ArrayList;
import java.util.HashMap;

/*
| ...               | <- 栈顶 (较高地址)
| 临时数据            | <- 临时使用的栈空间 (例如用于计算)
| 空闲空间            | <- 可用的额外栈空间
| ...               |
| 局部变量            | <- 函数的局部变量
| 保存的寄存器值       | <- 保存的寄存器值: a0~a8, s0~s12
| (sp) 栈指针        | <- 栈底 (较低地址)
*/
public class ASMFunction {
    public String name;
    public ArrayList<ASMBlock> blocks;
    public int spOffset;
    public int curSpOffset;

    public ASMBlock curBlock;

    public PhysicalReg physicalReg;

    //public VirtualReg virtualReg;
    public HashMap<String, Integer> virtualRegMap;

    // can use: sp(0) ~ sp(spOffsetMax - 4)
    public ASMFunction(String name, int spOffset) {
        this.name = name;
        this.blocks = new ArrayList<>();
        this.spOffset = ((spOffset - 1) / 16 + 1 ) * 16; // 16 字节对齐
        this.physicalReg = new PhysicalReg();
        this.curSpOffset = 0; // 从底开始分配
        this.virtualRegMap = new HashMap<>();
        curBlock = new ASMBlock(name, true, 2, this);
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
//            throw new RuntimeException("virtual register not found");
        }
        return virtualRegMap.get(regName);
    }

    public void newBlock(String label) {
        curBlock = new ASMBlock(label, this);
        blocks.add(curBlock);
    }

    public void addBlock(ASMBlock block) {
        blocks.add(block);
        curBlock = block;
    }

    public void addInst(ASMInst inst) {
        curBlock.addInst(inst);
    }

    public void addInst(ArrayList<ASMInst> inst) {
        curBlock.addInst(inst);
    }




    public String toString() {
        StringBuilder sb = new StringBuilder();
//        sb.append(".globl ").append(name).append("\n");
        sb.append(".type ").append(name).append(", @function\n");
//        sb.append(name).append(":\n");
        for (ASMBlock block : blocks) {
            sb.append(block.toString()).append("\n");
        }
        return sb.toString();
    }
}
