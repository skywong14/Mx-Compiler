package optimize.earlyOptimization;

import IR.IRStmts.BinaryExprStmt;
import IR.IRStmts.IRStmt;
import IR.IRStmts.MoveStmt;
import IR.IRStmts.UnaryExprStmt;
import optimize.IRBlock;
import optimize.IRCode;
import optimize.IRFunction;

public class ConstantFolding {
    int simplifyBinaryStmt(String operator, int val1, int val2) {
        return switch (operator) {
            case "add" -> val1 + val2;
            case "sub" -> val1 - val2;
            case "mul" -> val1 * val2;
            case "sdiv" -> val1 / val2;
            case "srem" -> val1 % val2;
            case "and" -> val1 & val2;
            case "or" -> val1 | val2;
            case "xor" -> val1 ^ val2;
            case "shl" -> val1 << val2;
            case "ashr" -> val1 >> val2;
            case "icmp eq" -> val1 == val2 ? 1 : 0;
            case "icmp ne" -> val1 != val2 ? 1 : 0;
            case "icmp slt" -> val1 < val2 ? 1 : 0;
            case "icmp sgt" -> val1 > val2 ? 1 : 0;
            case "icmp sle" -> val1 <= val2 ? 1 : 0;
            case "icmp sge" -> val1 >= val2 ? 1 : 0;
            default -> 0;
        };
    }
    int simplifyUnaryStmt(String operator, int val) {
        return switch (operator) {
            case "!" -> val^1;
            case "~" -> ~val;
            case "-" -> -val;
            case "++" -> val + 1;
            case "--" -> val - 1;
            default -> 0;
        };
    }
    int resolveValue(String name) {
        return switch (name) {
            case "true" -> 1;
            case "false", "null" -> 0;
            default -> Integer.parseInt(name);
        };
    }

    boolean isConstant(String str) {
        return !str.startsWith("@") && !str.startsWith("%");
    }

    void optimizeInFunc(IRFunction irFunc) {
        for (IRBlock block : irFunc.blocks) {
            for (int i = 0; i < block.stmts.size(); i++) {
                IRStmt stmt = block.stmts.get(i);
                if (stmt instanceof BinaryExprStmt binaryExpr) {
                    if (isConstant(binaryExpr.register1) && isConstant(binaryExpr.register2)) {
                        int val1 = resolveValue(binaryExpr.register1);
                        int val2 = resolveValue(binaryExpr.register2);
                        if (binaryExpr.operator.equals("sdiv") && val2 == 0) {
                            continue; // divided by zero
                        }
                        int ret = simplifyBinaryStmt(binaryExpr.operator, val1, val2);
                        block.stmts.remove(i);
                        block.stmts.add(i, new MoveStmt(binaryExpr.dest, String.valueOf(ret)));
                    }
                } else if (stmt instanceof UnaryExprStmt unaryExpr) {
                    if (isConstant(unaryExpr.register)) {
                        int val = resolveValue(unaryExpr.register);
                        int ret = simplifyUnaryStmt(unaryExpr.operator, val);
                        block.stmts.remove(i);
                        block.stmts.add(i, new MoveStmt(unaryExpr.dest, String.valueOf(ret)));
                    }
                }
            }
        }
    }

    public void optimize(IRCode irCode) {
        for (int i = 0; i < irCode.funcStmts.size(); i++) {
            optimizeInFunc(irCode.funcStmts.get(i));
        }
    }
}
