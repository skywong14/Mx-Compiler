package optimize;

import IR.IRStmts.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class RegAllocator {
    IRFunction func;

    ArrayList<IRBlock> linearOrder;
    boolean[] visited;

    void topoSort(IRBlock block) {
        if (visited[block.indexInFunc]) return;
        visited[block.indexInFunc] = true;
        for (IRBlock child : block.succ)
            topoSort(child);
        linearOrder.add(block);
    }

    void linearize() {
        linearOrder = new ArrayList<>();
        visited = new boolean[func.blocks.size()];
        topoSort(func.blocks.get(0));
        for (int i = 0; i < func.blocks.size(); i++) {
            if (!visited[i]) throw new RuntimeException("[linearize]: block not connected");
            linearOrder.get(i).topoIndex = i;
        }
    }

    public class Interval {
        int start, end;
        String regName;
        Interval(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }

    HashMap<String, Interval> intervals;
    ArrayList<IRStmt> linearStmts;
    ArrayList<HashSet<String>> liveIn, liveOut;
    HashMap<String, Integer> blockHeadNumber;

    void numberStmt() {
        linearStmts = new ArrayList<>();
        blockHeadNumber = new HashMap<>();
        int cnt = 0;
        for (IRBlock block : linearOrder) {
            blockHeadNumber.put(block.label, cnt);
            linearStmts.addAll(block.stmts);
            cnt += block.stmts.size();
        }
    }

    void livenessAnalysis() {
        liveIn = new ArrayList<>();
        liveOut = new ArrayList<>();
        // size + 1 for the boundary
        for (int i = 0; i <= linearStmts.size(); i++) {
            liveIn.add(new HashSet<>());
            liveOut.add(new HashSet<>());
        }

        boolean changeFlag = true;
        while (changeFlag) {
            changeFlag = false;
            for (int i = linearStmts.size() - 1; i >= 0; i--) {
                IRStmt stmt = linearStmts.get(i);
                HashSet<String> newLiveOut = new HashSet<>(), newLiveIn = new HashSet<>();
                // liveOut[s] = union liveIn[succ]
                if (stmt instanceof BranchStmt branchStmt && branchStmt.condition != null) {
                    int succIndex = blockHeadNumber.get(branchStmt.trueLabel);
                    newLiveOut.addAll(liveIn.get(succIndex));
                    succIndex = blockHeadNumber.get(branchStmt.falseLabel);
                    newLiveOut.addAll(liveIn.get(succIndex));
                } else if (i + 1 < linearStmts.size()) {
                    newLiveOut.addAll(liveIn.get(i + 1));
                }
                // liveIn[s] = use[s] + (liveOut[s] - def[s])
                newLiveIn.addAll(newLiveOut);
                newLiveIn.removeAll(getDef(stmt));
                newLiveIn.addAll(getUse(stmt));

                // check if changed
                if (!newLiveIn.equals(liveIn.get(i)) || !newLiveOut.equals(liveOut.get(i))) {
                    changeFlag = true;
                    liveIn.set(i, newLiveIn);
                    liveOut.set(i, newLiveOut);
                }
            }
        }
    }

    void calcIntervals() {

    }

    void getLiveIntervals() {
        intervals = new HashMap<>();
        numberStmt();
        livenessAnalysis();
        calcIntervals();
    }

    public RegAllocator(IRFunction func_) {
        this.func = func_;
        // get linear ordering of basic blocks
        linearize();

        // analyze live intervals
        getLiveIntervals();

        // allocate registers
    }

    HashSet<String> getUse(IRStmt stmt) {
        HashSet<String> ret = new HashSet<>();
        if (stmt instanceof BranchStmt branch) {
            ret.add(branch.condition);
        }
        if (stmt instanceof BinaryExprStmt binaryExpr) {
            ret.add(binaryExpr.register1);
            ret.add(binaryExpr.register2);
        }
        if (stmt instanceof CallStmt call) {
            ret.addAll(call.args);
        }
        if (stmt instanceof GetElementPtrStmt getElementPtr) {
            ret.add(getElementPtr.pointer);
            ret.add(getElementPtr.index); //todo modify? if has only one element
        }
        if (stmt instanceof LoadStmt load) {
            ret.add(load.pointer);
        }
        if (stmt instanceof ReturnStmt retStmt) {
            if (retStmt.src != null) ret.add(retStmt.src);
        }
        if (stmt instanceof SelectStmt select) {
            ret.add(select.cond);
            ret.add(select.trueVal);
            ret.add(select.falseVal);
        }
        if (stmt instanceof StoreStmt store) {
            ret.add(store.dest); // todo
            ret.add(store.val);
        }
        if (stmt instanceof UnaryExprStmt unaryExpr) {
            ret.add(unaryExpr.register);
        }
        if (stmt instanceof MoveStmt move) {
            ret.add(move.src);
        }
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
        if (stmt instanceof MoveStmt move)
            ret.add(move.dest);
        return ret;
    }
}
