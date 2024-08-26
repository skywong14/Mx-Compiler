package semantic;

import semantic.ASTNodes.*;

import java.util.ArrayList;
import java.util.Objects;

public class SemanticChecker implements ASTVisitor {
    private ScopeManager scopeManager;

    private void debug(ASTNode node, String s) {
//        int spaceNum = (scopeManager.getScopeSize() - 1) * 4;
//        String space = new String(new char[spaceNum]).replace("\0", "  ");
//        System.out.println(space + node.getClass().getSimpleName() + ": " + s);
    }

    public SemanticChecker(ScopeManager scopeManager) {
        this.scopeManager = scopeManager;
    }

    // 访问根节点
    @Override
    public void visit(ProgramNode it) {
        // check if one main function exists
        debug(it, "ProgramNode");

        FunctionNode mainFunction = null;
        if (scopeManager.isFunctionDeclaredInCurrentScope("main")) {
            mainFunction = scopeManager.resolveFunction("main");
            if (mainFunction.getParameterSize() != 0) {
                throw new RuntimeException("[Type Mismatch]: main function should not have any parameters");
            }
            if (!mainFunction.getReturnType().equals("int")) {
                throw new RuntimeException("[Type Mismatch]: main function should return int");
            }
        } else {
            throw new RuntimeException("[Type Mismatch]: main function not found");
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
    public void visit(ClassNode it) {
        debug(it, it.getName());

        scopeManager.enterScope("Class_" + it.getName(), it);

        // check fieldNodes
        for (FieldDeclarationNode field : it.getFieldNodes()) {
            field.accept(this);
        }

        // add methods to symbol table
        for (FunctionNode method : it.getMethodNodes()) {
            if (scopeManager.isFunctionDeclaredInCurrentScope(method.getName())) {
                throw new RuntimeException("[Multiple Definitions]: method [" + method.getName() + "] already declared");
            }
            scopeManager.declareFunction(method.getName(), method);
        }

        // check constructorNode
        if (it.getConstructorNode() != null) {
            if (!it.getName().equals(it.getConstructorNode().getName())) {
                throw new RuntimeException("[Type Mismatch] : constructor name should be the same as class name");
            }
            it.getConstructorNode().getBody().addStatement(new JumpStmtNode("return", null));
            it.getConstructorNode().accept(this);
        } else {
            // default constructor
            // doing nothing in semantic check
        }


        // check methodNodes
        for (FunctionNode method : it.getMethodNodes()) {
            method.accept(this);
        }

        scopeManager.exitScope();
    }

    // 访问构造函数
    public void visit(ConstructorNode it) {
        debug(it, it.getName());
        // enter scope
        scopeManager.enterScope("Function_Constructor_" + it.getName(), it);
        // check body
        it.getBody().accept(this);

        scopeManager.exitScope();
    }

    // 访问函数声明
    public void visit(FunctionNode it) {
        debug(it, it.getName());
        // enter scope
        scopeManager.enterScope("Function_" + it.getName(), it);

        // check parameters
        for (ParameterNode parameter : it.getParameters()) {
            parameter.accept(this);
            scopeManager.declareVariable(parameter.getName(),
                    new VariableNode(parameter.getTypeNode(), parameter.getName()));
        }

        // check body
        for (StatementNode statement : it.getBody().getStatements()) {
            statement.accept(this);
        }

        // check if have return statement
        if (!it.getReturnType().equals("void")) {
            if (!it.getBody().hasReturnStatement()) {
                throw new RuntimeException("[Missing Return Statement]: function [" + it.getName() + "] should have return statement");
            }
        } else {
            it.getBody().addStatement(new JumpStmtNode("return", null));
        }
        // exit scope
        scopeManager.exitScope();
    }

    public void visit(StatementNode it) {
        throw new RuntimeException("[Runtime Error]: StatementNode should not be visited");
    }

    // 访问函数调用
    public void visit(FunctionCallNode it) {
        debug(it, it.getIdentifier());
        // check if function is declared
        if (scopeManager.resolveFunction(it.getIdentifier()) == null) {
            throw new RuntimeException("[Undefined Identifier]: function [" + it.getIdentifier() + "] not declared");
        }

        // check arguments
        FunctionNode function = scopeManager.resolveFunction(it.getIdentifier());
        ArgListNode argListNode = it.getArgListNode();
        assert (argListNode != null);
        // check size
        if (argListNode.getArgList().size() != function.getParameterSize()) {
            throw new RuntimeException("[Type Mismatch]: function [" + it.getIdentifier() + "] argument size mismatch");
        }
        // check type
        for (int i = 0; i < argListNode.getArgList().size(); i++) {
            ExpressionNode arg = argListNode.getArgList().get(i);

            arg.accept(this);

            if (!arg.deduceType(scopeManager).equals(function.getParameters().get(i).getType())) {
                throw new RuntimeException("[Type Mismatch]: function [" + it.getIdentifier() + "] argument type mismatch");
            }
        }
        it.deduceType(scopeManager);
    }

    // 访问变量
    public void visit(VariableNode it) {
        throw new RuntimeException("[Runtime Error]: VariableNode should not be visited");
    }

    // 访问变量声明
    public void visit(VariableDeclarationNode it) {
        debug(it, "VariableDeclarationNode");
        // check type
        it.getTypeNode().accept(this);
        Type type = it.getType();
        if (type.baseType.equals("void"))
            throw new RuntimeException("[Invalid Type]: variable type cannot be void");

        for (VariableNode variable : it.getVariableNodes()) {
            if (variable.getValue() != null) {
                variable.getValue().accept(this);
                debug(it, "Variable type: " + type.toString() + "; Value type: " + variable.getValue().deduceType(scopeManager).toString());
                if (!type.equals(variable.getValue().deduceType(scopeManager))) {
                    throw new RuntimeException("[Type Mismatch]: type mismatch in variable declaration");
                }
            }
            scopeManager.declareVariable(variable.getName(), variable);
        }
    }

    // 访问复合语句
    public void visit(CompoundStmtNode it) {
        debug(it, "CompoundStmtNode");
        scopeManager.enterScope("{}", it);
        for (StatementNode statement : it.getStatements()) {
            statement.accept(this);
        }
        scopeManager.exitScope();
    }

    // 访问跳转语句
    public void visit(JumpStmtNode it) {
        debug(it, "JumpStmtNode");
        if (it.getJumpType().equals("return")) {
            ExpressionNode expressionNode = it.getExpression();

            Type expressionType = null;
            if (expressionNode != null){
                expressionNode.accept(this);
                expressionType = expressionNode.deduceType(scopeManager);
            } else {
                expressionType = new Type("void");
            }

            // check if in function
            if (!scopeManager.isInFunction()) {
                throw new RuntimeException("[Invalid Control Flow]: return statement not in function");
            }
            // check return type
            FunctionNode function = scopeManager.getCurrentFunction();
            if (!function.getReturnType().equals(expressionType)) {
                debug(it, "Return type: " + expressionType + " Expected: " + function.getReturnType());
                throw new RuntimeException("[Type Mismatch]: return type mismatch");
            }
        } else if (it.getJumpType().equals("break") || it.getJumpType().equals("continue")) {
            // check if in loop
            if (!scopeManager.isInLoop()) {
                throw new RuntimeException("[Invalid Control Flow]: break/continue statement not in loop");
            }
        } else
            throw new RuntimeException("[Runtime Error]: unknown jump statement");
    }

    // 访问成员访问
    public void visit(MemberAccessNode it) {
        debug(it, "MemberAccessNode");
        ExpressionNode expression = it.getExpression();

        expression.accept(this);

        Type primaryType = expression.deduceType(scopeManager);

        if (it.isMethod()) {
            if (primaryType.isArray()) {
                if (it.getIdentifier().equals("size")) {
                    if (!it.getArgListNode().getArgList().isEmpty())
                        throw new RuntimeException("[Type Mismatch]: array size does not have arguments");
                    return;
                } else {
                    throw new RuntimeException("[Undefined Identifier]: array type does not have member access except size");
                }
            }
            ClassNode classNode = scopeManager.resolveClass(primaryType.getBaseType());
            FunctionNode functionNode = classNode.getMethod(it.getIdentifier());
            // check argList type
            assert (it.getArgListNode() != null);
            ArrayList<ExpressionNode> argList = it.getArgListNode().getArgList();
            if (argList.size() != functionNode.getParameterSize())
                throw new RuntimeException("[Type Mismatch]: function [" + it.getIdentifier() + "] argument size mismatch");
            for (int i = 0; i < argList.size(); i++) {
                ExpressionNode arg = argList.get(i);

                arg.accept(this);

                if (!arg.deduceType(scopeManager).equals(functionNode.getParameters().get(i).getType())) {
                    throw new RuntimeException("[Type Mismatch]: function [" + it.getIdentifier() + "] argument type mismatch");
                }
            }
        } else {
            if (primaryType.isArray())
                throw new RuntimeException("[Undefined Identifier]: Array type does not have field access");
            ClassNode classNode = scopeManager.resolveClass(expression.deduceType(scopeManager).getBaseType());
            classNode.getField(it.getIdentifier());
        }

        it.deduceType(scopeManager);
    }

    // 访问条件语句
    public void visit(IfStatementNode it) {
        debug(it, "IfStatementNode");
        it.getCondition().accept(this);
        if (!it.getCondition().deduceType(scopeManager).equals("bool")) {
            throw new RuntimeException("[Invalid Type]: if condition should be bool");
        }

        scopeManager.enterScope("If", it);
        it.getIfStatement().accept(this);
        scopeManager.exitScope();

        if (it.getElseStatement() != null) {
            scopeManager.enterScope("If_Else", it);
            it.getElseStatement().accept(this);
            scopeManager.exitScope();
        }
    }

    // 访问常量表达式
    public void visit(ConstantNode it) {
        // do nothing
        debug(it, "ConstantNode");
    }

    // 访问表达式
    public void visit(ExpressionNode it){
        throw new RuntimeException("[Runtime Error]: ExpressionNode should not be visited");
    }

    // 访问Primary表达式
    public void visit(PrimaryExpressionNode it) {
        throw new RuntimeException("[Runtime Error]: PrimaryExpressionNode should not be visited");
    }

    // 访问new表达式
    public void visit(NewExprNode it) {
        debug(it, "NewExprNode");
        TypeNode typeNode = it.getTypeNode();
        if (typeNode == null) {
            throw new RuntimeException("[Runtime Error]: type node should not be null");
        }
        typeNode.accept(this);

        if (it.getArrayConstant() != null) {
            it.getArrayConstant().accept(this);
            if (!it.getArrayConstant().deduceType(scopeManager).equals(typeNode.getType())) {
                throw new RuntimeException("[Type Mismatch]: array size should be int");
            }
        }
        it.deduceType(scopeManager);
    }

    // 访问数组类型
    public void visit(ArrayAccessNode it) {
        debug(it, "ArrayAccessNode");

        for (ExpressionNode expression : it.getExpressions()) {

            expression.accept(this);

            if (!expression.deduceType(scopeManager).equals("int"))
                throw new RuntimeException("[Type Mismatch]: array index should be int");
        }

        ExpressionNode expression = it.getHeadExpression();

        expression.accept(this);

        Type primaryType = expression.deduceType(scopeManager);

        if (!primaryType.isArray())
            throw new RuntimeException("[Type Mismatch]: not an array type");
        if (it.getExpressions().size() > primaryType.getDimension())
            throw new RuntimeException("[Dimension Out Of Bound]: array access dimension mismatch");

        it.deduceType(scopeManager);
    }

    // 访问格式化字符串
    public void visit(FormattedStringNode it) {
        debug(it, "FormattedStringNode");
        for (ExpressionNode expression : it.getExpressions()) {
            Type deducedType = expression.deduceType(scopeManager);
            if (deducedType.equals("int") || deducedType.equals("string") || deducedType.equals("bool")) {
                expression.accept(this);
            } else {
                throw new RuntimeException("[Type Mismatch]: Invalid type in formatted string");
            }
        }
    }

    // 访问形参
    public void visit(ParameterNode it) {
        debug(it, "ParameterNode");
        it.getTypeNode().accept(this);
    }
    // 访问形参列表
    public void visit(ParameterListNode it) {
        throw new RuntimeException("[Runtime Error]: ParameterListNode should not be visited");
    }

    // 访问实参列表
    public void visit(ArgListNode it){
        // should not be visited, look at FunctionCall and MethodCall
        throw new RuntimeException("[Runtime Error]: ArgListNode should not be visited");
    }

    // 访问标识符
    public void visit(IdentifierNode it) {
        debug(it, it.getName());
        it.deduceType(scopeManager);
        // doing nothing
    }

    // 访问类型
    public void visit(TypeNode it) {
        debug(it, it.getType().toString());
        // check if type is declared
        if (!scopeManager.isTypeDeclared(it.getType().getBaseType())) {
            throw new RuntimeException("[Undefined Identifier]: type " + it.getType().getBaseType() + " not declared");
        }
        // check if array size is valid
        for (ExpressionNode expression : it.getExpressions()) {
            expression.accept(this);
            if (!expression.deduceType(scopeManager).equals("int")) {
                throw new RuntimeException("[Type Mismatch]: array size should be int");
            }
        }
    }

    // EmptyStmt
    public void visit(EmptyStmtNode it) {
        // do nothing
        debug(it, "EmptyStmtNode");
    }

    // ForStmt
    public void visit(ForStmtNode it) {
        debug(it, "ForStmtNode");
        scopeManager.enterScope("Loop", it);

        // check init
        StatementNode init = it.getInit();
        assert(init != null);
        init.accept(this);

        // check condition
        ExpressionNode condition = it.getCondition();
        if (condition != null) {
            condition.accept(this);
            if (!condition.deduceType(scopeManager).equals("bool")) {
                throw new RuntimeException("[Invalid Type]: for loop condition should be bool");
            }
        }

        // check update
        if (it.getStep() != null) {
            it.getStep().accept(this);
        }

        // check body
        StatementNode body = it.getBody();
        if (body instanceof CompoundStmtNode) {
            for (StatementNode statement : ((CompoundStmtNode) body).getStatements()) {
                statement.accept(this);
            }
        } else {
            body.accept(this);
        }

        scopeManager.exitScope();
    }

    // WhileStmt
    public void visit(WhileStmtNode it) {
        debug(it, "WhileStmtNode");
        scopeManager.enterScope("Loop", it);

        // check condition
        it.getCondition().accept(this);
        if (!it.getCondition().deduceType(scopeManager).equals("bool")) {
            throw new RuntimeException("[Invalid Type]: while loop condition should be bool");
        }

        // check body
        StatementNode body = it.getBody();
        if (body instanceof CompoundStmtNode) {
            for (StatementNode statement : ((CompoundStmtNode) body).getStatements()) {
                statement.accept(this);
            }
        } else {
            body.accept(this);
        }

        scopeManager.exitScope();
    }

    // ExpressionStmt
    public void visit(ExpressionStmtNode it) {
        debug(it, "ExpressionStmtNode");
        it.getExpression().accept(this);
    }

    // FieldDeclaration
    public void visit(FieldDeclarationNode it) {
        debug(it, "FieldDeclarationNode");
        TypeNode typeNode = it.getTypeNode();
        if (typeNode.getType().baseType.equals("void"))
            throw new RuntimeException("[Invalid Type]: variable type cannot be void");
        typeNode.accept(this);
        for (String name : it.getNames()) {
            scopeManager.declareVariable(name, new VariableNode(typeNode, name));
        }
    }

    // UnaryExpr
    public void visit(UnaryExprNode it) {
        debug(it, "UnaryExprNode");
        String operator = it.getOperator();
        boolean leftOp = it.isLeftOp();
        ExpressionNode expressionNode = it.getExpression();

        expressionNode.accept(this);

        if (operator.equals("--") || operator.equals("++")) {
            if (!expressionNode.isLeftValue())
                throw new RuntimeException("[Type Mismatch]: left operator should be left value");
        }

        if (operator.equals("--") || operator.equals("++") || operator.equals("-") || operator.equals("+") || operator.equals("~")) {
            if (!expressionNode.deduceType(scopeManager).equals("int"))
                throw new RuntimeException("[Type Mismatch]: " + operator + " cannot be applied to " + expressionNode.deduceType(scopeManager));
        } else if (operator.equals("!")) {
            if (!expressionNode.deduceType(scopeManager).equals("bool"))
                throw new RuntimeException("[Type Mismatch]: " + operator + " cannot be applied to " + expressionNode.deduceType(scopeManager));
        } else
            throw new RuntimeException("Type error: unknown operator at Unary: " + operator);

        it.deduceType(scopeManager);
    }

    // BinaryExpr
    public void visit(BinaryExprNode it) {
        debug(it, "BinaryExprNode");
        String operator = it.getOperator();
        ExpressionNode left = it.getLeft();
        ExpressionNode right = it.getRight();

        left.accept(this);
        right.accept(this);

        Type leftType = left.deduceType(scopeManager);
        Type rightType = right.deduceType(scopeManager);

        if (!leftType.equals(rightType))
            throw new RuntimeException("[Type Mismatch]: Binary expression type mismatch, left: " + leftType.toString() + ", right: " + rightType.toString());

        if (Objects.equals(operator, "+") || Objects.equals(operator, "<") || Objects.equals(operator, ">")
                || Objects.equals(operator, "<=") || Objects.equals(operator, ">=")) {
            if (!leftType.equals("int") && !leftType.equals("string"))
                throw new RuntimeException("[Invalid Type]: Binary expression(+ < > <= >=) type should be int or string, but received: " + leftType.toString());
            it.deduceType(scopeManager);
            return;
        }

        if (Objects.equals(operator, "-") ||
                Objects.equals(operator, "*") || Objects.equals(operator, "/") || Objects.equals(operator, "%")) {
            if (!leftType.equals("int"))
                throw new RuntimeException("[Invalid Type]: Binary expression(-*/%) type should be int, but received: " + leftType.toString());
            it.deduceType(scopeManager);
            return;
        }
        if (Objects.equals(operator, "&") || Objects.equals(operator, "^") || Objects.equals(operator, "|")) {
            if (!leftType.equals("int"))
                throw new RuntimeException("[Invalid Type]: Binary expression(+-*/%) type should be int, but received: " + leftType.toString());
            it.deduceType(scopeManager);
            return;
        }
        if (Objects.equals(operator, "==") || Objects.equals(operator, "!=")){
            it.deduceType(scopeManager);
            return;
        }
        if (Objects.equals(operator, "&&") || Objects.equals(operator, "||")){
            if (!leftType.equals("bool"))
                throw new RuntimeException("[Invalid Type]: Binary expression(&&,||) type should be bool, but received: " + leftType.toString());
            it.deduceType(scopeManager);
            return;
        }
        if (Objects.equals(operator, "<<") || Objects.equals(operator, ">>")) {
            if (!leftType.equals("int"))
                throw new RuntimeException("[Invalid Type]: Binary expression(<<,>>) type should be int, but received: " + leftType.toString());
            it.deduceType(scopeManager);
            return;
        }
        if (Objects.equals(operator, "=")) {
            throw new RuntimeException("[Runtime Error]: Assignment operator should not be here");
        }
        throw new RuntimeException("[Runtime Error]: Unknown operator at BinaryExpr: " + operator);
    }

    // TernaryExpr
    public void visit(TernaryExprNode it) {
        debug(it, "TernaryExprNode");
        ExpressionNode condition = it.getCondition();
        ExpressionNode trueExpr = it.getTrueExpr();
        ExpressionNode falseExpr = it.getFalseExpr();

        condition.accept(this);
        trueExpr.accept(this);
        falseExpr.accept(this);

        if (!condition.deduceType(scopeManager).equals("bool"))
            throw new RuntimeException("[Type Mismatch]: condition in ternary expression should be bool");

        if (!trueExpr.deduceType(scopeManager).equals(falseExpr.deduceType(scopeManager)))
            throw new RuntimeException("[Type Mismatch]: type mismatch in ternary expression");

        it.deduceType(scopeManager);
    }

    public void visit(AssignExprNode it) {
        ExpressionNode left = it.getLeft();
        ExpressionNode right = it.getRight();

        left.accept(this);
        right.accept(this);

        Type leftType = left.deduceType(scopeManager);
        Type rightType = right.deduceType(scopeManager);

        if (!leftType.equals(rightType))
            throw new RuntimeException("[Type Mismatch]: Binary expression type mismatch, left: " + leftType.toString() + ", right: " + rightType.toString());

        // 函数的形参变量 / 全局变量和局部变量 / 类的一个成员 / 数组对象的一个元素
        // this 指针作为左值视为语法错误 / 常量作为左值视为语法错误
        if (rightType.equals("void"))
            throw new RuntimeException("[Type Mismatch]: right value should not be void");
        if (!left.isLeftValue()) {
            throw new RuntimeException("[Type Mismatch]: left value should be assignable");
        }
        it.deduceType(scopeManager);
    }
}
