package semantic.ASTNodes;

import java.util.ArrayList;

public class ClassNode extends ASTNode {
    private String name;
    private ArrayList<ConstructorNode> constructor;
    private ArrayList<VariableNode> fields;
    private ArrayList<FunctionNode> methods;

    public ClassNode(String name_) {
        this.name = name_;
        this.fields = new ArrayList<>();
        this.methods = new ArrayList<>();
    }

    public void addVariable(VariableNode variableNode) {
        fields.add(variableNode);
    }
    public void addMethod(FunctionNode method) {
        methods.add(method);
    }
    public void addConstructor(ConstructorNode constructorNode) {
        constructor.add(constructorNode);
    }

    public String getName() {
        return name;
    }
    public ConstructorNode getConstructor() {
        if (constructor.isEmpty()) {
            return null; // todo defalut constructor
        }
        if (constructor.size() > 1) {
            throw new RuntimeException("Multiple constructors not supported!");
        }
        return constructor.getFirst();
    }
    public ArrayList<VariableNode> getFields() {
        return fields;
    }
    public ArrayList<FunctionNode> getMethods() {
        return methods;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
