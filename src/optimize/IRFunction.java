package optimize;

import IR.IRStmts.*;
import optimize.utils.FuncHeadStmt;
import optimize.utils.LivenessAnalysis;

import java.util.*;

public class IRFunction extends IRStmt {
    public String name;
    public BasicIRType returnType;
    public ArrayList<BasicIRType> argTypes;
    public ArrayList<String> argNames;
    public ArrayList<IRBlock> blocks;
    public HashMap<String, IRBlock> blockMap = new HashMap<>(); // string(Name) -> IRBlock

    private HashMap<String, BasicIRType> allocaVarType = new HashMap<>();// alloca出来的函数名字和对应类型
    class StringPair {
        String block;
        String value;
        public StringPair(String block, String value) {
            this.block = block;
            this.value = value;
        }
    }
    private HashMap<String, ArrayList<StringPair>> phiMap = new HashMap<>(); // phi函数的名字和对应的块和值

    private void debug(String msg) {
        System.out.println("; [Func: " + name + "] " + msg);
    }
    private void debugCFG(int status) {
        boolean flag = true;
        if (flag) return;
        // 0: print idom
        if (status == 0) {
            System.out.println("---idom---");
            if (status == 0) {
                for (IRBlock block : blocks) {
                    if (block.idomBlock == null)
                        System.out.println("   " + block.label + " -> null");
                    else
                        System.out.println("   " + block.label + " -> " + block.idomBlock.label);
                }
            }
            System.out.println("-------");
        }
        if (status == 1) {
            System.out.println("---domFrontier---");
            for (IRBlock block : blocks) {
                System.out.print(block.label + ": ");
                for (IRBlock frontier : block.domFrontier) {
                    System.out.print(frontier.label + " ");
                }
                System.out.println();
            }
            System.out.println("-------");
        }
        if (status == 2) {
            System.out.println("---dom---");
            for (int i = 0; i < blocks.size(); i++) {
                System.out.print(blocks.get(i).label + ": ");
                for (int j = 0; j < blocks.size(); j++) {
                    if (blocks.get(i).dom.get(j))
                        System.out.print(blocks.get(j).label + " ");
                }
                System.out.println();
            }
            System.out.println("-------");
        }
    }

    public IRFunction(FunctionImplementStmt func) {
        this.name = func.name;
        this.returnType = func.returnType;
        this.argTypes = func.argTypes;
        this.argNames = func.argNames;
        this.blocks = new ArrayList<>();

        // 展开NewArrayStmt
        for (int i = 0; i < func.blocks.size(); i++) {
            Block block = func.blocks.get(i);
            int sz = block.stmts.size();
            for (int j = 0; j < sz; j++) {
                IRStmt stmt = block.stmts.get(j);
                if (stmt instanceof NewArrayStmt newArray) {
                    for (IRStmt newStmt : newArray.stmts) {
                        block.stmts.add(j, newStmt);
                        j++;
                    }
                    block.stmts.remove(j);
                    j--;
                    sz = block.stmts.size();
                }
            }
        }
        // split the block by LabelStmt
        ArrayList<Block> tmpBlocks = new ArrayList<>();
        Block curBlock;
        for (Block block : func.blocks) {
            curBlock = new Block(block.label);
            for (IRStmt stmt : block.stmts)
                if (stmt instanceof LabelStmt) {
                    tmpBlocks.add(curBlock);
                    curBlock = new Block(((LabelStmt) stmt).label);
                } else {
                    curBlock.stmts.add(stmt);
                }
            tmpBlocks.add(curBlock);
        }
        func.blocks = tmpBlocks;

        // -----------------------
        // 收集函数内的alloca
        if (func.allocaFlag) {
            for (int i = 0; i < func.allocaStmts.size(); i++) {
                AllocaStmt allocaStmt = func.allocaStmts.get(i);
                allocaVarType.put(allocaStmt.dest, func.allocaStmts.get(i).type);
                phiMap.put(allocaStmt.dest, new ArrayList<>());
            }
        }
        // 复制每个Block
        for (int i = 0; i < func.blocks.size(); i++) {
            if (func.blocks.get(i).stmts.isEmpty()) {
                // ATTENTION: codegen/t64.mx has function without return
                Block block = func.blocks.get(i);
                if (func.returnType.typeName.equals("void")) {
                    block.addStmt(new ReturnStmt(new BasicIRType("void"), null));
                } else {
                    block.addStmt(new ReturnStmt(new BasicIRType(func.returnType.typeName), "0"));
                }
            }
            IRBlock block = new IRBlock(func.blocks.get(i));
            if (i == 0) block.label = this.name; // the entry of the function
            blocks.add(block);
            blockMap.put(block.label, block);
        }
        // 更新每个Block的前驱和后继
        for (IRBlock block : blocks) {
            if (block.tailStmt instanceof BranchStmt branch) {
                if (branch.condition == null) {
                    // jump
                    block.succ.add(blockMap.get(branch.trueLabel));
                    blockMap.get(branch.trueLabel).pred.add(block);
                } else {
                    // branch
                    block.succ.add(blockMap.get(branch.trueLabel));
                    blockMap.get(branch.trueLabel).pred.add(block);
                    block.succ.add(blockMap.get(branch.falseLabel));
                    blockMap.get(branch.falseLabel).pred.add(block);
                }
            }
        }
    }

