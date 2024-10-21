package optimize;

import IR.IRStmts.IRStmt;

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
        return "";
    }
}
