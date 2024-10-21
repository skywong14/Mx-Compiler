package ASM;

import ASM.inst.*;
import ASM.operand.PhysicalReg;
import ASM.section.ASMFunction;
import ASM.section.DataSection;
import ASM.section.RodataSection;
import ASM.section.TextSection;
import IR.IRStmts.*;
import optimize.*;

import java.util.ArrayList;
import java.util.HashMap;

public class ASMBuilder {
    public PhysicalReg physicalReg = new PhysicalReg();
    public TextSection textSection = new TextSection();
    public DataSection dataSection = new DataSection();
    public RodataSection rodataSection = new RodataSection();

    public IRCode irCode;

    RegAllocator regAllocator = null;

    static boolean outOfBound(int imm) {
        return imm < -2048 || imm > 2047;
    }

    void debug(String msg) {
        System.out.println("; ASM: " + msg);
    }

    public ArrayList<ASMInst> Lw(String rd, int offset, String rs) {
        ArrayList<ASMInst> insts = new ArrayList<>();
        if (outOfBound(offset)) {
            insts.add(new LiInst("t6", offset));
            insts.add(new ArithInst("+", "t6", rs, "t6"));
            insts.add(new LwInst(rd, 0, "t6"));
        } else {
            insts.add(new LwInst(rd, offset, rs));
        }
        return insts;
    }

    public ArrayList<ASMInst> Sw(String src, int offset, String rd) {
        ArrayList<ASMInst> insts = new ArrayList<>();
        if (outOfBound(offset)) {
            insts.add(new LiInst("t6", offset));
            insts.add(new ArithInst("+", "t6", rd, "t6"));
            insts.add(new SwInst(src, 0, "t6"));
        } else {
            insts.add(new SwInst(src, offset, rd));
        }
        return insts;
    }

