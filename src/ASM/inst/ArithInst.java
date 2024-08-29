package ASM.inst;

public class ArithInst extends ASMInst {
    // add, sub, and, or, xor, sll, srl, sra; slt, sltu
    // mul, div, rem
    public String rd, rs1, rs2;
    public String op;
    public String opType;

    public ArithInst(String op, String rd, String rs1, String rs2) {
        this.rd = rd;
        this.rs1 = rs1;
        this.rs2 = rs2;
        this.op = op;
        switch (this.op) {
            case "+": this.opType = "add"; break;
            case "-": this.opType = "sub"; break;
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
        }
    }

    @Override
    public String toString() {
        return opType + " " + rd + ", " + rs1 + ", " + rs2;
    }
}
