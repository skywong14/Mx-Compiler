package IR;

import parser.MxParser;
import semantic.ASTNodes.*;

import java.util.ArrayList;
import java.util.HashMap;

import IR.IRStmts.*;
import semantic.Type;

public class IRBuilder  {
    IRScope globalScope;
    ASTNode programNode;
    IRScope curScope;

    String leftValuePtr = null;

    ArrayList<IRStmt> irStmts = new ArrayList<>();
    ArrayList<IRStmt> constantStmts = new ArrayList<>();

    HashMap<String, HashMap<String, Integer> > classFieldIndex = new HashMap<>();

    FunctionImplementStmt curFunction;
    String curLoopContinue = null;
    String curLoopEnd = null;
    ClassNode curClassNode = null;

    HashMap<String, String> stringLiteral = new HashMap<>();
    HashMap<String, Boolean> hasConstructor = new HashMap<>();

    public IRBuilder(ProgramNode programNode) {
        globalScope = new IRScope( "Global", null);
        curScope = globalScope;
        this.programNode = programNode;
        this.curFunction = null;
    }

    private static void debug(String msg) {
        System.out.println("IR: " + msg);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        // debug(String.valueOf(irStmts.size()));
        for (IRStmt stmt : irStmts) {
            sb.append(stmt.toString()).append("\n");
        }
        return sb.toString();
    }

    void addStmt(IRStmt stmt) {
        if (curFunction != null) {
            curFunction.addStmt(stmt);
        } else {
            irStmts.add(stmt);
        }
    }

    int getFieldIndex(String className, String fieldName) {
        if (classFieldIndex.containsKey(className)) {
            HashMap<String, Integer> fieldIndex = classFieldIndex.get(className);
            if (fieldIndex.containsKey(fieldName)) {
                return fieldIndex.get(fieldName);
            }
        }
        throw new RuntimeException("Field not found: " + className + "." + fieldName);
    }

    // 访问根节点
    public void visitProgramNode(ProgramNode it) {
        // arrayAllocator
         addStmt(new MyIRCode());

        // declare all classes' fields
        for (ClassNode classNode : it.getClasses()) {
            ClassTypeDefineStmt stmt = new ClassTypeDefineStmt(classNode);
            classFieldIndex.put(classNode.getName(), stmt.getFieldIndex());
            addStmt(stmt);
            hasConstructor.put(classNode.getName(), classNode.getConstructorNode() != null);
        }
        // declare all functions and class methods
        for (FunctionNode function : it.getFunctions()) {
            FunctionDeclarationStmt stmt = new FunctionDeclarationStmt(function);
            curScope.addFunction(stmt.name, stmt);
//            addStmt(stmt);
        }
        for (ClassNode classNode : it.getClasses()) {
            for (FunctionNode method : classNode.getMethodNodes()) {
                FunctionDeclarationStmt stmt = new FunctionDeclarationStmt(classNode, method);
                curScope.addFunction(stmt.name, stmt);
//                addStmt(stmt);
            }
        }
        // declare class constructors
        for (ClassNode classNode : it.getClasses()) {
            if (classNode.getConstructorNode() != null) {
                FunctionDeclarationStmt stmt = new FunctionDeclarationStmt(classNode, classNode.getConstructorNode());
                curScope.addFunction(stmt.name, stmt);
//                addStmt(stmt);
            }
        }

        // declare all global variables
        for (VariableDeclarationNode variable : it.getVariables()) {
            for (VariableNode var : variable.getVariableNodes()) {
                addStmt(new GlobalVariableDeclareStmt(new BasicIRType(var.getType()), var.getName()));
                curScope.declareVariable(var.getName());
            }
        }
        // implement all functions and class methods
        for (FunctionNode function : it.getFunctions()) {
            if (!function.getName().equals("main")) {
                visitFunctionNode(function);
            }
        }
        for (ClassNode classNode : it.getClasses()) {
            visitClassNode(classNode);
        }

        // __Mx_global_var_init (a function)


        // __Mx_main
        for (FunctionNode function : it.getFunctions()) {
            if (function.getName().equals("main")) {
                visitFunctionNode(function);
            }
        }

        // add constantStmts to the head of irStmts
        irStmts.addAll(0, constantStmts);
    }

