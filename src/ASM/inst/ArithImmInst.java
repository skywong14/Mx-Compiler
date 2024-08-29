package ASM.inst;

public class ArithImmInst extends ASMInst {
    public String op;
    public String opType;
    public String rd;
    public String rs;
    public int imm;
    public ArithImmInst(String op, String rd, String rs, int imm) {
        this.op = op;
        this.rd = rd;
        this.rs = rs;
        this.imm = imm;
        switch (this.op) {
            case "+": this.opType = "addi"; break;
            case "&": this.opType = "andi"; break;
            case "|": this.opType = "ori"; break;
            case "^": this.opType = "xori"; break;
            case "<<": this.opType = "slli"; break;
            case ">>": this.opType = "srai"; break;
            case "<": this.opType = "slti"; break;
            case "<u": this.opType = "sltiu"; break;
            default: throw new RuntimeException("ArithImmInst: unknown op " + op);
        }
    }

    @Override
    public String toString() {
        return opType + " " + rd + ", " + rs + ", " + imm;
    }
}
