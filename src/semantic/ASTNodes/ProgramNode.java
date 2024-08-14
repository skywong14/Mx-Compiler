package semantic.ASTNodes;

import semantic.GlobalScope;
import semantic.Scope;

import java.util.ArrayList;

public class ProgramNode extends ASTNode {
    private ArrayList<ClassNode> classes;
    private ArrayList<FunctionNode> functions;
    private ArrayList<VariableDeclarationNode> variables;
    // todo: delete above three lines
    private ArrayList<ASTNode> allNodes;

    public ProgramNode() {
        this.classes = new ArrayList<>();
        this.functions = new ArrayList<>();
        this.variables = new ArrayList<>();
        this.allNodes = new ArrayList<>();
    }
    public void addFunction(FunctionNode function) {
        functions.add(function);
        allNodes.add(function);
    }
    public void addClass(ClassNode class_) {
        classes.add(class_);
        allNodes.add(class_);
    }
    public void addVariable(VariableDeclarationNode variable_) {
        variables.add(variable_);
        allNodes.add(variable_);
    }

    public ArrayList<ASTNode> getAllNodes() {
        return allNodes;
    }

    public void collectSymbol(Scope globalScope) {
        for (ASTNode node : allNodes) {
            if (node instanceof FunctionNode)
                globalScope.addFunction(((FunctionNode) node).getName(), (FunctionNode) node);
            else if (node instanceof ClassNode){
                globalScope.addClass(((ClassNode) node).getName(), (ClassNode) node);
                ((ClassNode) node).collectSymbol();
            }
        }
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
