package ASM.section;

import java.util.ArrayList;

public class TextSection extends ASMSection {
    String name = ".text";
    public ArrayList<ASMFunction> functions = new ArrayList<>();

    public TextSection() {
    }

    public void addFunction(ASMFunction func) {
        functions.add(func);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(".section ").append(name).append("\n");
        for (ASMFunction func : functions) {
            sb.append(func.toString());
        }
        return sb.toString();
    }
}