    void enterScope(String name) {
        curScope = new IRScope(name, curScope);
    }
    void exitScope() {
        curScope = curScope.parent;
    }

    void visitClassNode(ClassNode it) {
        enterScope("Class_" + it.getName());
        curClassNode = it;

//        for (FieldDeclarationNode field : it.getFieldNodes()) {
//            for (String name : field.getNames()) {
//                curScope.declareVariable(name);
//            }
//        }
        // implement all the methods and constructors

        if (it.getConstructorNode() != null) {
            visitMethodNode(it.getConstructorNode());
        }

        for (FunctionNode method : it.getMethodNodes()) {
            visitMethodNode(method);
        }

        curClassNode = null;
        exitScope();
    }

    void visitMethodNode(FunctionNode it) {
        // the first parameter must be %this
        enterScope("Function_" + curClassNode.getName() + "_" + it.getName());

        FunctionImplementStmt stmt = new FunctionImplementStmt(curClassNode.getName() + "." + it.getName(),
                curScope.getFunctionDeclaration(curClassNode.getName() + "." + it.getName()));
        curFunction = stmt;

        // declare parameters
        for (ParameterNode parameter : it.getParameters()) {
           curScope.declareVariable(parameter.getName());
            String varPtr = curScope.getVariable(parameter.getName());
            // varPtr = alloca <type>
            stmt.addStmt(new AllocaStmt(new BasicIRType(parameter.getType()), varPtr));
            // store <type> %<i> ptr <varPtr>
            stmt.addStmt(new StoreStmt(new BasicIRType(parameter.getType()), "%" + parameter.getName(), varPtr));
        }

        for (StatementNode node : it.getBody().getStatements()) {
            visitStatementNode(node);
            if (node instanceof JumpStmtNode) break;
        }

        irStmts.add(stmt);
        curFunction = null;
        exitScope();
    }

    void visitFunctionNode(FunctionNode it) {
        enterScope("Function_" + it.getName());

        FunctionImplementStmt stmt = new FunctionImplementStmt(it.getName(), curScope.getFunctionDeclaration(it.getName()));
        curFunction = stmt;

        // declare parameters
        int tmp_i = 0;
        for (ParameterNode parameter : it.getParameters()) {
            curScope.declareVariable(parameter.getName());
            String varPtr = curScope.getVariable(parameter.getName());
                    // varPtr = alloca <type>
            stmt.addStmt(new AllocaStmt(new BasicIRType(parameter.getType()), varPtr));
            // store <type> %<i> ptr <varPtr>
            stmt.addStmt(new StoreStmt(new BasicIRType(parameter.getType()), "%" + tmp_i, varPtr));
            tmp_i++;
        }

        for (StatementNode node : it.getBody().getStatements()) {
            visitStatementNode(node);
            if (node instanceof JumpStmtNode) break;
        }

        irStmts.add(stmt);
        curFunction = null;
        exitScope();
    }

    void visitStatementNode(StatementNode node) {
        if (node instanceof VariableDeclarationNode) {
            visitVariableDeclarationNode((VariableDeclarationNode) node);
        } else if (node instanceof ExpressionStmtNode) {
            visitExpressionNode(((ExpressionStmtNode) node).getExpression());
        } else if (node instanceof CompoundStmtNode) {
            visitCompoundStmtNode((CompoundStmtNode) node);
        } else if (node instanceof IfStatementNode) {
            visitIfStmtNode((IfStatementNode) node);
        } else if (node instanceof WhileStmtNode) {
            visitWhileStmtNode((WhileStmtNode) node);
        } else if (node instanceof ForStmtNode) {
            visitForStmtNode((ForStmtNode) node);
        } else if (node instanceof JumpStmtNode) {
            visitJumpStmtNode((JumpStmtNode) node);
        }
    }

