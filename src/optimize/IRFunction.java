package optimize;

import IR.IRStmts.*;

import javax.print.attribute.HashPrintJobAttributeSet;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;

public class IRFunction extends IRStmt {
    public String typeName;
    public String name;
    public BasicIRType returnType;
    public ArrayList<BasicIRType> argTypes;
    public ArrayList<String> argNames;
    public ArrayList<IRBlock> blocks;
    public HashMap<String, IRBlock> blockMap = new HashMap<>(); // string(Name) -> IRBlock

    private HashMap<String, BasicIRType> allocaVarMap = new HashMap<>();// alloca出来的函数名字和对应类型
    class StringPair {
        String block;
        String value;
        public StringPair(String block, String value) {
            this.block = block;
            this.value = value;
        }
    }
    private HashMap<String, ArrayList<StringPair>> phiMap = new HashMap<>(); // phi函数的名字和对应的块和值

    private static void debug(String msg) {
        System.out.println("IRFunc: " + msg);
    }
    private void debug(int status) {
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
                allocaVarMap.put(allocaStmt.dest, func.allocaStmts.get(i).type);
                phiMap.put(allocaStmt.dest, new ArrayList<>());
            }
        }
        // 复制每个Block
        for (int i = 0; i < func.blocks.size(); i++) {
            IRBlock newBlock = new IRBlock(func.blocks.get(i));
            if (i == 0) newBlock.label = this.name; // the entry of the function
            blocks.add(newBlock);
            blockMap.put(newBlock.label, newBlock);
        }
        // 更新每个Block的前驱和后继
        for (int i = 0; i < func.blocks.size(); i++) {
            IRBlock newBlock = blocks.get(i);
            if (newBlock.tailStmt instanceof BranchStmt branch) {
                if (branch.condition == null) {
                    // jump
                    newBlock.succ.add(blockMap.get(branch.trueLabel));
                    blockMap.get(branch.trueLabel).pred.add(newBlock);
                } else {
                    // branch
                    newBlock.succ.add(blockMap.get(branch.trueLabel));
                    blockMap.get(branch.trueLabel).pred.add(newBlock);
                    newBlock.succ.add(blockMap.get(branch.falseLabel));
                    blockMap.get(branch.falseLabel).pred.add(newBlock);
                }
            }
        }
    }

    void clearDeadBlock() {
        // 消除不可达的block
        for (IRBlock block : blocks) {
            if (!block.isHead && block.pred.isEmpty()) {
                throw new RuntimeException("IRFunction: clearDeadBlock: unreachable block");
                // todo
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
                } else if (stmt instanceof CallStmt call) {
                    if (isUsed.contains(call.dest)) newStmts.add(stmt);
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
            } else {
                // doing nothing?
            }
        }
    }

    void activityAnalysis() {
        // use and def in each block
        for (IRBlock block : blocks)
            block.activityAnalysis();
        // liveIn and liveOut among blocks

    }

    HashMap<IRBlock, String> getLastDefMap(String varName) {
        // 记录它在每个块中的最后一次 def (store)
        HashMap<IRBlock, String> lastDef = new HashMap<>();
        for (IRBlock block : blocks)
            for (IRStmt stmt : block.stmts)
                if (stmt instanceof StoreStmt store)
                    if (store.dest.equals(varName))
                        lastDef.put(block, store.val);
        return lastDef;
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
        // reference: https://www.cnblogs.com/lixingyang/p/17721341.html
        HashMap<String, HashMap<IRBlock, String>> all = new HashMap<>();
        for (String var : allocaVarMap.keySet()) {
            // 对每个alloca出来的变量单独处理
            debug("getPhiPosition: " + var);

            // 记录它在每个块中的最后一次 def (store)
            HashMap<IRBlock, String> lastDefMap = getLastDefMap(var);
            all.put(var, lastDefMap);
            // 初始化工作表
            HashSet<IRBlock> workTable = getWorkTable(var);
            // 初始化辅助集合
            HashSet<IRBlock> hasPhi = new HashSet<>();

            while (!workTable.isEmpty()) {
                IRBlock curBlock = workTable.iterator().next();
//                debug("curBlock: " + curBlock.label);
                workTable.remove(curBlock);
                if (!lastDefMap.containsKey(curBlock)) {
                    // 当前块没有对该变量的赋值，但在工作表内
                    lastDefMap.put(curBlock, null); // phiStmt as the last def
                }
                // 遍历当前块的支配边界，插入phi
                for (IRBlock frontier : curBlock.domFrontier) {
                    if (!hasPhi.contains(frontier)) {
                        // update phiMap & workTable
                        hasPhi.add(frontier);
                        workTable.add(frontier);
                    }
                    frontier.addPhi(var, allocaVarMap.get(var).toString(), curBlock.label, lastDefMap.get(curBlock));
                }
            }
        }
    }

    public void mem2reg() {
        clearDeadBlock(); // 消去不可达的block
        clearDeadStmt(); // 消去没有use的stmt

        // set index
        for (int i = 0; i < blocks.size(); i++)
            blocks.get(i).indexInFunc = i;

//        debug("begin to build dom tree");
        buildDomTree(); // 构建支配树，确定支配边界

//        debug("begin to activity analysis");
        // 遍历allocaMap中每个局部变量，获取需要插入phi的所有位置
        getPhiPosition();

//        debug("allocaMap: " + allocaVarMap);

//        debug("begin to mem2reg");

        debug(0);
        debug(2);

        // 逐个块进行mem2reg
        HashMap<String, Integer> varCounter = new HashMap<>();
        for (IRBlock block : blocks)
            block.mem2reg(varCounter);

    }

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

    @Override
    public int getSpSize() {
        //todo
        return 0;
    }
}
