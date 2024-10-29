package optimize.utils;

import IR.IRStmts.IRStmt;
import optimize.IRFunction;

import java.util.ArrayList;

public class FuncHeadStmt extends IRStmt {
    IRFunction func;
    public ArrayList<String> params;
    public FuncHeadStmt(IRFunction func) {
        this.func = func;
        this.params = func.argNames;
    }

    @Override
    public String toString() {
        return "# FunctionHead: " + func.name;
    }
}
