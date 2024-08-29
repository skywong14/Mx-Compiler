package ASM.section;

import ASM.operand.PhysicalReg;

import java.util.ArrayList;

/*
| ...              | <- 栈顶 (较高地址)
| 保存的寄存器值      | <- 比如 `ra`, `s0`, ...
| 局部变量           | <- 函数的局部变量
| 临时数据           | <- 临时使用的栈空间 (例如用于计算)
| 空闲空间           | <- 可用的额外栈空间
| ...               |
| (sp) 栈指针        | <- 栈底 (较低地址)
 */
public class ASMFunction {
    public String name;
    public ArrayList<ASMBlock> blocks;
    public int spOffset;

    public PhysicalReg physicalReg;

    public ASMFunction(String name, int spOffset) {
        this.name = name;
        this.blocks = new ArrayList<>();
        this.spOffset = spOffset;
        this.physicalReg = new PhysicalReg();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        return sb.toString();
    }
}