    void visitVariableDeclarationNode(VariableDeclarationNode it) {
        for (VariableNode var : it.getVariableNodes()) {
            curScope.declareVariable(var.getName());
            String varPtr = curScope.getVariable(var.getName());
            addStmt(new AllocaStmt(new BasicIRType(var.getType()), varPtr));
            if (var.getValue() != null) {
                String register = visitExpressionNode(var.getValue());
                addStmt(new StoreStmt(new BasicIRType(var.getType()), register, varPtr));
            }
        }
    }

    void visitIfStmtNode(IfStatementNode it) {
        String thenBlock = curScope.getNewBlock();
        String elseBlock = curScope.getNewBlock();
        String endBlock = curScope.getNewBlock();

        String conditionRegister = visitExpressionNode(it.getCondition());
        curFunction.addStmt(new BranchStmt(conditionRegister, thenBlock, elseBlock));

        curFunction.newBlock(thenBlock);
        visitStatementNode(it.getIfStatement());
        curFunction.addStmt(new BranchStmt(endBlock));


        curFunction.newBlock(elseBlock);
        if (it.getElseStatement() != null) {
            visitStatementNode(it.getElseStatement());
        }
        curFunction.addStmt(new BranchStmt(endBlock));

        curFunction.newBlock(endBlock);
    }

    void visitForStmtNode(ForStmtNode it) {
        enterScope("Loop_ForStmt");
        String conditionBlock = curScope.getNewBlock();
        String stepBlock = curScope.getNewBlock();
        String bodyBlock = curScope.getNewBlock();
        String endBlock = curScope.getNewBlock();
        String lstEnd = curLoopEnd;
        String lstContinue = curLoopContinue;
        curLoopEnd = endBlock;
        curLoopContinue = stepBlock;

        // init
        visitStatementNode(it.getInit());
        curFunction.addStmt(new BranchStmt(conditionBlock));

        // condition
        curFunction.newBlock(conditionBlock);
        String conditionRegister = visitExpressionNode(it.getCondition());
        curFunction.addStmt(new BranchStmt(conditionRegister, bodyBlock, endBlock));

        // body
        curFunction.newBlock(bodyBlock);
        visitStatementNode(it.getBody());
        curFunction.addStmt(new BranchStmt(stepBlock));

        // step
        curFunction.newBlock(stepBlock);
        visitExpressionNode(it.getStep());
        curFunction.addStmt(new BranchStmt(conditionBlock));

        // end

        curFunction.newBlock(endBlock);
        exitScope();
        curLoopEnd = lstEnd;
        curLoopContinue = lstContinue;
    }

    void visitWhileStmtNode(WhileStmtNode it) {
        enterScope("Loop_WhileStmt");
        String conditionBlock = curScope.getNewBlock();
        String bodyBlock = curScope.getNewBlock();
        String endBlock = curScope.getNewBlock();
        String lstEnd = curLoopEnd;
        String lstContinue = curLoopContinue;
        curLoopEnd = endBlock;
        curLoopContinue = conditionBlock;

        curFunction.addStmt(new BranchStmt(conditionBlock));

        // condition
        curFunction.newBlock(conditionBlock);
        String conditionRegister = visitExpressionNode(it.getCondition());
        curFunction.addStmt(new BranchStmt(conditionRegister, bodyBlock, endBlock));

        // body
        curFunction.newBlock(bodyBlock);
        visitStatementNode(it.getBody());
        curFunction.addStmt(new BranchStmt(conditionBlock));

        // end
        curFunction.newBlock(endBlock);
        exitScope();
        curLoopEnd = lstEnd;
        curLoopContinue = lstContinue;
    }

    void visitJumpStmtNode(JumpStmtNode it) {
        if (it.getJumpType().equals("break")) {
            curFunction.addStmt(new BranchStmt(curLoopEnd));
        } else if (it.getJumpType().equals("continue")) {
            curFunction.addStmt(new BranchStmt(curLoopContinue));
        } else if (it.getJumpType().equals("return")) {
            String register = visitExpressionNode(it.getExpression());
            curFunction.addStmt(new ReturnStmt(new BasicIRType(it.getExpression().deduceType(null)), register));
        }
    }

