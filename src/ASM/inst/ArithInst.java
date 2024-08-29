package ASM.inst;

public class ArithInst extends ASMInst {
    // add, sub, and, or, xor, sll, srl, sra; slt, sltu
    // mul, div, rem
    public String rd, rs1, rs2;
    public String op;

    public ArithInst(String rd, String rs1, String rs2, String op) {
        this.rd = rd;
        this.rs1 = rs1;
        this.rs2 = rs2;
        this.op = op;
    }

    @Override
    public String toString() {
        return op + " " + rd + ", " + rs1 + ", " + rs2;
    }
}
