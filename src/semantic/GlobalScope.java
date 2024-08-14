package semantic;

import semantic.ASTNodes.*;

public class GlobalScope extends Scope{
    public GlobalScope() {
        super(null, "Global");
        init();
    }

    public void init() {
        // void print(string str);
        // void println(string str);
        // void printInt(int n);
        // void printlnInt(int n);
        // string getString();
        // int getInt();
        // string toString(int i);

        TypeNode stringType = new TypeNode(new Type("string", false, 0));
        TypeNode intType = new TypeNode(new Type("int", false, 0));
        TypeNode voidType = new TypeNode(new Type("void", false, 0));
        CompoundStmtNode emptyBody = new CompoundStmtNode();
        ParameterListNode stringParam = new ParameterListNode();
        stringParam.addParameter(new ParameterNode("str", stringType));
        ParameterListNode intParam = new ParameterListNode();
        intParam.addParameter(new ParameterNode("n", intType));
        ParameterListNode emptyParam = new ParameterListNode();

        addFunction("print", new FunctionNode(voidType, "print", stringParam, emptyBody));
        addFunction("println", new FunctionNode(voidType, "println", stringParam, emptyBody));
        addFunction("printInt", new FunctionNode(voidType, "printInt", intParam, emptyBody));
        addFunction("printlnInt", new FunctionNode(voidType, "printlnInt", intParam, emptyBody));
        addFunction("getString", new FunctionNode(stringType, "getString", emptyParam, emptyBody));
        addFunction("getInt", new FunctionNode(intType, "getInt", emptyParam, emptyBody));
        addFunction("toString", new FunctionNode(stringType, "toString", intParam, emptyBody));
    }
}
