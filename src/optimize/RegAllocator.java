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
    HashSet<String> visited;

    void topoSort(IRBlock block) {
        if (visited.contains(block.label)) return;
        visited.add(block.label);
        for (IRBlock child : block.succ)
            topoSort(child);
        linearOrder.add(block);
    }

    void linearize() {
        linearOrder = new ArrayList<>();
        visited = new HashSet<>();
        topoSort(func.blocks.get(0));
        for (int i = 0; i < func.blocks.size(); i++) {
            if (!visited.contains(func.blocks.get(i).label)) {
                func.blocks.remove(i);
                i--;
                continue;
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
        Interval(int start, int end, String regName) {
            this.start = start;
            this.end = end;
            this.regName = regName;
        }

        public int compareByStart(Interval other) {
            return Integer.compare(this.start, other.start); // 按start值升序排列
        }

        @Override
        public int compareTo(Interval other) {
            return Integer.compare(this.start, other.start); // 按start值升序排列
        }

        @Override
        public String toString() {
            return regName + ": [" + start + ", " + end + ")";
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

    void livenessAnalysisPlus() {
        liveIn = new ArrayList<>();
        liveOut = new ArrayList<>();
        // size + 1 for the boundary
        for (int i = 0; i <= linearStmts.size(); i++) {
            liveIn.add(new HashSet<>());
            liveOut.add(new HashSet<>());
        }
        LivenessAnalysis util = new LivenessAnalysis();
        ArrayList<HashSet<String>> blockUse = new ArrayList<>(), blockDef = new ArrayList<>();
        for (int i = 0; i < linearOrder.size(); i++) {
            blockUse.add(new HashSet<>());
            blockDef.add(new HashSet<>());
        }
        // analyze [use] and [def] inside a block
        for (int i = 0; i < linearOrder.size(); i++) {
            IRBlock block = linearOrder.get(i);
            HashSet<String> use = new HashSet<>(), def = new HashSet<>();
            HashSet<String> curUse, curDef;
            for (IRStmt stmt : block.stmts) {
                curUse = util.getUse(stmt);
                curDef = util.getDef(stmt);
                def.addAll(curDef);
                for (String reg : curUse)
                    if (!def.contains(reg)) use.add(reg);
            }
            blockUse.set(i, use);
            blockDef.set(i, def);
        }
        // worklist algorithm
        ArrayList<HashSet<String>> blockLiveIn = new ArrayList<>(), blockLiveOut = new ArrayList<>();
        for (int i = 0; i < linearOrder.size(); i++) {
            blockLiveIn.add(new HashSet<>());
            blockLiveOut.add(new HashSet<>());
            blockLiveIn.get(i).addAll(blockUse.get(i));
        }

        ArrayList<ArrayList<String>> newLiveIn = new ArrayList<>(), newLiveOut = new ArrayList<>();
        ArrayList<ArrayList<String>> lstLiveIn = new ArrayList<>();
        for (int i = 0; i < linearOrder.size(); i++) {
            newLiveIn.add(new ArrayList<>());
            newLiveIn.get(i).addAll(blockLiveIn.get(i));
            lstLiveIn.add(newLiveIn.get(i));
            newLiveOut.add(new ArrayList<>());
        }
//        System.out.println("# [livenessAnalysisPlus]: start");
        boolean changeFlag = true;
        while (changeFlag) {
            changeFlag = false;
            for (int i = linearOrder.size() - 1; i >= 0; i--) {
                IRBlock block = linearOrder.get(i);
                HashSet<String> curLiveOut = blockLiveOut.get(i);
                // liveOut[s] = union liveIn[succ]
                for (IRBlock succ : block.succ) {
                    int succIndex = succ.topoIndex;
                    if (succIndex == -1) {
                        // debug
                        continue;
                    }
                    for (String reg : lstLiveIn.get(succIndex)) {
                        if (curLiveOut.contains(reg)) continue;
                        changeFlag = true;
                        curLiveOut.add(reg);
                        newLiveOut.get(i).add(reg);
                    }
                }
                // liveIn[s] = use[s] + (liveOut[s] - def[s])
                HashSet<String> curLiveIn = blockLiveIn.get(i);
                for (String reg : newLiveOut.get(i)) {
                    if (blockDef.get(i).contains(reg)) continue;
                    if (curLiveIn.contains(reg)) continue;
                    changeFlag = true;
                    curLiveIn.add(reg);
                    newLiveIn.get(i).add(reg);
                }
            }

            for (int i = 0; i < linearOrder.size(); i++) {
                lstLiveIn.set(i, newLiveIn.get(i));
            }
            newLiveIn = new ArrayList<>();
            newLiveOut = new ArrayList<>();
            for (int i = 0; i < linearOrder.size(); i++) {
                newLiveIn.add(new ArrayList<>());
                newLiveOut.add(new ArrayList<>());
            }
        }
        // update liveIn and liveOut in linearStmts
        for (int i = 0; i < linearOrder.size(); i++) {
            IRBlock block = linearOrder.get(i);
            HashSet<String> curLiveOut = blockLiveOut.get(i);
            ArrayList<IRStmt> stmts = block.stmts;
            if (block.isHead) {
                stmts = new ArrayList<>(block.stmts);
                stmts.add(0, new FuncHeadStmt(func)); // add the function head for [def]
            }

            int cnt = blockHeadNumber.get(block.label);
            liveOut.get(cnt + stmts.size() - 1).addAll(curLiveOut);

            // liveOut[s] = union liveIn[succ]
            // liveIn[s] = use[s] + (liveOut[s] - def[s])
            for (int j = stmts.size() - 1; j >= 0; j--) {
                IRStmt stmt = stmts.get(j);
                HashSet<String> curLiveIn = liveIn.get(cnt + j), curLiveOut2 = liveOut.get(cnt + j);
                if (j != stmts.size() - 1) curLiveOut2.addAll(liveIn.get(cnt + j + 1));
                curLiveIn.addAll(util.getUse(stmt));
                for (String reg : curLiveOut2) {
                    if (util.getDef(stmt).contains(reg)) continue;
                    curLiveIn.add(reg);
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
        intervals.get(reg).end = max(stmtIndex + 1, intervals.get(reg).end);
    }

    void calcIntervals() {
        for (int i = 0; i < linearStmts.size(); i++) {
            for (String reg : liveOut.get(i)) {
                updateInterval(reg, i);
            }
        }
    }

    void getCallLiveOut() {
        for (int i = 0; i < linearStmts.size(); i++) {
            if (linearStmts.get(i) instanceof CallStmt callStmt) {
                callStmt.liveIn.addAll(liveIn.get(i));
                callStmt.liveOut.addAll(liveOut.get(i));
                for (String reg: liveIn.get(i)) {
                    // add to liveInPhyReg if it is a physical register
                    if (intervals.containsKey(reg) && intervals.get(reg).useReg != -1) {
                        callStmt.liveInPhyReg.add(intervals.get(reg).useReg);
                    }
                }
                for (String reg: liveOut.get(i)) {
                    // add to liveOutPhyReg if it is a physical register
                    if (intervals.containsKey(reg) && intervals.get(reg).useReg != -1) {
                        callStmt.liveOutPhyReg.add(intervals.get(reg).useReg);
                    }
                }
            }
        }
    }

    void getLiveIntervals() {
        intervals = new HashMap<>();
        numberStmt();
        livenessAnalysisPlus();
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
        int freeRegNum = 22; // [0, 22)
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
            }
        }

        while (!freeIntervals.isEmpty()) {
            Interval curInterval = freeIntervals.get(0);
            freeIntervals.remove(0);
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

        // live out Regs at call stmt
        getCallLiveOut();
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
