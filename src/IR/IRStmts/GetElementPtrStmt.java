package IR.IRStmts;

import java.util.ArrayList;

// <result> = getelementptr <ty>, ptr <ptrval>{, <ty> <idx>}*
public class GetElementPtrStmt extends IRStmt {
    public String dest;
    public String pointer;
    public BasicIRType type;
    public ArrayList<String> index;

    public GetElementPtrStmt(BasicIRType type, String pointer, ArrayList<String> index, String dest) {
        this.pointer = pointer;
        this.dest = dest;
        this.type = type;
        this.index = index;
    }

    public GetElementPtrStmt(BasicIRType type, String pointer, String index, String dest) {
        this.pointer = pointer;
        this.dest = dest;
        this.type = type;
        this.index = new ArrayList<>();
        this.index.add(index);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(dest).append(" = getelementptr ").append(type.toString()).append(", ptr ").append(pointer);
        sb.append(", i32 0"); // 从第0个元素（基址）进行偏移
        for (String idx : index) {
            sb.append(", i32 ").append(idx);
        }
        return sb.toString();
    }
}
