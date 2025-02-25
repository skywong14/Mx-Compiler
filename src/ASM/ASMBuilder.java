package ASM;

import ASM.inst.*;
import ASM.operand.PhysicalReg;
import ASM.section.ASMBlock;
import ASM.section.DataSection;
import ASM.section.RodataSection;
import ASM.section.TextSection;
import IR.IRStmts.*;
import optimize.*;
import optimize.utils.DependencyAnalysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class ASMBuilder {
    public PhysicalReg physicalReg = new PhysicalReg();
    public TextSection textSection = new TextSection();
    public DataSection dataSection = new DataSection();
    public RodataSection rodataSection = new RodataSection();

    public IRCode irCode;

    RegAllocator regAllocator = null;

    boolean[] usedReg;

    static boolean outOfBound(int imm) {
        return imm < -2048 || imm > 2047;
    }

    void debug(String msg) {
        System.out.println("# ASM: " + msg);
    }

    public ArrayList<ASMInst> Lw(String rd, int offset, String rs) {
        ArrayList<ASMInst> insts = new ArrayList<>();
        if (outOfBound(offset)) {
            insts.add(new LiInst("t5", offset));
            insts.add(new ArithInst("+", "t5", rs, "t5"));
            insts.add(new LwInst(rd, 0, "t5"));
        } else {
            insts.add(new LwInst(rd, offset, rs));
        }
        return insts;
    }

    public ArrayList<ASMInst> Sw(String src, int offset, String rd) {
        ArrayList<ASMInst> insts = new ArrayList<>();
        if (outOfBound(offset)) {
            insts.add(new LiInst("t5", offset));
            insts.add(new ArithInst("+", "t5", rd, "t5"));
            insts.add(new SwInst(src, 0, "t5"));
        } else {
            insts.add(new SwInst(src, offset, rd));
        }
        return insts;
    }

    public ArrayList<ASMInst> ArithImm(String op, String rd, String rs, int imm) {
        ArrayList<ASMInst> insts = new ArrayList<>();
        if (outOfBound(imm)) {
            insts.add(new LiInst("t5", imm));
            insts.add(new ArithInst(op, rd, rs, "t5"));
            return insts;
        } else {
            insts.add(new ArithImmInst(op, rd, rs, imm));
            return insts;
        }
    }

    public ASMBuilder(IRCode irCode_) {
        irCode = irCode_;
    }

    static class AllocaState{
        int state = 0; // 0: has physical register, 1: in stack
        int offset = -1; // if -1, not in stack
        int physicRegId = -1; // if -1, not in physical register
        public AllocaState(int state, int offset, int physicRegId) {
            this.state = state;
            this.offset = offset;
            this.physicRegId = physicRegId;
        }
    }
    HashMap<String, AllocaState> allocaStateMap;

    int calcOffset(IRFunction func) {
        int spOffset = 92; // 92 bytes for ra, a0 ~ a7, s0 ~ s11 ,tp, gp
        for (String regName : regAllocator.intervals.keySet()) {
            if (regAllocator.hasReg(regName) != -1 || regAllocator.isSpilt(regName))
                spOffset += 4;
        }
        int maxSpiltArgs = 0;
        for (IRBlock block : func.blocks)
            for (IRStmt irStmt : block.stmts)
                if (irStmt instanceof CallStmt callStmt) {
                    maxSpiltArgs = Math.max(maxSpiltArgs, callStmt.args.size() - 8);
                }
        spOffset += maxSpiltArgs * 4;

        spOffset = ((spOffset - 1) / 16 + 1 ) * 16; // 16 字节对齐
        return spOffset;
    }

    void colloectUsedReg() {
        usedReg = new boolean[32];
        for (String regName : regAllocator.intervals.keySet()) {
            if (regAllocator.hasReg(regName) != -1) {
                usedReg[regAllocator.hasReg(regName)] = true;
            }
        }
    }

    void outputAllocaResult() {
        for (String regName : allocaStateMap.keySet()) {
            AllocaState state = allocaStateMap.get(regName);
            if (state.state == 0)
                System.out.println("# [alloc] " + regName + ": " + physicalReg.getReg(state.physicRegId).name);
            else
                System.out.println("# [stack] " + regName + ": " + state.offset);
        }
    }

    void buildFunction(IRFunction func, int funcCnt) {
//        debug("[function begin] " + func.name);

        regAllocator = new RegAllocator(func); // allocate virtual registers to physical registers

//        debug("[allocate finish] " + func.name);

        ASMFunc asmFunc = new ASMFunc(func.name, funcCnt);

        // top -> bottom: ra, a0 ~ a7, s0 ~ s11, gp, tp, spilt registers, spilt arguments(bottom to top)
        // ra : [top - 4, top)
        // a7-> a0 : [top - 36 , top - 4)
        // tp, gp, s11-> s0 : [top - 92, top - 36)
        // spilt registers: num1 [top - 92 - num1 * 4, top - 92)
        // spilt arguments: num2 [bottom, bottom + num2 * 4)
        // altogether: 100 + num1 * 4 + num2 * 4 (align to 16)

        // calc maxOffset
        asmFunc.spOffset = calcOffset(func);

        // 预处理寄存器:
        // 1.被溢出: 相对于sp的偏移量 2.分配到物理寄存器：寄存器编号
        allocaStateMap = new HashMap<>();
        int tmpOffset = asmFunc.spOffset - 92 - 4;
        for (String regName : regAllocator.intervals.keySet()) {
            if (regAllocator.hasReg(regName) != -1) {
                if (regAllocator.isSpilt(regName)) {
                    allocaStateMap.put(regName, new AllocaState(2, tmpOffset, regAllocator.hasReg(regName)));
                    tmpOffset -= 4;
                } else {
                    allocaStateMap.put(regName, new AllocaState(0, -1, regAllocator.hasReg(regName)));
                }
            } else {
                allocaStateMap.put(regName, new AllocaState(1, tmpOffset, -1));
                tmpOffset -= 4;
            }
        }

//        outputAllocaResult();

        // init usedReg
        colloectUsedReg();

        // asmFunc.prologue: move sp, store ra, s0 ~ s11, arguments
        ArrayList<ASMInst> prologue = new ArrayList<>();
        prologue.addAll(ArithImm("+", "sp", "sp", -asmFunc.spOffset)); // move sp
        prologue.addAll(Sw("ra", asmFunc.spOffset - 4, "sp")); // store ra
        // store s0 ~ s11, tp, gp
        for (int i = 0; i < 14; i++)
            if (usedReg[i + 8]){
                prologue.addAll(Sw(physicalReg.getName(i + 8), asmFunc.spOffset - 40 - i * 4, "sp"));
            }
        asmFunc.setPrologue(prologue);

        // store arguments
        int argSize = Math.min(func.argNames.size(), 8);
        ArrayList<String> from = new ArrayList<>();
        ArrayList<String> to = new ArrayList<>();
        for (int i = 0; i < argSize; i++)
            if (allocaStateMap.containsKey("%" + func.argNames.get(i))) {
                from.add("a" + i);
                to.add("%" + func.argNames.get(i));
            }
        physical2Virtual(from, to, asmFunc, 0);

        for (int i = 8; i < func.argNames.size(); i++) {
            AllocaState state = allocaStateMap.get("%" + func.argNames.get(i));
            if (state == null) continue;
            int argumentOffset = asmFunc.spOffset + (i - 8) * 4;
            if (state.state == 0) {
                String regName = physicalReg.getReg(state.physicRegId).name;
                asmFunc.addInst(Lw(regName, argumentOffset, "sp"));
            } else {
                asmFunc.addInst(Lw("t0", argumentOffset, "sp"));
                asmFunc.addInst(Sw("t0", state.offset, "sp"));
            }
        }


        asmFunc.addInst(new CommentInst(""));


        // asmFunc.epilogue: restore ra, sp, s0 ~ s11, tp, gp, return
        ArrayList<ASMInst> epilogue = new ArrayList<>();
        epilogue.addAll(Lw("ra", asmFunc.spOffset - 4, "sp")); // restore ra
        // restore s0 ~ s11, tp, gp
        for (int i = 0; i < 14; i++)
            if (usedReg[i + 8]) {
                epilogue.addAll(Lw(physicalReg.getName(i + 8), asmFunc.spOffset - 40 - i * 4, "sp"));
            }
        asmFunc.setEpilogue(epilogue);

        // func body
        int stmtCnt = -1;
        for (IRBlock irBlock : regAllocator.linearOrder) {
            if (irBlock.label.equals(asmFunc.name))
                asmFunc.curBlock = asmFunc.blocks.get(0);
            else
                asmFunc.newBlock(asmFunc.blockHead + irBlock.label);
            for (int i = 0; i < irBlock.stmts.size(); i++) {
                stmtCnt++;
                IRStmt irStmt = irBlock.stmts.get(i);
                if (irStmt instanceof CallStmt callStmt && isTailRecursion(callStmt, irBlock.stmts.get(i + 1), asmFunc)) {
                    // tail recursion
                    visitTailRecursion(callStmt, asmFunc, stmtCnt, irBlock);
                    break;
                }
                visitIRStmt(irStmt, asmFunc, stmtCnt);
            }
        }

        // add to textSection
        if (!asmFunc.needPrologueBlock)
            asmFunc.blocks.get(0).insts.addAll(0, prologue);
        else {
            // add a new block for prologue and tailRecursion
            ASMBlock headBlock = asmFunc.blocks.get(0);
            ASMBlock prologueBlock = new ASMBlock(headBlock.label);
            prologueBlock.addInst(prologue); prologueBlock.isGlobal = true; prologueBlock.alignSize = 2;
            headBlock.isGlobal = false; headBlock.alignSize = 0;
            prologueBlock.addInst(new JInst(asmFunc.blockHead + asmFunc.name + ".prologue"));
            headBlock.label = asmFunc.blockHead + asmFunc.name + ".prologue";
            asmFunc.blocks.add(0, prologueBlock);
        }

        epilogue.addAll(ArithImm("+", "sp", "sp", asmFunc.spOffset)); // restore sp
        asmFunc.epilogue.add(new RetInst()); // return

        textSection.addFunction(asmFunc);
    }

    public void build() {
        buildStringConst();

        buildGlobalVariables();

        // Functions, Methods
        int funcCnt = 0;
        for (IRFunction func : irCode.funcStmts){
            buildFunction(func, funcCnt);
            funcCnt++;
        }
    }

    void buildStringConst() {
        for (StringDeclareStmt irStmt: irCode.stringDeclarations) {
            String asmString = StringDeclareStmt.convert2riscv(irStmt.printValue);
            String label = irStmt.dest.substring(1); // remove @
            rodataSection.addStringConst(label, asmString);
        }
    }

    void buildGlobalVariables() {
        for (GlobalVariableDeclareStmt irStmt: irCode.globalVariables)
            dataSection.addGlobalVariable(irStmt.name, "0");
    }

    void visitIRStmt(IRStmt irStmt, ASMFunc func, int stmtCnt) {
        if (irStmt instanceof BinaryExprStmt) {
            visitBinaryExprStmt((BinaryExprStmt) irStmt, func, stmtCnt);
        } else if (irStmt instanceof UnaryExprStmt) {
            visitUnaryExprStmt((UnaryExprStmt) irStmt, func, stmtCnt);
        } else if (irStmt instanceof BranchStmt) {
            visitBranchStmt((BranchStmt) irStmt, func, stmtCnt);
        } else if (irStmt instanceof CallStmt) {
            visitCallStmt((CallStmt) irStmt, func, stmtCnt);
        } else if (irStmt instanceof SelectStmt) {
            visitSelectStmt((SelectStmt) irStmt, func, stmtCnt);
        } else if (irStmt instanceof LoadStmt) {
            visitLoadStmt((LoadStmt) irStmt, func, stmtCnt);
        } else if (irStmt instanceof StoreStmt) {
            visitStoreStmt((StoreStmt) irStmt, func, stmtCnt);
        } else if (irStmt instanceof ReturnStmt) {
            visitReturnStmt((ReturnStmt) irStmt, func, stmtCnt);
        } else if (irStmt instanceof GetElementPtrStmt) {
            visitGetElementPtrStmt((GetElementPtrStmt) irStmt, func, stmtCnt);
        } else if (irStmt instanceof MoveStmt) {
            visitMoveStmt((MoveStmt) irStmt, func, stmtCnt);
        } else {
            throw new RuntimeException("unknown IRStmt: " + irStmt);
        }
    }

    void visitMoveStmt(MoveStmt irStmt, ASMFunc func, int stmtCnt) {
        AllocaState destState = allocaStateMap.get(irStmt.dest);
        if (destState == null)
            return;
        if (destState.state == 0) {
            // has physical register
            if (!isRegister(irStmt.src)) {
                int val = resolveValue(irStmt.src);
                String dest = physicalReg.getReg(destState.physicRegId).name;
                func.addInst(new LiInst(dest, val));
            } else {
                String src = resolveRegister(irStmt.src, func, stmtCnt, "t6");
                String dest = physicalReg.getReg(destState.physicRegId).name;
                if (!src.equals(dest)) {
                    func.addInst(new MvInst(dest, src));
                }
            }
        } else {
            int offset = destState.offset;
            if (!isRegister(irStmt.src)) {
                int val = resolveValue(irStmt.src);
                func.addInst(new LiInst("t6", val));
                func.addInst(Sw("t6", offset, "sp"));
            } else {
                String src = resolveRegister(irStmt.src, func, stmtCnt, "t6");
                func.addInst(Sw(src, offset, "sp"));
            }
        }

    }

    void visitGetElementPtrStmt(GetElementPtrStmt irStmt, ASMFunc func, int stmtCnt) {
        // slli t1, t1, 2      # 计算偏移量：t1 << 2
        // add t2, t0, t1      # 计算结果地址：t0 + 偏移量
        String ptr = "t0";
        if (!isRegister(irStmt.pointer)) {
            // [ATTENTION] something may be wrong [Undefined Behavior]
            // throw new RuntimeException("GetElementPtrStmt: pointer is not register");
        } else {
            ptr = resolveRegister(irStmt.pointer, func, stmtCnt, "t0");
        }

        String dest = getDestReg(irStmt.dest, stmtCnt, "t2");

        if (!isRegister(irStmt.index)) {
            int indexVal = resolveValue(irStmt.index);
            indexVal *= 4;
            func.addInst(ArithImm("+", dest, ptr, indexVal));

            writeBackReg(dest, irStmt.dest, func, "t2");
            return;
        }

        String index = resolveRegister(irStmt.index, func, stmtCnt, "t1");

        func.addInst(ArithImm("<<", "t1", index, 2));
        func.addInst(new ArithInst("+", dest, ptr, "t1"));

        writeBackReg(dest, irStmt.dest, func, "t2");
    }

    void visitBranchStmt(BranchStmt irStmt, ASMFunc func, int stmtCnt) {
        if (irStmt.condition == null) {
            func.addInst(new JInst(func.blockHead + irStmt.trueLabel));
        } else if (isRegister(irStmt.condition)) {
            String condReg = resolveRegister(irStmt.condition, func, stmtCnt, "t0");
            // bnez t0, if_true  # 如果 t0 不为 0，跳转到 if_true
            // j if_false        # 否则，跳转到 if_false
            // todo optimize
            String blockForJump = func.curBlock.getBlockForJump(func.blockHead + irStmt.trueLabel);
            func.addInst(new BranchIfInst("bnez", condReg, null, blockForJump));
//            func.addInst(new BranchIfInst("bnez", condReg, null, func.blockHead + irStmt.trueLabel));
            func.addInst(new JInst(func.blockHead + irStmt.falseLabel));
        } else {
            int val = resolveValue(irStmt.condition);
            if (val != 0) {
                func.addInst(new JInst(func.blockHead + irStmt.trueLabel));
            } else {
                func.addInst(new JInst(func.blockHead + irStmt.falseLabel));
            }
        }
    }

    void visitSelectStmt(SelectStmt irStmt, ASMFunc func, int stmtCnt) {
        String destReg = getDestReg(irStmt.dest, stmtCnt, "t3");
        String condReg = "t0";
        String trueReg = "t1";
        String falseReg = "t2";

        if (isRegister(irStmt.cond)) condReg = resolveRegister(irStmt.cond, func, stmtCnt, "t0");
        else func.addInst(new LiInst("t0", resolveValue(irStmt.cond)));

        if (isRegister(irStmt.trueVal)) trueReg = resolveRegister(irStmt.trueVal, func, stmtCnt, "t1");
        else func.addInst(new LiInst("t1", resolveValue(irStmt.trueVal)));

        if (isRegister(irStmt.falseVal)) falseReg = resolveRegister(irStmt.falseVal, func, stmtCnt, "t2");
        else func.addInst(new LiInst("t2", resolveValue(irStmt.falseVal)));

        // # 首先，根据条件构造一个全为0或全为1的掩码
        //sltu t3, x0, t0    # t3 = (t0 != 0) ? 1 : 0，t3现在是1或0
        //neg t3, t3         # t3 = -t3，如果t3是1，则t3=-1（即全1的掩码）；否则是0
        //# 用掩码选择true_val或false_val
        //and t4, t1, t3     # t4 = true_val & t3，如果t3全为1，则t4 = true_val；否则t4 = 0
        //not t3, t3         # 反转t3，即t3 = ~t3
        //and t3, t2, t3     # t3 = false_val & ~t3，如果t3全为0，则t3 = false_val；否则t3 = 0
        //# 最后将选择的值合并
        //or t3, t4, t3      # t3 = t4 | t3，得到最终的选择结果
        func.addInst(new ArithInst("<u", "t3", "x0", condReg));
        func.addInst(new UnaryRegInst("neg", "t3", "t3"));

        func.addInst(new ArithInst("&", "t4", trueReg, "t3"));
        func.addInst(new UnaryRegInst("not", "t3", "t3"));
        func.addInst(new ArithInst("&", "t3", falseReg, "t3"));

        func.addInst(new ArithInst("|", destReg, "t4", "t3"));

        writeBackReg(destReg, irStmt.dest, func, "t3");
    }

    String getPhysicalReg(String regName) {
        if (allocaStateMap.get(regName) == null) return null;
        int physicRegId = allocaStateMap.get(regName).physicRegId;
        if (physicRegId == -1) return null;
        return physicalReg.getReg(physicRegId).name;
    }

    boolean isRegister(String regName) {
        return regName.startsWith("%") || regName.startsWith("@");
    }

    String resolveRegister(String regName, ASMFunc func, int stmtCnt, String tempReg) {
        // [use] of a register
        if (regName.startsWith("%")) {
            AllocaState state = allocaStateMap.get(regName);
            if (state == null) {
                throw new RuntimeException("resolveRegister in (" + func.name + "), regName is not in allocaStateMap: " + regName);
            }
            if (state.state == 0) {
                // in physical register
                return physicalReg.getReg(state.physicRegId).name;
            } else if (state.state == 1) {
                // in stack: get value from stack, store to tempReg
                func.addInst(Lw(tempReg, state.offset, "sp"));
                return tempReg;
            } else
                throw new RuntimeException("resolveRegister in (" + func.name + "), regName is not in allocaStateMap: " + regName);
        } else if (regName.startsWith("@")) {
            func.addInst(new LaInst(tempReg, regName.substring(1)));
            return tempReg;
        } else {
            throw new RuntimeException("unknown register: " + regName);
        }
    }

    int resolveValue(String name) {
        return switch (name) {
            case "true" -> 1;
            case "false", "null" -> 0;
            default -> Integer.parseInt(name);
        };
    }

    String getDestReg(String regName, int stmtCnt, String tempReg) {
        // [def] of a register
        AllocaState destState = allocaStateMap.get(regName);
        if (destState == null)
            return "t6";

        String destReg = tempReg;
        if (destState.state == 0)
            destReg = physicalReg.getReg(destState.physicRegId).name;
        return destReg;
    }
    int getOffset(String regName) {
        return allocaStateMap.get(regName).offset;
    }

    void writeBackReg(String destReg, String regName, ASMFunc func, String tempReg) {
        if (destReg.equals(tempReg)) {
            int offset = getOffset(regName);
            func.addInst(Sw(destReg, offset, "sp"));
        }
    }

    void visitBinaryExprStmt(BinaryExprStmt irStmt, ASMFunc func, int stmtCnt) {
        String reg1 = null;
        String reg2 = null;
        String destReg = getDestReg(irStmt.dest, stmtCnt, "t0");

        if (!isRegister(irStmt.register1) && !isRegister(irStmt.register2)) {
            int val1 = resolveValue(irStmt.register1);
            int val2 = resolveValue(irStmt.register2);
            func.addInst(new LiInst("t1", val1));
            func.addInst(new LiInst("t2", val2));
            reg1 = "t1"; reg2 = "t2";
        } else
        if (!isRegister(irStmt.register1) || !isRegister(irStmt.register2)) {
            boolean nextFlag = false;
            if (!isRegister(irStmt.register2)) {
                // register1 is register, register2 is value
                reg1 = resolveRegister(irStmt.register1, func, stmtCnt, "t1");
                int val2 = resolveValue(irStmt.register2);
                switch (irStmt.operator) {
                    case "add":
                        func.addInst(ArithImm("+", destReg, reg1, val2));
                        break;
                    case "sub":
                        func.addInst(ArithImm("+", destReg, reg1, -val2));
                        break;
                    case "and":
                        func.addInst(ArithImm("&", destReg, reg1, val2));
                        break;
                    case "or":
                        func.addInst(ArithImm("|", destReg, reg1, val2));
                        break;
                    case "xor":
                        func.addInst(ArithImm("^", destReg, reg1, val2));
                        break;
                    case "shl":
                        func.addInst(ArithImm("<<", destReg, reg1, val2));
                        break;
                    case "ashr":
                        func.addInst(ArithImm(">>", destReg, reg1, val2));
                        break;
                    case "icmp eq":
                        // xor t3, t1, val2     # t3 = t1 ^ val2，如果相等则 t3 = 0，否则 t3 !=0
                        // sltiu t0, t3, 1    # 如果 t3 < 1 (t3 == 0)，则 t0 = 1，否则 t0 = 0
                        func.addInst(ArithImm("^", "t3", reg1, val2));
                        func.addInst(ArithImm("<u", destReg, "t3", 1));
                        break;
                    case "icmp ne":
                        func.addInst(ArithImm("^", "t3", reg1, val2));
                        func.addInst(ArithImm("<u", "t3", "t3", 1));
                        func.addInst(ArithImm("^", destReg, "t3", 1));
                        break;
                    case "icmp slt": // <
                        func.addInst(ArithImm("<", destReg, reg1, val2));
                        break;
                    default:
                        nextFlag = true;
                }
                if (nextFlag) {
                    reg2 = "t2";
                    func.addInst(new LiInst("t2", val2));
                } else {
                    writeBackReg(destReg, irStmt.dest, func, "t0");
                    return;
                }
            } else {
                // register1 is register, register2 is value
                reg2 = resolveRegister(irStmt.register2, func, stmtCnt, "t2");
                int val1 = resolveValue(irStmt.register1);
                switch (irStmt.operator) {
                    case "add":
                        func.addInst(ArithImm("+", destReg, reg2, val1));
                        break;
                    case "and":
                        func.addInst(ArithImm("&", destReg, reg2, val1));
                        break;
                    case "or":
                        func.addInst(ArithImm("|", destReg, reg2, val1));
                        break;
                    case "xor":
                        func.addInst(ArithImm("^", destReg, reg2, val1));
                        break;
                    case "icmp eq":
                        // xor t3, t2, val1     # t3 = t2 ^ val1，如果相等则 t3 = 0，否则 t3 !=0
                        // sltiu t0, t3, 1    # 如果 t3 < 1 (t3 == 0)，则 t0 = 1，否则 t0 = 0
                        func.addInst(ArithImm("^", "t3", reg2, val1));
                        func.addInst(ArithImm("<u", destReg, "t3", 1));
                        break;
                    case "icmp ne":
                        func.addInst(ArithImm("^", "t3", reg2, val1));
                        func.addInst(ArithImm("<u", "t3", "t3", 1));
                        func.addInst(ArithImm("^", destReg, "t3", 1));
                        break;
                    case "icmp sgt": // >
                        func.addInst(ArithImm("<", destReg, reg2, val1));
                        break;
                    default:
                        nextFlag = true;
                }
                if (nextFlag) {
                    reg1 = "t1";
                    func.addInst(new LiInst("t1", val1));
                } else {
                    writeBackReg(destReg, irStmt.dest, func, "t0");
                    return;
                }
            }
        } else {
            reg1 = resolveRegister(irStmt.register1, func, stmtCnt, "t1");
            reg2 = resolveRegister(irStmt.register2, func, stmtCnt, "t2");
        }

        if (reg1 == null || reg2 == null)
            throw new RuntimeException("BinaryStmt: reg1 or reg2 is null");

        switch (irStmt.operator) {
            case "add":
                func.addInst(new ArithInst("+", destReg, reg1, reg2));
                break;
            case "sub":
                func.addInst(new ArithInst("-", destReg, reg1, reg2));
                break;
            case "mul":
                func.addInst(new ArithInst("*", destReg, reg1, reg2));
                break;
            case "sdiv":
                func.addInst(new ArithInst("/", destReg, reg1, reg2));
                break;
            case "srem":
                func.addInst(new ArithInst("%", destReg, reg1, reg2));
                break;
            case "and":
                func.addInst(new ArithInst("&", destReg, reg1, reg2));
                break;
            case "or":
                func.addInst(new ArithInst("|", destReg, reg1, reg2));
                break;
            case "xor":
                func.addInst(new ArithInst("^", destReg, reg1, reg2));
                break;
            case "shl":
                func.addInst(new ArithInst("<<", destReg, reg1, reg2));
                break;
            case "ashr":
                func.addInst(new ArithInst(">>", destReg, reg1, reg2));
                break;
            case "icmp eq":
                // xor t3, t1, t2     # t3 = t1 ^ t2，如果相等则 t3 = 0，否则 t3 !=0
                // sltiu t0, t3, 1    # 如果 t3 < 1 (t3 == 0)，则 t0 = 1，否则 t0 = 0
                func.addInst(new ArithInst("^", "t3", reg1, reg2));
                func.addInst(ArithImm("<u", destReg, "t3", 1));
                break;
            case "icmp ne":
                func.addInst(new ArithInst("^", "t3", reg1, reg2));
                func.addInst(ArithImm("<u", "t3", "t3", 1));
                func.addInst(ArithImm("^", destReg, "t3", 1));
                break;
            case "icmp slt": // <
                func.addInst(new ArithInst("<", destReg, reg1, reg2));
                break;
            case "icmp sgt": // >
                func.addInst(new ArithInst("<", destReg, reg2, reg1));
                break;
            case "icmp sle": // <=
                func.addInst(new ArithInst("<", "t0", reg2, reg1));
                func.addInst(ArithImm("^", destReg, "t0", 1));
                break;
            case "icmp sge": // >=
                func.addInst(new ArithInst("<", "t0", reg1, reg2));
                func.addInst(ArithImm("^", destReg, "t0", 1));
                break;
            default:
                throw new RuntimeException("unknown operator at BinaryStmt: " + irStmt.operator);
        }

        writeBackReg(destReg, irStmt.dest, func, "t0");
    }

    void visitUnaryExprStmt(UnaryExprStmt irStmt, ASMFunc func, int stmtCnt) {
        String srcReg = "t1";
        String destReg = getDestReg(irStmt.dest, stmtCnt, "t0");
        if (!isRegister(irStmt.register)) {
            int val = resolveValue(irStmt.register);
            func.addInst(new LiInst("t1", val));
        } else {
            srcReg = resolveRegister(irStmt.register, func, stmtCnt, "t1");
        }
        switch (irStmt.operator) {
            case "!":
                func.addInst(ArithImm("^", destReg, srcReg, 1));
                break;
            case "~":
                func.addInst(ArithImm("^", destReg, srcReg, -1));
                break;
            case "-":
                func.addInst(new UnaryRegInst("neg", destReg, srcReg));
                break;
            case "++":
                func.addInst(ArithImm("+", destReg, srcReg, 1));
                break;
            case "--":
                func.addInst(ArithImm("+", destReg, srcReg, -1));
                break;
            default:
                throw new RuntimeException("unknown operator at UnaryStmt: " + irStmt.operator);
        }
        writeBackReg(destReg, irStmt.dest, func, "t0");
    }

    void visitStoreStmt(StoreStmt irStmt, ASMFunc func, int stmtCnt) {
        if (!isRegister(irStmt.val)) {
            int val = resolveValue(irStmt.val);
            String dest = resolveRegister(irStmt.dest, func, stmtCnt, "t0");
            func.addInst(new LiInst("t1", val));
            func.addInst(new SwInst("t1", 0, dest));
            return;
        }
        String src = resolveRegister(irStmt.val, func, stmtCnt, "t0");
        String dest = resolveRegister(irStmt.dest, func, stmtCnt, "t1");
        func.addInst(new SwInst(src, 0, dest));
    }

    void visitLoadStmt(LoadStmt irStmt, ASMFunc func, int stmtCnt) {
        String ptr = resolveRegister(irStmt.pointer, func, stmtCnt, "t1");
        String dest = getDestReg(irStmt.dest, stmtCnt, "t0");

        func.addInst(new LwInst(dest, 0, ptr)); // load t1, 0(t0)

        writeBackReg(dest, irStmt.dest, func, "t0");
    }

    void visitReturnStmt(ReturnStmt irStmt, ASMFunc func, int stmtCnt) {
        if (irStmt.src != null) {
            if (!isRegister(irStmt.src)) {
                int val = resolveValue(irStmt.src);
                func.addInst(new LiInst("a0", val));
            } else {
                String src = resolveRegister(irStmt.src, func, stmtCnt, "ra");
                if (!src.equals("a0")) {
                    func.addInst(new MvInst("a0", src));
                }
            }
        }
        func.addInst(func.epilogue);
    }

    void physical2Virtual(ArrayList<String> from_, ArrayList<String> to_, ASMFunc func, int stmtCnt) {
        int size = from_.size();
        DependencyAnalysis dependency = new DependencyAnalysis("t0");

        for (int i = 0; i < size; i++)
            if (getPhysicalReg(to_.get(i)) == null) {
                int offset = getOffset(to_.get(i));
                func.addInst(Sw(from_.get(i), offset, "sp"));
            }

        for (int i = 0; i < size; i++) {
            String destReg = getPhysicalReg(to_.get(i));
            if (destReg == null) continue;
            dependency.addDependency(from_.get(i), destReg);
        }

        dependency.analyze();

        ArrayList<String> from = dependency.getFromList();
        ArrayList<String> to = dependency.getToList();
        for (int i = 0; i < from.size(); i++) {
            String fromReg = from.get(i), toReg = to.get(i);
            func.addInst(new MvInst(toReg, fromReg));
        }
    }

    void virtual2Physical(ArrayList<String> from_, ArrayList<String> to_, ASMFunc func, int stmtCnt) {
        int size = from_.size();
        DependencyAnalysis dependency = new DependencyAnalysis("t0");
        for (int i = 0; i < size; i++) {
            String srcReg = getPhysicalReg(from_.get(i));
            if (srcReg == null) continue;
            dependency.addDependency(srcReg, to_.get(i));
        }

        dependency.analyze();

        ArrayList<String> from = dependency.getFromList();
        ArrayList<String> to = dependency.getToList();
        for (int i = 0; i < from.size(); i++) {
            String fromReg = from.get(i), toReg = to.get(i);
            func.addInst(new MvInst(toReg, fromReg));
        }
        // dependency.debug();
        for (int i = 0; i < size; i++)
            if (getPhysicalReg(from_.get(i)) == null) {
                if (isRegister(from_.get(i))) {
                    String srcReg = resolveRegister(from_.get(i), func, stmtCnt, "t0");
                    func.addInst(new MvInst(to_.get(i), srcReg));
                } else {
                    int val = resolveValue(from_.get(i));
                    func.addInst(new LiInst(to_.get(i), val));
                }
            }
    }

    boolean isTailRecursion(CallStmt irStmt, IRStmt nxtStmt, ASMFunc func) {
        if (!irStmt.funcName.equals(func.name)) return false;
        if (nxtStmt instanceof ReturnStmt ret
                && (ret.src == null || ret.src.equals(irStmt.dest))) return true;
        return false;
    }

    void visitTailRecursion(CallStmt irStmt, ASMFunc func, int stmtCnt, IRBlock curBlock) {
        // store arguments to a0 ~ a7
        func.needPrologueBlock = true;

        // on stack
        for (int i = irStmt.args.size() - 1; i >= 8; i--) {
            int offset = (i - 8) * 4 + func.spOffset;
            String srcReg;
            if (isRegister(irStmt.args.get(i))) srcReg = resolveRegister(irStmt.args.get(i), func, stmtCnt, "t0");
            else {
                int val = resolveValue(irStmt.args.get(i));
                func.addInst(new LiInst("t0", val));
                srcReg = "t0";
            }
            func.addInst(Sw(srcReg, offset, "sp"));
        }

        // store arguments to a0 ~ a7
        int argSize = Math.min(irStmt.args.size(), 8);
        ArrayList<String> from = new ArrayList<>();
        ArrayList<String> to = new ArrayList<>();
        for (int i = 0; i < argSize; i++) {
            from.add(irStmt.args.get(i));
            to.add("a" + i);
        }
        virtual2Physical(from, to, func, stmtCnt);

        func.addInst(new JInst(func.blockHead + irStmt.funcName + ".prologue"));
    }

    void visitCallStmt(CallStmt irStmt, ASMFunc func, int stmtCnt) {
        func.addInst(new CommentInst(""));

        // use some temp regs to save a-registers
        HashSet<Integer> freeRegList = new HashSet<>();
        HashMap<String, String> useFreeReg = new HashMap<>();

        for (int i = 8; i < 22; i++)
            if (!irStmt.liveOutPhyReg.contains(i) && !irStmt.liveInPhyReg.contains(i))
                freeRegList.add(i);

        for (int i = 0; i < 8; i++)
            if (irStmt.liveOutPhyReg.contains(i)){
                if (freeRegList.isEmpty())
                    func.addInst(Sw("a" + i, func.spOffset - 36 + (7 - i) * 4, "sp"));
                else {
                    int freeRegId = freeRegList.iterator().next();
                    String freeReg = physicalReg.getName(freeRegId);
                    freeRegList.remove(freeRegId);
                    useFreeReg.put("a" + i, freeReg);
                    func.addInst(new MvInst(freeReg, "a" + i));
                    if (!usedReg[freeRegId]) {
                        usedReg[freeRegId] = true;
                        func.prologue.add(Sw(physicalReg.getName(freeRegId), func.spOffset - 40 - freeRegId * 4, "sp"));
                        func.epilogue.add(Lw(physicalReg.getName(freeRegId), func.spOffset - 40 - freeRegId * 4, "sp"));
                    }
                }
            }

        // on stack
        for (int i = irStmt.args.size() - 1; i >= 8; i--) {
            int offset = (i - 8) * 4;
            String srcReg;
            if (isRegister(irStmt.args.get(i))) srcReg = resolveRegister(irStmt.args.get(i), func, stmtCnt, "t0");
            else {
                int val = resolveValue(irStmt.args.get(i));
                func.addInst(new LiInst("t0", val));
                srcReg = "t0";
            }
            func.addInst(Sw(srcReg, offset, "sp"));
        }

        // store arguments to a0 ~ a7
        int argSize = Math.min(irStmt.args.size(), 8);
        ArrayList<String> from = new ArrayList<>();
        ArrayList<String> to = new ArrayList<>();
        for (int i = 0; i < argSize; i++) {
            from.add(irStmt.args.get(i));
            to.add("a" + i);
        }
        virtual2Physical(from, to, func, stmtCnt);

        func.addInst(new CallInst(irStmt.funcName)); // call


        if (irStmt.retType.typeName.equals("void")) {
            // void: restore a0 ~ a7
            for (int i = 0; i < 8; i++)
                if (irStmt.liveOutPhyReg.contains(i)) {
                    if (!useFreeReg.containsKey("a" + i))
                        func.addInst(Lw("a" + i, func.spOffset - 36 + (7 - i) * 4, "sp"));
                    else
                        func.addInst(new MvInst("a" + i, useFreeReg.get("a" + i)));
                }
            func.addInst(new CommentInst(""));
            return;
        }

        String destReg = getDestReg(irStmt.dest, stmtCnt, "t0");
        if (!destReg.equals("a0")) {
            if (destReg.equals("t0")) {
                // dest does not have physical register
                int offset = getOffset(irStmt.dest);
                func.addInst(Sw("a0", offset, "sp"));
            } else {
                // dest has physical register
                func.addInst(new MvInst(destReg, "a0"));
            }
        }
        // restore a0 ~ a7
        for (int i = 0; i < 8; i++)
            if (!destReg.equals("a" + i) && irStmt.liveOutPhyReg.contains(i)) {
                if (!useFreeReg.containsKey("a" + i))
                    func.addInst(Lw("a" + i, func.spOffset - 36 + (7 - i) * 4, "sp"));
                else
                    func.addInst(new MvInst("a" + i, useFreeReg.get("a" + i)));
            }
        func.addInst(new CommentInst(""));
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(textSection.toString());
        sb.append(dataSection.toString());
        sb.append(rodataSection.toString());
        return sb.toString();
    }
}
