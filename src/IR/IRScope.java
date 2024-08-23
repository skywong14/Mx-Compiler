package IR;

import IR.IRStmts.FunctionDeclarationStmt;

import java.util.HashMap;

public class IRScope {
    private boolean isGlobal;
    public String name;
    public IRScope parent;
    public int tmpVarCounter = -1;
    public HashMap<String, String> varTable = new HashMap<>();
    public HashMap<String, Integer> repeatVar = new HashMap<>();

    public HashMap<String, FunctionDeclarationStmt> functions = new HashMap<>();


    public IRScope(String name,  IRScope parent) {
        this.name = name;
        this.parent = parent;
        isGlobal = (parent == null);
    }


    public IRScope getFunctionScope() {
        IRScope current = this;
        while (current != null) {
            if (current.name.startsWith("Function_")) return current;
            current = current.parent;
        }
        return null;
    }

    public String declareVariable(String varName) {
        IRScope funcScope = getFunctionScope();
        if (funcScope == null) throw new RuntimeException("No function scope found");
        if (funcScope.repeatVar.containsKey(varName)) {
            funcScope.repeatVar.put(varName, funcScope.repeatVar.get(varName) + 1);
            varTable.put(varName, varName + "." + funcScope.repeatVar.get(varName));
            return varName + "." + funcScope.repeatVar.get(varName);
        } else {
            funcScope.repeatVar.put(varName, 1);
            varTable.put(varName, varName + ".1");
            return varName + ".1";
        }
    }

    public String getVariable(String varName) {
        if (varTable.containsKey(varName)) {
            if (isGlobal)
                return "@" + varTable.get(varName);
            else
                return "%" + varTable.get(varName);
        } else {
            if (parent != null) return parent.getVariable(varName);
            else throw new RuntimeException("Variable not found: " + varName);
        }
    }

    public void addFunction(String name, FunctionDeclarationStmt function) {
        // must be global scope
        functions.put(name, function);
    }

    public FunctionDeclarationStmt getFunctionDeclaration(String name) {
        if (functions.containsKey(name)) return functions.get(name);
        if (parent != null) return parent.getFunctionDeclaration(name);
        throw new RuntimeException("Function not found: " + name);
    }



    public String getNewRegister() {
        IRScope funcScope = getFunctionScope();
        if (funcScope == null) throw new RuntimeException("No function scope found");
        funcScope.tmpVarCounter++;
        return "%" + funcScope.tmpVarCounter;
    }
}
