package IR.IRStmts;

import semantic.ASTNodes.ArgListNode;

import java.util.ArrayList;

public class NewClassStmt extends IRStmt{
    // alloc memory, then call constructor, return ptr to dest
    public String dest;
    public String className;

    public AllocaStmt allocStmt;
    public CallStmt callStmt;

    public BitCastStmt bitCastStmt;
    public PtrToIntStmt ptrToIntStmt;
    public MemsetStmt memsetStmt;
    public String tmp1, tmp2;

    public NewClassStmt(String className, String tmp1, String tmp2, String dest, Boolean hasConstructor) {
        this.dest = dest;
        this.className = className;
        this.tmp1 = tmp1;
        this.tmp2 = tmp2;
        generateStmt(hasConstructor);
    }

    void generateStmt(boolean hasConstructor) {
        // %ret = alloca %classType
        // %ptr = bitcast ptr %ret to ptr
        // %size = ptrtoint ptr getelementptr (%classType, ptr null, i32 1) to i32
        // call void @llvm.memset.p0.p0.i32(ptr %ptr, i8 0, i32 %size, i1 false)
        allocStmt = new AllocaStmt(new ClassTypeStmt(className, null), dest);
        bitCastStmt = new BitCastStmt(dest, tmp1);
        ptrToIntStmt = new PtrToIntStmt("%class." + className, "null", tmp2);
        memsetStmt = new MemsetStmt(tmp1, "0", tmp2, "false");

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
        if (callStmt == null) return allocStmt.toString() + "\n\t" + bitCastStmt.toString()+ "\n\t" +
                ptrToIntStmt.toString() + "\n\t" + memsetStmt.toString();
        return allocStmt.toString() + "\n\t" + bitCastStmt.toString()+ "\n\t" +
                ptrToIntStmt.toString() + "\n\t" + memsetStmt.toString() + "\n\t" + callStmt.toString();
    }
}
