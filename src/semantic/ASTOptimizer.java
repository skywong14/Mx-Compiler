package semantic;

import semantic.ASTNodes.*;

// 常量折叠
public class ASTOptimizer implements ASTVisitor {

    @Override
    public void visit(VariableNode it) {}

    @Override
    public void visit(TernaryExprNode it) {
        it.getCondition().accept(this);
        it.getTrueExpr().accept(this);
        it.getFalseExpr().accept(this);

        if (it.getCondition() instanceof ConstantNode) {
            // removing dead branches
        }
    }

    @Override
    public void visit(StatementNode it) {}

    @Override
    public void visit(PrimaryExpressionNode it) {}

    @Override
    public void visit(ParameterListNode it) {
        for (ParameterNode parameter : it.getParameters()) {
            parameter.accept(this);
        }
    }

    @Override
    public void visit(IdentifierNode it) {}

    @Override
    public void visit(FormattedStringNode it) {
        for (ExpressionNode expression : it.getExpressions()) {
            expression.accept(this);
        }
    }

    @Override
    public void visit(ExpressionNode it) {}

    @Override
    public void visit(EmptyStmtNode it) {}

    @Override
    public void visit(CompoundStmtNode it) {
        for (StatementNode statement : it.getStatements()) {
            statement.accept(this);
        }
    }

    @Override
    public void visit(ArrayAccessNode it) {
        it.getHeadExpression().accept(this);
        for (ExpressionNode expression : it.getExpressions()) {
            expression.accept(this);
        }
    }

    @Override
    public void visit(ProgramNode it) {
        for (ASTNode node : it.getAllNodes()) {
            node.accept(this);
        }
    }

    @Override
    public void visit(ClassNode it) {
        if (it.getConstructorNode() != null) {
            it.getConstructorNode().accept(this);
        }

        for (FunctionNode method : it.getMethodNodes()) {
            method.accept(this);
        }
    }

    @Override
    public void visit(FieldDeclarationNode it) {}

    @Override
    public void visit(ConstructorNode it) {
        for (StatementNode statement : it.getBody().getStatements()) {
            statement.accept(this);
        }
    }

    @Override
    public void visit(FunctionNode it) {
        for (StatementNode statement : it.getBody().getStatements()) {
            statement.accept(this);
        }
    }

    @Override
    public void visit(VariableDeclarationNode it) {
        for (int i = 0; i < it.getVariableNodes().size(); i++) {
            VariableNode variable = it.getVariableNodes().get(i);
            if (variable.getValue() != null) {
                variable.getValue().accept(this);
            }
        }
    }

    @Override
    public void visit(UnaryExprNode it) {
        it.getExpression().accept(this);
        if (it.getExpression() instanceof ConstantNode) {
            replaceWith(it.getParent(), it, foldConstant(it));
        }
    }

    @Override
    public void visit(BinaryExprNode it) {
        it.getLeft().accept(this);
        it.getRight().accept(this);
        if (it.getLeft() instanceof ConstantNode && it.getRight() instanceof ConstantNode) {
            replaceWith(it.getParent(), it, foldConstant(it));
        }
    }

    @Override
    public void visit(ConstantNode it) {}

    @Override
    public void visit(IfStatementNode it) {
        it.getCondition().accept(this);
        it.getIfStatement().accept(this);
        if (it.getElseStatement() != null)
            it.getElseStatement().accept(this);
        if (it.getCondition() instanceof ConstantNode) {
            // removing dead branches
        }
    }

    @Override
    public void visit(WhileStmtNode it) {
        it.getCondition().accept(this);
        it.getBody().accept(this);

        if (it.getCondition() instanceof ConstantNode) {
            // removing dead loops
        }
    }

    @Override
    public void visit(ForStmtNode it) {
        if (it.getInit() != null) it.getInit().accept(this);
        if (it.getCondition() != null) it.getCondition().accept(this);
        if (it.getStep() != null) it.getStep().accept(this);
        it.getBody().accept(this);

        if (it.getCondition() instanceof ConstantNode) {
            // removing dead loops
        }
    }

    @Override
    public void visit(JumpStmtNode it) {
        if (it.getExpression() != null) {
            it.getExpression().accept(this);
            if (it.getExpression() instanceof ConstantNode) {
                // removing dead code
            }
        }
    }

