package IR.IRStmts;

public class BinaryExprStmt extends IRStmt{
    public String operator;
    public String register1, register2, dest;
    public BasicIRType type;

    public BinaryExprStmt(String operator, BasicIRType type, String register1, String register2, String dest) {
        this.operator = operator;
        this.register1 = register1;
        this.register2 = register2;
        this.dest = dest;
        this.type = type;
    }

    @Override
    public String toString() {
        return dest + " = " + operator + " " + type.toString() + " " + register1 + ", " + register2 + "\n";
    }

    @Override
    public int getSpSize() { return 1; }
}
