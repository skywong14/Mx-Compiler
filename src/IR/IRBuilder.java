package IR;

import semantic.ASTNodes.*;

import java.util.ArrayList;

import IR.IRStmts.*;
import semantic.Type;

public class IRBuilder  {
    IRScope globalScope;
    ASTNode programNode;
    IRScope curScope;

    String leftValuePtr = null;

    ArrayList<IRStmt> irStmts = new ArrayList<>();

    public IRBuilder(ProgramNode programNode) {
        globalScope = new IRScope( "Global", null);
        curScope = globalScope;
        this.programNode = programNode;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (IRStmt stmt : irStmts) {
            sb.append(stmt.toString()).append("\n");
        }
        return sb.toString();
    }

    void addStmt(IRStmt stmt) {
        irStmts.add(stmt);
    }

    // 访问根节点
    public void visitProgramNode(ProgramNode it) {
        // set type: %<typename> = type { <type1>, <type2>, ... }
        // set global variable: @<varName> = global <type> <init>
        // set function: define <retType> @<funcName>(<argType1> <argName1>, <argType2> <argName2>, ...)
        // set block: { <stmt1>, <stmt2>, ... }
        // set return: ret <type> <value>

        // arrayAllocator
        addStmt(new MyIRCode());

        // declare all classes' fields
        for (ClassNode classNode : it.getClasses()) {
            addStmt(new ClassTypeDefineStmt(classNode));
        }
        // declare all functions and class methods
        for (FunctionNode function : it.getFunctions()) {
            FunctionDeclarationStmt stmt = new FunctionDeclarationStmt(function);
            curScope.addFunction(stmt.name, stmt);
            addStmt(stmt);
        }
        for (ClassNode classNode : it.getClasses()) {
            for (FunctionNode method : classNode.getMethodNodes()) {
                FunctionDeclarationStmt stmt = new FunctionDeclarationStmt(classNode, method);
                curScope.addFunction(stmt.name, stmt);
                addStmt(stmt);
            }
        }
        // declare class constructors
        // todo

        // declare all global variables
        for (VariableDeclarationNode variable : it.getVariables()) {
            for (VariableNode var : variable.getVariableNodes()) {
                addStmt(new GlobalVariableDeclareStmt(new BasicIRType(var.getType()), var.getName()));
                curScope.declareVariable(var.getName());
            }
        }
        // implement all functions and class methods
        for (FunctionNode function : it.getFunctions()) {
            visitFunctionNode(function);
        }
        for (ClassNode classNode : it.getClasses()) {
            for (FunctionNode method : classNode.getMethodNodes()) {
                visitMethodNode(classNode, method);
            }
        }
        // implement all class constructors
        // todo


        // __Mx_global_var_init (a function)


        // __Mx_main
        for (FunctionNode function : it.getFunctions()) {
            if (function.getName().equals("main")) {
                visitFunctionNode(function);
            }
        }
    }

    void enterScope(String name) {
        curScope = new IRScope(name, curScope);
    }
    void exitScope() {
        curScope = curScope.parent;
    }

    void visitFunctionNode(FunctionNode it) {
        enterScope("Function_" + it.getName());

        FunctionImplementStmt stmt = new FunctionImplementStmt(it.getName(), curScope.getFunctionDeclaration(it.getName()));

        // declare parameters
        int tmp_i = 0;
        for (ParameterNode parameter : it.getParameters()) {
            String varPtr = curScope.declareVariable(parameter.getName());
            // varPtr = alloca <type>
            stmt.addStmt(new AllocaStmt(new BasicIRType(parameter.getType()), varPtr));
            // store <type> %<i> ptr <varPtr>
            stmt.addStmt(new StoreStmt(new BasicIRType(parameter.getType()), "%" + tmp_i, varPtr));
            tmp_i++;
        }

        for (ASTNode node : it.getStatements()) {
            if (node instanceof VariableDeclarationNode) {
                visitVariableDeclarationNode((VariableDeclarationNode) node);
            } else if (node instanceof ExpressionStmtNode) {
                visitExpressionNode(((ExpressionStmtNode) node).getExpression());
            } else if (node instanceof CompoundStmtNode) {
                visitCompoundStmtNode((CompoundStmtNode) node);
            } else if (node instanceof IfStatementNode) {
                visitIfStmtNode((IfStmtNode) node);
            } else if (node instanceof WhileStmtNode) {
                visitWhileStmtNode((WhileStmtNode) node);
            } else if (node instanceof ForStmtNode) {
                visitForStmtNode((ForStmtNode) node);
            } else if (node instanceof JumpStmtNode) {
                visitJumpStmtNode((JumpStmtNode) node);
            }
        }

        exitScope();
    }