    void clearDeadBlock() {
        // 消除不可达的block
        for (int i = 0; i < blocks.size(); i++) {
            IRBlock block = blocks.get(i);
            if (block.isHead) continue;
            if (block.pred.isEmpty()) {
                // unreachable block
                for (IRBlock succBlock : block.succ)
                    succBlock.pred.remove(block);
                blocks.remove(block);
                i--;
            }
        }
    }

    void clearDeadStmt() {
        // 消除没有use的stmt
        HashSet<String> isUsed = new HashSet<>();
        for (IRBlock block : blocks) {
            for (IRStmt stmt : block.stmts) {
                if (stmt instanceof StoreStmt store) {
                    isUsed.add(store.dest);
                    isUsed.add(store.val);
                } else if (stmt instanceof LoadStmt load) {
                    isUsed.add(load.pointer);
                } else if (stmt instanceof CallStmt call) {
                    isUsed.addAll(call.args);
                } else if (stmt instanceof BinaryExprStmt binaryExpr) {
                    isUsed.add(binaryExpr.register1);
                    isUsed.add(binaryExpr.register2);
                } else if (stmt instanceof GetElementPtrStmt getElementPtr) {
                    isUsed.add(getElementPtr.pointer);
                    isUsed.add(getElementPtr.index);
                } else if (stmt instanceof SelectStmt select) {
                    isUsed.add(select.cond);
                    isUsed.add(select.trueVal);
                    isUsed.add(select.falseVal);
                } else if (stmt instanceof BranchStmt branch) {
                    isUsed.add(branch.condition);
                } else if (stmt instanceof ReturnStmt ret) {
                    if (ret.src != null) isUsed.add(ret.src);
                } else if (stmt instanceof UnaryExprStmt unaryExpr) {
                    isUsed.add(unaryExpr.register);
                } else
                    throw new RuntimeException("IRFunction: clearDeadStmt: unknown stmt");
            }
        }
        for (IRBlock block : blocks) {
            ArrayList<IRStmt> newStmts = new ArrayList<>();
            for (IRStmt stmt : block.stmts) {
                if (stmt instanceof LoadStmt load) {
                    if (isUsed.contains(load.dest)) newStmts.add(stmt);
                } else if (stmt instanceof BinaryExprStmt binaryExpr) {
                    if (isUsed.contains(binaryExpr.dest)) newStmts.add(stmt);
                } else if (stmt instanceof GetElementPtrStmt getElementPtr) {
                    if (isUsed.contains(getElementPtr.dest)) newStmts.add(stmt);
                } else if (stmt instanceof SelectStmt select) {
                    if (isUsed.contains(select.dest)) newStmts.add(stmt);
                } else if (stmt instanceof UnaryExprStmt unaryExpr) {
                    if (isUsed.contains(unaryExpr.dest)) newStmts.add(stmt);
                } else
                    newStmts.add(stmt);
            }
            block.stmts = newStmts;
        }
    }

