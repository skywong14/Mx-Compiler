package optimize.optimizations;

import IR.IRStmts.*;
import optimize.IRBlock;
import optimize.IRCode;
import optimize.IRFunction;
import optimize.utils.ValueSimplify;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

// SCCP
public class ConstantPropagation {
    ValueSimplify simplify = new ValueSimplify();
    // Lattice
    enum LatticeValue {
        BOTTOM,  // ⊥
        CONSTANT, // c
        TOP      // ⊤
    }
    class LatticeCell {
        LatticeValue type;
        int value;
        LatticeCell() {
            type = LatticeValue.TOP;
            value = 0;
        }
        LatticeCell(LatticeValue type) {
            this.type = type;
            this.value = 0;
        }
        LatticeCell(LatticeValue type, int value) {
            this.type = type;
            this.value = value;
        }
    }
    LatticeValue meet(LatticeValue a, LatticeValue b) {
        if (a == LatticeValue.BOTTOM || b == LatticeValue.BOTTOM) return LatticeValue.BOTTOM;
        if (a == LatticeValue.TOP) return b;
        if (b == LatticeValue.TOP) return a;
        if (a == b) return a;
        return LatticeValue.TOP;
    }

    class cfgEdge {
        IRBlock from, to;
        cfgEdge(IRBlock from, IRBlock to) {
            this.from = from;
            this.to = to;
        }
    }
    class ssaEdge {
        public String name;
        public IRStmt def;
        public ArrayList<IRStmt> uses;
        ssaEdge(String name, IRStmt def, ArrayList<IRStmt> uses) {
            this.name = name;
            this.def = def;
            this.uses = uses;
        }
    }

    HashMap<String, LatticeCell> latticeMap;
    HashSet<cfgEdge> flowWL;
    HashSet<ssaEdge> ssaWL;
    HashMap<IRStmt, Boolean> execFlags;
    HashMap<String, Boolean> blockExec; // 标记 block 是否可达
    HashMap<String, IRBlock> blockMap;
    HashMap<IRStmt, IRBlock> belongedBlock;
    HashMap<String, ArrayList<IRStmt>> useMap;
    HashMap<String, HashSet<String>> jumpMap; // key: dest, val: src
    HashMap<String, HashSet<String>> falseJumpMap;

    void addUse(String name, IRStmt stmt) {
        if (name == null || !name.startsWith("%")) return;
        if (!useMap.containsKey(name))
            useMap.put(name, new ArrayList<>());
        useMap.get(name).add(stmt);
    }

    void collectUse(IRStmt stmt) {
        if (stmt instanceof PhiStmt phi) {
            for (String blockLabel : phi.val.keySet()) {
                addUse(phi.val.get(blockLabel), phi);
            }
        } else if (stmt instanceof BinaryExprStmt expr) {
            addUse(expr.register1, expr);
            addUse(expr.register2, expr);
        } else if (stmt instanceof UnaryExprStmt expr) {
            addUse(expr.register, expr);
        } else if (stmt instanceof CallStmt call) {
            for (String arg : call.args)
                addUse(arg, call);
        } else if (stmt instanceof MoveStmt move) {
            addUse(move.src, move);
        } else if (stmt instanceof LoadStmt load) {
            addUse(load.pointer, load);
        } else if (stmt instanceof StoreStmt store) {
            addUse(store.val, store);
            addUse(store.dest, store);
        } else if (stmt instanceof ReturnStmt ret) {
            if (ret.src != null) {
                addUse(ret.src, ret);
            }
        } else if (stmt instanceof GetElementPtrStmt expr) {
            addUse(expr.pointer, expr);
            addUse(expr.index, expr);
        } else if (stmt instanceof BranchStmt branch) {
            if (branch.condition != null)
                addUse(branch.condition, branch);
        } else if (stmt instanceof SelectStmt select) {
            addUse(select.cond, select);
            addUse(select.trueVal, select);
            addUse(select.falseVal, select);
        } else
            throw new RuntimeException("[SCCP] Unknown stmt: " + stmt);
    }

