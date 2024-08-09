package semantic;

public class Scope {
    public Scope parent;
    public String name;
    public SymbolTable symbolTable;
    public Scope(Scope parent, String name) {
        this.parent = parent;
        this.name = name;
        this.symbolTable = new SymbolTable();
    }
    public Scope(Scope parent) {
        this(parent, "");
    }
    public Scope() {
        this(null, "");
    }
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
    public void addSymbol(String name, Type type) {
        symbolTable.put(name, type);
    }
    public Type getSymbol(String name) {
        return symbolTable.get(name);
    }
    public boolean containsSymbol(String name) {
        return symbolTable.containsKey(name);
    }
    public String toString() {
        return "Scope{name='" + name + "', symbolTable='" + symbolTable + "'}";
    }
    public boolean equals(Scope otherScope) {
        return this.symbolTable.equals(otherScope.symbolTable);
    }
}
