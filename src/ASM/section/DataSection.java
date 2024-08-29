package ASM.section;

import ASM.inst.Directive;

import java.util.ArrayList;

public class DataSection extends ASMSection{
    String name = ".data";

    ArrayList<ASMBlock> data = new ArrayList<>();

    public DataSection() {}

    public void addGlobalVariable(String label, String literal) {
        ASMBlock block = new ASMBlock(label, true, 4, null);
        block.addInst(new Directive(".word", literal));
        data.add(block);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(".section ").append(name).append("\n");
        for (ASMBlock block : data) {
            sb.append(block.toString()).append("\n");
        }
        return sb.toString();
    }
}