    void initialize(IRFunction func) {
        // init all lattice values to TOP
        latticeMap = new HashMap<>();
        blockExec = new HashMap<>();
        blockMap = new HashMap<>();
        belongedBlock = new HashMap<>();
        useMap = new HashMap<>();
        jumpMap = new HashMap<>();
        falseJumpMap = new HashMap<>();
        execFlags = new HashMap<>();
        // in funcHead
        for (String arg : func.argNames) {
            latticeMap.put("%" + arg, new LatticeCell(LatticeValue.BOTTOM));
        }
        // in body, init execFlags
        for (IRBlock block : func.blocks) {
            jumpMap.put(block.label, new HashSet<>());
            falseJumpMap.put(block.label, new HashSet<>());
            blockExec.put(block.label, false);
            blockMap.put(block.label, block);
            for (IRStmt stmt : block.stmts) {
                belongedBlock.put(stmt, block);
                if (stmt.getDest() != null) {
                    if (stmt.getDest().startsWith("@")) {
                        latticeMap.put(stmt.getDest(), new LatticeCell(LatticeValue.BOTTOM));
                        throw new RuntimeException("[SCCP] Global variable not supported: " + stmt);
                    } else {
                        if (!stmt.getDest().startsWith("%")) throw new RuntimeException("[SCCP] Global variable not supported: " + stmt);
                        latticeMap.put(stmt.getDest(), new LatticeCell());
                    }
                }
                // collect uses
                collectUse(stmt);
                execFlags.put(stmt, false);
            }
        }
        // init flowWL
        flowWL = new HashSet<>();
        IRBlock entry = func.blocks.get(0);
        flowWL.add(new cfgEdge(null, entry));
        // init ssaWL
        ssaWL = new HashSet<>();
    }

    void optimizeInFunc(IRFunction func) {
        initialize(func);
        while (!flowWL.isEmpty() || !ssaWL.isEmpty()) {
            if (!flowWL.isEmpty()) {
                // work on flowWL
                cfgEdge curEdge = flowWL.iterator().next();
                flowWL.remove(curEdge);

                IRBlock toBlock = curEdge.to;
                if (!blockExec.get(toBlock.label)) { // 如果 block 不可达，标记为可达并处理 block 内语句
                    blockExec.put(toBlock.label, true);
                    for (IRStmt stmt : toBlock.stmts) {
                        execFlags.put(stmt, true); // 标记语句为可执行
                        visitStmt(stmt);
                    }
                }
            } else {
                // work on ssaWL
                ssaEdge curEdge = ssaWL.iterator().next();
                ssaWL.remove(curEdge);

                // 重新处理受影响的 use 语句
                for (IRStmt useStmt : curEdge.uses) {
                    if (execFlags.get(useStmt)) { // 只处理可执行的语句
                        visitStmt(useStmt);
                    }
                }
            }
        }

        // print all lattice values
        // for (String key : latticeMap.keySet())
        //     debug("in {" + func.name + "} " + key + ": " + typeName(latticeMap.get(key).type) + " " + latticeMap.get(key).value);

        performConstantReplacement(func);
    }