    void visitCompoundStmtNode(CompoundStmtNode it) {
        enterScope("CompoundStmt");
        for (StatementNode node : it.getStatements()) {
            visitStatementNode(node);
            if (node instanceof JumpStmtNode) break;
        }
        exitScope();
    }

    String visitExpressionNode(ExpressionNode it) {
        if (it instanceof BinaryExprNode) {
            return visitBinaryExprNode((BinaryExprNode) it);
        } else if (it instanceof UnaryExprNode) {
            return visitUnaryExprNode((UnaryExprNode) it);
        } else if (it instanceof TernaryExprNode) {
            return visitTernaryExprNode((TernaryExprNode) it);
        } else if (it instanceof FunctionCallNode) {
            return visitFunctionCallNode((FunctionCallNode) it);
        } else if (it instanceof NewExprNode) {
            return visitNewExprNode((NewExprNode) it);
        } else if (it instanceof ConstantNode) {
            return visitConstantNode((ConstantNode) it);
        } else if (it instanceof IdentifierNode) {
            return visitIdentifierNode((IdentifierNode) it);
        } else if (it instanceof AssignExprNode) {
            return visitAssignExprNode((AssignExprNode) it);
        } else if (it instanceof MemberAccessNode) {
            return visitMemberAccessNode((MemberAccessNode) it);
        } else if (it instanceof ArrayAccessNode) {
            return visitArrayAccessNode((ArrayAccessNode) it);
        } else if (it instanceof FormattedStringNode) {
            return visitFormattedStringNode((FormattedStringNode) it);
        }
        throw new RuntimeException("Unknown expression node: " + it);
    } // return the register storing the result

    String visitFunctionCallNode(FunctionCallNode it) {
        // call <funcname>(<arg1>, <arg2>, ...)
        ArrayList<String> args = new ArrayList<>();
        ArrayList<BasicIRType> argTypes = new ArrayList<>();
        for (ExpressionNode arg : it.getArgListNode().getArgList()) {
            args.add(visitExpressionNode(arg));
            argTypes.add(new BasicIRType(arg.deduceType(null)));
        }
        String retReg = curScope.getNewRegister();

        if (curClassNode != null) {
            if (globalScope.functions.containsKey(curClassNode.getName() + "." + it.getIdentifier())) {
                // if in a class method, add this as the first argument
                argTypes.add(0, new BasicIRType("ptr"));
                args.add(0, "%this");
                addStmt(new CallStmt(new BasicIRType(it.deduceType(null)),
                        curClassNode.getName() + "." + it.getIdentifier(), argTypes, args, retReg));
            } else {
                addStmt(new CallStmt(new BasicIRType(it.deduceType(null)), it.getIdentifier(), argTypes, args, retReg));
            }
        } else {
            addStmt(new CallStmt(new BasicIRType(it.deduceType(null)), it.getIdentifier(), argTypes, args, retReg));
        }
        return retReg;
    }

