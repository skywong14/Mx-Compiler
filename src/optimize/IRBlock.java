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
    // activity analysis
    public HashSet<String> liveIn = new HashSet<>();
    public HashSet<String> liveOut = new HashSet<>();
    // dominant nodes
    public int idom = -1;
    public IRBlock idomBlock = null;
    public ArrayList<IRBlock> domChildren = new ArrayList<>();
    public BitSet dom = null;
    public ArrayList<IRBlock> domFrontier = new ArrayList<>(); // 支配边界
    // block id
    public int indexInFunc = -1;
    // all phi stmts (phi函数的名字和对应的块和值)
    public HashMap<String, PhiStmt> phiStmts = new HashMap<>();
    // lstDef
    HashMap<String, String> lstDef = new HashMap<>();

    private void debug(String msg) {
//        System.out.println(label + ": " + msg);
    }

    String getInPred(String var) {
        IRBlock predBlock = idomBlock;
        while (predBlock != null) {
            // debug("getInPred: " + predBlock.label);
            if (predBlock.lstDef.containsKey(var)) {
                return predBlock.lstDef.get(var);
            }
            predBlock = predBlock.idomBlock;
        }
        throw new RuntimeException("IRBlock: getInPred: no pred block contains var: " + var);
    }

    String getReg(String reg) {
        if (lstDef.containsKey(reg)) reg = lstDef.get(reg);
        return reg;
    }
    public void mem2reg(HashMap<String, Integer> varCounter, HashMap<String, BasicIRType> allocaVarMap) {
//        lstDef = new HashMap<>();
        // 收集phi中的def，替换phi中的use
        for (String phi : phiStmts.keySet()) {
            PhiStmt phiStmt = phiStmts.get(phi);
            if (!varCounter.containsKey(phiStmt.originDest))
                varCounter.put(phiStmt.originDest, 0);
            int index = varCounter.get(phiStmt.originDest);
            phiStmt.dest = phiStmt.originDest + "." + index;
            lstDef.put(phiStmt.originDest, phiStmt.dest);
            varCounter.put(phiStmt.originDest, index + 1);
        }
        // 遍历block内的stmt，复制
        ArrayList<IRStmt> newStmts = new ArrayList<>();
        for (IRStmt stmt : stmts) {
            // debug(stmt.toString());
            if (stmt instanceof StoreStmt store) {
                if (!allocaVarMap.containsKey(store.dest)) {
                    newStmts.add(new StoreStmt(store.type, getReg(store.val), store.dest));
                } else {
                    lstDef.put(store.dest, store.val);
                }
            } else if (stmt instanceof LoadStmt load) {
                if (!allocaVarMap.containsKey(load.pointer)) {
                    newStmts.add(load);
                } else {
                    if (lstDef.containsKey(load.pointer))
                        lstDef.put(load.dest, lstDef.get(load.pointer));
                    else
                        lstDef.put(load.dest, getInPred(load.pointer));
                }
            } else if (stmt instanceof BinaryExprStmt binaryExpr) {
                newStmts.add(new BinaryExprStmt(binaryExpr.operator,binaryExpr.type,
                        getReg(binaryExpr.register1), getReg(binaryExpr.register2), binaryExpr.dest));
            } else if (stmt instanceof CallStmt call) {
                ArrayList<String> newArgs = new ArrayList<>();
                for (String arg : call.args) newArgs.add(getReg(arg));
                newStmts.add(new CallStmt(call.retType, call.funcName, call.argTypes, newArgs, call.dest));
            } else if (stmt instanceof GetElementPtrStmt getElementPtr) {
                newStmts.add(new GetElementPtrStmt(getElementPtr.type, getReg(getElementPtr.pointer),
                        getReg(getElementPtr.index), getElementPtr.dest, getElementPtr.hasZero));
            } else if (stmt instanceof UnaryExprStmt unaryExpr) {
                newStmts.add(new UnaryExprStmt(unaryExpr.operator, unaryExpr.type, getReg(unaryExpr.register), unaryExpr.dest));
            } else if (stmt instanceof SelectStmt select) {
                newStmts.add(new SelectStmt(select.type, select.cond, getReg(select.trueVal), getReg(select.falseVal), select.dest));
            } else if (stmt instanceof BranchStmt branch) {
                newStmts.add(new BranchStmt(getReg(branch.condition), branch.trueLabel, branch.falseLabel));
                break;
            } else if (stmt instanceof ReturnStmt ret) {
                newStmts.add(new ReturnStmt(ret.type, getReg(ret.src)));
                break;
            } else {
                throw new RuntimeException("IRBlock: mem2reg: unknown stmt: " + stmt);
            }
        }
        stmts = newStmts;
        // 处理succ
        if (succ.size() == 1) {
            return;
        }

        for (IRBlock succBlock : succ) {
            for (String phi : succBlock.phiStmts.keySet()) {
                if (lstDef.containsKey(phi)) {
                    succBlock.phiStmts.get(phi).addVal(lstDef.get(phi), label);
                }
            }
        }

    }

    public void addPhi(String dest, String type, String block, String value) {
        if (!phiStmts.containsKey(dest)) {
            phiStmts.put(dest, new PhiStmt(dest, type));
        }
        phiStmts.get(dest).addVal(value, block);
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
            } else if (stmt instanceof NewArrayStmt newArrayStmt) {

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
            ret.add(getElementPtr.index); //todo modify? if has only one element
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
        for (String phi : phiStmts.keySet()) {
            sb.append("\t").append(phiStmts.get(phi).toString()).append("\n");
        }
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
