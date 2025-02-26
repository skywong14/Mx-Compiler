package optimize.optimizations;

import IR.IRStmts.*;
import optimize.IRBlock;
import optimize.IRCode;
import optimize.IRFunction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class FunctionInline {
    // const parameter: max_inline_depth, max_inline_length
    IRCode irCode = null;
    static final int max_inline_depth = 2, max_inline_length = 20, max_parameter_length = 4;

    ArrayList<IRBlock> newBlocks;
    IRBlock curBlock;
    String beginBlockLabel;

    class PhiModify {
        String fromLabel, toLabel, newName;
        public PhiModify(String fromLabel, String toLabel, String newName) {
            this.fromLabel = fromLabel;
            this.toLabel = toLabel;
            this.newName = newName;
        }
    }
    ArrayList<PhiModify> phiModifyTable;

    static void debug(String str) {
        System.out.println("# [Inline]: " + str);
    }

    HashMap<String, IRFunction> funcMap = null;
    HashSet<String> inlineMap = null;

    boolean shouldInline(IRFunction func) {
        int size = 0;
        if (func.argNames.size() > max_parameter_length)
            return false; // too many parameters
        for (IRBlock block : func.blocks) {
            size += block.stmts.size();
            for (IRStmt stmt : block.stmts) {
                if (stmt instanceof CallStmt callStmt
                    && callStmt.funcName.equals(func.name))
                    return false; // recursive function
            }
        }
        if (size > max_inline_length)
            return false; // too long
        return true;
    }

    String renameReg(String name, HashMap<String, String> argMap, String inlineHead) {
        if (name == null) return null;
        if (name.startsWith("%")) {
            if (!argMap.containsKey(name))
                argMap.put(name, "%" + inlineHead + name.substring(1));
            return argMap.get(name);
        } else {
            return name;
        }
    }

    String renameLabel(String label, String funcName, String inlineHead) {
        if (label.equals(funcName)) return beginBlockLabel;
        return inlineHead + label;
    }

    IRStmt replaceName(IRStmt stmt, HashMap<String, String> argMap, String ilHead, String funcName) {
        // Label Name & RegName
        if (stmt instanceof PhiStmt phi) {
            PhiStmt newPhi = new PhiStmt(renameReg(phi.dest, argMap, ilHead), phi.type);
            newPhi.setDest(renameReg(phi.dest, argMap, ilHead));
            for (String key: phi.val.keySet()) {
                debug("{replaceName}: " + key + " -> " + renameLabel(key, funcName, ilHead) + ", in " + phi.toString() );
                newPhi.addVal(renameReg(phi.val.get(key), argMap, ilHead), renameLabel(key, funcName, ilHead));
            }
            return newPhi;
        } else if (stmt instanceof StoreStmt store) {
            return new StoreStmt(store.type, renameReg(store.val, argMap, ilHead), renameReg(store.dest, argMap, ilHead));
        } else if (stmt instanceof MoveStmt move) {
            return new MoveStmt(renameReg(move.dest, argMap, ilHead), renameReg(move.src, argMap, ilHead));
        } else if (stmt instanceof LoadStmt load) {
            return new LoadStmt(load.type, renameReg(load.pointer, argMap, ilHead), renameReg(load.dest, argMap, ilHead));
        } else if (stmt instanceof ReturnStmt) {
            throw new RuntimeException("replaceName: ReturnStmt should not appear here");
        } else if (stmt instanceof CallStmt call) {
            CallStmt newCall = new CallStmt(call.retType, call.funcName, call.argTypes, new ArrayList<>(), renameReg(call.dest, argMap, ilHead));
            for (String arg: call.args)
                newCall.args.add(renameReg(arg, argMap, ilHead));
            return newCall;
        } else if (stmt instanceof BinaryExprStmt binExpr) {
            return new BinaryExprStmt(binExpr.operator, binExpr.type,
                    renameReg(binExpr.register1, argMap, ilHead), renameReg(binExpr.register2, argMap, ilHead),
                    renameReg(binExpr.dest, argMap, ilHead));
        } else if (stmt instanceof GetElementPtrStmt eleStmt) {
            return new GetElementPtrStmt(eleStmt.type, renameReg(eleStmt.pointer, argMap, ilHead), renameReg(eleStmt.index, argMap, ilHead),
                    renameReg(eleStmt.dest, argMap, ilHead), eleStmt.hasZero);
        } else if (stmt instanceof SelectStmt select) {
            return new SelectStmt(select.type, renameReg(select.cond, argMap, ilHead),
                    renameReg(select.trueVal, argMap, ilHead), renameReg(select.falseVal, argMap, ilHead), renameReg(select.dest, argMap, ilHead));
        } else if (stmt instanceof BranchStmt branch) {
            if (branch.condition != null) {
                return new BranchStmt(renameReg(branch.condition, argMap, ilHead), renameLabel(branch.trueLabel, funcName, ilHead), renameLabel(branch.falseLabel, funcName, ilHead));
            } else {
                return new BranchStmt(renameLabel(branch.trueLabel, funcName, ilHead));
            }
        } else if (stmt instanceof UnaryExprStmt unaryExpr) {
            return new UnaryExprStmt(unaryExpr.operator, unaryExpr.type, renameReg(unaryExpr.register, argMap, ilHead), renameReg(unaryExpr.dest, argMap, ilHead));
        } else
            throw new RuntimeException("IRFunction: clearDeadStmt: unknown stmt");
    }

    void inlineBody(CallStmt callStmt, IRFunction func, int inlineCnt, String prefixName, String funcName) {
        HashMap<String, String> argMap = new HashMap<>();
        HashMap<String, String> result = new HashMap<>(); // block label -> return value
        String inlineHead = "." + prefixName + inlineCnt + funcName + "_";
        String endBlock = prefixName + inlineCnt + funcName + ".phi";
        beginBlockLabel = curBlock.label;

        // need_phi (has more than one return stmt)
        int ret_cnt = 0;
        if (!func.returnType.typeName.equals("void")) {
            for (IRBlock block: func.blocks)
                for (IRStmt stmt: block.stmts)
                    if (stmt instanceof ReturnStmt)
                        ret_cnt++;
        }

        // put arguments into argMap
        for (int i = 0; i < callStmt.args.size(); i++)
            argMap.put("%" + func.argNames.get(i), callStmt.args.get(i));

        // clone the function body
        for (int i = 0; i < func.blocks.size(); i++) {
            if (i != 0) {
                newBlocks.add(curBlock);
                curBlock = new IRBlock(inlineHead +  func.blocks.get(i).label);
            }
            for (IRStmt stmt : func.blocks.get(i).stmts) {
                if (stmt instanceof ReturnStmt ret) {
                    if (!func.returnType.typeName.equals("void")) result.put(curBlock.label, renameReg(ret.src, argMap, inlineHead)); // save return value
                    if (ret_cnt > 1) curBlock.stmts.add(new BranchStmt(endBlock)); // add branch to endBlock
                } else {
                    curBlock.stmts.add(replaceName(stmt, argMap, inlineHead, func.name));
                }
            }

        }

        // epilogue
        if (ret_cnt > 1) {
            // add endBlock
            newBlocks.add(curBlock);
            curBlock = new IRBlock(endBlock);
        }

        if (result.isEmpty()) {
            // no return value, doing nothing
        } else if (result.size() == 1) {
            // only one return value, mv stmt
            curBlock.stmts.add(new MoveStmt(callStmt.dest, result.values().iterator().next()));
        } else {
            // multiple return values, phi stmt
            PhiStmt phiStmt = new PhiStmt(callStmt.dest, callStmt.retType.typeName);
            phiStmt.setDest(callStmt.dest);
            for (String key: result.keySet())
                phiStmt.addVal(result.get(key), key);
            curBlock.stmts.add(phiStmt);
        }
    }

    IRStmt copyStmt(IRStmt stmt) {
        if (stmt instanceof PhiStmt phi) {
            PhiStmt newPhi = new PhiStmt(phi.dest, phi.type);
            newPhi.setDest(phi.dest);
            for (String key: phi.val.keySet())
                newPhi.addVal(phi.val.get(key), key);
            return newPhi;
        } else if (stmt instanceof StoreStmt store) {
            return new StoreStmt(store.type, store.val, store.dest);
        } else if (stmt instanceof MoveStmt move) {
            return new MoveStmt(move.dest, move.src);
        } else if (stmt instanceof LoadStmt load) {
            return new LoadStmt(load.type, load.pointer, load.dest);
        } else if (stmt instanceof ReturnStmt ret) {
            return new ReturnStmt(ret.type, ret.src);
        } else if (stmt instanceof CallStmt call) {
            CallStmt newCall = new CallStmt(call.retType, call.funcName, call.argTypes, new ArrayList<>(), call.dest);
            for (String arg: call.args)
                newCall.args.add(arg);
            return newCall;
        } else if (stmt instanceof BinaryExprStmt binExpr) {
            return new BinaryExprStmt(binExpr.operator, binExpr.type, binExpr.register1, binExpr.register2, binExpr.dest);
        } else if (stmt instanceof GetElementPtrStmt eleStmt) {
            return new GetElementPtrStmt(eleStmt.type, eleStmt.pointer, eleStmt.index, eleStmt.dest, eleStmt.hasZero);
        } else if (stmt instanceof SelectStmt select) {
            return new SelectStmt(select.type, select.cond, select.trueVal, select.falseVal, select.dest);
        } else if (stmt instanceof BranchStmt branch) {
            if (branch.condition != null) {
                return new BranchStmt(branch.condition, branch.trueLabel, branch.falseLabel);
            } else {
                return new BranchStmt(branch.trueLabel);
            }
        } else if (stmt instanceof UnaryExprStmt unaryExpr) {
            return new UnaryExprStmt(unaryExpr.operator, unaryExpr.type, unaryExpr.register, unaryExpr.dest);
        } else
            throw new RuntimeException("IRFunction: clearDeadStmt: unknown stmt");
    }

    ArrayList<IRBlock> inlineFunction(IRFunction func,String prefixName) {
        int inlineCnt = 0;
        newBlocks = new ArrayList<>();
        HashSet<String> oldBlocks = new HashSet<>();
        curBlock = null;
        phiModifyTable = new ArrayList<>();

        for (IRBlock originBlock: func.blocks) {
            oldBlocks.add(originBlock.label);
            curBlock = new IRBlock(originBlock.label);
            for (IRStmt stmt: originBlock.stmts)
                if (stmt instanceof CallStmt callStmt
                        && inlineMap.contains(callStmt.funcName)) {
                    // inline
                    inlineBody(callStmt, funcMap.get(callStmt.funcName), inlineCnt++, prefixName, func.name);
                } else {
                    curBlock.stmts.add(copyStmt(stmt));
                }
            newBlocks.add(curBlock);

            if (!curBlock.label.equals(originBlock.label)) {
                // if this block ends with a branch, may need to modify PhiStmt at the branch target
                if (curBlock.stmts.get(curBlock.stmts.size() - 1) instanceof BranchStmt branchStmt) {
                    phiModifyTable.add(new PhiModify(originBlock.label, branchStmt.trueLabel, curBlock.label));
                    if (branchStmt.falseLabel != null)
                        phiModifyTable.add(new PhiModify(originBlock.label, branchStmt.falseLabel, curBlock.label));
                }
            }
        }
        newBlocks.get(0).isHead = true; // for regAlloc: if (block.isHead) add funcHead

        // update phi stmts
        // phiModifyTable (fromLabel(old name), toLabel, fromLabel(new name))
        HashMap<String, IRBlock> tmpBlockMap = new HashMap<>();
        for (IRBlock block: newBlocks)
            tmpBlockMap.put(block.label, block);
        for (PhiModify phiModify: phiModifyTable) {
            IRBlock targetBlock = tmpBlockMap.get(phiModify.toLabel);
            for (IRStmt stmt: targetBlock.stmts)
                if (stmt instanceof PhiStmt phiStmt) {
                    if (phiStmt.val.containsKey(phiModify.fromLabel)) {
                        String tmp = phiStmt.val.get(phiModify.fromLabel);
                        phiStmt.val.remove(phiModify.fromLabel);
                        phiStmt.val.put(phiModify.newName, tmp);
                    }
                } else break;
        }

        return newBlocks;
    }

    public void optimize(IRCode irCode_) {
        // initialize
        irCode = irCode_;
        ArrayList<ArrayList<IRBlock>> funcBodys;
        int inline_depth = 0;
        String prefixName = "IL";
        // inline
        while (inline_depth < max_inline_depth) {
            inline_depth++;

            funcMap = new HashMap<>();
            inlineMap = new HashSet<>();
            for (IRFunction func : irCode.funcStmts) {
                funcMap.put(func.name, func);
                if (shouldInline(func))
                    inlineMap.add(func.name);
            }
            funcBodys = new ArrayList<>();

            for (IRFunction func: irCode.funcStmts)
                funcBodys.add(inlineFunction(func, prefixName));

            for (int i = 0; i < irCode.funcStmts.size(); i++){
                IRFunction func = irCode.funcStmts.get(i);
                func.blocks = funcBodys.get(i);
                func.updateBlockMap();
                func.updatePredAndSucc();
            }
            prefixName = "I" + prefixName;
        }
    }
}