    IRStmt replaceConstant(IRStmt stmt) {
        if (stmt instanceof BranchStmt branch) {
            if (branch.condition != null) {
                LatticeCell cond = getLatticeMap(branch.condition);
                if (cond.type == LatticeValue.CONSTANT) {
                    if (cond.value == 1) {
                        return new BranchStmt(null, branch.trueLabel, null);
                    } else {
                        return new BranchStmt(null, branch.falseLabel, null);
                    }
                }
            }
            return branch;
        } else if (stmt instanceof PhiStmt phi) {
            HashMap<String, String> newVals = new HashMap<>();
            HashSet<String> preSet = jumpMap.get(belongedBlock.get(phi).label);
            // debug("phi: " + phi + ", belonged:" + belongedBlock.get(phi).label + "  preSet: " + preSet);
            for (String blockLabel : phi.val.keySet()){
                if (preSet.contains(blockLabel)) {
                    LatticeCell val = getLatticeMap(phi.val.get(blockLabel));
                    if (val.type == LatticeValue.CONSTANT)
                        newVals.put(blockLabel, String.valueOf(val.value));
                    else
                        newVals.put(blockLabel, phi.val.get(blockLabel));
                }
            }
            if (newVals.isEmpty()) {
                // unreachable phi, replace with a move
                return new MoveStmt(phi.dest, "0");
            }
            if (newVals.size() == 1) {
                return new MoveStmt(phi.dest, newVals.values().iterator().next());
            }
            phi.val = newVals;
            return phi;
        } else if (stmt instanceof BinaryExprStmt expr) {
            LatticeCell dest = getLatticeMap(expr.dest);
            if (dest.type == LatticeValue.CONSTANT) {
                return new MoveStmt(expr.dest, String.valueOf(dest.value));
            } else {
                LatticeCell op1 = getLatticeMap(expr.register1);
                LatticeCell op2 = getLatticeMap(expr.register2);
                if (op1.type == LatticeValue.CONSTANT) expr.register1 = String.valueOf(op1.value);
                if (op2.type == LatticeValue.CONSTANT) expr.register2 = String.valueOf(op2.value);
                return expr;
            }
        } else if (stmt instanceof UnaryExprStmt expr) {
            LatticeCell dest = getLatticeMap(expr.dest);
            if (dest.type == LatticeValue.CONSTANT) {
                return new MoveStmt(expr.dest, String.valueOf(dest.value));
            } else
                return expr;
        } else if (stmt instanceof CallStmt call) {
            for (int i = 0; i < call.args.size(); i++) {
                LatticeCell arg = getLatticeMap(call.args.get(i));
                if (arg.type == LatticeValue.CONSTANT)
                    call.args.set(i, String.valueOf(arg.value));
            }
            return call;
        } else if (stmt instanceof MoveStmt move) {
            LatticeCell dest = getLatticeMap(move.dest);
            if (dest.type == LatticeValue.CONSTANT) {
                return new MoveStmt(move.dest, String.valueOf(dest.value));
            } else
                return move;
        } else if (stmt instanceof LoadStmt load) {
            LatticeCell ptr = getLatticeMap(load.pointer);
            if (ptr.type == LatticeValue.CONSTANT)
                load.pointer = String.valueOf(load.pointer);
            return load;
        } else if (stmt instanceof StoreStmt store) {
            LatticeCell val = getLatticeMap(store.val);
            if (val.type == LatticeValue.CONSTANT)
                store.val = String.valueOf(val.value);
            return store;
        } else if (stmt instanceof ReturnStmt ret) {
            if (ret.src != null) {
                LatticeCell src = getLatticeMap(ret.src);
                if (src.type == LatticeValue.CONSTANT)
                    ret.src = String.valueOf(src.value);
            }
            return ret;
        } else if (stmt instanceof GetElementPtrStmt expr) {
            LatticeCell ptr = getLatticeMap(expr.pointer);
            LatticeCell index = getLatticeMap(expr.index);
            if (ptr.type == LatticeValue.CONSTANT)
                expr.pointer = String.valueOf(ptr.value);
            if (index.type == LatticeValue.CONSTANT)
                expr.index = String.valueOf(index.value);
            return expr;
        } else if (stmt instanceof SelectStmt select) {
            LatticeCell cond = getLatticeMap(select.cond);
            LatticeCell trueVal = getLatticeMap(select.trueVal);
            LatticeCell falseVal = getLatticeMap(select.falseVal);
            if (trueVal.type == LatticeValue.CONSTANT) select.trueVal = String.valueOf(trueVal.value);
            if (falseVal.type == LatticeValue.CONSTANT) select.falseVal = String.valueOf(falseVal.value);
            if (cond.type == LatticeValue.CONSTANT) {
                if (cond.value == 1)
                    return new MoveStmt(select.dest, select.trueVal);
                else
                    return new MoveStmt(select.dest, select.falseVal);
            }
            return select;
        } else
            throw new RuntimeException("[SCCP] Unknown stmt: " + stmt);
    }

    void performConstantReplacement(IRFunction func) {
        for (IRBlock block : func.blocks) {
            block.stmts.replaceAll(this::replaceConstant);
        }
    }
    
    LatticeCell getLatticeMap(String reg) {
        if (reg.startsWith("%")) {
            return latticeMap.get(reg);
        } else if (reg.startsWith("@")) {
            return new LatticeCell(LatticeValue.BOTTOM);
        } else {
            return new LatticeCell(LatticeValue.CONSTANT, simplify.resolveValue(reg));
        }
    }
    
