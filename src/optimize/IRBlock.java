package optimize;

import IR.IRStmts.*;

import java.util.ArrayList;
import java.util.BitSet;
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
    // activity analysis
    public HashSet<String> liveIn = new HashSet<>();
    public HashSet<String> liveOut = new HashSet<>();
    // dominant nodes
    public int idom = -1;
    public IRBlock idomBlock = null;
    public BitSet dom = null;
    // block id
    public int indexInFunc = -1;

    public IRBlock(Block block) {
        block.updateBlock(); // remove all stmts after branch or return

        label = block.label;

        if (block.label.isEmpty()) isHead = true; // the first block of a function
        if (block.stmts.get(block.stmts.size() - 1) instanceof ReturnStmt) isTail = true; // one of the last blocks of a function

        stmts.addAll(block.stmts);

        // predecessors and successors will be set in IRFunction

        // for debug only
        tailStmt = stmts.get(stmts.size() - 1);
        if (tailStmt instanceof ReturnStmt || tailStmt instanceof BranchStmt) return;
        throw new RuntimeException("IRBlock: unknown tail stmt: " + tailStmt);
    }

    HashSet<String> getUse(IRStmt stmt) {
        HashSet<String> ret = new HashSet<>();
        if (stmt instanceof BranchStmt branch){
            ret.add(branch.condition);
        }
        if (stmt instanceof BinaryExprStmt binaryExpr){
            ret.add(binaryExpr.register1);
            ret.add(binaryExpr.register2);
        }
        if (stmt instanceof CallStmt call){
            ret.addAll(call.args);
        }
        if (stmt instanceof GetElementPtrStmt getElementPtr){
            ret.add(getElementPtr.pointer);
            ret.addAll(getElementPtr.index); //todo modify? if has only one element
        }
        if (stmt instanceof LoadStmt load){
            ret.add(load.pointer); //todo
        }
        if (stmt instanceof ReturnStmt retStmt){
            if (retStmt.src != null) ret.add(retStmt.src);
        }
        if (stmt instanceof SelectStmt select){
            ret.add(select.cond);
            ret.add(select.trueVal);
            ret.add(select.falseVal);
        }
        if (stmt instanceof StoreStmt store){
            ret.add(store.dest); // todo
            ret.add(store.val);
        }
        if (stmt instanceof UnaryExprStmt unaryExpr){
            ret.add(unaryExpr.register);
        }
        if (stmt instanceof AllocaStmt)
            throw new RuntimeException("IRBlock: getUse: alloca stmt");
        return ret;
    }
    HashSet<String> getDef(IRStmt stmt) {
        HashSet<String> ret = new HashSet<>();
        if (stmt instanceof BinaryExprStmt binaryExpr)
            ret.add(binaryExpr.dest);
        if (stmt instanceof CallStmt call)
            ret.add(call.dest);
        if (stmt instanceof GetElementPtrStmt getElementPtr)
            ret.add(getElementPtr.dest);
        if (stmt instanceof LoadStmt load)
            ret.add(load.dest);
        if (stmt instanceof SelectStmt select)
            ret.add(select.dest);
        if (stmt instanceof UnaryExprStmt unaryExpr)
            ret.add(unaryExpr.dest);
        if (stmt instanceof AllocaStmt)
            throw new RuntimeException("IRBlock: getDef: alloca stmt");
        return ret;
    }

    HashSet<String> use = new HashSet<>();
    HashSet<String> def = new HashSet<>();
    public void activityAnalysis() {
        // use[pn] = use[p] V (use[n] - def[n])
        // def[pn] = def[p] V def[n]
        for (IRStmt stmt : stmts) {
            HashSet<String> curUse = getUse(stmt);
            HashSet<String> curDef = getDef(stmt);
            if (!curDef.isEmpty())
                curUse.remove(curDef);
            use.addAll(curUse);
            def.addAll(curDef);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (!isHead) sb.append(label).append(":\n");
        for (IRStmt stmt : stmts) {
            sb.append("\t").append(stmt.toString()).append("\n");
        }
        return sb.toString();
    }

    @Override
    public int getSpSize() {
        //todo
        return 0;
    }
}