    void visitMethodNode(ClassNode classNode, FunctionNode it) {

    }

    void visitCompoundStmtNode(CompoundStmtNode it) {
        enterScope("CompoundStmt");
        for (ASTNode node : it.getStatements()) {
            if (node instanceof VariableDeclarationNode) {
                visitVariableDeclarationNode((VariableDeclarationNode) node);
            } else if (node instanceof ExpressionStmtNode) {
                visitExpressionNode(((ExpressionStmtNode) node).getExpression());
            } else if (node instanceof CompoundStmtNode) {
                visitCompoundStmtNode((CompoundStmtNode) node);
            } else if (node instanceof IfStatementNode) {
                visitIfStmtNode((IfStmtNode) node);
            } else if (node instanceof WhileStmtNode) {
                visitWhileStmtNode((WhileStmtNode) node);
            } else if (node instanceof ForStmtNode) {
                visitForStmtNode((ForStmtNode) node);
            } else if (node instanceof JumpStmtNode) {
                visitJumpStmtNode((JumpStmtNode) node);
            }
        }
        exitScope();
    }

    String visitExpressionNode(ExpressionNode it) {
        if (it instanceof BinaryExprNode) {
            return visitBinaryExprNode((BinaryExprNode) it);
        } else if (it instanceof UnaryExprNode) {
            return visitUnaryExprNode((UnaryExprNode) it);
        } else if (it instanceof TernaryExprNode) {
            return visitFunctionCallNode((TernaryExprNode) it);
        } else if (it instanceof FunctionCallNode) {
            return visitFunctionCallNode((FunctionCallNode) it);
        } else if (it instanceof NewExprNode) {
            return visitNewExprNode((NewExprNode) it);
        } else if (it instanceof NewExprNode) {
            return visitNewExprNode((NewExprNode) it);
        } else if (it instanceof ConstantNode) {
            return visitConstantNode((ConstantNode) it);
        } else if (it instanceof IdentifierNode) {
            return visitIdentifierNode((IdentifierNode) it);
        } else if (it instanceof AssignExprNode) {
            return visitAssignExprNode((AssignExprNode) it);
        }
    } // return the register storing the result

    String visitUnaryExprNode(UnaryExprNode it) {
        String register = visitExpressionNode(it.getExpression());
        String destRegister = curScope.getNewRegister();
        String operator = it.getOperator();

        if (operator.equals("~") || operator.equals("!")) {
            addStmt(new UnaryExprStmt(operator, new BasicIRType("i1"), register, destRegister));
        } else if (operator.equals("++")) {
            String destRegister2 = curScope.getNewRegister();
            addStmt(new LoadStmt(new BasicIRType("i32"), leftValuePtr, destRegister2)); // load to destRegister2
            addStmt(new UnaryExprStmt("++", new BasicIRType("i32"), destRegister2, destRegister));
            addStmt(new StoreStmt(new BasicIRType("i32"), destRegister, leftValuePtr)); // store
            if (it.isLeftOp()){
                return destRegister;
            } else {
                return destRegister2;
            }
        } else if (operator.equals("--")) {
            String destRegister2 = curScope.getNewRegister();
            addStmt(new LoadStmt(new BasicIRType("i32"), leftValuePtr, destRegister2)); // load to destRegister2
            addStmt(new UnaryExprStmt("--", new BasicIRType("i32"), destRegister2, destRegister));
            addStmt(new StoreStmt(new BasicIRType("i32"), destRegister, leftValuePtr)); // store
            if (it.isLeftOp()){
                return destRegister;
            } else {
                return destRegister2;
            }
        } else if (operator.equals("-")) {
            addStmt(new UnaryExprStmt("-", new BasicIRType("i32"), register, destRegister));
        } else if (!operator.equals("+")) {
            throw new RuntimeException("Unknown operator: " + operator);
        }
        return destRegister;
    }

