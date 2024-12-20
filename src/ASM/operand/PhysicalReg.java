package ASM.operand;

import java.util.HashMap;

public class PhysicalReg {
    public Register zero, ra, sp, gp, tp;
    public Register[] a, s, t; // a: 0~7  s: 0~11  t: 0~6
    public int[] offset;
    public HashMap<Integer, Register> regMap;
    public PhysicalReg(){
        offset = new int[32];
        zero = new Register("zero", 0);
        ra = new Register("ra", 1);
        sp = new Register("sp", 2);
        gp = new Register("gp", 3);
        tp = new Register("tp", 4);
        a = new Register[8];
        for (int i = 0; i < 8; i++)
            a[i] = new Register("a" + i, i + 10);
        s = new Register[12];
        s[0] = new Register("s0", 8);
        s[1] = new Register("s1", 9);
        for (int i = 2; i < 12; i++)
            s[i] = new Register("s" + i, i + 16); // s0, s1 are reserved
        t = new Register[7];
        t[0] = new Register("t0", 5);
        t[1] = new Register("t1", 6);
        t[2] = new Register("t2", 7);
        t[3] = new Register("t3", 28);
        t[4] = new Register("t4", 29);
        t[5] = new Register("t5", 30);
        t[6] = new Register("t6", 31);

        // able to use: a0~a7, s0~s11, sp, tp
        regMap = new HashMap<>();
        // [0, 8)
        for (int i = 0; i < 8; i++)
            regMap.put(i, a[i]);
        // [8, 20)
        for (int i = 0; i < 12; i++)
            regMap.put(i + 8, s[i]);
        regMap.put(20, gp);
        regMap.put(21, tp);
        for (int i = 0; i < 7; i++)
            regMap.put(i + 22, t[i]);
    }

    public Register getReg(int index) {
        return regMap.get(index);
    }
    public String getName(int index) {
        return regMap.get(index).name;
    }
}
