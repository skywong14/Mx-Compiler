package optimize.optimizations;

import IR.IRStmts.*;
import optimize.IRBlock;
import optimize.IRCode;
import optimize.IRFunction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Global2Local {

    boolean hasCallStmt(IRFunction func) {
        for (IRBlock block : func.blocks)
            for (IRStmt stmt : block.stmts)
                if (stmt instanceof CallStmt) return true;
        return false;
    }

    void g2l(IRFunction func) {
        // if without funcCall, then replace globalVar with localVar
        if (hasCallStmt(func)) return;
        // collect all global variables
        HashMap<String, BasicIRType> globalVarTypes = new HashMap<>();
        HashMap<String, HashSet<String>> defMap = new HashMap<>();
        for (IRBlock block : func.blocks)
            for (IRStmt stmt : block.stmts) {
                if (stmt instanceof LoadStmt load && load.pointer.startsWith("@")) {
                    globalVarTypes.put(load.pointer, load.type);
                    if (!defMap.containsKey(load.pointer)) defMap.put(load.pointer, new HashSet<>());
                    defMap.get(load.pointer).add(load.dest);
                }
            }
        HashSet<String> isModified = new HashSet<>();
        HashMap<String, String> globalVars = new HashMap<>();
        ArrayList<IRStmt> loadStmts = new ArrayList<>();
        for (String key : globalVarTypes.keySet()) {
            String newName = "%.g2l." + key.substring(1);
            globalVars.put(key, newName);
            loadStmts.add(new LoadStmt(globalVarTypes.get(key), key, newName));
        }

        // modify loadStmt and storeStmt
        for (IRBlock block : func.blocks){
            ArrayList<IRStmt> newStmts = new ArrayList<>();
            for (IRStmt stmt : block.stmts) {
                if (stmt instanceof StoreStmt store) {
                    if (store.dest.startsWith("@")) {
                        if (!defMap.containsKey(store.dest)) {
                            newStmts.add(stmt);
                            continue;
                        }
                        newStmts.add(new MoveStmt(globalVars.get(store.dest), store.val));
                        isModified.add(store.dest);
                    } else {
                        newStmts.add(stmt);
                    }
                } else if (stmt instanceof LoadStmt load) {
                    if (load.pointer.startsWith("@")) {
                        newStmts.add(new MoveStmt(load.dest, globalVars.get(load.pointer)));
                    } else {
                        newStmts.add(stmt);
                    }
                } else
                    newStmts.add(stmt);
            }
            block.stmts = newStmts;
        }

        // put newStmts to head
        func.blocks.get(0).stmts.addAll(0, loadStmts);

        // write back the global vars
        ArrayList<IRStmt> storeStmts = new ArrayList<>();
        for (String key : globalVars.keySet())
            if (isModified.contains(key)){
                String newName = globalVars.get(key);
                storeStmts.add(new StoreStmt(globalVarTypes.get(key), newName, key));
            }
        // add store stmts in front of RetStmts
        for (IRBlock block : func.blocks) {
            for (int i = 0; i < block.stmts.size(); i++) {
                IRStmt stmt = block.stmts.get(i);
                if (stmt instanceof ReturnStmt) {
                    block.stmts.addAll(i, storeStmts);
                    break;
                }
            }
        }
    }

    public void optimize(IRCode irCode) {
        for (IRFunction func : irCode.funcStmts) {
            g2l(func);
        }
    }
}
