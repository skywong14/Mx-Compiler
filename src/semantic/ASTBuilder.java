package semantic;

import org.antlr.v4.runtime.tree.ParseTree;
import parser.MxBaseVisitor;
import parser.MxParser;
import semantic.ASTNodes.*;

import java.util.ArrayList;
import java.util.Objects;

/* In MxBaseVisitor, visit(ParseTree tree) is defined as:
 *     public T visit(ParseTree tree) {
 *        return tree.accept(this);
 *    }
 */
public class ASTBuilder extends MxBaseVisitor<ASTNode> {

    @Override
    public ASTNode visitFile_input(MxParser.File_inputContext ctx) {
        // 创建一个根节点
        ProgramNode program = new ProgramNode();
        // 遍历所有的子节点，并将其添加到程序节点中
        for (ParseTree child : ctx.children) {
            if (child instanceof MxParser.Function_declarationContext) {
//                program.addFunction(child.accept(this));
                program.addFunction((FunctionNode) visit(child));
            } else if (child instanceof MxParser.Variable_declarationContext) {
                program.addVariable((VariableDeclarationNode) visit(child));
            } else if (child instanceof MxParser.Class_declarationContext) {
                program.addClass((ClassNode) visit(child));
            }
        }
        program.notifyParent();
        return program;
    }

    @Override
    public ASTNode visitFunction_declaration(MxParser.Function_declarationContext ctx) {
        FunctionNode functionNode = new FunctionNode(
                (TypeNode) visit(ctx.type()),
                ctx.IDENTIFIER().toString(),
                (ParameterListNode) visit(ctx.trailer()), // trailer -> parameter_list
                (CompoundStmtNode) visit(ctx.compound_stmt()));
        functionNode.notifyParent();
        return functionNode;
    }

    @Override
    public ASTNode visitTrailer(MxParser.TrailerContext ctx) {
        if (ctx.parameter_list() == null) {
            return new ParameterListNode();
        } else {
            return visit(ctx.parameter_list());
        }
    }

    @Override
    public ASTNode visitParameter_list(MxParser.Parameter_listContext ctx) {
        ParameterListNode parameterListNode = new ParameterListNode();
        for (ParseTree child : ctx.children) {
            if (child instanceof MxParser.ParameterContext) {
                parameterListNode.addParameter((ParameterNode) visit(child));
            }
        }
        parameterListNode.notifyParent();
        return parameterListNode;
    }

    @Override
    public ASTNode visitParameter(MxParser.ParameterContext ctx) {
        ParameterNode parameterNode = new ParameterNode(ctx.IDENTIFIER().toString(), (TypeNode) visit(ctx.type()));
        parameterNode.notifyParent();
        return parameterNode;
    }

    @Override
    public ASTNode visitClass_declaration(MxParser.Class_declarationContext ctx) {
        ClassNode classNode = new ClassNode(ctx.IDENTIFIER().toString());

        for (ParseTree child : ctx.class_body().children) {
            if (child instanceof MxParser.Field_declrationContext) {
                classNode.addField((FieldDeclarationNode) visit(child));
            } else if (child instanceof MxParser.Function_declarationContext) {
                classNode.addMethod((FunctionNode) visit(child));
            } else if (child instanceof MxParser.Constructor_declarationContext) {
                classNode.addConstructor((ConstructorNode) visit(child));
            }
        }
        classNode.notifyParent();
        return classNode;
    }

    @Override
    public ASTNode visitConstructor_declaration(MxParser.Constructor_declarationContext ctx) {
        ConstructorNode constructorNode = new ConstructorNode(ctx.IDENTIFIER().toString(),
                (CompoundStmtNode) visit(ctx.compound_stmt()));
        constructorNode.notifyParent();
        return constructorNode;
    }