    String visitMemberAccessNode(MemberAccessNode it) {
        if (it.isMethod()) {
            // expression '.' IDENTIFIER '(' arglist? ')'
            // call <classname.methodName>(<expression>, <arg1>, <arg2>, ...)
            String classInstance = visitExpressionNode(it.getExpression());
            ArrayList<String> args = new ArrayList<>();
            ArrayList<BasicIRType> argTypes = new ArrayList<>();
            argTypes.add(new BasicIRType("ptr"));
            if (curClassNode != null) {
                args.add("%this");
            } else {
                args.add(classInstance);
            }
            for (ExpressionNode arg : it.getArgListNode().getArgList()) {
                args.add(visitExpressionNode(arg));
                argTypes.add(new BasicIRType(arg.deduceType(null)));
            }
            String retReg = curScope.getNewRegister();
            addStmt(new CallStmt(new BasicIRType(it.deduceType(null)),
                    it.getExpression().deduceType(null).baseType + "." +  it.getIdentifier(), argTypes, args, retReg));
            return retReg;
        } else {
            // expression '.' IDENTIFIER
            // get the address of the field
            String classInstance = visitExpressionNode(it.getExpression());
            String fieldPtr = curScope.getNewRegister();
            String fieldName = it.getIdentifier();
            int index = getFieldIndex(it.getExpression().deduceType(null).baseType, fieldName);
            addStmt(new GetElementPtrStmt(new BasicIRType(it.deduceType(null)), classInstance, Integer.toString(index), fieldPtr));
            // load the value of the field
            String register = curScope.getNewRegister();
            addStmt(new LoadStmt(new BasicIRType(it.deduceType(null)), fieldPtr, register));
            leftValuePtr = fieldPtr;
            return register;
        }
    }

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
        String Operator = it.getOperator();
        String curBlockLabel = curFunction.curBlockLabel();
        if (Operator.equals("&&") || Operator.equals("||")) {
            // phi i1 [true, %curBlock], [false %endBlock]
            String leftRegister = visitExpressionNode(it.getLeft());
            String nextBlock = curScope.getNewBlock();
            String endBlock = curScope.getNewBlock();

            if (Operator.equals("&&")) {
                addStmt(new BranchStmt(leftRegister, nextBlock, endBlock));
            } else {
                addStmt(new BranchStmt(leftRegister, endBlock, nextBlock));
            }

            curFunction.newBlock(nextBlock);
            String rightRegister = visitExpressionNode(it.getRight());
            addStmt(new BranchStmt(rightRegister, endBlock, endBlock));

            curFunction.newBlock(endBlock);
            String destRegister = curScope.getNewRegister();
            if (Operator.equals("&&")) {
                addStmt(new PhiStmt(new BasicIRType("i1"), new String[]{"false", "true"},
                        new String[]{"%" + curBlockLabel, "%" + nextBlock}, destRegister));
            } else {
                addStmt(new PhiStmt(new BasicIRType("i1"), new String[]{"true", "false"},
                        new String[]{"%" + curBlockLabel, "%" + nextBlock}, destRegister));
            }
            return destRegister;
        }



        String leftRegister = visitExpressionNode(it.getLeft());
        String rightRegister = visitExpressionNode(it.getRight());

