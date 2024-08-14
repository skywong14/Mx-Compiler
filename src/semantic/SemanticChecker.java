package semantic;

import semantic.ASTNodes.*;

public class SemanticChecker implements ASTVisitor {
    private ScopeManager scopeManager;
    public SemanticChecker (ScopeManager scopeManager) {
        this.scopeManager = scopeManager;
    }

    // 访问根节点
    @Override
    public void visit(ProgramNode it){
        // check if one main function exists
        FunctionNode mainFunction = null;
        if (scopeManager.isFunctionDeclaredInCurrentScope("main")) {
            mainFunction = scopeManager.resolveFunction("main");
            if (mainFunction.getParameterSize() != 0) {
                throw new RuntimeException("Error: main function should not have any parameters");
            }
            if (!mainFunction.getReturnType().equals("int")) {
                throw new RuntimeException("Error: main function should return int");
            }
        } else {
            throw new RuntimeException("Error: main function not found");
        }

        // add "return 0;" to the end of main function
        JumpStmtNode returnStmt = new JumpStmtNode("return", new ConstantNode("0", false));
        mainFunction.getBody().addStatement(returnStmt);

        // check in sequence
        for (ASTNode node : it.getAllNodes()) {
            if (node instanceof FunctionNode) {
                FunctionNode function = (FunctionNode) node;
                function.accept(this);
            } else if (node instanceof ClassNode) {
                ClassNode class_ = (ClassNode) node;
                class_.accept(this);
            } else if (node instanceof VariableDeclarationNode) {
                VariableDeclarationNode variable = (VariableDeclarationNode) node;
                variable.accept(this);
            }
        }
    }

    // 访问类声明
    public void visit(ClassNode it){
        // enter scope
        scopeManager.enterScope(it.getName());

        // check constructorNode
        if (it.getConstructorNode() != null) {
            it.getConstructorNode().accept(this);
        } else {
            // default constructor
            // doing nothing in semantic check
        }

        // check fieldNodes
        for (FieldDeclarationNode field : it.getFieldNodes()) {
            field.accept(this);
        }
        // check methodNodes
        for (FunctionNode method : it.getMethodNodes()) {
            method.accept(this);
        }
    }

    // 访问构造函数
    public void visit(ConstructorNode it){
        // check body
        it.getBody().accept(this);
    }

    // 访问函数声明
    public void visit(FunctionNode it) {
        // enter scope
        scopeManager.enterScope(it.getName());

        // check parameters
        for (ParameterNode parameter : it.getParameters()) {
            parameter.accept(this);
            scopeManager.declareVariable(parameter.getName(),
                    new VariableNode(parameter.getTypeNode(), parameter.getName()));
        }

        // check body
        for (StatementNode statement : it.getBody().getStatement()) {
            statement.accept(this);
        }

        // exit scope
        scopeManager.exitScope();
    }

    // 访问函数调用
    public void visit(FunctionCallNode it) {
        // check if function is declared
        if (scopeManager.resolveFunction(it.getIdentifier()) == null) {
            throw new RuntimeException("Error: function [" + it.getIdentifier() + "] not declared");
        }

        // check arguments
        FunctionNode function = scopeManager.resolveFunction(it.getIdentifier());
        ArgListNode argListNode = it.getArgListNode();
        assert(argListNode != null);
        // check size
        if (argListNode.getArgList().size() != function.getParameterSize()) {
            throw new RuntimeException("Error: function [" + it.getIdentifier() + "] argument size mismatch");
        }
        // check type
        for (int i = 0; i < argListNode.getArgList().size(); i++) {
            ExpressionNode arg = argListNode.getArgList().get(i);

            arg.accept(this);

            if (!arg.deduceType(scopeManager).equals(function.getParameters().get(i).getType())) {
                throw new RuntimeException("Error: function [" + it.getIdentifier() + "] argument type mismatch");
            }
        }
    }

    // 访问变量
    // public void visit(VariableNode it);

    // 访问变量声明
    public void visit(VariableDeclarationNode it) {
        // check type
        it.getTypeNode().accept(this);
        Type type = it.getType();
        for (VariableNode variable : it.getVariableNodes()) {
            if (variable.getValue() != null) {
                variable.getValue().accept(this);
                if (!type.equals(variable.getValue().deduceType(scopeManager))) {
                    throw new RuntimeException("Error: type mismatch in variable declaration");
                }
            }
            scopeManager.declareVariable(variable.getName(), variable);
        }
    }

    // 访问复合语句
    public void visit(CompoundStmtNode it){
        scopeManager.enterScope();
        for (StatementNode statement : it.getStatement()) {
            statement.accept(this);
        }
        scopeManager.exitScope();
    }

    // 访问跳转语句
    public void visit(JumpStmtNode it);

    // 访问条件语句
    public void visit(IfStatementNode it);

    // 访问常量表达式
    public void visit(ConstantNode it);

    // 访问表达式
    // public void visit(ExpressionNode it);

    // 访问主表达式
    // public void visit(PrimaryExpressionNode it);

    // 访问new表达式
    public void visit(NewExprNode it);

    // 访问数组类型
    public void visit(ArrayAccessNode it);

    // 访问格式化字符串
    public void visit(FormattedStringNode it) {
        for (ExpressionNode expression : it.getExpressions()) {
            expression.accept(this);
        }
    }

    // 访问形参
    public void visit(ParameterNode it);
    // 访问形参列表
    public void visit(ParameterListNode it);

    // 访问实参
    public void visit(ArgNode it);
    // 访问实参列表
    public void visit(ArgListNode it);

    // 访问标识符
    // public void visit(IdentifierNode it);

    // 访问类型
    public void visit(TypeNode it) {
        // check if type is declared
        if (!scopeManager.isTypeDeclared(it.getType())) {
            throw new RuntimeException("Error: type " + it.getType() + " not declared");
        }
    }

    // EmptyStmt
    public void visit(EmptyStmtNode it);

    // ForStmt
    public void visit(ForStmtNode it);

    // WhileStmt
    public void visit(WhileStmtNode it);

    // ExpressionStmt
    public void visit(ExpressionStmtNode it);

    // FieldDeclaration
    public void visit(FieldDeclarationNode it);
}
