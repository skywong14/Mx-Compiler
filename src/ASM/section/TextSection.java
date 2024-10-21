package ASM.section;

import optimize.ASMFunc;

import java.util.ArrayList;

public class TextSection extends ASMSection {
    String name = ".text";
    public ArrayList<ASMFunc> functions = new ArrayList<>();

    public TextSection() {
    }

    public void addFunction(ASMFunc func) {
        functions.add(func);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(".section ").append(name).append("\n");
        for (ASMFunc func : functions) {
            sb.append(func.toString());
        }
        return sb.toString();
    }
}