    void clearJumpBlock() {
        // 消除只有jump的block
        for (int i = 0; i < blocks.size(); i++) {
            IRBlock block = blocks.get(i);
            if (block.succ.size() == 1 && block.stmts.get(0) instanceof BranchStmt branch && branch.condition == null) {
                // only jump
                IRBlock succBlock = block.succ.get(0);
                blocks.remove(block);
                succBlock.pred.addAll(block.pred);
                succBlock.pred.remove(block);

                for (IRBlock predBlock : block.pred) {
                    predBlock.succ.remove(block);
                    predBlock.succ.add(succBlock);
                    IRStmt tailStmt = predBlock.stmts.get(predBlock.stmts.size() - 1);
                    if (tailStmt instanceof BranchStmt branchStmt) {
                        if (branchStmt.condition == null) {
                            branchStmt.trueLabel = succBlock.label;
                        } else {
                            if (branchStmt.trueLabel.equals(block.label))
                                branchStmt.trueLabel = succBlock.label;
                            if (branchStmt.falseLabel.equals(block.label))
                                branchStmt.falseLabel = succBlock.label;
                            if (branchStmt.trueLabel.equals(branchStmt.falseLabel)){
                                predBlock.stmts.remove(tailStmt);
                                predBlock.stmts.add(new BranchStmt(branchStmt.trueLabel));
                            }
                        }
                    } else {
                        throw new RuntimeException("clearJumpBlock: unknown tail stmt: " + tailStmt);
                    }
                }
                i--;
            }
        }
    }

    boolean[] boolArray;
    void dfs(IRBlock curBlock) {
        boolArray[curBlock.indexInFunc] = false;
        for (IRBlock succBlock : curBlock.succ) {
            if (boolArray[succBlock.indexInFunc]) {
                dfs(succBlock);
            }
        }
    }
    void buildDomTree() {
        // 目前使用朴素算法
        int sz = blocks.size();
        for (int i = 0; i < sz; i++)
            blocks.get(i).dom = new BitSet(sz); // init: false
        IRBlock headBlock = blocks.get(0);
        boolArray = new boolean[sz];
        for (int i = 0; i < sz; i++)
            blocks.get(i).dom.set(0); // every block is dominated by the head block
        // 得到dom节点
        for (int i = 1; i < sz; i++) {
            for (int j = 0; j < sz; j++) boolArray[j] = true;
            boolArray[i] = false;
            dfs(headBlock);
            for (int j = 0; j < sz; j++)
                if (boolArray[j]) blocks.get(j).dom.set(i);
            blocks.get(i).dom.set(i);
        }
        // immediate dominator
        for (int i = 1; i < sz; i++) {
            IRBlock curBlock = blocks.get(i);
            for (int j = 0; j < sz; j++)
                if (curBlock.dom.get(j)) {
                    // tmp = (dom[j] & dom[i]) ^ dom[i];
                    BitSet tmp = (BitSet) blocks.get(j).dom.clone();
                    tmp.and(curBlock.dom);
                    tmp.xor(curBlock.dom);

                    if (tmp.cardinality() == 1 && tmp.get(i)) {
                        curBlock.idom = j;
                        curBlock.idomBlock = blocks.get(j);
                        blocks.get(j).domChildren.add(curBlock);
                        break;
                    }
                }
        }

        // get dominance frontier
        for (int i = 0; i < sz; i++) {
            IRBlock curBlock = blocks.get(i);
            if (curBlock.pred.size() > 1) {
                for (IRBlock predBlock : curBlock.pred) {
                    IRBlock runner = predBlock;
                    while (runner != null && runner != curBlock.idomBlock) {
                        runner.domFrontier.add(curBlock);  // 添加支配边界
                        runner = runner.idomBlock;
                    }
                }
            }
        }
    }

    HashSet<IRBlock> getWorkTable(String varName) {
        // 记录所有包含 store(varName) 的块
        HashSet<IRBlock> workTable = new HashSet<>();
        for (IRBlock block : blocks)
            for (IRStmt stmt : block.stmts)
                if (stmt instanceof StoreStmt store)
                    if (store.dest.equals(varName))
                        workTable.add(block);
        return workTable;
    }

    void getPhiPosition() {
        for (String var : allocaVarType.keySet()) {
            // 对每个alloca出来的变量单独处理
            // 初始化工作表
            HashSet<IRBlock> workTable = getWorkTable(var);
            // 初始化辅助集合
            HashSet<IRBlock> hasPhi = new HashSet<>();

            while (!workTable.isEmpty()) {
                IRBlock curBlock = workTable.iterator().next();
                workTable.remove(curBlock);
                // 遍历当前块的支配边界，插入phi
                for (IRBlock frontier : curBlock.domFrontier) {
                    if (!hasPhi.contains(frontier)) {
                        // update phiMap & workTable
                        hasPhi.add(frontier);
                        workTable.add(frontier);
                    }
                    frontier.addPhi(var, allocaVarType.get(var).toString());
                }
            }
        }
    }

