package optimize.optimizations;

import IR.IRStmts.BinaryExprStmt;
import IR.IRStmts.IRStmt;
import IR.IRStmts.MoveStmt;
import optimize.IRBlock;
import optimize.IRCode;
import optimize.IRFunction;

import java.util.ArrayList;
import java.util.HashMap;

public class CommonSubexpr {
    private static void debug(String msg) {
        System.out.println("# [CSE]: " + msg);
    }

    int tmpVarCnt = 0;
    HashMap<String, AvailableExpr> AEBank = null;
    HashMap<String, ArrayList<String>> operandMap = null;
    ArrayList<tmpNode> waitingList = null;

    String getNewTmp() {
        return "%.subexpr." + (tmpVarCnt++);
    }

    boolean commutative(String opr) {
        if (opr.equals("add") || opr.equals("mul") || opr.equals("and") || opr.equals("or") || opr.equals("xor")
            || opr.equals("icmp eq") || opr.equals("icmp ne"))
            return true;
        return false;
    }

    String getOperator(String str) {
        // the slice before the first ","
        return str.substring(0, str.indexOf(","));
    }

    String getIndex(BinaryExprStmt stmt) {
        return stmt.operator + "," + stmt.register1 + "," + stmt.register2;
    }
    String getIndex2(BinaryExprStmt stmt) {
        return stmt.operator + "," + stmt.register2 + "," + stmt.register1;
    }

    class AvailableExpr {
        int pos; // position in the block
        String operator, operand1, operand2;
        String tmpVarName;
        String index;
        AvailableExpr(int pos, String operator, String operand1, String operand2) {
            this.pos = pos;
            this.operator = operator;
            this.operand1 = operand1;
            this.operand2 = operand2;
            this.tmpVarName = null;
            this.index = operator + "," + operand1 + "," + operand2;
        }
    }
    static class tmpNode {
        int pos;
        String tmpVarName;
        tmpNode(int pos, String tmpVarName) {
            this.pos = pos;
            this.tmpVarName = tmpVarName;
        }
    }

    void localCSE_Block(IRBlock irBlock) {
//        debug("CSE Block begin: " + irBlock.label);
        // record Available Expression
        ArrayList<String> tmpList = null;
        AEBank = new HashMap<>();
        waitingList = new ArrayList<>();
        operandMap = new HashMap<>();
        int i = 0;
        while (i < irBlock.stmts.size()) {
            IRStmt stmt = irBlock.stmts.get(i);
            // step 1 (if is BinaryExpr)
            if (stmt instanceof BinaryExprStmt binStmt) {
                // check if the expression is in the bank
                if (AEBank.containsKey(getIndex(binStmt))
                    || (commutative(binStmt.operator) && AEBank.containsKey(getIndex2(binStmt)))) {
                    // found, replace the stmt with the tmpVar
                    if (!AEBank.containsKey(getIndex(binStmt))) {
                        binStmt = new BinaryExprStmt(binStmt.operator, binStmt.type, binStmt.register2, binStmt.register1, binStmt.dest);
                    }
                    assert AEBank.containsKey(getIndex(binStmt));
                    AvailableExpr ae = AEBank.get(getIndex(binStmt));
                    if (ae.tmpVarName == null) {
                        ae.tmpVarName = getNewTmp();
                        waitingList.add(new tmpNode(ae.pos, ae.tmpVarName));
                    }
                    irBlock.stmts.set(i, new MoveStmt(binStmt.dest, ae.tmpVarName));
                } else {
                    // not found, insert new tuple
                    AvailableExpr ae = new AvailableExpr(i, binStmt.operator, binStmt.register1, binStmt.register2);
                    AEBank.put(ae.index, ae);
                    if (!operandMap.containsKey(binStmt.register1)) {
                        tmpList = new ArrayList<>(); tmpList.add(ae.index);
                        operandMap.put(binStmt.register1, tmpList);
                    } else {
                        operandMap.get(binStmt.register1).add(ae.index);
                    }
                    if (!operandMap.containsKey(binStmt.register2)) {
                        tmpList = new ArrayList<>(); tmpList.add(ae.index);
                        operandMap.put(binStmt.register2, tmpList);
                    } else {
                        operandMap.get(binStmt.register2).add(ae.index);
                    }
                }
            }

            // step 2 (if has dest_reg)
            String dest = stmt.getDest();
            if (dest != null) {
                // remove the expression from the bank
                if (operandMap.containsKey(dest)) {
                    for (String index : operandMap.get(dest)) {
                        AEBank.remove(index);
                    }
                    operandMap.remove(dest);
                }
            }
            i++;
        }

        // add MoveStmt for each place in waitingList (reverse order)
        for (int j = waitingList.size() - 1; j >= 0; j--) {
            tmpNode tn = waitingList.get(j);
            // tn.pos: change Binary.dest to tn.tmpVarName
            // tn.pos+1: Binary.dest = tn.tmpVarName
            BinaryExprStmt binStmt = (BinaryExprStmt) irBlock.stmts.get(tn.pos);
            irBlock.stmts.add(tn.pos + 1, new MoveStmt(binStmt.dest, tn.tmpVarName));
            binStmt.dest = tn.tmpVarName;
        }
    }

    void localCSE(IRCode irCode) {
        // for each Block in each Function
        for (IRFunction irFunc : irCode.funcStmts) {
            for (IRBlock irBlock : irFunc.blocks) {
                localCSE_Block(irBlock);
            }
        }
    }

    public void optimize(IRCode irCode) {
        localCSE(irCode);
    }
}