    String visitBinaryExprNode(BinaryExprNode it) {
        String leftRegister = visitExpressionNode(it.getLeft());
        String rightRegister = visitExpressionNode(it.getRight());

        String Operator = it.getOperator();

        String destRegister = curScope.getNewRegister();
        if (Operator.equals("+")) {
            if (it.getLeft().deduceType(null).equals("int")) {
                addStmt(new BinaryExprStmt("add", new BasicIRType("i32"),  leftRegister, rightRegister, destRegister));
            } else {
                // is string
                //todo
            }
        } else if (Operator.equals("*") || Operator.equals("/") || Operator.equals("%")  || Operator.equals("-")) {
            switch (Operator) {
                case "*" -> addStmt(new BinaryExprStmt("mul", new BasicIRType("i32"),  leftRegister, rightRegister, destRegister));
                case "/" -> addStmt(new BinaryExprStmt("sdiv", new BasicIRType("i32"),  leftRegister, rightRegister, destRegister));
                case "%" -> addStmt(new BinaryExprStmt("srem", new BasicIRType("i32"),  leftRegister, rightRegister, destRegister));
                case "-" -> addStmt(new BinaryExprStmt("sub", new BasicIRType("i32"),  leftRegister, rightRegister, destRegister));
            }
        } else  if (Operator.equals("<<") || Operator.equals(">>") || Operator.equals("&") || Operator.equals("^") || Operator.equals("|")) {
            switch (Operator) {
                case "<<" -> addStmt(new BinaryExprStmt("shl", new BasicIRType("i32"),  leftRegister, rightRegister, destRegister));
                case ">>" -> addStmt(new BinaryExprStmt("ashr", new BasicIRType("i32"),  leftRegister, rightRegister, destRegister));
                case "&" -> addStmt(new BinaryExprStmt("and", new BasicIRType("i32"),  leftRegister, rightRegister, destRegister));
                case "^" -> addStmt(new BinaryExprStmt("xor", new BasicIRType("i32"),  leftRegister, rightRegister, destRegister));
                case "|" -> addStmt(new BinaryExprStmt("or", new BasicIRType("i32"),  leftRegister, rightRegister, destRegister));
            }
        } else if (Operator.equals("<") || Operator.equals(">") || Operator.equals("<=") || Operator.equals(">=") ){
            if (it.getLeft().deduceType(null).equals("int")) {
                switch (Operator) {
                    case "<" ->
                            addStmt(new BinaryExprStmt("icmp slt", new BasicIRType("i32"), leftRegister, rightRegister, destRegister));
                    case ">" ->
                            addStmt(new BinaryExprStmt("icmp sgt", new BasicIRType("i32"), leftRegister, rightRegister, destRegister));
                    case "<=" ->
                            addStmt(new BinaryExprStmt("icmp sle", new BasicIRType("i32"), leftRegister, rightRegister, destRegister));
                    case ">=" ->
                            addStmt(new BinaryExprStmt("icmp sge", new BasicIRType("i32"), leftRegister, rightRegister, destRegister));
                }
            } else {
                // is string
                //todo
            }
        } else if (Operator.equals("==") || Operator.equals("!=")) {
            Type type = it.getLeft().deduceType(null);
            if (type.equals("int")) {
                switch (Operator) {
                    case "==" ->
                            addStmt(new BinaryExprStmt("icmp eq", new BasicIRType("i32"), leftRegister, rightRegister, destRegister));
                    case "!=" ->
                            addStmt(new BinaryExprStmt("icmp ne", new BasicIRType("i32"), leftRegister, rightRegister, destRegister));
                }
            } else if (type.equals("bool")) {
                if (Operator.equals("==")) {
                    addStmt(new BinaryExprStmt("icmp eq", new BasicIRType("i1"), leftRegister, rightRegister, destRegister));
                } else {
                    addStmt(new BinaryExprStmt("icmp ne", new BasicIRType("i1"), leftRegister, rightRegister, destRegister));
                }
            } else if (type.equals("string")) {
                // is string
                //todo
            } else {
                // is class ptr
                // ptrtoint, then compare

            }
        } else if (Operator.equals("&&") || Operator.equals("||")) {
            if (Operator.equals("&&")) {
                addStmt(new BinaryExprStmt("and", new BasicIRType("i1"), leftRegister, rightRegister, destRegister));
            } else {
                addStmt(new BinaryExprStmt("or", new BasicIRType("i1"), leftRegister, rightRegister, destRegister));
            }
        } else {
            throw new RuntimeException("Unknown operator: " + Operator);
        }
        return destRegister;
    }

