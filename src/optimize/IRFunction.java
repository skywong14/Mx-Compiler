package optimize;

import IR.IRStmts.*;

import javax.print.attribute.HashPrintJobAttributeSet;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;

public class IRFunction extends IRStmt {
    public String typeName, funcName;
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

    public IRFunction(FunctionImplementStmt func) {
        this.name = func.name;
        this.returnType = func.returnType;
        this.argTypes = func.argTypes;
        this.argNames = func.argNames;
        this.blocks = new ArrayList<>();

        // 收集函数内的alloca
        if (func.allocaFlag) {
            for (int i = 0; i < func.allocaStmts.size(); i++) {
                AllocaStmt allocaStmt = func.allocaStmts.get(i);
                allocaVarMap.put(allocaStmt.dest, func.allocaStmts.get(i).type);
                phiMap.put(allocaStmt.dest, new ArrayList<>());
            }
        }
        // 拷贝每个Block
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
        // todo: 数据流迭代
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
            int[] idom = new int[sz]; // for debug only
            IRBlock curBlock = blocks.get(i);
            for (int j = 0; j < sz; j++)
                if (curBlock.dom.get(j)) {
                    // tmp = (dom[v] & dom[u]) ^ dom[u];
                    BitSet tmp = (BitSet) curBlock.dom.clone();
                    tmp.and(blocks.get(j).dom);
                    tmp.xor(blocks.get(j).dom);

                    if (tmp.cardinality() == 1 && tmp.get(i)) {
                        idom[i] = j;
                        curBlock.idom = j;
                        curBlock.idomBlock = blocks.get(j);
                        break;
                    }
                }
        }

    }

    void activityAnalysis() {
        clearDeadBlock(); // 消去不可达的block

        // set index
        for (int i = 0; i < blocks.size(); i++)
            blocks.get(i).indexInFunc = i;

        buildDomTree(); // 构建支配树

        // 活跃分析

        // use and def in each block
        for (IRBlock block : blocks)
            block.activityAnalysis();
        // liveIn and liveOut among blocks

    }

    public void mem2reg() {
        activityAnalysis();
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
