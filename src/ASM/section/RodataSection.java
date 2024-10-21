package ASM.section;

import ASM.inst.Directive;

import java.util.ArrayList;

public class RodataSection extends ASMSection {
    public String name = ".rodata";
    public ArrayList<ASMBlock> strings = new ArrayList<>();

    public RodataSection() {
    }

    public void addStringConst(String label, String literal) {
        ASMBlock block = new ASMBlock(label, true, 1);
        strings.add(block);
        block.addInst(new Directive(".asciz", "\"" + literal + "\""));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(".section ").append(name).append("\n");
        for (ASMBlock block : strings) {
            sb.append(block.toString());
        }
        return sb.toString();
    }
}