    public ArrayList<ASMInst> ArithImm(String op, String rd, String rs, int imm) {
        ArrayList<ASMInst> insts = new ArrayList<>();
        if (outOfBound(imm)) {
            insts.add(new LiInst("t6", imm));
            insts.add(new ArithInst(op, rd, rs, "t6"));
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
        int state = 0; // 0: has physical register, 1: in stack, 2: spilt halfway
        int offset = -1; // if -1, not in stack
        int physicRegId = -1; // if -1, not in physical register
        int spillTime = -1; // need to spill at spillTime
        public AllocaState(int state, int offset, int physicRegId, int spillTime) {
            this.state = state;
            this.offset = offset;
            this.physicRegId = physicRegId;
            this.spillTime = spillTime;
        }
    }
    HashMap<String, AllocaState> allocaStateMap;

    void buildFunction(IRFunction func) {
        regAllocator = new RegAllocator(func); // allocate virtual registers to physical registers

        ASMFunc asmFunc = new ASMFunc(func.name);

        // todo: 重新规划占空间的使用
        // 预处理寄存器，计算空间，预处理:
        // 1.被溢出: 相对于sp的偏移量 2.分配到物理寄存器：寄存器编号
        allocaStateMap = new HashMap<>();
        int headOffset = 4;
        int tmpOffset = headOffset; // [0, 4) is used to store ra
        for (String regName : regAllocator.intervals.keySet()) {
            if (regAllocator.isSpilt(regName)) {
                allocaStateMap.put(regName, new AllocaState(2, tmpOffset, regAllocator.hasReg(regName), regAllocator.getSpillTime(regName)));
                tmpOffset += 4;
            } else if (regAllocator.hasReg(regName) != -1) {
                allocaStateMap.put(regName, new AllocaState(0, -1, regAllocator.hasReg(regName), -1));
            } else {
                allocaStateMap.put(regName, new AllocaState(1, tmpOffset, -1, -1));
                tmpOffset += 4;
            }
        }
        asmFunc.spOffset = tmpOffset;

        // asmFunc.inits: store ra, move sp, store the arguments
        // store ra
        asmFunc.addInst(Sw("ra", , "sp"));
        // store s0~s11
        // todo

        // func body
        boolean firstBlock = true;
        int stmtCnt = -1;
        for (IRBlock irBlock : regAllocator.linearOrder) {
            if (firstBlock) firstBlock = false;
            else asmFunc.newBlock(asmFunc.blockHead + irBlock.label);
            for (IRStmt irStmt : irBlock.stmts) {
                stmtCnt++;
                visitIRStmt(irStmt, asmFunc, stmtCnt);
            }
        }
        // asmFunc.ends



        // add to textSection
        textSection.addFunction(asmFunc);
    }

    public void build() {
        buildStringConst();

        buildGlobalVariables();

        // Functions, Methods
        for (IRFunction func : irCode.funcStmts)
            buildFunction(func);
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

    /*void buildFunctions(FunctionImplementStmt irFunction) {
        ASMFunction func = new ASMFunction(irFunction.name, getSpSize(irFunction) + regSize);
        //init: move sp, store the arguments
        func.blockHead = "B" + functionCnt + ".";
        func.addInst(ArithImm("+", "sp", "sp", -func.spOffset));
        // store ra
        func.addInst(Sw("ra", func.allocMemory(4), "sp"));

        // store s0~s11
        /*
        sSize = 48;
        for (int i = 0; i < 12; i++) {
            func.addInst(Sw("s" + i, i * 4, "sp")); // use sp ~ sp + 12 * 4
            func.putVirtualReg("s" + i, func.allocMemory(4));
        }

        // store args
        for (int i = 0; i < irFunction.argNames.size(); i++) {
            if (i < 8) {
                int offset = func.allocMemory(4);
                func.addInst(Sw("a" + i, offset, "sp")); // use sp + sSize ~ sp + sSize min(8, args.size()) * 4
                func.putVirtualReg("%" + irFunction.argNames.get(i), offset);
            } else {
                func.putVirtualReg("%" + irFunction.argNames.get(i), func.spOffset + (i - 8) * 4);
            }
        }

        boolean firstBlock = true;
        for (Block block : irFunction.blocks) {
            if (firstBlock) firstBlock = false;
            else func.newBlock(func.blockHead + block.label);
            for (IRStmt irStmt : block.stmts) {
                visitIRStmt(irStmt, func);
            }
        }
        textSection.addFunction(func);
    }
    */

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
        // todo: check if need to spill
    }

    void visitMoveStmt(MoveStmt irStmt, ASMFunc func, int stmtCnt) {
        AllocaState destState = allocaStateMap.get(irStmt.dest);
        if (destState.state == 0 || destState.state == 2 && destState.spillTime < stmtCnt) {
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
        // todo: if has imm
        // slli t1, t1, 2      # 计算偏移量：t1 << 2
        // add t2, t0, t1      # 计算结果地址：t0 + 偏移量
        String ptr = resolveRegister(irStmt.pointer, func, stmtCnt, "t0");
        String index = resolveRegister(irStmt.index, func, stmtCnt, "t1");
        String dest = getDestReg(irStmt.dest, stmtCnt, "t2");

        func.addInst(new ArithImmInst("<<", index, index, 2));
        func.addInst(new ArithInst("+", dest, ptr, index));

        writeBackReg(dest, irStmt.dest, func);
    }

    void visitBranchStmt(BranchStmt irStmt, ASMFunc func, int stmtCnt) {
        if (irStmt.condition == null) {
            func.addInst(new JInst(func.blockHead + irStmt.trueLabel));
        } else {
            String condReg = resolveRegister(irStmt.condition, func, stmtCnt, "t0");
            // bnez t0, if_true  # 如果 t0 不为 0，跳转到 if_true
            // j if_false        # 否则，跳转到 if_false
            func.addInst(new BranchIfInst("bnez", condReg, null, func.blockHead + irStmt.trueLabel));
            func.addInst(new JInst(func.blockHead + irStmt.falseLabel));
        }
    }

    void visitSelectStmt(SelectStmt irStmt, ASMFunc func, int stmtCnt) {
        String condReg = resolveRegister(irStmt.cond, func, stmtCnt, "t0");
        String trueReg = resolveRegister(irStmt.trueVal, func, stmtCnt, "t1");
        String falseReg = resolveRegister(irStmt.falseVal, func, stmtCnt, "t2");
        String destReg = getDestReg(irStmt.dest, stmtCnt, "t3");
        // # 首先，根据条件构造一个全为0或全为1的掩码
        //sltu t3, x0, t0    # t3 = (t0 != 0) ? 1 : 0，t3现在是1或0
        //neg t3, t3         # t3 = -t3，如果t3是1，则t3=-1（即全1的掩码）；否则是0
        //# 用掩码选择true_val或false_val
        //and t4, t1, t3     # t4 = true_val & t3，如果t3全为1，则t4 = true_val；否则t4 = 0
        //not t3, t3         # 反转t3，即t3 = ~t3
        //and t5, t2, t3     # t5 = false_val & ~t3，如果t3全为0，则t5 = false_val；否则t5 = 0
        //# 最后将选择的值合并
        //or t3, t4, t5      # t3 = t4 | t5，得到最终的选择结果
        func.addInst(new ArithInst("<u", "t3", "x0", condReg));
        func.addInst(new UnaryRegInst("neg", "t3", "t3"));

        func.addInst(new ArithInst("&", "t4", trueReg, "t3"));
        func.addInst(new UnaryRegInst("not", "t3", "t3"));
        func.addInst(new ArithInst("&", "t5", falseReg, "t3"));

        func.addInst(new ArithInst("|", destReg, "t4", "t5"));

        writeBackReg(destReg, irStmt.dest, func);
    }

//    void resolveRegister(String regName, String destReg, ASMFunction func) {
//        if (regName.startsWith("%")) {
//            int offset1 = func.getVirtualReg(regName);
//            func.addInst(Lw(destReg, offset1, "sp"));
//        } else if (regName.startsWith("@")) {
//            func.addInst(new LaInst(destReg, regName.substring(1)));
//        } else {
//            int val;
//            if (regName.equals("true")) val = 1;
//            else if (regName.equals("false")) val = 0;
//            else if (regName.equals("null")) val = 0;
//            else val = Integer.parseInt(regName);
//            func.addInst(new LiInst(destReg, val));
//        }
//    }

    boolean isRegister(String regName) {
        return regName.startsWith("%") || regName.startsWith("@");
    }

    String resolveRegister(String regName, ASMFunc func, int stmtCnt, String tempReg) {
        // [use] of a register
        if (regName.startsWith("%")) {
            AllocaState state = allocaStateMap.get(regName);
            if (state.state == 0) {
                // in physical register
                return physicalReg.getReg(state.physicRegId).name;
            } else if (state.state == 1) {
                // in stack: get value from stack, store to tempReg
                func.addInst(Lw(tempReg, state.offset, "sp"));
                return tempReg;
            } else {
                // spilt halfway
                if (stmtCnt > state.spillTime) {
                    func.addInst(Lw(tempReg, state.offset, "sp"));
                    return tempReg;
                } else {
                    return physicalReg.getReg(state.physicRegId).name;
                }
            }
        } else if (regName.startsWith("@")) {
            func.addInst(new LaInst(tempReg, regName.substring(1)));
            return tempReg;
        } else {
            throw new RuntimeException("unknown register: " + regName);
        }
    }

    int resolveValue(String name) {
        int val = switch (name) {
            case "true" -> 1;
            case "false", "null" -> 0;
            default -> Integer.parseInt(name);
        };
        return val;
    }

    String getDestReg(String regName, int stmtCnt, String tempReg) {
        // [def] of a register
        AllocaState destState = allocaStateMap.get(regName);
        String destReg = tempReg;
        if (destState.state == 0 || destState.state == 2 && destState.spillTime < stmtCnt)
            destReg = physicalReg.getReg(destState.physicRegId).name;
        return destReg;
    }
    int getOffset(String regName) {
        return allocaStateMap.get(regName).offset;
    }

    void writeBackReg(String destReg, String regName, ASMFunc func) {
        if (destReg.startsWith("t")) {
            // store t0 to stack
            int offset = getOffset(regName);
            func.addInst(Sw("t0", offset, "sp"));
        }
    }

    void visitBinaryExprStmt(BinaryExprStmt irStmt, ASMFunc func, int stmtCnt) {
        if (!isRegister(irStmt.register1) || !isRegister(irStmt.register2)) {
            // todo
        }

        String reg1 = resolveRegister(irStmt.register1, func, stmtCnt, "t1");
        String reg2 = resolveRegister(irStmt.register2, func, stmtCnt, "t2");
        String destReg = getDestReg(irStmt.dest, stmtCnt, "t0");


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
                func.addInst(new ArithImmInst("<u", destReg, "t3", 1));
                break;
            case "icmp ne":
                func.addInst(new ArithInst("^", "t3", reg1, reg2));
                func.addInst(new ArithImmInst("<u", "t3", "t3", 1));
                func.addInst(new ArithImmInst("^", destReg, "t3", 1));
                break;
            case "icmp slt": // <
                func.addInst(new ArithInst("<", destReg, reg1, reg2));
                break;
            case "icmp sgt": // >
                func.addInst(new ArithInst("<", destReg, "t2", reg1));
                break;
            case "icmp sle": // <=
                func.addInst(new ArithInst("<", "t0", reg2, reg1));
                func.addInst(new ArithImmInst("^", destReg, "t0", 1));
                break;
            case "icmp sge": // >=
                func.addInst(new ArithInst("<", "t0", reg1, reg2));
                func.addInst(new ArithImmInst("^", destReg, "t0", 1));
                break;
            default:
                throw new RuntimeException("unknown operator at BinaryStmt: " + irStmt.operator);
        }

        writeBackReg(destReg, irStmt.dest, func);
    }

    void visitUnaryExprStmt(UnaryExprStmt irStmt, ASMFunc func, int stmtCnt) {
        String srcReg = resolveRegister(irStmt.register, func, stmtCnt, "t1");
        String destReg = getDestReg(irStmt.dest, stmtCnt, "t0");
        switch (irStmt.operator) {
            case "!":
                func.addInst(new ArithImmInst("^", destReg, srcReg, 1));
                break;
            case "~":
                func.addInst(new ArithImmInst("^", destReg, srcReg, -1));
                break;
            case "-":
                func.addInst(new UnaryRegInst("neg", destReg, srcReg));
                break;
            case "++":
                func.addInst(new ArithImmInst("+", destReg, srcReg, 1));
                break;
            case "--":
                func.addInst(new ArithImmInst("+", destReg, srcReg, -1));
                break;
            default:
                throw new RuntimeException("unknown operator at UnaryStmt: " + irStmt.operator);
        }
        writeBackReg(destReg, irStmt.dest, func);
    }

    void visitStoreStmt(StoreStmt irStmt, ASMFunc func, int stmtCnt) {
        String src = resolveRegister(irStmt.val, func, stmtCnt, "t0");
        String dest = resolveRegister(irStmt.dest, func, stmtCnt, "t1");
        func.addInst(new SwInst(src, 0, dest));
    }

    void visitLoadStmt(LoadStmt irStmt, ASMFunc func, int stmtCnt) {
        String ptr = resolveRegister(irStmt.pointer, func, stmtCnt, "t0");
        String dest = getDestReg(irStmt.dest, stmtCnt, "t1");

        func.addInst(new LwInst(dest, 0, ptr)); // load t1, 0(t0)

        writeBackReg(dest, irStmt.dest, func);
    }

    void visitReturnStmt(ReturnStmt irStmt, ASMFunc func, int stmtCnt) {
        if (irStmt.src != null) {
            String src = resolveRegister(irStmt.src, func, stmtCnt, "ra");
            if (!src.equals("a0")) {
                func.addInst(new MvInst("a0", src));
            }
        }
        // restore ra
        func.addInst(Lw("ra", 0, "sp"));
        // restore sp
        func.addInst(ArithImm("+", "sp", "sp", func.spOffset));
        func.addInst(new RetInst());
    }

    void visitCallStmt(CallStmt irStmt, ASMFunction func) {
        // save a0~a7

        // store arguments to a0~a7, or more to stack
        int extraSpOffset = 0;
        if (irStmt.args.size() > 8) {
            extraSpOffset = (irStmt.args.size() - 8) * 4;
            // store to stack
        }
        // sp -= extraSpOffset
        int curOffset = 0;
        if (extraSpOffset > 0)
            func.addInst(ArithImm("+", "sp", "sp", -extraSpOffset));
        for (int i = 0; i < irStmt.args.size(); i++) {
            if (i < 8) {
                resolveRegister(irStmt.args.get(i), "a" + i, func);
            } else {
                resolveRegister(irStmt.args.get(i), "t0", func);
                func.addInst(Sw("t0", curOffset, "sp"));
                curOffset += 4;
            }
        }

        func.addInst(new CallInst(irStmt.funcName));

        // sp += extraSpOffset
        if (extraSpOffset > 0)
            func.addInst(ArithImm("+", "sp", "sp", extraSpOffset));
        // restore a0~a7
        // get a0
        if (!irStmt.retType.typeName.equals("void")) {
            int offset = func.getVirtualReg(irStmt.dest);
            func.addInst(Sw("a0", offset, "sp"));
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(textSection.toString());
        sb.append(dataSection.toString());
        sb.append(rodataSection.toString());
        return sb.toString();
    }
}
