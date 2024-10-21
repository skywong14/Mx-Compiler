package IR.IRStmts;

import java.util.ArrayList;

// <result> = getelementptr <ty>, ptr <ptrval>{, <ty> <idx>}*
public class GetElementPtrStmt extends IRStmt {
    public String dest;
    public String pointer;
    public IRType type;
    public String index;

    public boolean hasZero = false;

    public GetElementPtrStmt(IRType type, String pointer, String index, String dest, boolean hasZero) {
        this.pointer = pointer;
        this.dest = dest;
        this.type = type;
        this.index = index;
        this.hasZero = hasZero;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(dest).append(" = getelementptr ").append(type.toString()).append(", ptr ").append(pointer);
        if (hasZero)
            sb.append(", i32 0"); // 从第0个元素（基址）进行偏移
        sb.append(", i32 ").append(index);
        return sb.toString();
    }
}