    @Override
    public ASTNode visitField_declration(MxParser.Field_declrationContext ctx) {
        FieldDeclarationNode fieldDeclarationNode = new FieldDeclarationNode((TypeNode) visit(ctx.type()));
        // field_declration : type IDENTIFIER (',' IDENTIFIER)* ';' ;
        int sz = ctx.IDENTIFIER().size();
        for (int i = 0; i < sz; i++)
            fieldDeclarationNode.addName(ctx.IDENTIFIER(i).toString());
        fieldDeclarationNode.notifyParent();
        return fieldDeclarationNode;
    }

    @Override
    public ASTNode visitType(MxParser.TypeContext ctx) {
        TypeNode typeNode;
        if (ctx.array_type() != null) {
            // array_type
            typeNode = (TypeNode) visit(ctx.array_type());
        } else {
            // basic_Type
            typeNode = new TypeNode(new Type(ctx.basic_type().getText(), false, 0));
        }
        return typeNode;
    }

    @Override
    public ASTNode visitArray_type(MxParser.Array_typeContext ctx) {
        int dimension = 0;
        for (ParseTree child : ctx.children) {
            if (Objects.equals(child.getText(), "["))
                dimension++;
        }
        Type type = new Type(ctx.basic_type().getText(), true, dimension);
        TypeNode typeNode = new TypeNode(type);
        /* for (ParseTree child : ctx.children) {
            if (child instanceof MxParser.ExpressionContext) {
                typeNode.addExpression((ExpressionNode) visit(child));
                throw new RuntimeException("[Runtime Error]: Array type declaration should not have expression");
            }
        } */
        return typeNode;
    }

    @Override
    public ASTNode visitStatement(MxParser.StatementContext ctx) {
        if (ctx.compound_stmt() != null) {
            return visit(ctx.compound_stmt());
        } else if (ctx.expression_stmt() != null) {
            return visit(ctx.expression_stmt());
        } else if (ctx.if_stmt() != null) {
            return visit(ctx.if_stmt());
        } else if (ctx.for_stmt() != null) {
            return visit(ctx.for_stmt());
        } else if (ctx.while_stmt() != null) {
            return visit(ctx.while_stmt());
        } else if (ctx.jump_stmt() != null) {
            return visit(ctx.jump_stmt());
        } else if (ctx.variable_declaration() != null) {
            return visit(ctx.variable_declaration());
        } else if (ctx.empty_stmt() != null) {
            return new EmptyStmtNode();
        }
        throw new RuntimeException("[Runtime Error]: Unknown statement type");
    }

    @Override
    public ASTNode visitCompound_stmt(MxParser.Compound_stmtContext ctx) {
        CompoundStmtNode compoundStmtNode = new CompoundStmtNode();
        for (ParseTree child : ctx.children) {
            if (child instanceof MxParser.StatementContext) {
                compoundStmtNode.addStatement((StatementNode) visit(child));
            }
        }
        compoundStmtNode.notifyParent();
        return compoundStmtNode;
    }

    @Override
    public ASTNode visitIf_stmt(MxParser.If_stmtContext ctx) {
        if (ctx.Else() == null) {
            IfStatementNode ifStatementNode = new IfStatementNode(
                    (ExpressionNode) visit(ctx.expression()),
                    (StatementNode) visit(ctx.statement(0)),
                    null);
            ifStatementNode.notifyParent();
            return ifStatementNode;
        } else {
            IfStatementNode ifStatementNode = new IfStatementNode(
                    (ExpressionNode) visit(ctx.expression()),
                    (StatementNode) visit(ctx.statement(0)),
                    (StatementNode) visit(ctx.statement(1)));
            ifStatementNode.notifyParent();
            return ifStatementNode;
        }
    }

    @Override
    public ASTNode visitJump_stmt(MxParser.Jump_stmtContext ctx) {
        if (ctx.return_stmt() != null) {
            if (ctx.return_stmt().expression() != null) {
                JumpStmtNode jumpStmtNode = new JumpStmtNode("return", (ExpressionNode) visit(ctx.return_stmt().expression()));
                jumpStmtNode.notifyParent();
                return jumpStmtNode;
            } else {
                return new JumpStmtNode("return", null);
            }
        } else if (ctx.break_stmt() != null) {
            return new JumpStmtNode("break", null);
        } else {
            return new JumpStmtNode("continue", null);
        }
    }

