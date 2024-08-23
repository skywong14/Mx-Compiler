package IR.IRStmts;

import java.util.ArrayList;

public class CallStmt extends IRStmt {
    public String dest;
    public String funcName;
    public BasicIRType retType;
    public ArrayList<BasicIRType> argTypes;
    public ArrayList<String> args;

    CallStmt(){

    }


    @Override
    public String toString() {
        if (retType.typeName.equals("void")) {
            // call void @<FunctionName>(<arguments>)

        } else {
            // <result> = call <ResultType> @<FunctionName>(<arguments>)
        }
    }
}
