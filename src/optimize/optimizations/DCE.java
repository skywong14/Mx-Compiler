package optimize.optimizations;

import IR.IRStmts.CallStmt;
import IR.IRStmts.IRStmt;
import optimize.IRBlock;
import optimize.IRCode;
import optimize.IRFunction;
import optimize.utils.FuncHeadStmt;
import optimize.utils.LivenessAnalysis;

import java.util.HashSet;

public class DCE {

    boolean useEmpty(IRFunction func, String var) {
        LivenessAnalysis util = new LivenessAnalysis();
        for (IRBlock block : func.blocks)
            for (IRStmt stmt : block.stmts) {
                HashSet<String> use = util.getUse(stmt);
                if (use.contains(var)) return false;
            }
        return true;
    }

    HashSet<String> collectAllVariables(IRFunction func) {
        LivenessAnalysis util = new LivenessAnalysis();
        HashSet<String> ret = new HashSet<>();
        for (IRBlock block : func.blocks)
            for (IRStmt stmt : block.stmts) {
                ret.addAll(util.getUse(stmt));
                ret.addAll(util.getDef(stmt));
            }
        return ret;
    }

    void DCE(IRFunction func) {
        // all variables in the function
        LivenessAnalysis util = new LivenessAnalysis();
        HashSet<String> workTable = collectAllVariables(func);
        while (!workTable.isEmpty()) {
            String var = workTable.iterator().next();
            workTable.remove(var);
            if (useEmpty(func, var)) {
                // remove all def stmt of var (if no side effect)
                for (IRBlock block : func.blocks)
                    for (int i = 0; i < block.stmts.size(); i++) {
                        IRStmt stmt = block.stmts.get(i);
                        if (util.getDef(stmt).contains(var) && !(stmt instanceof CallStmt) && !(stmt instanceof FuncHeadStmt)) {
                            block.stmts.remove(i);
                            i--;
                            workTable.addAll(util.getUse(stmt));
                        }
                    }
            }
        }
    }

    public void aggressiveDCE() {
        //todo
    }


    public void optimize(IRCode irCode) {
        for (IRFunction func : irCode.funcStmts) {
            DCE(func);
        }
    }
}
