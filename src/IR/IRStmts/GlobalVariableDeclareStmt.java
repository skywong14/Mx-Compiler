package IR.IRStmts;

public class GlobalVariableDeclareStmt extends IRStmt{
    public String name;
    public BasicIRType type;

    public GlobalVariableDeclareStmt(BasicIRType type, String name) {
        this.name = name;
        this.type = type;
    }

    @Override
    public String toString() {
        if (type.typeName.equals("ptr")) return "@" + name + " = global " + type.toString() + " null";
        return "@" + name + " = global " + type.toString() + " 0";
    }

    @Override
    public int getSpSize() { return 0; }
}