    String visitNewExprNode(NewExprNode it) {
        if (it.getIdentifier() == null) {
            // new Array
            TypeNode typeNode = it.getTypeNode();

            ConstantNode arrayConstant = it.getArrayConstant();
            if (arrayConstant == null){
                // default constructor
            } else {
                // set the array with value of arrayConstant
            }

        } else {
            // new Class
            // alloc memory
            // call <classname.classname>
            String reg = curScope.getNewRegister();
            String className = it.getIdentifier();
            addStmt(new NewClassStmt(className, reg));
        }
    }

    String visitConstantNode(ConstantNode it) {
        Type type = it.deduceType(null);
        String reg = curScope.getNewRegister();
        if (type.equals("int")) {
            addStmt(new ConstantStmt("int", it.literal_value, reg));
        } else if (type.equals("bool")) {
            addStmt(new ConstantStmt("bool", it.literal_value, reg));
        } else if (type.equals("string")) {
            // todo
        } else if (type.equals("null")) {
            addStmt(new ConstantStmt("null", null, reg));
        } else {
            // is array

        }
        return reg;
    }

    String visitIdentifierNode(IdentifierNode it) {
        // load the value of the identifier
        String register = curScope.getNewRegister();

        leftValuePtr = curScope.getVariable(it.getName());
        addStmt(new LoadStmt(new BasicIRType(it.deduceType(null)), leftValuePtr, register));

        return register;
    }

    String visitArrayAccessNode(ArrayAccessNode it) {
        // get the address of the array
        String arrayHeadPtr = visitExpressionNode(it.getHeadExpression());

        // get all the address of the index, all i32 type
        ArrayList <String> elementIndex = new ArrayList<>();
        for (ExpressionNode expression : it.getExpressions()) {
            String tmpReg = curScope.getNewRegister();
            elementIndex.add(tmpReg);
        }
        // <result> = getelementptr <ty>, ptr <ptrval>{, <ty> <idx>}*
        // get the address of the element
        String elementPtr = curScope.getNewRegister();
        addStmt(new GetElementPtrStmt(new BasicIRType(it.deduceType(null)), arrayHeadPtr, elementIndex, elementPtr));
        // load the value of the element
        String register = curScope.getNewRegister();
        addStmt(new LoadStmt(new BasicIRType(it.deduceType(null)), elementPtr, register));

        leftValuePtr = elementPtr;
        return register;
    }

    String visitAssignExprNode(AssignExprNode it) {
        String rightRegister = visitExpressionNode(it.getRight());
        visitExpressionNode(it.getLeft());
        addStmt(new StoreStmt(new BasicIRType(it.getLeft().deduceType(null)), rightRegister, leftValuePtr));
        // leftValuePtr不变
        return rightRegister;
    }
}
