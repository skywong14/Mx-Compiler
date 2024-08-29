package ASM;

import ASM.inst.*;
import ASM.operand.PhysicalReg;
import ASM.section.ASMFunction;
import ASM.section.DataSection;
import ASM.section.RodataSection;
import ASM.section.TextSection;
import IR.IRBuilder;
import IR.IRStmts.*;

import java.util.ArrayList;

public class ASMBuilder {
    public static final int regSize = 32;
    public IRBuilder irBuilder;
    public PhysicalReg physicalReg = new PhysicalReg();

    public TextSection textSection = new TextSection();
    public DataSection dataSection = new DataSection();
    public RodataSection rodataSection = new RodataSection();

    static boolean outOfBound(int imm) {
        return imm < -2048 || imm > 2047;
    }

    public ArrayList<ASMInst> Lw(String rd, int offset, String rs) {
        ArrayList<ASMInst> insts = new ArrayList<>();
        if (outOfBound(offset)) {
            insts.add(new LiInst("t0", offset));
            insts.add(new ArithInst("+", "t0", rs, "t0"));
            insts.add(new LwInst(rd, 0, "t0"));
        } else {
            insts.add(new LwInst(rd, offset, rs));
        }
        return insts;
    }

    public ArrayList<ASMInst> Sw(String src, int offset, String rd) {
        ArrayList<ASMInst> insts = new ArrayList<>();
        if (outOfBound(offset)) {
            insts.add(new LiInst("t0", offset));
            insts.add(new ArithInst("+", "t0", rd, "t0"));
            insts.add(new SwInst(src, 0, "t0"));
        } else {
            insts.add(new SwInst(src, offset, rd));
        }
        return insts;
    }

    public ArrayList<ASMInst> ArithImm(String op, String rd, String rs, int imm) {
        ArrayList<ASMInst> insts = new ArrayList<>();

        insts.add(new LiInst("t0", imm));
        insts.add(new ArithInst(op, rd, rs, "t0"));
        return insts;
        /*if (outOfBound(imm)) {
            insts.add(new LiInst("t0", imm));
            insts.add(new ArithInst(op, rd, rs, "t0"));
            return insts;
        } else {
            insts.add(new ArithImmInst(op, rd, rs, imm));
            return insts;
        }*/
    }

    void init(IRBuilder irBuilder_) {
        irBuilder = irBuilder_;
    }
    public ASMBuilder(IRBuilder irBuilder_) {
        init(irBuilder_);
    }

    void buildStringConst() {
        for (IRStmt irStmt: irBuilder.constantStmts) {
            if (irStmt instanceof StringDeclareStmt stmt) {
                String asmString = StringDeclareStmt.convert2riscv(stmt.printValue);
                String label = stmt.dest.substring(1); // remove @
                rodataSection.addStringConst(label, asmString);
            }
        }
    }

    void buildGlobalVariables() {
        for (IRStmt irStmt: irBuilder.irStmts) {
            if (irStmt instanceof GlobalVariableDeclareStmt stmt) {
                String label = stmt.name.substring(1); // remove @
                dataSection.addGlobalVariable(label, "0");
            }
        }
    }

    int getSpSize(FunctionImplementStmt irFunction) {
        int spSize = 0;
        for (Block block : irFunction.blocks) {
            for (IRStmt irStmt : block.stmts) {
                spSize += irStmt.getSpSize();
            }
        }
        return spSize * 4;
    }

