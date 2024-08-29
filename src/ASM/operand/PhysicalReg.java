package ASM.operand;

public class PhysicalReg {
    public class Register{
        public String name;
        public int value;
        public int id;
        public Register(String name, int id){
            this.name = name;
            this.id = id;
            this.value = 0;
        }
        public void set(int value){ this.value = value; }
        public int get(){ return this.value; }
        public String toString(){ return this.name; }
    }
    public Register zero, ra, sp, gp, tp;
    public Register[] a, s, t;

    public PhysicalReg(){
        zero = new Register("zero", 0);
        ra = new Register("ra", 1);
        sp = new Register("sp", 2);
        gp = new Register("gp", 3);
        tp = new Register("tp", 4);
        a = new Register[8];
        for (int i = 0; i < 8; i++)
            a[i] = new Register("a" + i, i + 10);
        s = new Register[12];
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
    }
}
