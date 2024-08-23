package semantic;

import semantic.ASTNodes.*;

import java.util.Stack;

public class ScopeManager {
    private Stack<Scope> scopeStack;

    public ScopeManager(ProgramNode programNode) {
        scopeStack = new Stack<>();
        GlobalScope globalScope = new GlobalScope(programNode);
        scopeStack.push(globalScope);
    }


    public void enterScope(String name, ASTNode node) {
        Scope newScope = new Scope(getCurrentScope(), name, node);
        scopeStack.push(newScope);
    }

    public void exitScope() {
        if (scopeStack.size() > 1) { // 避免弹出全局作用域
            scopeStack.pop();
        }
    }

    public Scope getCurrentScope() {
        return scopeStack.peek();
    }

    public void declareVariable(String name, VariableNode node) {
        getCurrentScope().addVariable(name, node);
    }

    public void declareFunction(String name, FunctionNode node) {
        getCurrentScope().addFunction(name, node);
    }

    public void declareClass(String name, ClassNode node) {
        getCurrentScope().addClass(name, node);
    }

    public VariableNode resolveVariable(String name) {
        return getCurrentScope().resolveVariable(name);
    }

    public FunctionNode resolveFunction(String name) {
        return getCurrentScope().resolveFunction(name);
    }

    public ClassNode resolveClass(String name) {
        return getCurrentScope().resolveClass(name);
    }

    public boolean isVariableDeclaredInCurrentScope(String name) {
        return getCurrentScope().isVariableDeclaredInCurrentScope(name);
    }

    public boolean isFunctionDeclaredInCurrentScope(String name) {
        return getCurrentScope().isFunctionDeclaredInCurrentScope(name);
    }

    public boolean isClassDeclaredInCurrentScope(String name) {
        return getCurrentScope().isClassDeclaredInCurrentScope(name);
    }

    public boolean isTypeDeclared(String name) {
        if (name.equals("int") || name.equals("void") || name.equals("bool") || name.equals("string")) {
            return true;
        }
        return getCurrentScope().getGlobalScope().isClassDeclaredInCurrentScope(name);
    }

    public boolean isInLoop() { return getCurrentScope().isInLoop(); }

    public boolean isInFunction() { return getCurrentScope().isInFunction(); }

    public boolean isInClass() { return getCurrentScope().isInClass(); }

    public FunctionNode getCurrentFunction() { return getCurrentScope().getCurrentFunction(); }

    public ClassNode getCurrentClass() { return getCurrentScope().getCurrentClass(); }
}
