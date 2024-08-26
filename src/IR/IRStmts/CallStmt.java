package IR.IRStmts;

import java.util.ArrayList;

public class CallStmt extends IRStmt {
    public String dest;
    public String funcName;
    public BasicIRType retType;
    public ArrayList<BasicIRType> argTypes;
    public ArrayList<String> args;

    public CallStmt(BasicIRType retType, String funcName, ArrayList<BasicIRType> argTypes, ArrayList<String> args, String dest){
        this.retType = retType;
        this.argTypes = argTypes;
        this.args = args;
        this.dest = dest;
        this.funcName = funcName;
    }


    @Override
    public String toString() {
        if (retType.typeName.equals("void")) {
            // call void @<FunctionName>(<arguments>)
            StringBuilder sb = new StringBuilder();
            sb.append("call void @").append(funcName).append("(");
            for (int i = 0; i < args.size(); i++) {
                sb.append(argTypes.get(i)).append(" ").append(args.get(i));
                if (i != args.size() - 1) {
                    sb.append(", ");
                }
            }
            sb.append(")\n");
            return sb.toString();
        } else {
            // <result> = call <ResultType> @<FunctionName>(<arguments>)
            StringBuilder sb = new StringBuilder();
            sb.append(dest).append(" = call ").append(retType).append(" @").append(funcName).append("(");
            for (int i = 0; i < args.size(); i++) {
                sb.append(argTypes.get(i)).append(" ").append(args.get(i));
                if (i != args.size() - 1) {
                    sb.append(", ");
                }
            }
            sb.append(")");
            return sb.toString();
        }
    }
}
