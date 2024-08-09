package semantic;

import java.util.HashMap;

public class SymbolTable {
    public HashMap<String, Type> table;
    public SymbolTable() {
        table = new HashMap<>();
    }
    public void put(String name, Type type) {
        table.put(name, type);
    }
    public Type get(String name) {
        return table.get(name);
    }
    public boolean containsKey(String name) {
        return table.containsKey(name);
    }
    public String toString() {
        return table.toString();
    }
    public boolean equals(SymbolTable otherTable) {
        return this.table.equals(otherTable.table);
    }
}
