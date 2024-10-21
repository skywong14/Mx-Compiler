package IR.IRStmts;

public class ClassTypeStmt extends IRType{
    public String className;

    public ClassTypeDefineStmt classTypeDefineStmt;

    public ClassTypeStmt(String className, ClassTypeDefineStmt classTypeDefineStmt) {
        this.className = className;
        this.classTypeDefineStmt = classTypeDefineStmt;
    }

    @Override
    public String toString() {
        return "%class." + className;
    }
}
