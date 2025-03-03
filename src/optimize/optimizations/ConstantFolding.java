package optimize.optimizations;

import IR.IRStmts.*;
import optimize.IRBlock;
import optimize.IRCode;
import optimize.IRFunction;
import optimize.utils.ValueSimplify;

public class ConstantFolding {
    ValueSimplify simplify = new ValueSimplify();

    boolean isConstant(String str) {
        return !str.startsWith("@") && !str.startsWith("%");
    }

    void optimizeInFunc(IRFunction irFunc) {
        for (IRBlock block : irFunc.blocks) {
            for (int i = 0; i < block.stmts.size(); i++) {
                IRStmt stmt = block.stmts.get(i);
                if (stmt instanceof BinaryExprStmt binaryExpr) {
                    if (isConstant(binaryExpr.register1) && isConstant(binaryExpr.register2)) {
                        int val1 = simplify.resolveValue(binaryExpr.register1);
                        int val2 = simplify.resolveValue(binaryExpr.register2);
                        if (binaryExpr.operator.equals("sdiv") && val2 == 0) {
                            continue; // divided by zero
                        }
                        int ret = simplify.simplifyBinaryStmt(binaryExpr.operator, val1, val2);
                        block.stmts.set(i, new MoveStmt(binaryExpr.dest, String.valueOf(ret)));
                    }
                } else if (stmt instanceof UnaryExprStmt unaryExpr) {
                    if (isConstant(unaryExpr.register)) {
                        int val = simplify.resolveValue(unaryExpr.register);
                        int ret = simplify.simplifyUnaryStmt(unaryExpr.operator, val);
                        block.stmts.set(i, new MoveStmt(unaryExpr.dest, String.valueOf(ret)));
                    }
                } else if (stmt instanceof SelectStmt select) {
                    if (isConstant(select.cond)) {
                        int val = simplify.resolveValue(select.cond);
                        if (val == 1) {
                            block.stmts.set(i, new MoveStmt(select.dest, select.trueVal));
                        } else {
                            block.stmts.set(i, new MoveStmt(select.dest, select.falseVal));
                        }
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
