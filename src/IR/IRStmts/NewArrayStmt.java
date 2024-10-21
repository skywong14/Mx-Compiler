package IR.IRStmts;

import semantic.ASTNodes.NewExprNode;

import java.util.ArrayList;

public class NewArrayStmt extends IRStmt{
    public ArrayList<IRStmt> stmts;

    public String dest;
    public int dim;
    public ArrayList<String> sizes;

    String headStr;

    FunctionImplementStmt curFunction;

    public NewArrayStmt(int dim, ArrayList<String> sizes, String identifier, String dest, FunctionImplementStmt curFunction) {
        this.dim = dim;
        this.sizes = sizes;
        this.dest = dest;
        this.curFunction = curFunction;
        stmts = new ArrayList<>();
        headStr = ".newArray" + identifier + ".";
        generateStmt(1);
    }

    void addStmt(IRStmt stmt) {
        stmts.add(stmt);
    }

    public String curBlock() {
        return headStr + "loopBody" + 0;
    }

    String generateStmt(int curDim) {
        String retRegName;
        if (curDim > 1)
            retRegName = "%" + headStr + "ptr" + curDim;
        else
            retRegName = dest;
        ArrayList<BasicIRType> int1 = new ArrayList<>(); int1.add(new BasicIRType("i32"));
        ArrayList<String> sz = new ArrayList<>(); sz.add(sizes.get(curDim - 1));
        addStmt(new CallStmt(new BasicIRType("ptr"), "__allocateArray__",
                int1, sz, retRegName));
        if (curDim == dim) {
            return retRegName;
        }
        String indexName = "%" + headStr + "index" + curDim; // is a ptr
        String conditionLabel = headStr + "loopCond" + curDim;
        String bodyLabel = headStr + "loopBody" + curDim;
        String endLabel = headStr + "loopEnd" + curDim;

        curFunction.addAllocaStmt(new AllocaStmt(new BasicIRType("i32"), indexName));
        addStmt(new StoreStmt(new BasicIRType("i32"), "0", indexName));
        addStmt(new BranchStmt(conditionLabel));
        // condition block
        addStmt(new LabelStmt(conditionLabel));
        String indexVal = "%" + headStr + "indexVal" + curDim;
        addStmt(new LoadStmt(new BasicIRType("i32"), indexName, indexVal));
        String cond = "%" + headStr + "cond" + curDim;
        addStmt(new BinaryExprStmt("icmp slt", new BasicIRType("i32"), indexVal, sizes.get(curDim - 1), cond));
        addStmt(new BranchStmt(cond, bodyLabel, endLabel));
        // body block
        addStmt(new LabelStmt(bodyLabel));

        String retPtr = generateStmt(curDim + 1);

        String ptr_i = "%" + headStr + "ptr_i" + curDim;
        addStmt(new GetElementPtrStmt(new BasicIRType("ptr"), retRegName, indexVal, ptr_i, false));
        addStmt(new StoreStmt(new BasicIRType("ptr"), retPtr, ptr_i));
        String nextIndex = "%" + headStr + "nextIndex" + curDim;
        addStmt(new BinaryExprStmt("add", new BasicIRType("i32"), indexVal, "1", nextIndex));
        addStmt(new StoreStmt(new BasicIRType("i32"), nextIndex, indexName));

        addStmt(new BranchStmt(conditionLabel));

        addStmt(new LabelStmt(endLabel));
        return retRegName;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;
        for (IRStmt stmt : stmts){
            if (stmt instanceof LabelStmt){
                sb.append(stmt.toString()).append("\n");
            } else {
                if (isFirst) {
                    isFirst = false;
                    sb.append(stmt.toString()).append("\n");
                } else{
                    sb.append("\t").append(stmt.toString()).append("\n");
                }
            }
        }
        return sb.toString();
    }
}