    @Override
    public ASTNode visitFor_stmt(MxParser.For_stmtContext ctx) {
        // for_stmt: 'for' '(' (empty_stmt | expression_stmt | variable_declaration) (expression)? ';' (expression)? ')' statement;
        ExpressionNode first_expr = null, second_expr = null;
        if (ctx.contdition_expr != null)
            first_expr = (ExpressionNode) visit(ctx.contdition_expr);
        if (ctx.step_expr != null)
            second_expr = (ExpressionNode) visit(ctx.step_expr);
        ForStmtNode forStmtNode = null;
        if (ctx.empty_stmt() != null){
            forStmtNode = new ForStmtNode(new EmptyStmtNode(), first_expr,
                       second_expr, (StatementNode) visit(ctx.statement()));
        } else if (ctx.expression_stmt() != null) {
            forStmtNode = new ForStmtNode((ExpressionStmtNode) visit(ctx.expression_stmt()),
                    first_expr,
                    second_expr, (StatementNode) visit(ctx.statement()));
        } else if (ctx.variable_declaration() != null) {
            forStmtNode = new ForStmtNode((VariableDeclarationNode) visit(ctx.variable_declaration()),
                    first_expr,
                    second_expr, (StatementNode) visit(ctx.statement()));
        } else {
            throw new RuntimeException("[Runtime Error]: Unknown for_stmt type");
        }
        forStmtNode.notifyParent();
        return forStmtNode;
    }

    @Override
    public ASTNode visitWhile_stmt(MxParser.While_stmtContext ctx) {
        WhileStmtNode whileStmtNode = new WhileStmtNode((ExpressionNode) visit(ctx.expression()), (StatementNode) visit(ctx.statement()));
        whileStmtNode.notifyParent();
        return whileStmtNode;
    }

    @Override
    public ASTNode visitVariable_declaration(MxParser.Variable_declarationContext ctx) {
        TypeNode type = (TypeNode) visit(ctx.type());
        VariableDeclarationNode variable = new VariableDeclarationNode(type);
        for (ParseTree child : ctx.variable_declaration_list().children) {
            if (child instanceof MxParser.Single_variable_declarationContext child_ctx) {
                if (child_ctx.expression() != null) {
                    variable.addVariable(child_ctx.IDENTIFIER().toString(), (ExpressionNode) visit(child_ctx.expression()));
                } else {
                    variable.addVariable(child_ctx.IDENTIFIER().toString(), null);
                }
            }
        }
        variable.notifyParent();
        return variable;
    }

    @Override
    public ASTNode visitExpression_stmt(MxParser.Expression_stmtContext ctx) {
        ExpressionStmtNode expressionStmtNode = new ExpressionStmtNode((ExpressionNode) visit(ctx.expression()));
        expressionStmtNode.notifyParent();
        return expressionStmtNode;
    }

    @Override
    public ASTNode visitUnaryExpression(MxParser.UnaryExpressionContext ctx) {
        UnaryExprNode unaryExprNode = null;
        if (ctx.opLeft != null)
            unaryExprNode = new UnaryExprNode(ctx.opLeft.getText(), (ExpressionNode) visit(ctx.expression()), true);
        else
            unaryExprNode = new UnaryExprNode(ctx.op.getText(), (ExpressionNode) visit(ctx.expression()), false);
        unaryExprNode.notifyParent();
        return unaryExprNode;
    }

    @Override
    public ASTNode visitExpression_primary(MxParser.Expression_primaryContext ctx) {
        return visit(ctx.primary_expression());
    }

    @Override
    public ASTNode visitExpression_parenthesis(MxParser.Expression_parenthesisContext ctx) {
        return visit(ctx.expression());
    }

    @Override
    public ASTNode visitBinaryExpression(MxParser.BinaryExpressionContext ctx) {
        BinaryExprNode binaryExprNode = new BinaryExprNode(ctx.op.getText(), (ExpressionNode) visit(ctx.expression(0)), (ExpressionNode) visit(ctx.expression(1)));
        binaryExprNode.notifyParent();
        return binaryExprNode;
    }

