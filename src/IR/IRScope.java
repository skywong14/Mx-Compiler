package IR;

import IR.IRStmts.FunctionDeclarationStmt;

import java.util.HashMap;

public class IRScope {
    private boolean isGlobal;
    public String name;
    public IRScope parent;
    public int tmpVarCounter = 0; // %0 is used for default block
    public HashMap<String, String> varTable = new HashMap<>();
    public HashMap<String, Integer> repeatVar = new HashMap<>();

    public int blockLabelCounter = 0;

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

    public boolean isClassScope() {
        return name.startsWith("Class_");
    }

    public IRScope getLoopScope() {
        IRScope current = this;
        while (current != null) {
            if (current.name.startsWith("Loop_")) return current;
            current = current.parent;
        }
        return null;
    }



    public String declareVariable(String varName) {
        if (isGlobal) {
            varTable.put(varName, varName);
            return varName;
        }
        if (isClassScope()) {
            // field
            if (varTable.containsKey(varName)) throw new RuntimeException("Field redeclaration: " + varName);
            varTable.put(varName, varName);
            return varName;
        }
        // not global
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

    public boolean isField(String varName) {
        if (varTable.containsKey(varName)) {
            return isClassScope();
        } else {
            if (parent != null) return parent.isField(varName);
            else throw new RuntimeException("Variable not found: " + varName);
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
        return "%." + funcScope.tmpVarCounter; // add "."
    }

    public String getNewBlock() {
        IRScope funcScope = getFunctionScope();
        if (funcScope == null) throw new RuntimeException("No function scope found");
        funcScope.blockLabelCounter++;
        return "Block_Label." + funcScope.blockLabelCounter;
    }
}