    String getReg(HashMap<String, String> lstDef, String var) {
        if (!lstDef.containsKey(var)) return var;
        return lstDef.get(var);
    }

    void renameVarInBlock(IRBlock curBlock, HashMap<String, Stack<String>> varName, HashMap<String, Integer> newItemNum, HashMap<String, String> lstDef) {
        ArrayList<IRStmt> newStmts = new ArrayList<>();
        for (IRStmt stmt : curBlock.stmts) {
            if (stmt instanceof StoreStmt store) {
                if (!varName.containsKey(store.dest)) {
                    newStmts.add(new StoreStmt(store.type, getReg(lstDef, store.val), store.dest));
                } else {
                    // is local variable
                    varName.get(store.dest).add(getReg(lstDef, store.val));
                    newItemNum.put(store.dest, newItemNum.get(store.dest) + 1);
                }
            } else if (stmt instanceof LoadStmt load) {
                if (!varName.containsKey(load.pointer)) {
                    newStmts.add(load);
                } else {
                    lstDef.put(load.dest, varName.get(load.pointer).peek());
                }
            } else if (stmt instanceof BinaryExprStmt binaryExpr) {
                newStmts.add(new BinaryExprStmt(binaryExpr.operator,binaryExpr.type,
                        getReg(lstDef, binaryExpr.register1), getReg(lstDef, binaryExpr.register2), binaryExpr.dest));
            } else if (stmt instanceof CallStmt call) {
                ArrayList<String> newArgs = new ArrayList<>();
                for (String arg : call.args) newArgs.add(getReg(lstDef, arg));
                newStmts.add(new CallStmt(call.retType, call.funcName, call.argTypes, newArgs, call.dest));
            } else if (stmt instanceof GetElementPtrStmt getElementPtr) {
                newStmts.add(new GetElementPtrStmt(getElementPtr.type, getReg(lstDef, getElementPtr.pointer),
                        getReg(lstDef, getElementPtr.index), getElementPtr.dest, getElementPtr.hasZero));
            } else if (stmt instanceof UnaryExprStmt unaryExpr) {
                newStmts.add(new UnaryExprStmt(unaryExpr.operator, unaryExpr.type, getReg(lstDef, unaryExpr.register), unaryExpr.dest));
            } else if (stmt instanceof SelectStmt select) {
                newStmts.add(new SelectStmt(select.type, select.cond, getReg(lstDef, select.trueVal), getReg(lstDef, select.falseVal), select.dest));
            } else if (stmt instanceof BranchStmt branch) {
                newStmts.add(new BranchStmt(getReg(lstDef, branch.condition), branch.trueLabel, branch.falseLabel));
                break;
            } else if (stmt instanceof ReturnStmt ret) {
                newStmts.add(new ReturnStmt(ret.type, getReg(lstDef, ret.src)));
                break;
            } else {
                throw new RuntimeException("[rename]: unknown stmt: " + stmt);
            }
        }
        curBlock.stmts = newStmts;
    }

    void setPhiVal(IRBlock curBlock, IRBlock predBlock, HashMap<String, Stack<String>> varName) {
        for (String var : curBlock.phiStmts.keySet()) {
            PhiStmt phiStmt = curBlock.phiStmts.get(var);
            if (!varName.get(var).isEmpty()) {
                phiStmt.addVal(varName.get(var).peek(), predBlock.label);
            } else {
                phiStmt.addEmptyVal(predBlock.label);
            }
        }
    }

    void renameVar(IRBlock curBlock, HashMap<String, Stack<String>> varName, HashMap<String, String> lstDef) {
        HashMap<String, Integer> newItemNum = new HashMap<>();
        for (String var : varName.keySet())
            newItemNum.put(var, 0);

        // 先处理phi
        for (String originName: curBlock.phiStmts.keySet()) {
            PhiStmt phiStmt = curBlock.phiStmts.get(originName);
            phiStmt.setDest(phiStmt.originDest + ".phi." +curBlock.indexInFunc);
            varName.get(phiStmt.originDest).push(phiStmt.dest);
            newItemNum.put(phiStmt.originDest, newItemNum.get(phiStmt.originDest) + 1);
        }

        // 遍历block内的stmt，更新stmt中的变量名，去掉store/load
        renameVarInBlock(curBlock, varName, newItemNum, lstDef);

        // 向CFG上的后继的phi语句传入新的varName
        for (IRBlock succBlock : curBlock.succ) {
            setPhiVal(succBlock, curBlock, varName);
        }

        // 递归进入dom-tree上的子树
        for (IRBlock domChild : curBlock.domChildren) {
            renameVar(domChild, varName, lstDef);
        }

        // pop stack
        for (String var : newItemNum.keySet()) {
            for (int i = 0; i < newItemNum.get(var); i++)
                varName.get(var).pop();
        }
    }