    @Override
    public ASTNode visitTernaryExpression(MxParser.TernaryExpressionContext ctx) {
        TernaryExprNode ternaryExprNode = new TernaryExprNode((ExpressionNode) visit(ctx.expression(0)),
                (ExpressionNode) visit(ctx.expression(1)),
                (ExpressionNode) visit(ctx.expression(2)));
        ternaryExprNode.notifyParent();
        return ternaryExprNode;
    }

    @Override
    public ASTNode visitAssignExpression(MxParser.AssignExpressionContext ctx) {
        AssignExprNode assignExprNode = new AssignExprNode((ExpressionNode) visit(ctx.expression(0)), (ExpressionNode) visit(ctx.expression(1)));
        assignExprNode.notifyParent();
        return assignExprNode;
    }

    @Override
    public ASTNode visitPrimary_formatted_string(MxParser.Primary_formatted_stringContext ctx) {
        return visit(ctx.formatted_string());
    }

    @Override
    public ASTNode visitPrimary_this(MxParser.Primary_thisContext ctx) {
        return new IdentifierNode("this");
    }

    @Override
    public ASTNode visitPrimary_new(MxParser.Primary_newContext ctx) {
        return visit(ctx.new_expression());
    }

    @Override
    // primary_constant
    public ASTNode visitPrimary_constant(MxParser.Primary_constantContext ctx) {
        return visit(ctx.constant());
    }

    @Override
    public ASTNode visitPrimary_identifier(MxParser.Primary_identifierContext ctx) {
        return new IdentifierNode(ctx.IDENTIFIER().toString());
    }

    @Override
    public ASTNode visitPrimary_function_call(MxParser.Primary_function_callContext ctx) {
        FunctionCallNode functionCallNode = null;
        if (ctx.arglist() != null)
            functionCallNode = new FunctionCallNode(ctx.IDENTIFIER().toString(), (ArgListNode) visit(ctx.arglist()));
        else
            functionCallNode = new FunctionCallNode(ctx.IDENTIFIER().toString(), new ArgListNode());
        functionCallNode.notifyParent();
        return functionCallNode;
    }

    @Override
    public ASTNode visitArray_access(MxParser.Array_accessContext ctx) {
        ArrayAccessNode arrayAccessNode = new ArrayAccessNode((ExpressionNode) visit(ctx.expression(0)));
        int sz = ctx.expression().size();
        for (int i = 1; i < sz; i++) {
            arrayAccessNode.addExpression((ExpressionNode) visit(ctx.expression(i)));
        }
        arrayAccessNode.notifyParent();
        return arrayAccessNode;
    }

    @Override
    public ASTNode visitMember_access(MxParser.Member_accessContext ctx) {
        MemberAccessNode memberAccessNode = new MemberAccessNode((ExpressionNode) visit(ctx.expression()), ctx.IDENTIFIER().toString(), false, new ArgListNode());
        memberAccessNode.notifyParent();
        return memberAccessNode;
    }

    @Override
    // member_function_call
    public ASTNode visitMember_function_call(MxParser.Member_function_callContext ctx) {
        MemberAccessNode memberAccessNode = null;
        if (ctx.arglist() != null)
            memberAccessNode = new MemberAccessNode((ExpressionNode) visit(ctx.expression()), ctx.IDENTIFIER().toString(), true, (ArgListNode) visit(ctx.arglist()));
        else
            memberAccessNode = new MemberAccessNode((ExpressionNode) visit(ctx.expression()), ctx.IDENTIFIER().toString(), true, new ArgListNode());
        memberAccessNode.notifyParent();
        return memberAccessNode;
    }

    @Override
    public ASTNode visitArglist(MxParser.ArglistContext ctx) {
        ArgListNode argListNode = new ArgListNode();
        for (ParseTree child : ctx.children) {
            if (child instanceof MxParser.ExpressionContext) {
                argListNode.addExpression((ExpressionNode) visit(child));
            }
        }
        argListNode.notifyParent();
        return argListNode;
    }