    void visitBranchStmt(BranchStmt branch) {
        if (branch.condition == null) {
            flowWL.add(new cfgEdge(belongedBlock.get(branch), blockMap.get(branch.trueLabel)));
            jumpMap.get(branch.trueLabel).add(belongedBlock.get(branch).label);
        return;
        }

        LatticeCell cond = getLatticeMap(branch.condition);
        IRBlock curBlock = belongedBlock.get(branch);
        if (cond.type == LatticeValue.CONSTANT) {
            if (cond.value == 1) {
                flowWL.add(new cfgEdge(curBlock, blockMap.get(branch.trueLabel)));
                jumpMap.get(branch.trueLabel).add(curBlock.label);
                jumpMap.get(branch.falseLabel).remove(curBlock.label);
                falseJumpMap.get(branch.falseLabel).add(curBlock.label);
            } else {
                flowWL.add(new cfgEdge(curBlock, blockMap.get(branch.falseLabel)));
                jumpMap.get(branch.falseLabel).add(curBlock.label);
                jumpMap.get(branch.trueLabel).remove(curBlock.label);
                falseJumpMap.get(branch.trueLabel).add(curBlock.label);
            }
        } else if (cond.type == LatticeValue.BOTTOM) {
            flowWL.add(new cfgEdge(curBlock, blockMap.get(branch.trueLabel)));
            flowWL.add(new cfgEdge(curBlock, blockMap.get(branch.falseLabel)));
            jumpMap.get(branch.trueLabel).add(curBlock.label);
            jumpMap.get(branch.falseLabel).add(curBlock.label);
        } else {
            flowWL.add(new cfgEdge(curBlock, blockMap.get(branch.trueLabel)));
            flowWL.add(new cfgEdge(curBlock, blockMap.get(branch.falseLabel)));
            jumpMap.get(branch.trueLabel).add(curBlock.label);
            jumpMap.get(branch.falseLabel).add(curBlock.label);
        }
        //ATTENTION: multiple cfg edges in flowWL
    }

    void updateSSAWL(String destVar, IRStmt stmt, LatticeCell newLat) {
        LatticeCell oldLat = getLatticeMap(destVar);

        if (oldLat.type != newLat.type) {
            latticeMap.put(destVar, newLat);
            if (!useMap.containsKey(destVar)) {
                useMap.put(destVar, new ArrayList<>());
            }
            ssaWL.add(new ssaEdge(destVar, stmt, useMap.get(destVar)));
        }
    }

    void visitPhiStmt(PhiStmt phi) {
        // LatticeCell newLat = new LatticeCell();
        LatticeCell meetLat = new LatticeCell();
        boolean hasPre = true;
        HashSet<String> preSet = jumpMap.get(belongedBlock.get(phi).label);
        HashSet<String> falsePreSet = falseJumpMap.get(belongedBlock.get(phi).label);
        for (String blockLabel : phi.val.keySet())
            if (preSet.contains(blockLabel)) { // consider only executable predecessor blocks
                LatticeCell valLat = getLatticeMap(phi.val.get(blockLabel));

                if (valLat.type == LatticeValue.BOTTOM) {
                    meetLat.type = LatticeValue.BOTTOM;
                    break; // if meet becomes BOTTOM, no need to continue
                } else if (valLat.type == LatticeValue.CONSTANT) {
                    if (meetLat.type == LatticeValue.CONSTANT) {
                        if (meetLat.value != valLat.value){
                            meetLat.type = LatticeValue.BOTTOM;
                            break;
                        }
                        // else maintain CONSTANT
                    } else {
                        // meet.type == TOP
                        // meet undecided, take the first constant value
                        if (hasPre) {
                            // the first constant value
                            meetLat.type = LatticeValue.CONSTANT;
                            meetLat.value = valLat.value;
                        } else {
                            // doing nothing
                        }
                    }
                } else {
                    // val is TOP
                    if (meetLat.type == LatticeValue.CONSTANT) {
                        meetLat.type = LatticeValue.TOP;
                    } // else maintain TOP
                }
                hasPre = false;
            } else if (!falsePreSet.contains(blockLabel)) {
                // ATTENTION: undecided predecessor, set meet to Bottom
                meetLat.type = LatticeValue.BOTTOM;
                break;
            }

        updateSSAWL(phi.dest, phi, meetLat);
    }