    public void mem2reg() {
        clearDeadBlock(); // 消除不可达的block
        clearDeadStmt(); // 消去没有use的stmt
        clearJumpBlock(); // 消除只有jump的block

        // set index
        for (int i = 0; i < blocks.size(); i++)
            blocks.get(i).indexInFunc = i;

        buildDomTree(); // 构建支配树，确定支配边界

        // 遍历allocaMap中每个局部变量，获取需要插入phi的所有位置
        getPhiPosition();

        // rename var
        HashMap<String, Stack<String>> varName = new HashMap<>(); // curName for each alloca var
        for (String var : allocaVarType.keySet())
            varName.put(var, new Stack<>());
        HashMap<String, String> lstDef = new HashMap<>();
        renameVar(blocks.get(0), varName, lstDef);
    }

    // -------------------------

    void updateBlockMap() {
        blockMap.clear();
        for (IRBlock block : blocks)
            blockMap.put(block.label, block);
    }

    public void erasePhi() {
        updateBlockMap();

        // erase phi in stmt
        for (IRBlock block : blocks) {
            ArrayList<PhiStmt> phiList = new ArrayList<>();
            for (int i = 0; i < block.stmts.size(); i++){
                if (block.stmts.get(i) instanceof PhiStmt){
                    phiList.add((PhiStmt) block.stmts.get(i));
                    block.stmts.remove(i);
                    i--;
                } else break;
            }

            for (PhiStmt phiStmt : phiList) {
                for (String blockLabel : phiStmt.val.keySet()){
                    int pos = blockMap.get(blockLabel).stmts.size() - 1;
                    blockMap.get(blockLabel).stmts.add(pos, new MoveStmt(phiStmt.dest + ".tmp", phiStmt.val.get(blockLabel)));
                }
                block.stmts.add(0, new MoveStmt(phiStmt.dest, phiStmt.dest + ".tmp"));
            }
        }
    }

    // -------------------------

    boolean useEmpty(String var) {
        LivenessAnalysis util = new LivenessAnalysis();
        for (IRBlock block : blocks)
            for (IRStmt stmt : block.stmts) {
                HashSet<String> use = util.getUse(stmt);
                if (use.contains(var)) return false;
            }
        return true;
    }

    HashSet<String> collectAllVariables() {
        LivenessAnalysis util = new LivenessAnalysis();
        HashSet<String> ret = new HashSet<>();
        for (IRBlock block : blocks)
            for (IRStmt stmt : block.stmts) {
                ret.addAll(util.getUse(stmt));
                ret.addAll(util.getDef(stmt));
            }
        return ret;
    }

    public void DCE() {
        // all variables in the function
        LivenessAnalysis util = new LivenessAnalysis();
        HashSet<String> workTable = collectAllVariables();
        while (!workTable.isEmpty()) {
            String var = workTable.iterator().next();
            workTable.remove(var);
            if (useEmpty(var)) {
                // remove all def stmt of var (if no side effect)
                for (IRBlock block : blocks)
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

    // -------------------------

    public void addPhi() {
        for (IRBlock block : blocks) {
            if (block.phiStmts.isEmpty()) continue;
            for (String phi : block.phiStmts.keySet()) {
                PhiStmt phiStmt = block.phiStmts.get(phi);
                block.stmts.add(0, phiStmt);
            }
        }
    }

    // -------------------------

    public void constantPropagation() {
        //todo
    }

    // -------------------------
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("define ").append(returnType.toString()).append(" @").append(name).append("(");
        for (int i = 0; i < argTypes.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(argTypes.get(i).toString()).append(" %").append(argNames.get(i));
        }
        sb.append(") {\n");
        for (IRBlock block : blocks) {
            sb.append(block.toString());
        }
        sb.append("}\n");
        return sb.toString();
    }
}