    void buildFunctions(FunctionImplementStmt irFunction) {
        ASMFunction func = new ASMFunction(irFunction.name, getSpSize(irFunction) + regSize);
        //init: move sp, store the arguments
        func.addInst(ArithImm("+", "sp", "sp", -func.spOffset));

        int sSize = 0;
        // store s0~s11
        /*
        sSize = 48;
        for (int i = 0; i < 12; i++) {
            func.addInst(Sw("s" + i, i * 4, "sp")); // use sp ~ sp + 12 * 4
            func.putVirtualReg("s" + i, func.allocMemory(4));
        }
        */

        // store args
        for (int i = 0; i < irFunction.argNames.size(); i++) {
            if (i < 8) {
                func.addInst(Sw("a" + i, i * 4 + sSize, "sp")); // use sp + sSize ~ sp + sSize min(8, args.size()) * 4
                func.putVirtualReg(irFunction.argNames.get(i), func.allocMemory(4));
            } else {
                func.putVirtualReg(irFunction.argNames.get(i), func.spOffset + (i - 8) * 4);
            }
        }

        boolean firstBlock = true;
        for (Block block : irFunction.blocks) {
            if (firstBlock) firstBlock = false;
            else func.newBlock(block.label);
            for (IRStmt irStmt : block.stmts) {
                visitIRStmt(irStmt, func);
            }
        }

        // restore s0~s11
        // todo
        // end: restore sp
        func.addInst(ArithImm("+", "sp", "sp", func.spOffset));

        textSection.addFunction(func);
    }

    void visitIRStmt(IRStmt irStmt, ASMFunction func) {
        if (irStmt instanceof AllocaStmt) {
            func.putVirtualReg(((AllocaStmt) irStmt).dest, func.allocMemory(4));
        } else if (irStmt instanceof BinaryExprStmt) {
            visitBinaryExprStmt((BinaryExprStmt) irStmt, func);
        } else if (irStmt instanceof UnaryExprStmt) {
            visitUnaryExprStmt((UnaryExprStmt) irStmt, func);
        } else if (irStmt instanceof BranchStmt) {
            visitBranchStmt((BranchStmt) irStmt, func);
        } else if (irStmt instanceof CallStmt) {
            visitCallStmt((CallStmt) irStmt, func);
        } else if (irStmt instanceof SelectStmt) {
            visitSelectStmt((SelectStmt) irStmt, func);
        } else if (irStmt instanceof LoadStmt) {
            visitLoadStmt((LoadStmt) irStmt, func);
        } else if (irStmt instanceof StoreStmt) {
            visitStoreStmt((StoreStmt) irStmt, func);
        } else if (irStmt instanceof ReturnStmt) {
            visitReturnStmt((ReturnStmt) irStmt, func);
        } else if (irStmt instanceof NewArrayStmt) {
            visitNewArrayStmt((NewArrayStmt) irStmt, func);
        } else if (irStmt instanceof GetElementPtrStmt) {
            visitGetElementPtrStmt((GetElementPtrStmt) irStmt, func);
        } else {
            throw new RuntimeException("unknown IRStmt: " + irStmt);
        }
    }

    void visitGetElementPtrStmt(GetElementPtrStmt irStmt, ASMFunction func) {
        if (irStmt.hasZero) {
            resolveRegister(irStmt.pointer, "t0", func);
            resolveRegister(irStmt.index.get(0), "t1", func);
            // slli t2, t1, 2      # 计算偏移量：t1 << 2，相当于乘以4（因为i32是4字节）
            // add t2, t0, t2      # 计算结果地址：t0 + 偏移量
            func.addInst(new ArithImmInst("slli", "t1", "t1", 2));
            func.addInst(new ArithInst("+", "t2", "t0", "t1"));

            int offset = func.getVirtualReg(irStmt.dest);
            func.addInst(Sw("t2", offset, "sp"));
        } else {
            resolveRegister(irStmt.pointer, "t0", func);
            resolveRegister(irStmt.index.get(0), "t1", func);
            //slli t1, t1, 2      # 计算偏移量：t1 << 2
            // add t2, t0, t1      # 计算结果地址：t0 + 偏移量
            func.addInst(new ArithImmInst("slli", "t1", "t1", 2));
            func.addInst(new ArithInst("+", "t2", "t0", "t1"));

            int offset = func.getVirtualReg(irStmt.dest);
            func.addInst(Sw("t2", offset, "sp"));
        }
    }

    void visitNewArrayStmt(NewArrayStmt irStmt, ASMFunction func) {
        for (IRStmt stmt: irStmt.stmts)
            if (stmt instanceof LabelStmt) {
                func.newBlock(((LabelStmt) stmt).label);
            } else {
                visitIRStmt(stmt, func);
            }
    }

