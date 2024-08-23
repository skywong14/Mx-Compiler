package IR.IRStmts;

public class NewClassStmt extends IRStmt{
    // alloc memory, then call constructor, return ptr to dest
    public String dest;
    public String className;

    public AllocaStmt allocStmt;
    public CallStmt callStmt;

    public NewClassStmt(String className, String dest) {
        this.dest = dest;
        this.className = className;
        generateStmt();
    }

    void generateStmt() {
        allocStmt = new AllocaStmt(new ClassTypeStmt(className, null), dest);
        callStmt = new CallStmt(); // void, className, dest
        //todo
    }


    @Override
    public String toString() {
        return allocStmt.toString() + callStmt.toString();
    }
}
