package optimize;

import IR.IRStmts.*;

import java.util.*;

import static java.lang.Integer.max;
import static java.lang.Integer.min;

public class RegAllocator {
    IRFunction func;

    public ArrayList<IRBlock> linearOrder;
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

    // 左闭右开 [start, end)
    public class Interval implements Comparable<Interval> {
        int start, end;
        String regName = null;
        boolean spiltFlag = false;
        int useReg = -1, useEnd = -1;
//        int inherit = -1; // joint
        Interval(int start, int end, String regName) {
            this.start = start;
            this.end = end;
            this.regName = regName;
        }

        public int compareByEnd(Interval other) {
            return Integer.compare(other.end, this.end); // end值大的优先
        }

        public int compareByStart(Interval other) {
            return Integer.compare(other.end, this.end); // 按start值升序排列
        }

        @Override
        public int compareTo(Interval other) {
            return Integer.compare(this.start, other.start); // 按start值升序排列
        }
    }

    public HashMap<String, Interval> intervals;
    public ArrayList<IRStmt> linearStmts;
    ArrayList<HashSet<String>> liveIn, liveOut;
    HashMap<String, Integer> blockHeadNumber;

    void numberStmt() {
        linearStmts = new ArrayList<>();
        blockHeadNumber = new HashMap<>();
        linearStmts.add(new FuncHeadStmt(func));
        int cnt = 1;
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

    void updateInterval(String reg, int stmtIndex) {
        if (!intervals.containsKey(reg)) {
            intervals.put(reg, new Interval(stmtIndex, stmtIndex, reg));
            return;
        }
        intervals.get(reg).start = min(intervals.get(reg).start, stmtIndex);
        intervals.get(reg).end = max(stmtIndex, intervals.get(reg).end);
    }

    void calcIntervals() {
        for (int i = 0; i < linearStmts.size(); i++) {
            IRStmt stmt = linearStmts.get(i);
            for (String reg : liveOut.get(i)) {
                updateInterval(reg, i);
            }
        }
    }

    void getLiveIntervals() {
        intervals = new HashMap<>();
        numberStmt();
        livenessAnalysis();
        calcIntervals();
    }

    HashSet<Integer> freeRegs;
    ArrayList<Interval> occupiedIntervals; // 当前占有寄存器的区间，按end值的大根堆
    ArrayList<Interval> freeIntervals; // 存入所有变量的interval
    HashMap<Interval, Integer> tempMap; // interval和对应的寄存器
    ArrayList<Interval> spiltIntervals; // 被溢出的interval

    void spill(Interval curInterval, int startIndex) {
        curInterval.spiltFlag = true;
        spiltIntervals.add(curInterval);
    }

    void trySpill(Interval curInterval) {
        Interval maxEndInterval = occupiedIntervals.get(0);
        int ind = 0;
        for (int i = 1; i < occupiedIntervals.size(); i++)
            if (occupiedIntervals.get(i).end > maxEndInterval.end){
                ind = i;
                maxEndInterval = occupiedIntervals.get(i);
            }
        if (maxEndInterval.end > curInterval.end) {
            // spill
            int reg = tempMap.get(maxEndInterval);
            occupiedIntervals.remove(ind);
            maxEndInterval.useEnd = curInterval.start;
            curInterval.useReg = reg;
            tempMap.put(curInterval, reg);
            occupiedIntervals.add(curInterval);
            spill(maxEndInterval, curInterval.start);
        } else {
            spill(curInterval, curInterval.start);
        }
    }

    void allocateRegisters() {
        int freeRegNum = 10; //todo
        freeRegs = new HashSet<>();
        for (int i = 0; i < freeRegNum; i++) freeRegs.add(i);
        occupiedIntervals = new ArrayList<>(); // 当前占有寄存器的区间，按end值的大根堆
        freeIntervals = new ArrayList<>(intervals.values()); // 存入所有变量的interval
        freeIntervals.sort(Interval::compareByStart); // 按start值升序排列
        tempMap = new HashMap<>(); // interval和对应的寄存器
        spiltIntervals = new ArrayList<>();

        // init for function arguments
        for (int i = 0; i < func.argNames.size(); i++) {
            String argName = func.argNames.get(i);
            if (!intervals.containsKey(argName))
                continue;
            if (i < 8) {
                freeRegs.remove(i);
                Interval curInterval = intervals.get(argName);
                curInterval.useReg = i; // 0~7: a0~a7
                tempMap.put(curInterval, i);
                occupiedIntervals.add(curInterval);
                freeIntervals.remove(curInterval);
            } else {
                continue;
            }
        }

        while (!freeIntervals.isEmpty()) {
            Interval curInterval = freeIntervals.get(0);
            freeIntervals.remove(0);
            // 尝试接合
            if (linearStmts.get(curInterval.start) instanceof MoveStmt moveStmt) // dest = src
                if (intervals.get(moveStmt.src).end == curInterval.start) {
                    Interval prevInterval = intervals.get(moveStmt.src);
                    if (prevInterval.useReg != -1) { // 如果src的区间已经分配了寄存器
                        curInterval.useReg = prevInterval.useReg;
                        tempMap.put(curInterval, prevInterval.useReg);
                        occupiedIntervals.add(curInterval);
                        occupiedIntervals.remove(prevInterval);
                        continue;
                    }
                }
            // 释放过期的寄存器
            for (int i = 0; i < occupiedIntervals.size(); i++)
                if (occupiedIntervals.get(i).end <= curInterval.start) {
                    freeRegs.add(tempMap.get(occupiedIntervals.get(i)));
                    occupiedIntervals.remove(i);
                    i--;
                }

            if (freeRegs.isEmpty()) {
                trySpill(curInterval);
            } else {
                // get one freeReg
                int reg = freeRegs.iterator().next();
                freeRegs.remove(reg);
                // add the binding
                curInterval.useReg = reg;
                tempMap.put(curInterval, reg);
                occupiedIntervals.add(curInterval);
            }

        }

    }

    public RegAllocator(IRFunction func_) {
        this.func = func_;
        // get linear ordering of basic blocks
        linearize();

        // analyze live intervals
        getLiveIntervals();

        // allocate registers
        allocateRegisters();
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
            ret.add(getElementPtr.index);
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
            ret.add(store.dest);
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
        if (stmt instanceof FuncHeadStmt funcHead)
            ret.addAll(funcHead.params);
        return ret;
    }

    public int getAllocaState(String regName, int index) {
        Interval interval = intervals.get(regName);
        if (index < interval.start || index > interval.end) throw new RuntimeException("getAllocaState: index out of range");
        if (interval.spiltFlag) {
            if (index < interval.useEnd) return interval.useReg;
            return -1;
        }
        return interval.useReg;
    }
    public boolean isSpilt(String regName) {
        return intervals.get(regName).spiltFlag;
    }
    public int hasReg(String regName) {
        return intervals.get(regName).useReg;
    }
    public int getSpillTime(String regName) {
        return intervals.get(regName).useEnd;
    }
}
