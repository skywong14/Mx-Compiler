package IR.IRStmts;

import java.util.ArrayList;

public class FunctionImplementStmt extends IRStmt {
    public String name;
    public BasicIRType returnType;
    public ArrayList<BasicIRType> argTypes;
    public ArrayList<String> argNames;
    public Block curBlock;
    public ArrayList<Block> blocks;
    public ArrayList<AllocaStmt> allocaStmts;

    public FunctionImplementStmt(String name, FunctionDeclarationStmt declaration) {
        this.name = name;
        this.returnType = declaration.returnType;
        this.argTypes = declaration.argTypes;
        this.argNames = declaration.argNames;
        this.curBlock = new Block(""); // head block
        this.blocks = new ArrayList<>();
        this.blocks.add(curBlock);
        this.allocaStmts = new ArrayList<>();
    }

    public void addAllocaStmt(AllocaStmt stmt) {
        allocaStmts.add(stmt);
    }

    public void addStmt(IRStmt stmt) {
        curBlock.addStmt(stmt);
    }

    public void newBlock(String label) {
        curBlock = new Block(label);
        blocks.add(curBlock);
    }

    public int getCurBlockIndex() {
        for (int i = 0; i < blocks.size(); i++) {
            if (blocks.get(i) == curBlock) return i;
        }
        throw new RuntimeException("curBlock not found");
    }

    public String curBlockLabel() {
        if (curBlock.label.equals("")) {
            return "0";
        }
        return curBlock.label;
    }



    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("define ").append(returnType.toString()).append(" @").append(name).append("(");
        for (int i = 0; i < argTypes.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(argTypes.get(i).toString()).append(" %").append(argNames.get(i));
        }
        sb.append(") {\n");
        for (IRStmt stmt : allocaStmts) {
            sb.append("\t").append(stmt.toString()).append("\n");
        }
        for (Block block : blocks) {
            sb.append(block.toString());
        }
        sb.append("}\n");
        return sb.toString();
    }
}
