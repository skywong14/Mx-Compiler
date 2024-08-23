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
        return "@" + name + " = global " + type.toString();
    }
}
