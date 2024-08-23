package semantic.ASTNodes;

import semantic.Scope;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Queue;

public class ClassNode extends ASTNode {
    public String name;
    private ArrayList<ConstructorNode> constructorNode;
    private ArrayList<FieldDeclarationNode> fieldNodes;
    private ArrayList<FunctionNode> methodNodes;

    private HashMap<String, ASTNode> symbolTable;
    public ArrayList<ASTNode> declarationQueue;

    public boolean visited_in_declaration = false;

    public ClassNode(String name_) {
        this.name = name_;
        this.fieldNodes = new ArrayList<>();
        this.methodNodes = new ArrayList<>();
        this.constructorNode = new ArrayList<>();
        this.symbolTable = new HashMap<>();
        this.declarationQueue = new ArrayList<>();
    }

    public void addField(FieldDeclarationNode fieldDeclarationNode) {
        fieldNodes.add(fieldDeclarationNode);
    }
    public void addMethod(FunctionNode method) {
        methodNodes.add(method);
    }
    public void addConstructor(ConstructorNode constructorNode) {
        this.constructorNode.add(constructorNode);
    }
    public void notifyParent() {
        for (FieldDeclarationNode node : fieldNodes) node.setParent(this);
        for (FunctionNode node : methodNodes)        node.setParent(this);
        for (ConstructorNode node : constructorNode) node.setParent(this);
    }

    public String getName() {
        return name;
    }
    public ConstructorNode getConstructorNode() {
        if (constructorNode.isEmpty()) {
            return null; // todo defalut constructor if needed
        }
        if (constructorNode.size() > 1) {
            throw new RuntimeException("[Multiple Definitions]: Multiple constructors not supported!");
        }
        return constructorNode.get(0);
    }
    public ArrayList<FieldDeclarationNode> getFieldNodes() {
        return fieldNodes;
    }
    public ArrayList<FunctionNode> getMethodNodes() {
        return methodNodes;
    }
    public FunctionNode getMethod(String name) {
        if (!symbolTable.containsKey(name))
            throw new RuntimeException("[Multiple Definitions]: Method member [" + name + "] not found");
        return (FunctionNode) symbolTable.get(name);
    }
    public VariableNode getField(String name) {
        if (!symbolTable.containsKey(name))
            throw new RuntimeException("[Multiple Definitions]: Field member [" + name + "] not found");
        return (VariableNode) symbolTable.get(name);
    }

    public void collectSymbol() {
        // add fields and functions to symbol table
        // if multiplied, throw exception
        symbolTable.clear();
        for (FieldDeclarationNode field : fieldNodes)
            for (String name : field.getNames()) {
                if (symbolTable.containsKey(name))
                    throw new RuntimeException("[Multiple Definitions]: field [" + name + "] already declared");
                symbolTable.put(name, new VariableNode(field.getTypeNode(), name));
            }
        for (FunctionNode method : methodNodes) {
            if (symbolTable.containsKey(method.getName()))
                throw new RuntimeException("[Multiple Definitions]: method [" + method.getName() + "] already declared");
            symbolTable.put(method.getName(), method);
        }
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    // for IR
    public HashMap<String, Integer> fieldIndex = new HashMap<>();
    public int fieldNum = 0;
    public void initFieldIndex() {
        for (FieldDeclarationNode field : fieldNodes)
            for (String name : field.getNames()) {
                fieldIndex.put(name, fieldNum++);
            }
    }
}