    @Override
    public ASTNode visitConstant(MxParser.ConstantContext ctx) {
        if (ctx.integerConstant() != null) {
            return new ConstantNode(ctx.getText(), false);
        } else if (ctx.booleanConstant() != null) {
            return new ConstantNode(ctx.getText(), false);
        } else if (ctx.nullConstant() != null) {
            return new ConstantNode(ctx.getText(), false);
        } else if (ctx.stringConstant() != null) {
            return new ConstantNode(ctx.getText(), false);
        } else if (ctx.arrayConstant() != null) {
            return visit(ctx.arrayConstant());
        }
        throw new RuntimeException("[Runtime Error] Unknown constant type:" + ctx.getText());
    }

    @Override
    public ASTNode visitArrayConstant(MxParser.ArrayConstantContext ctx) {
        ConstantNode constantNode = new ConstantNode("", true);
        for (ParseTree child : ctx.children) {
            if (child instanceof MxParser.ConstantContext) {
                constantNode.addConstant((ConstantNode) visit(child));
            }
        }
        constantNode.notifyParent();
        return constantNode;
    }

    @Override
    public ASTNode visitFormatted_string(MxParser.Formatted_stringContext ctx) {
        if (ctx.Formatted_string_plain() != null) {
            return new FormattedStringNode(ctx.Formatted_string_plain().toString());
        }
        // begin
        int length = ctx.Formatted_string_begin().toString().length();
        assert(length >= 3);
        FormattedStringNode node = new FormattedStringNode(ctx.Formatted_string_begin().toString().substring(2, length - 1));
        node.addExpression((ExpressionNode) visit(ctx.expression(0)));
        // middle
        int mid_size = ctx.Formatted_string_middle().size();
        for (int i = 0; i < mid_size; i++) {
            length = ctx.Formatted_string_middle(i).toString().length();
            node.addPlainString(ctx.Formatted_string_middle(i).toString().substring(1, length - 1));
            node.addExpression((ExpressionNode) visit(ctx.expression(i + 1)));
        }
        // end
        length = ctx.Formatted_string_end().toString().length();
        node.addPlainString(ctx.Formatted_string_end().toString().substring(1, length - 1));

        node.notifyParent();
        return node;
    }

    @Override
    public ASTNode visitNew_expression(MxParser.New_expressionContext ctx) {
        if (ctx.new_array_type() != null) {
            //new_array_type : basic_type ('[' expression? ']')+
            ArrayList<ExpressionNode> expressions = new ArrayList<>();
            int sz = 0;
            boolean flag = true;
            MxParser.New_array_typeContext new_array_type = ctx.new_array_type();
            ArrayList<ParseTree> children = new ArrayList<>(new_array_type.children);
            for (int i = 1; i < children.size(); i++) {
                if (children.get(i).getText().equals("[")) {
                    sz++;
                    if (children.get(i + 1).getText().equals("]")) {
                        flag = false;
                    }
                    if (children.get(i + 1) instanceof MxParser.ExpressionContext) {
                        if (!flag) throw new RuntimeException("[Invalid Identifier] New array type error");
                        expressions.add((ExpressionNode) visit(children.get(i + 1)));
                    }
                }
            }
            Type type = new Type(new_array_type.basic_type().getText(), true, sz);
            TypeNode typeNode = new TypeNode(type);
            for (ExpressionNode expression : expressions) {
                typeNode.addExpression(expression);
            }

            NewExprNode newExprNode = null;
            if (ctx.arrayConstant() != null) {
                newExprNode = new NewExprNode(typeNode, null, (ConstantNode) visit(ctx.arrayConstant()));
            } else {
                newExprNode = new NewExprNode(typeNode, null, null);
            }
            newExprNode.notifyParent();
            return newExprNode;
        } else {
            NewExprNode newExprNode = new NewExprNode(new TypeNode(new Type(ctx.IDENTIFIER().toString())), ctx.IDENTIFIER().toString(), null);
            newExprNode.notifyParent();
            return newExprNode;
        }
    }
}
