package optimize;

import IR.IRStmts.*;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;

// block for optimization
// todo: simplify the block structure
public class IRBlock extends IRStmt {
    // basic info
    public String label;
    public ArrayList<IRStmt> stmts = new ArrayList<>();
    public boolean isHead = false;
    // advanced info
    public ArrayList<IRBlock> succ = null;
    public ArrayList<IRBlock> pred = null;
    // dominant nodes
    public IRBlock idomBlock = null;
    public ArrayList<IRBlock> domChildren = new ArrayList<>();
    public BitSet dom = null;
    public ArrayList<IRBlock> domFrontier = new ArrayList<>(); // 支配边界
    // block id
    public int indexInFunc = -1, topoIndex = -1;
    // all phi stmts (phi函数的名字和对应的块和值)
    public HashMap<String, PhiStmt> phiStmts = new HashMap<>(); // only used in mem2reg & addPhi

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
        for (IRStmt stmt : block.stmts) {
            if (stmt instanceof NewClassStmt newClassStmt) {
                stmts.add(newClassStmt.mallocStmt);
                if (newClassStmt.callStmt != null) stmts.add(newClassStmt.callStmt);
            } else {
                stmts.add(stmt);
            }
        }
        // predecessors and successors will be set in IRFunction
    }

    public IRBlock(String label) {
        this.label = label;
    }

    @Override
    public String getDest() { return null; }

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
