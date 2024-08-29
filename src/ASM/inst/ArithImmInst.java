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
            case "*": this.opType = "mul"; break;
            case "/": this.opType = "div"; break;
            case "%": this.opType = "rem"; break;
            case "&": this.opType = "and"; break;
            case "|": this.opType = "or"; break;
            case "^": this.opType = "xor"; break;
            case "<<": this.opType = "sll"; break;
            case ">>": this.opType = "sra"; break;
            case "<": this.opType = "slt"; break;
            case "<u": this.opType = "sltu"; break;
            default: throw new RuntimeException("ArithImmInst: unknown op " + op);
        }
    }

    @Override
    public String toString() {
        return opType + " " + rd + ", " + rs + ", " + imm;
    }
}
