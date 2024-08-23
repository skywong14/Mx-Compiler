package IR.IRStmts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class Block extends IRStmt {
    public String label;
    public ArrayList<IRStmt> stmts;

    public Block(String label_) {
        label = label_;
        stmts = new ArrayList<>();
    }

    public void addStmt(IRStmt stmt) {
        stmts.add(stmt);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (!Objects.equals(label, "")) sb.append(label).append(":\n");
        for (IRStmt stmt : stmts) {
            sb.append("\t").append(stmt.toString()).append("\n");
        }
        return sb.toString();
    }
}