    void visitBranchStmt(BranchStmt irStmt, ASMFunction func) {
        if (irStmt.condition == null) {
            func.addInst(new JInst(irStmt.trueLabel));
        } else {
            resolveRegister(irStmt.condition, "t0", func);
            // bnez t0, if_true  # 如果 t0 不为 0，跳转到 if_true
            // j if_false        # 否则，跳转到 if_false
            func.addInst(new BranchIfInst("bnez", "t0", null, irStmt.trueLabel));
            func.addInst(new JInst(irStmt.falseLabel));
        }
    }

    void visitSelectStmt(SelectStmt irStmt, ASMFunction func) {
        resolveRegister(irStmt.cond, "t0", func);
        resolveRegister(irStmt.trueBranch, "t1", func);
        resolveRegister(irStmt.falseBranch, "t2", func);
        // # 首先，根据条件构造一个全为0或全为1的掩码
        //sltu t3, x0, t0    # t3 = (t0 != 0) ? 1 : 0，t3现在是1或0
        //neg t3, t3         # t3 = -t3，如果t3是1，则t3=-1（即全1的掩码）；否则是0
        //# 用掩码选择true_val或false_val
        //and t4, t1, t3     # t4 = true_val & t3，如果t3全为1，则t4 = true_val；否则t4 = 0
        //not t3, t3         # 反转t3，即t3 = ~t3
        //and t5, t2, t3     # t5 = false_val & ~t3，如果t3全为0，则t5 = false_val；否则t5 = 0
        //# 最后将选择的值合并
        //or t3, t4, t5      # t3 = t4 | t5，得到最终的选择结果
        func.addInst(new ArithInst("<u", "t3", "x0", "t0"));
        func.addInst(new UnaryRegInst("neg", "t3", "t3"));

        func.addInst(new ArithInst("&", "t4", "t1", "t3"));
        func.addInst(new UnaryRegInst("not", "t3", "t3"));
        func.addInst(new ArithInst("&", "t5", "t2", "t3"));

        func.addInst(new ArithInst("|", "t3", "t4", "t5"));

        int offset = func.getVirtualReg(irStmt.dest);
        func.addInst(Sw("t3", offset, "sp"));
    }

    void resolveRegister(String regName, String destReg, ASMFunction func) {
        if (regName.startsWith("%")) {
            int offset1 = func.getVirtualReg(regName);
            func.addInst(Lw(destReg, offset1, "sp"));
        } else if (regName.startsWith("@")) {
            func.addInst(new LaInst(destReg, regName.substring(1)));
            func.addInst(Lw(destReg, 0, destReg));
        } else {
            int val;
            if (regName.equals("true")) val = 1;
            else if (regName.equals("false")) val = 0;
            else if (regName.equals("null")) val = 0;
            else val = Integer.parseInt(regName);
            func.addInst(new LiInst(destReg, val));
        }
    }

    void visitBinaryExprStmt(BinaryExprStmt irStmt, ASMFunction func) {
        resolveRegister(irStmt.register1, "t1", func);
        resolveRegister(irStmt.register2, "t2", func);
        switch (irStmt.operator) {
            case "add":
                func.addInst(new ArithInst("+", "t0", "t1", "t2"));
                break;
            case "sub":
                func.addInst(new ArithInst("-", "t0", "t1", "t2"));
                break;
            case "mul":
                func.addInst(new ArithInst("*", "t0", "t1", "t2"));
                break;
            case "sdiv":
                func.addInst(new ArithInst("/", "t0", "t1", "t2"));
                break;
            case "srem":
                func.addInst(new ArithInst("%", "t0", "t1", "t2"));
                break;
            case "and":
                func.addInst(new ArithInst("&", "t0", "t1", "t2"));
                break;
            case "or":
                func.addInst(new ArithInst("|", "t0", "t1", "t2"));
                break;
            case "xor":
                func.addInst(new ArithInst("^", "t0", "t1", "t2"));
                break;
            case "shl":
                func.addInst(new ArithInst("<<", "t0", "t1", "t2"));
                break;
            case "ashr":
                func.addInst(new ArithInst(">>", "t0", "t1", "t2"));
                break;
            case "icmp eq":
                // xor t3, t1, t2     # t3 = t1 ^ t2，如果相等则 t3 = 0，否则 t3 !=0
                // sltiu t0, t3, 1    # 如果 t3 < 1 (t3 == 0)，则 t0 = 1，否则 t0 = 0
                func.addInst(new ArithInst("^", "t3", "t1", "t2"));
                func.addInst(new ArithImmInst("<u", "t0", "t3", 1));
                break;
            case "icmp ne":
                func.addInst(new ArithInst("^", "t3", "t1", "t2"));
                func.addInst(new ArithImmInst("<u", "t3", "t3", 1));
                func.addInst(new ArithImmInst("^", "t0", "t3", 1));
                break;
            case "icmp slt": // <
                func.addInst(new ArithInst("<", "t0", "t1", "t2"));
                break;
            case "icmp sgt": // >
                func.addInst(new ArithInst("<", "t0", "t2", "t1"));
                break;
            case "icmp sle": // <=
                func.addInst(new ArithInst("<", "t0", "t2", "t1"));
                func.addInst(new ArithImmInst("^", "t0", "t0", 1));
                break;
            case "icmp sge": // >=
                func.addInst(new ArithInst("<", "t0", "t1", "t2"));
                func.addInst(new ArithImmInst("^", "t0", "t0", 1));
                break;
            default:
                throw new RuntimeException("unknown operator at BinaryStmt: " + irStmt.operator);
        }
        int offset = func.getVirtualReg(irStmt.dest);
        func.addInst(Sw("t0", offset, "sp"));
    }

