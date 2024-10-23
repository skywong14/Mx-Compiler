package optimize;

import IR.IRStmts.*;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;

// block for optimization
public class IRBlock extends IRStmt {
    // basic info
    public String label;
    public ArrayList<IRStmt> stmts = new ArrayList<>();;
    public boolean isHead = false;
    public boolean isTail = false;
    public IRStmt tailStmt;
    // advanced info
    public ArrayList<IRBlock> succ = new ArrayList<>();
    public ArrayList<IRBlock> pred = new ArrayList<>();
    // dominant nodes
    public int idom = -1;
    public IRBlock idomBlock = null;
    public ArrayList<IRBlock> domChildren = new ArrayList<>();
    public BitSet dom = null;
    public ArrayList<IRBlock> domFrontier = new ArrayList<>(); // 支配边界
    // block id
    public int indexInFunc = -1, topoIndex = -1;
    // all phi stmts (phi函数的名字和对应的块和值)
    public HashMap<String, PhiStmt> phiStmts = new HashMap<>();

    private void debug(String msg) {
        System.out.println("; [IRBlock: " + label + "]: " + msg);
    }

    public void addPhi(String dest, String type) {
        if (!phiStmts.containsKey(dest)) {
            phiStmts.put(dest, new PhiStmt(dest, type));
        }
    }

    public IRBlock(Block block) {
        block.updateBlock(); // remove all stmts after branch or return

        label = block.label;
        if (block.label.isEmpty()) isHead = true; // the first block of a function
        if (block.stmts.get(block.stmts.size() - 1) instanceof ReturnStmt) isTail = true; // one of the last blocks of a function

        for (IRStmt stmt : block.stmts) {
            if (stmt instanceof NewClassStmt newClassStmt) {
                stmts.add(newClassStmt.mallocStmt);
                if (newClassStmt.callStmt != null) stmts.add(newClassStmt.callStmt);
            } else if (stmt instanceof NewArrayStmt) {
                throw new RuntimeException("IRBlock: shouldn't have NewArrayStmt");
            } else {
                stmts.add(stmt);
            }
        }

        // predecessors and successors will be set in IRFunction

        // for debug only
        tailStmt = stmts.get(stmts.size() - 1);
        if (tailStmt instanceof ReturnStmt || tailStmt instanceof BranchStmt) return;
        throw new RuntimeException("IRBlock: unknown tail stmt: " + tailStmt);
    }

    public String toStringWithPhi() {
        StringBuilder sb = new StringBuilder();
        sb.append(label).append(":\n");
        for (String phi : phiStmts.keySet()) {
            sb.append("\t").append(phiStmts.get(phi).toString()).append("\n");
        }
        for (IRStmt stmt : stmts) {
            sb.append("\t").append(stmt.toString()).append("\n");
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
            sb.append(label).append(":\n");
        for (IRStmt stmt : stmts) {
            sb.append("\t").append(stmt.toString()).append("\n");
        }
        return sb.toString();
    }
}
