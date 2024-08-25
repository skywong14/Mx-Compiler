package IR.IRStmts;

public class StringDeclareStmt extends IRStmt{
    public String dest;
    public String value;

    public StringDeclareStmt(String value, String dest) {
        this.dest = dest;
        this.value = value;
    }

    // <dest> = private unnamed_addr constant [4 x i8] c"value\00"
    @Override
    public String toString() {
        return dest + " = private unnamed_addr constant [" + value.length() + " x i8] c\"" + value + "\\00\"";
    }
}