    void visitBinaryExprStmt(BinaryExprStmt expr) {
        // update dest, ATTENTION: sdiv 0
        LatticeCell newLat = new LatticeCell();
        LatticeCell op1 = getLatticeMap(expr.register1);
        LatticeCell op2 = getLatticeMap(expr.register2);
        if (op1.type == LatticeValue.CONSTANT && op2.type == LatticeValue.CONSTANT) {
            if ((expr.operator.equals("sdiv") || expr.operator.equals("srem")) && op2.value == 0) {
                newLat.type = LatticeValue.TOP;
            } else {
                newLat.type = LatticeValue.CONSTANT;
                newLat.value = simplify.simplifyBinaryStmt(expr.operator, op1.value, op2.value);
            }
        } else {
            newLat.type = meet(op1.type, op2.type);
        }
        updateSSAWL(expr.dest, expr, newLat);
    }

    void visitUnaryExprStmt(UnaryExprStmt expr) {
        LatticeCell newLat = new LatticeCell();
        LatticeCell op = getLatticeMap(expr.register);
        if (op.type == LatticeValue.CONSTANT) {
            newLat.type = LatticeValue.CONSTANT;
            newLat.value = simplify.simplifyUnaryStmt(expr.operator, op.value);
        } else {
            newLat.type = op.type;
        }
        updateSSAWL(expr.dest, expr, newLat);
    }

    void visitMoveStmt(MoveStmt stmt) {
        LatticeCell newLat = getLatticeMap(stmt.src);
        updateSSAWL(stmt.dest, stmt, newLat);
    }

    void visitSelectStmt(SelectStmt stmt) {
        LatticeCell newLat = new LatticeCell();
        LatticeCell cond = getLatticeMap(stmt.cond);
        LatticeCell trueVal = getLatticeMap(stmt.trueVal);
        LatticeCell falseVal = getLatticeMap(stmt.falseVal);
        if (cond.type == LatticeValue.CONSTANT) {
            if (cond.value == 1) {
                newLat = trueVal;
            } else {
                newLat = falseVal;
            }
        } else {
            newLat.type = meet(trueVal.type, falseVal.type);
        }
        updateSSAWL(stmt.dest, stmt, newLat);
    }

    void visitBottomStmt(IRStmt stmt) {
        if (stmt.getDest() != null) {
            LatticeCell newLat = new LatticeCell(LatticeValue.BOTTOM);
            updateSSAWL(stmt.getDest(), stmt, newLat);
        }
    }

    void visitStmt(IRStmt stmt) {
        if (stmt instanceof BranchStmt branch)
            visitBranchStmt(branch);
        else if (stmt instanceof PhiStmt phi)
            visitPhiStmt(phi);
        else if (stmt instanceof BinaryExprStmt expr)
            visitBinaryExprStmt(expr);
        else if (stmt instanceof UnaryExprStmt expr)
            visitUnaryExprStmt(expr);
        else if (stmt instanceof MoveStmt move)
            visitMoveStmt(move);
        else if (stmt instanceof SelectStmt select)
            visitSelectStmt(select);
        else if (stmt instanceof LoadStmt || stmt instanceof StoreStmt || stmt instanceof GetElementPtrStmt
                || stmt instanceof ReturnStmt || stmt instanceof CallStmt)
            visitBottomStmt(stmt);
        else
            throw new RuntimeException("[SCCP] Unknown stmt: " + stmt);
    }

    public void optimize(IRCode irCode) {
        for (int i = 0; i < irCode.funcStmts.size(); i++) {
            optimizeInFunc(irCode.funcStmts.get(i));
        }
    }
    private String typeName(LatticeValue type) {
        switch (type) {
            case BOTTOM: return "BOTTOM";
            case CONSTANT: return "CONSTANT";
            case TOP: return "TOP";
            default: return "UNKNOWN";
        }
    }
    private void debug(String msg) {
        System.out.println("# [SCCP]: "  + msg);
    }
}
