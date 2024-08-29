package IR.IRStmts;

import semantic.ASTNodes.ArgListNode;

import java.util.ArrayList;

public class NewClassStmt extends IRStmt{
    // malloc memory, then call constructor, return ptr to dest
    public String dest;
    public String className;

    public CallStmt mallocStmt;
    public CallStmt callStmt;

    public int fieldSize;

    public NewClassStmt(String className, String dest, Boolean hasConstructor, int fieldSize) {
        this.dest = dest;
        this.className = className;
        this.fieldSize = fieldSize;
        generateStmt(hasConstructor);
    }

    void generateStmt(boolean hasConstructor) {
        // %ret = malloc %classType.size
        ArrayList<BasicIRType> int1 = new ArrayList<>(); int1.add(new BasicIRType("i32"));
        ArrayList<String> arg1 = new ArrayList<>(); arg1.add(Integer.toString(fieldSize));
        mallocStmt = new CallStmt(new BasicIRType("ptr"), "_malloc", int1, arg1, dest);

        if (!hasConstructor) return;
        // Constructor call
        // ArgType : {"ptr"}, Args : {dest}
        ArrayList<BasicIRType> argTypes = new ArrayList<>();
        argTypes.add(new BasicIRType("ptr"));
        ArrayList<String> args = new ArrayList<>();
        args.add(dest);
        callStmt = new CallStmt(new BasicIRType("void"), className + "." + className, argTypes, args, null); // className, dest
    }


    @Override
    public String toString() {
        if (callStmt == null) return mallocStmt.toString();
        else return mallocStmt.toString() + "\n\t" + callStmt.toString();
    }

    @Override
    public int getSpSize() {
        return 1;
    }
}