        String destRegister = curScope.getNewRegister();
        if (Operator.equals("+")) {
            if (it.getLeft().deduceType(null).equals("int")) {
                addStmt(new BinaryExprStmt("add", new BasicIRType("i32"),  leftRegister, rightRegister, destRegister));
            } else {
                // is string, call string.add
                ArrayList<BasicIRType> argTypes = new ArrayList<>(); argTypes.add(new BasicIRType("ptr")); argTypes.add(new BasicIRType("ptr"));
                ArrayList<String> args = new ArrayList<>(); args.add(leftRegister); args.add(rightRegister);
                addStmt(new CallStmt(new BasicIRType("ptr"), "string.add", argTypes, args, destRegister));
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
                ArrayList<BasicIRType> argTypes = new ArrayList<>(); argTypes.add(new BasicIRType("ptr")); argTypes.add(new BasicIRType("ptr"));
                ArrayList<String> args = new ArrayList<>(); args.add(leftRegister); args.add(rightRegister);
                switch (Operator) {
                    case "<" ->
                        addStmt(new CallStmt(new BasicIRType("i1"), "string.less", argTypes, args, destRegister));
                    case ">" ->
                        addStmt(new CallStmt(new BasicIRType("i1"), "string.greater", argTypes, args, destRegister));
                    case "<=" ->
                        addStmt(new CallStmt(new BasicIRType("i1"), "string.lessEqual", argTypes, args, destRegister));
                    case ">=" ->
                        addStmt(new CallStmt(new BasicIRType("i1"), "string.greaterEqual", argTypes, args, destRegister));
                }
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
                // string_equal, string_notEqual
                ArrayList<BasicIRType> argTypes = new ArrayList<>(); argTypes.add(new BasicIRType("ptr")); argTypes.add(new BasicIRType("ptr"));
                ArrayList<String> args = new ArrayList<>(); args.add(leftRegister); args.add(rightRegister);
                if (Operator.equals("=="))
                    addStmt(new CallStmt(new BasicIRType("i1"), "string.equal", argTypes, args, destRegister));
                else
                    addStmt(new CallStmt(new BasicIRType("i1"), "string.notEqual", argTypes, args, destRegister));
            } else {
                // is class ptr
                ArrayList<BasicIRType> argTypes = new ArrayList<>(); argTypes.add(new BasicIRType("ptr")); argTypes.add(new BasicIRType("ptr"));
                ArrayList<String> args = new ArrayList<>(); args.add(leftRegister); args.add(rightRegister);
                if (Operator.equals("=="))
                    addStmt(new CallStmt(new BasicIRType("i1"), "__ptrEqual__", argTypes, args, destRegister));
                else
                    addStmt(new CallStmt(new BasicIRType("i1"), "__ptrNotEqual__", argTypes, args, destRegister));
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

            String retReg = curScope.getNewRegister();

            // retReg = generateMultiDimensionalArrayIR(typeNode, arrayConstant);

            if (arrayConstant != null) {
                // set the array with value of arrayConstant
                // ptr __arrayDeepCopy__(ptr arrPtr)


            }
            return "todo";
        } else {
            // new Class
            // alloc memory
            // call <classname.classname>
            String reg = curScope.getNewRegister();
            String tmp1 = curScope.getNewRegister();
            String tmp2 = curScope.getNewRegister();
            String className = it.getIdentifier();

            addStmt(new NewClassStmt(className, tmp1, tmp2, reg, hasConstructor.get(className)));
            return reg;
        }
    }

    String stringDeclareInGlobal(String str) {
        if (stringLiteral.containsKey(str)) {
            return stringLiteral.get(str);
        } else {
            String strPtr = "@.str." + stringLiteral.size();
            stringLiteral.put(str, strPtr);
            constantStmts.add(new StringDeclareStmt(str, strPtr));
            return strPtr;
            // @my_string = global i8* getelementptr inbounds ([4 x i8], [4 x i8]* @.str, i32 0, i32 0)
        }
        // @.str = private unnamed_addr constant [4 x i8] c"Mx*\00"
    }

    String arrayDeclareInGlobal(ConstantNode it) {

    }

    String visitConstantNode(ConstantNode it) {
        Type type = it.deduceType(null);
        String reg;
        if (type.equals("int")) {
            reg = it.literal_value;
            // addStmt(new ConstantStmt("i32", it.literal_value, reg));
        } else if (type.equals("bool")) {
            reg = it.literal_value;
            // addStmt(new ConstantStmt("i1", it.literal_value, reg));
        } else if (type.equals("string")) {
            reg = stringDeclareInGlobal(it.literal_value); //todo
        } else if (type.equals("null")) {
            reg = "null";
            // addStmt(new ConstantStmt("ptr", "null", reg));
        } else {
            reg = arrayDeclareInGlobal(it);
        }
        return reg;
    }

    String visitIdentifierNode(IdentifierNode it) {
        // load the value of the identifier
        if (it.getName().equals("this")) {
            return "%this";
        }

        if (curScope.isField(it.getName())) {
            // tmpReg = getelementptr <classType>, ptr %this, i32 0, i32 <fieldIndex>
            // retReg = load <fieldType> tmpReg
            String tmpReg = curScope.getNewRegister();
            String retReg = curScope.getNewRegister();

            addStmt(new GetElementPtrStmt(new BasicIRType(it.deduceType(null)),
                    "%this", Integer.toString(getFieldIndex(curClassNode.name, it.getName())), tmpReg));
            addStmt(new LoadStmt(new BasicIRType(it.deduceType(null)), tmpReg, retReg));
            leftValuePtr = tmpReg;
            return retReg;
        } else {
            // get the value of the identifier
            String register = curScope.getNewRegister();

            leftValuePtr = curScope.getVariable(it.getName());
            addStmt(new LoadStmt(new BasicIRType(it.deduceType(null)), leftValuePtr, register));

            return register;
        }
    }

