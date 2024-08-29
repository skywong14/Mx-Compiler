package IR.IRStmts;

import java.util.ArrayList;
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

    public void addAllocaStmts(ArrayList<AllocaStmt> stmts_) {
        stmts.addAll(0, stmts_);
    }

    void updateBlock() {
        if (stmts.isEmpty()) label = "";
        else {
            int sz = stmts.size();
            for (IRStmt stmt : stmts) {
                if (stmt instanceof BranchStmt || stmt instanceof ReturnStmt) {
                    // remove all stmts after branch or return
                    int idx = stmts.indexOf(stmt);
                    if (idx == sz - 1) break;
                    stmts = new ArrayList<>(stmts.subList(0, idx + 1));
                    break;
                }
            }
        }
    }

    @Override
    public String toString() {
        updateBlock();
        StringBuilder sb = new StringBuilder();
        if (!Objects.equals(label, "")) sb.append(label).append(":\n");
        for (IRStmt stmt : stmts) {
            sb.append("\t").append(stmt.toString()).append("\n");
        }
        return sb.toString();
    }

    @Override
    public int getSpSize() {
        int spSize = 0;
        for (IRStmt stmt : stmts) {
            spSize += stmt.getSpSize();
        }
        return spSize;
    }
}