    @Override
    public void visit(ExpressionStmtNode it) {
        it.getExpression().accept(this);
    }

    @Override
    public void visit(ParameterNode it) {
        it.getTypeNode().accept(this);
    }


    @Override
    public void visit(MemberAccessNode it) {
        it.getExpression().accept(this);
        // Further optimization: object is a constant.
    }

    @Override
    public void visit(NewExprNode it) {
        it.getTypeNode().accept(this);
    }

    @Override
    public void visit(TypeNode it) {
        for (ExpressionNode expression : it.getExpressions()) {
            expression.accept(this);
        }
    }

    @Override
    public void visit(AssignExprNode it) {
        it.getRight().accept(this);
    }

    @Override
    public void visit(FunctionCallNode it) {
        it.getArgListNode().accept(this);
    }

    @Override
    public void visit(ArgListNode it) {
        for (ExpressionNode expression : it.getArgList()) {
            expression.accept(this);
        }
    }

    private void replaceWith(ASTNode parent, ExpressionNode oldNode, ConstantNode newNode) {
        if (parent instanceof ExpressionStmtNode exprStmt) {
            exprStmt.setExpression(newNode);
            return;
        }
        if (parent instanceof IfStatementNode ifStmt) {
            ifStmt.setCondition(newNode);
            return;
        }
        if (parent instanceof WhileStmtNode whileStmt) {
            whileStmt.setCondition(newNode);
            return;
        }
        if (parent instanceof ForStmtNode forStmt) {
            if (forStmt.getCondition() == oldNode) {
                forStmt.setCondition(newNode);
            } else if (forStmt.getStep() == oldNode) {
                forStmt.setStep(newNode);
            }
            return;
        }
        if (parent instanceof JumpStmtNode jumpStmt) {
            jumpStmt.setExpression(newNode);
            return;
        }

        // Expression nodes
        if (parent instanceof BinaryExprNode binExpr) {
            if (binExpr.getLeft() == oldNode) {
                binExpr.setLeft(newNode);
            } else {
                binExpr.setRight(newNode);
            }
            return;
        }
        if (parent instanceof UnaryExprNode unaryExpr) {
            unaryExpr.setExpression(newNode);
            return;
        }
        if (parent instanceof MemberAccessNode memberAccess) {
            memberAccess.setExpression(newNode);
            return;
        }
        if (parent instanceof ArrayAccessNode arrayAccess) {
            if (arrayAccess.getHeadExpression() == oldNode) {
                arrayAccess.setHeadExpression(newNode);
            } else {
                for (int i = 0; i < arrayAccess.getExpressions().size(); i++) {
                    if (arrayAccess.getExpressions().get(i) == oldNode) {
                        arrayAccess.getExpressions().set(i, newNode);
                        break;
                    }
                }
            }
            return;
        }
        if (parent instanceof AssignExprNode assignExpr) {
            assignExpr.setRight(newNode);
            return;
        }
        if (parent instanceof TernaryExprNode ternaryExpr) {
            if (ternaryExpr.getCondition() == oldNode) {
                ternaryExpr.setCondition(newNode);
            } else if (ternaryExpr.getTrueExpr() == oldNode) {
                ternaryExpr.setTrueExpr(newNode);
            } else {
                ternaryExpr.setFalseExpr(newNode);
            }
            return;
        }


        throw new RuntimeException("[Runtime Error] Unsupported parent node type in Optimizer: " + parent.getClass().getName());
    }

    private ConstantNode foldConstant(ExpressionNode expr) {
        if (expr instanceof BinaryExprNode binExpr) {
            String operator = binExpr.getOperator();
            ConstantNode left = (ConstantNode) binExpr.getLeft();
            ConstantNode right = (ConstantNode) binExpr.getRight();

            // integer arithmetic
            if (left.deduceType(null).equals("int")) {

            }
            // bool arithmetic
            if (left.deduceType(null).equals("bool")) {

            }
            // string arithmetic
            if (left.deduceType(null).equals("string")) {

            }

//            return new ConstantNode(, false);
        }

        if (expr instanceof UnaryExprNode unaryExpr) {
            ConstantNode operand = (ConstantNode) unaryExpr.getExpression();

            // integer arithmetic

            // bool arithmetic

//            return new ConstantNode(, false);
        }

        throw new RuntimeException("[Runtime Error] Unsupported constant folding for expression: " + expr.getClass().getName());
    }
}