    String visitTernaryExprNode(TernaryExprNode it) {
        String trueBlock = curScope.getNewBlock();
        String falseBlock = curScope.getNewBlock();
        String endBlock = curScope.getNewBlock();
        String retReg = curScope.getNewRegister();

        String conditionRegister = visitExpressionNode(it.getCondition());
        curFunction.addStmt(new BranchStmt(conditionRegister, trueBlock, falseBlock));

        curFunction.newBlock(trueBlock);
        String trueReg = visitExpressionNode(it.getTrueExpr());
        curFunction.addStmt(new BranchStmt(endBlock));

        curFunction.newBlock(falseBlock);
        String falseReg = visitExpressionNode(it.getFalseExpr());
        curFunction.addStmt(new BranchStmt(endBlock));

        curFunction.newBlock(endBlock);
        curFunction.addStmt(new SelectStmt(new BasicIRType(it.deduceType(null)), conditionRegister, trueReg, falseReg, retReg));

        return retReg;
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

    String visitFormattedStringNode(FormattedStringNode it) {
        int size = it.getExpressions().size();
        String reg0 = stringDeclareInGlobal(it.getPlainStrings().get(0));
        for (int i = 0; i < size; i++) {
            String reg = visitExpressionNode(it.getExpressions().get(i));
            // todo
            String regStr = stringDeclareInGlobal(it.getPlainStrings().get(i + 1));
            // todo
        }
        return ;
    }

    public static String generateMultiDimensionalArrayIR(int n, ArrayList<String> sizeRegisters, int currentDimension) {
        StringBuilder ir = new StringBuilder();

        // 获取当前维度的size寄存器
        String currentSize = sizeRegisters.get(currentDimension);

        // 调用alloc_array函数分配当前维度的数组
        String arrayPtr = "%arrayPtr" + currentDimension;
        ir.append(arrayPtr).append(" = call ptr @alloc_array(i32 ").append(currentSize).append(", i32 4)\n");

        if (currentDimension < n - 1) {
            // 如果还没有到达最后一个维度，递归生成内部数组
            String loopIndex = "%i" + currentDimension;
            String loopEnd = "%loopEnd" + currentDimension;
            String loopStartLabel = "loop_start_" + currentDimension;
            String loopEndLabel = "loop_end_" + currentDimension;

            // 创建循环，递归为每个子数组分配内存
            ir.append(loopIndex).append(" = alloca i32\n");
            ir.append("store i32 0, ptr ").append(loopIndex).append("\n");
            ir.append(loopEnd).append(" = load i32, ptr ").append(currentSize).append("\n");

            ir.append(loopStartLabel).append(":\n");
            ir.append("  %idx = load i32, ptr ").append(loopIndex).append("\n");
            ir.append("  %cond = icmp slt i32 %idx, ").append(loopEnd).append("\n");
            ir.append("  br i1 %cond, label %").append(loopStartLabel).append("_body, label %").append(loopEndLabel).append("\n");

            ir.append(loopStartLabel).append("_body:\n");
            ir.append("  %subArrayPtr = getelementptr ptr, ptr ").append(arrayPtr).append(", i32 %idx\n");
            ir.append("  store ptr ").append(generateMultiDimensionalArrayIR(n, sizeRegisters, currentDimension + 1))
                    .append(", ptr %subArrayPtr\n");
            ir.append("  %newIdx = add i32 %idx, 1\n");
            ir.append("  store i32 %newIdx, ptr ").append(loopIndex).append("\n");
            ir.append("  br label %").append(loopStartLabel).append("\n");

            ir.append(loopEndLabel).append(":\n");
        }

        if (currentDimension == 0) {
            // 如果是最外层维度，返回最外层数组的指针
            ir.append("ret ptr ").append(arrayPtr).append("\n");
        }

        return ir.toString();
    }
}
