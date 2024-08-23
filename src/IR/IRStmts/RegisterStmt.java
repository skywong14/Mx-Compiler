package IR.IRStmts;

public class RegisterStmt extends IRStmt{
    public String name;

    public RegisterStmt(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
