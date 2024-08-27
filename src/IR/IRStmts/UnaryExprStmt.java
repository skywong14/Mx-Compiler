package IR.IRStmts;

public class UnaryExprStmt extends IRStmt{
    public String operator;
    public String register, dest;
    public BasicIRType type;

    public UnaryExprStmt(String operator, BasicIRType type, String register, String dest) {
        this.operator = operator;
        this.register = register;
        this.dest = dest;
        this.type = type;
    }

    @Override
    public String toString() {
        if (operator.equals("~")) {
            return dest + " = not " + type.toString() + " " + register + "\n";
            // %result = not i32 %a
        }
        if (operator.equals("!")) {
            return dest + " = xor " + type.toString() + " " + register + ", true\n";
            // %result = xor i1 %a, true
        }
        if (operator.equals("++")) {
            return dest + " = add " + type.toString() + " " + register + ", 1\n";
        }
        if (operator.equals("--")) {
            return dest + " = sub " + type.toString() + " " + register + ", 1\n";
        }
        if (operator.equals("-")) {
            return dest + " = sub " + type.toString() + " 0, " + register + "\n";
        }
        return "";
    }
}
