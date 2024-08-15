package semantic.ASTNodes;

import semantic.Scope;

import java.util.ArrayList;
import java.util.HashMap;

public class ClassNode extends ASTNode {
    private String name;
    private ArrayList<ConstructorNode> constructorNode;
    private ArrayList<FieldDeclarationNode> fieldNodes;
    private ArrayList<FunctionNode> methodNodes;

    private HashMap<String, ASTNode> symbolTable;

    public ClassNode(String name_) {
        this.name = name_;
        this.fieldNodes = new ArrayList<>();
        this.methodNodes = new ArrayList<>();
        this.constructorNode = new ArrayList<>();
        this.symbolTable = new HashMap<>();
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

    public String getName() {
        return name;
    }
    public ConstructorNode getConstructorNode() {
        if (constructorNode.isEmpty()) {
            return null; // todo defalut constructor
        }
        if (constructorNode.size() > 1) {
            throw new RuntimeException("Multiple constructors not supported!");
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
            throw new RuntimeException("Error: Method member [" + name + "] not found");
        return (FunctionNode) symbolTable.get(name);
    }
    public VariableNode getField(String name) {
        if (!symbolTable.containsKey(name))
            throw new RuntimeException("Error: Field member [" + name + "] not found");
        return (VariableNode) symbolTable.get(name);
    }

    public void collectSymbol() {
        // add fields and functions to symbol table
        // if multiplied, throw exception
        for (FieldDeclarationNode field : fieldNodes)
            for (String name : field.getNames()) {
                if (symbolTable.containsKey(name))
                    throw new RuntimeException("Error: field [" + name + "] already declared");
                symbolTable.put(name, new VariableNode(field.getTypeNode(), name));
            }
        for (FunctionNode method : methodNodes) {
            if (symbolTable.containsKey(method.getName()))
                throw new RuntimeException("Error: method [" + method.getName() + "] already declared");
            symbolTable.put(method.getName(), method);
        }
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
