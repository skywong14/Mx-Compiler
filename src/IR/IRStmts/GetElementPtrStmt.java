package IR.IRStmts;

import java.util.ArrayList;

// <result> = getelementptr <ty>, ptr <ptrval>{, <ty> <idx>}*
public class GetElementPtrStmt extends IRStmt {
    public String dest;
    public String pointer;
    public IRType type;
    public ArrayList<String> index;

    boolean hasZero = false;

    public GetElementPtrStmt(IRType type, String pointer, ArrayList<String> index, String dest, boolean hasZero) {
        this.pointer = pointer;
        this.dest = dest;
        this.type = type;
        this.index = index;
        this.hasZero = hasZero;
    }

    public GetElementPtrStmt(IRType type, String pointer, String index, String dest, boolean hasZero) {
        this.pointer = pointer;
        this.dest = dest;
        this.type = type;
        this.index = new ArrayList<>();
        this.index.add(index);
        this.hasZero = hasZero;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(dest).append(" = getelementptr ").append(type.toString()).append(", ptr ").append(pointer);
        if (hasZero)
            sb.append(", i32 0"); // 从第0个元素（基址）进行偏移
        for (String idx : index) {
            sb.append(", i32 ").append(idx);
        }
        return sb.toString();
    }

    @Override
    public int getSpSize() { return 1; }
}