    void visitUnaryExprStmt(UnaryExprStmt irStmt, ASMFunction func) {
        resolveRegister(irStmt.register, "t1", func);
        switch (irStmt.operator) {
            case "!":
                func.addInst(new ArithImmInst("xor", "t0", "t1", 1));
                break;
            case "~":
                func.addInst(new ArithImmInst("xor", "t0", "t1", -1));
                break;
            case "-":
                func.addInst(new UnaryRegInst("neg", "t0", "t1"));
                break;
            case "++":
                func.addInst(new ArithImmInst("+", "t0", "t1", 1));
                break;
            case "--":
                func.addInst(new ArithImmInst("+", "t0", "t1", -1));
                break;
            default:
                throw new RuntimeException("unknown operator at UnaryStmt: " + irStmt.operator);
        }
        int offset = func.getVirtualReg(irStmt.dest);
        func.addInst(Sw("t0", offset, "sp"));
    }

    void visitStoreStmt(StoreStmt irStmt, ASMFunction func) {
        resolveRegister(irStmt.val, "t0", func);
        resolveRegister(irStmt.dest, "t1", func);
        func.addInst(new SwInst("t0", 0, "t1"));
    }

    void visitLoadStmt(LoadStmt irStmt, ASMFunction func) {
        resolveRegister(irStmt.pointer, "t0", func);
        func.addInst(new LwInst("t1", 0, "t0")); // load t1, 0(t0)
        int offset = func.getVirtualReg(irStmt.dest);
        func.addInst(Sw("t1", offset, "sp")); // store t1, offset(sp)
    }

    void visitReturnStmt(ReturnStmt irStmt, ASMFunction func) {
        if (irStmt.src != null) {
            resolveRegister(irStmt.src, "a0", func);
        }
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
        for (int i = 0; i < irStmt.args.size(); i++) {
            if (i < 8) {
                resolveRegister(irStmt.args.get(i), "a" + i, func);
            } else {
                int offset = func.getVirtualReg(irStmt.args.get(i));
                func.addInst(Sw("a" + i, offset, "sp"));
            }
        }

        func.addInst(new CallInst(irStmt.funcName));

        // sp += extraSpOffset
        // restore a0~a7
        // get a0
        if (!irStmt.retType.typeName.equals("void")) {
            int offset = func.getVirtualReg(irStmt.dest);
            func.addInst(Sw("a0", offset, "sp"));
        }
    }

    public void visitProgram() {
        buildStringConst();

        buildGlobalVariables();

        for (IRStmt irStmt: irBuilder.irStmts)
            if (irStmt instanceof FunctionImplementStmt irFunction) {
                buildFunctions(irFunction);
            }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(dataSection.toString());
        sb.append(rodataSection.toString());
        sb.append(textSection.toString());
        return sb.toString();
    }
}