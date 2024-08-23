package IR.IRStmts;

public class LoadStmt extends IRStmt{
    // <result> = load <ty>, ptr <pointer>
    public String dest;
    public String pointer;
    public BasicIRType type;

    public LoadStmt(BasicIRType type, String pointer, String dest) {
        this.pointer = pointer;
        this.dest = dest;
        this.type = type;
    }

    @Override
    public String toString() {
        return dest + " = load " + type.toString() + ", " + type.toString() + "* " + pointer;
    }
}
