package semantic;

import java.util.HashMap;

import semantic.ASTNodes.ASTNode;
import semantic.ASTNodes.ClassNode;
import semantic.ASTNodes.FunctionNode;
import semantic.ASTNodes.VariableNode;

public class Scope {
    public Scope parent;
    public String name;
    ASTNode node;
    private HashMap<String, VariableNode> variableSymbolTable; // 存储变量名及其类型
    private HashMap<String, FunctionNode> functionSymbolTable; // 存储函数名及其返回类型
    private HashMap<String, ClassNode> classSymbolTable;   // 存储类名及其定义

    public HashMap<String, Integer> localVariableCounter = new HashMap<>(); // 局部变量计数器，从<name>.1开始

    public Scope(Scope parent, String name, ASTNode node) {
        this.parent = parent;
        this.name = name; // for debug
        this.variableSymbolTable = new HashMap<>();
        this.functionSymbolTable = new HashMap<>();
        this.classSymbolTable = new HashMap<>();
        this.node = node;
    }

    public Scope getParent() { return parent;  }
    public Scope getGlobalScope() {
        Scope current = this;
        while (current.parent != null) {
            current = current.parent;
        }
        return current;
    }
    public Scope getScope(String name) {
        Scope current = this;
        while (current != null) {
            if (current.name.equals(name)) return current;
            current = current.parent;
        }
        return null;
    }

    public int getLocalVariableCounter(String name) {
        if (localVariableCounter.containsKey(name)) {
            return localVariableCounter.get(name);
        }
        return 0;
    }

    public void addVariable(String name, VariableNode variableNode) {
        // 变量名不能与函数名重复
        if (variableSymbolTable.containsKey(name) || functionSymbolTable.containsKey(name)) {
            throw new RuntimeException("[Multiple Definitions]: Variable name conflict: " + name);
        }
        variableSymbolTable.put(name, variableNode);
        localVariableCounter.put(name, 0); // 初始化计数器
    }
    public void addFunction(String name, FunctionNode functionNode) {
        // 函数名不能与变量名或类名重复
        if (functionSymbolTable.containsKey(name) || variableSymbolTable.containsKey(name) || classSymbolTable.containsKey(name)) {
            throw new RuntimeException("[Multiple Definitions]: Function name conflict: " + name);
        }
        functionSymbolTable.put(name, functionNode);
    }
    public void addClass(String name, ClassNode classNode) {
        // 类名不能与函数名重复
        if (classSymbolTable.containsKey(name) || functionSymbolTable.containsKey(name)) {
            throw new RuntimeException("[Multiple Definitions]: Class name conflict: " + name);
        }
        classSymbolTable.put(name, classNode);
    }

    public boolean isVariableDeclaredInCurrentScope(String name) {
        return variableSymbolTable.containsKey(name);
    }
    public boolean isFunctionDeclaredInCurrentScope(String name) {
        return functionSymbolTable.containsKey(name);
    }
    public boolean isClassDeclaredInCurrentScope(String name) {
        return classSymbolTable.containsKey(name);
    }

    public VariableNode resolveVariable(String name) {
        VariableNode ptr = variableSymbolTable.get(name);
        if (ptr != null) return ptr;
        else if (parent != null) return parent.resolveVariable(name);
        throw new RuntimeException("[Undefined Identifier]: Variable not found: " + name);
    }
    public FunctionNode resolveFunction(String name) {
        FunctionNode ptr = functionSymbolTable.get(name);
        if (ptr != null) return ptr;
        else if (parent != null) return parent.resolveFunction(name);
        throw new RuntimeException("[Undefined Identifier]: Function not found: " + name);
    }
    public ClassNode resolveClass(String name) {
        ClassNode ptr = classSymbolTable.get(name);
        if (ptr != null) return ptr;
        else if (parent != null) return parent.resolveClass(name);
        throw new RuntimeException("[Undefined Identifier]: Class not found: " + name);
    }

    public boolean isInLoop() {
        if (parent == null) return false;
        if (name.equals("Loop")) return true;
        return parent.isInLoop();
    }

    public boolean isInFunction() {
        if (parent == null) return false;
        /// 如果name以“Function”开头，则说明当前作用域是函数作用域
        if (name.startsWith("Function")) return true;
        return parent.isInFunction();
    }

    public boolean isInClass() {
        if (parent == null) return false;
        if (name.startsWith("Class")) return true;
        return parent.isInClass();
    }

    public FunctionNode getCurrentFunction() {
        if (name.startsWith("Function")) return (FunctionNode) node;
        if (parent == null) return null;
        return parent.getCurrentFunction();
    }

    public ClassNode getCurrentClass() {
        if (name.startsWith("Class")) return (ClassNode) node;
        if (parent == null) return null;
        return parent.getCurrentClass();
    }
}
