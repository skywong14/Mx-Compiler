package optimize;

import IR.IRStmts.*;
import optimize.utils.FuncHeadStmt;
import optimize.utils.LivenessAnalysis;

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
            if (!visited[i]) {
                func.blocks.remove(i);
                i--;
                continue;
                // throw new RuntimeException("[linearize]: block not connected: " + func.blocks.get(i).label);
            }
            linearOrder.get(i).topoIndex = i;
        }
    }

    void debug(String str) {
        System.out.println("# [RegAllocator]: " + str);
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
        int cnt = 0;
        for (IRBlock block : linearOrder) {
            blockHeadNumber.put(block.label, cnt);
            if (block.isHead){
                linearStmts.add(new FuncHeadStmt(func)); // the declaration of function, [def]
                cnt++;
            }
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
        LivenessAnalysis util = new LivenessAnalysis();
        while (changeFlag) {
            changeFlag = false;
            for (int i = linearStmts.size() - 1; i >= 0; i--) {
                IRStmt stmt = linearStmts.get(i);
                HashSet<String> newLiveOut = new HashSet<>(), newLiveIn = new HashSet<>();
                // liveOut[s] = union liveIn[succ]
                if (stmt instanceof BranchStmt branchStmt) {
                    if (branchStmt.condition != null) {
                        int succIndex = blockHeadNumber.get(branchStmt.trueLabel);
                        newLiveOut.addAll(liveIn.get(succIndex));
                        succIndex = blockHeadNumber.get(branchStmt.falseLabel);
                        newLiveOut.addAll(liveIn.get(succIndex));
                    } else {
                        int succIndex = blockHeadNumber.get(branchStmt.trueLabel);
                        newLiveOut.addAll(liveIn.get(succIndex));
                    }
                } else if (stmt instanceof ReturnStmt returnStmt) {
                    // do nothing
                } else if (i + 1 < linearStmts.size()) {
                    newLiveOut.addAll(liveIn.get(i + 1));
                }
                // liveIn[s] = use[s] + (liveOut[s] - def[s])
                newLiveIn.addAll(newLiveOut);
                newLiveIn.removeAll(util.getDef(stmt));
                newLiveIn.addAll(util.getUse(stmt));

                if (util.getUse(stmt).contains("@x") || util.getDef(stmt).contains("@x")) {
                    debug("{stmt}: " + i + " | " + linearStmts.get(i));
                    debug("     liveIn: " + newLiveIn);
                    debug("     liveOut: " + newLiveOut);
                    debug("     def: " + util.getDef(stmt));
                    debug("     use: " + util.getUse(stmt));
                }

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
            intervals.put(reg, new Interval(stmtIndex, stmtIndex + 1, reg));
            return;
        }
        intervals.get(reg).start = min(intervals.get(reg).start, stmtIndex);
        intervals.get(reg).end = max(stmtIndex, intervals.get(reg).end + 1);
    }

    void calcIntervals() {
        for (int i = 0; i < linearStmts.size(); i++) {
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

    void spill(Interval curInterval) {
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
            maxEndInterval.useReg = -1;
            tempMap.remove(maxEndInterval);
            curInterval.useReg = reg;
            tempMap.put(curInterval, reg);
            occupiedIntervals.add(curInterval);
            spill(maxEndInterval);
        } else {
            spill(curInterval);
        }
    }

    void allocateRegisters() {
        int freeRegNum = 22; // [0, 20)
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
                curInterval.useReg = i; // 0 ~ 7: a0 ~ a7
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
            /*
            todo
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
             */
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

    public boolean isSpilt(String regName) {
        return intervals.get(regName).spiltFlag;
    }
    public int hasReg(String regName) {
        return intervals.get(regName).useReg;
    }
    public int getSpillTime(String regName) {
        return intervals.get(regName).useEnd;
    }

    public void outputAllocaResult() {
        for (String regName : intervals.keySet()) {
            if (intervals.get(regName).useReg != -1) {
                if (isSpilt(regName))
                    System.out.println("# [spilt] " + regName + ": "  + intervals.get(regName).useReg + ", time:" + getSpillTime(regName));
                else
                    System.out.println("# [alloc] " + regName + ": " + intervals.get(regName).useReg);
            } else {
                System.out.println("# [stack] " + regName);
            }
        }
    }
}
