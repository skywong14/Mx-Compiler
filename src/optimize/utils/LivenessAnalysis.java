package optimize.utils;

import IR.IRStmts.*;

import java.util.HashSet;

public class LivenessAnalysis {
    boolean isLocalReg(String regName) {
        if (regName == null) return false;
        return regName.startsWith("%");
    }
    public HashSet<String> getUse(IRStmt stmt) {
        HashSet<String> ret = new HashSet<>();
        if (stmt instanceof BranchStmt branch &&  branch.condition != null && isLocalReg(branch.condition)) {
            ret.add(branch.condition);
        }
        if (stmt instanceof BinaryExprStmt binaryExpr) {
            if (isLocalReg(binaryExpr.register1)) ret.add(binaryExpr.register1);
            if (isLocalReg(binaryExpr.register2)) ret.add(binaryExpr.register2);
        }
        if (stmt instanceof CallStmt call) {
            for (String arg : call.args)
                if (isLocalReg(arg)) ret.add(arg);
        }
        if (stmt instanceof GetElementPtrStmt getElementPtr) {
            if (isLocalReg(getElementPtr.pointer)) ret.add(getElementPtr.pointer);
            if (isLocalReg(getElementPtr.index)) ret.add(getElementPtr.index);
        }
        if (stmt instanceof LoadStmt load) {
            if (isLocalReg(load.pointer)) ret.add(load.pointer);
        }
        if (stmt instanceof ReturnStmt retStmt) {
            if (retStmt.src != null && isLocalReg(retStmt.src)) ret.add(retStmt.src);
        }
        if (stmt instanceof SelectStmt select) {
            if (isLocalReg(select.cond)) ret.add(select.cond);
            if (isLocalReg(select.trueVal)) ret.add(select.trueVal);
            if (isLocalReg(select.falseVal)) ret.add(select.falseVal);
        }
        if (stmt instanceof StoreStmt store) {
            if (isLocalReg(store.dest)) ret.add(store.dest);
            if (isLocalReg(store.val)) ret.add(store.val);
        }
        if (stmt instanceof UnaryExprStmt unaryExpr) {
            if (isLocalReg(unaryExpr.register)) ret.add(unaryExpr.register);
        }
        if (stmt instanceof MoveStmt move) {
            if (isLocalReg(move.src)) ret.add(move.src);
        }
        if (stmt instanceof PhiStmt phi) {
            for (String reg : phi.val.values())
                if (isLocalReg(reg)) ret.add(reg);
        }
        return ret;
    }
    public HashSet<String> getDef(IRStmt stmt) {
        HashSet<String> ret = new HashSet<>();
        if (stmt instanceof BinaryExprStmt binaryExpr)
            if (isLocalReg(binaryExpr.dest)) ret.add(binaryExpr.dest);
        if (stmt instanceof CallStmt call)
            if (call.dest != null && isLocalReg(call.dest)) ret.add(call.dest);
        if (stmt instanceof GetElementPtrStmt getElementPtr)
            if (isLocalReg(getElementPtr.dest)) ret.add(getElementPtr.dest);
        if (stmt instanceof LoadStmt load)
            if (isLocalReg(load.dest)) ret.add(load.dest);
        if (stmt instanceof SelectStmt select)
            if (isLocalReg(select.dest)) ret.add(select.dest);
        if (stmt instanceof UnaryExprStmt unaryExpr)
            if (isLocalReg(unaryExpr.dest)) ret.add(unaryExpr.dest);
        if (stmt instanceof MoveStmt move)
            if (isLocalReg(move.dest)) ret.add(move.dest);
        if (stmt instanceof FuncHeadStmt funcHead){
            for (String param : funcHead.params)
                if (isLocalReg(param)) ret.add(param);
        }
        if (stmt instanceof PhiStmt phi)
            if (isLocalReg(phi.dest)) ret.add(phi.dest);
        return ret;
    }
}
