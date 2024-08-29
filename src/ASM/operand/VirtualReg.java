package ASM.operand;

import java.util.HashMap;

public class VirtualReg {
    HashMap<String, Integer> regMap = new HashMap<>(); // regName -> memoryOffset
    int regCnt = 0;

    public VirtualReg() {
    }

    public int getReg(String regName) {
        if (!regMap.containsKey(regName)) {
            regMap.put(regName, regCnt);
            regCnt+=4;
        }
        return regMap.get(regName);
    }
}
