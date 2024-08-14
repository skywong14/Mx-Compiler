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
//    FileInputNode fileInputNode;

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
        return program;
    }

    @Override
    public ASTNode visitFunction_declaration(MxParser.Function_declarationContext ctx) {
        FunctionNode functionNode = new FunctionNode(
                (TypeNode) visit(ctx.type()),
                ctx.IDENTIFIER().toString(),
                (ParameterListNode) visit(ctx.trailer()), // trailer -> parameter_list
                (CompoundStmtNode) visit(ctx.compound_stmt()));
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
    public ASTNode visitClass_declaration(MxParser.Class_declarationContext ctx) {
        ClassNode classNode = new ClassNode(ctx.IDENTIFIER().toString());

        for (ParseTree child : ctx.class_body().children) {
            if (child instanceof MxParser.Field_declrationContext) {
                classNode.addField((FieldDeclarationNode) visit(child));
            } else if (child instanceof MxParser.Function_declarationContext) {
                classNode.addConstructor((ConstructorNode) visit(child));
            } else if (child instanceof MxParser.Constructor_declarationContext) {
                classNode.addMethod((FunctionNode) visit(child));
            }
        }
        return classNode;
    }

    @Override
    public ASTNode visitConstructor_declaration(MxParser.Constructor_declarationContext ctx) {
        return new ConstructorNode(ctx.IDENTIFIER().toString(),
                (CompoundStmtNode) visit(ctx.compound_stmt()));
    }


    @Override
    public ASTNode visitField_declration(MxParser.Field_declrationContext ctx) {
        FieldDeclarationNode fieldDeclarationNode = new FieldDeclarationNode((TypeNode) visit(ctx.type()));
        // field_declration : type IDENTIFIER (',' IDENTIFIER)* ';' ;
        int sz = ctx.IDENTIFIER().size();
        for (int i = 0; i < sz; i++)
            fieldDeclarationNode.addName(ctx.IDENTIFIER(i).toString());
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
        for (ParseTree child : ctx.children) {
            if (child instanceof MxParser.ExpressionContext) {
                typeNode.addExpression((ExpressionNode) visit(child));
            }
        }
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
        throw new RuntimeException("Unknown statement type");
//        return null; // should be unreachable
    }

    @Override
    public ASTNode visitCompound_stmt(MxParser.Compound_stmtContext ctx) {
        CompoundStmtNode compoundStmtNode = new CompoundStmtNode();
        for (ParseTree child : ctx.children) {
            if (child instanceof MxParser.StatementContext) {
                compoundStmtNode.addStatement((StatementNode) visit(child));
            }
        }
        return compoundStmtNode;
    }

    @Override
    public ASTNode visitIf_stmt(MxParser.If_stmtContext ctx) {
        if (ctx.Else() == null) {
            return new IfStatementNode(
                    (ExpressionNode) visit(ctx.expression()),
                    (StatementNode) visit(ctx.statement(0)),
                    null);
        } else {
            return new IfStatementNode(
                    (ExpressionNode) visit(ctx.expression()),
                    (StatementNode) visit(ctx.statement(0)),
                    (StatementNode) visit(ctx.statement(1)));
        }
    }

    @Override
    public ASTNode visitJump_stmt(MxParser.Jump_stmtContext ctx) {
        if (ctx.return_stmt() != null) {
            if (ctx.return_stmt().expression() != null) {
                return new JumpStmtNode("return", (ExpressionNode) visit(ctx.return_stmt().expression()));
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
        // for_stmt: 'for' '(' (empty_stmt | expression_stmt | variable_declaration) expression ';' (expression)? ')' statement;
        ExpressionNode second_expr = null;
        if (ctx.expression().size() == 2)
            second_expr = (ExpressionNode) visit(ctx.expression(1));
        if (ctx.empty_stmt() != null){
            return new ForStmtNode(new EmptyStmtNode(), (ExpressionNode) visit(ctx.expression(0)),
                       second_expr, (StatementNode) visit(ctx.statement()));
        } else if (ctx.expression_stmt() != null) {
            return new ForStmtNode((ExpressionStmtNode) visit(ctx.expression_stmt()),
                    (ExpressionNode) visit(ctx.expression(0)),
                    second_expr, (StatementNode) visit(ctx.statement()));
        } else if (ctx.variable_declaration() != null) {
            return new ForStmtNode((VariableDeclarationNode) visit(ctx.variable_declaration()),
                    (ExpressionNode) visit(ctx.expression(0)),
                    second_expr, (StatementNode) visit(ctx.statement()));
        }
        throw new RuntimeException("Unknown for_stmt type");
    }

    @Override
    public ASTNode visitWhile_stmt(MxParser.While_stmtContext ctx) {
        return new WhileStmtNode((ExpressionNode) visit(ctx.expression()), (StatementNode) visit(ctx.statement()));
    }

    @Override
    public ASTNode visitVariable_declaration(MxParser.Variable_declarationContext ctx) {
        TypeNode type = (TypeNode) visit(ctx.type());
        VariableDeclarationNode variable = new VariableDeclarationNode(type);
        for (ParseTree child : ctx.variable_declaration_list().children) {
            if (child instanceof MxParser.Single_variable_declarationContext child_ctx) {
                if (((MxParser.Single_variable_declarationContext) child).expression() != null) {
                    variable.addVariable(child_ctx.IDENTIFIER().toString(), (ExpressionNode) visit(child_ctx.expression()));
                } else {
                    variable.addVariable(child_ctx.IDENTIFIER().toString(), null);
                }
            }
        }
        return variable;
    }

    @Override
    public ASTNode visitExpression_stmt(MxParser.Expression_stmtContext ctx) {
        return new ExpressionStmtNode((ExpressionNode) visit(ctx.expression()));
    }

    @Override
    public ASTNode visitExpression(MxParser.ExpressionContext ctx) {
        if (ctx.primary_expression() != null) {
            return visit(ctx.primary_expression());
        }
        if (ctx.LeftParen() != null && ctx.RightParen() != null) {
            return visit(ctx.expression().getFirst());
        }

        if (ctx.opLeft != null) {
            if (ctx.opLeft.getText().equals("~") || ctx.opLeft.getText().equals("!")) {
                return new UnaryExprNode(ctx.opLeft.getText(), (ExpressionNode) visit(ctx.expression().getFirst()), true);
            } else if (ctx.opLeft.getText().equals("-") || ctx.opLeft.getText().equals("+")) {
                return new UnaryExprNode(ctx.opLeft.getText(), (ExpressionNode) visit(ctx.expression().getFirst()), true);
            } else if (ctx.opLeft.getText().equals("++") || ctx.opLeft.getText().equals("--")) {
                return new UnaryExprNode(ctx.opLeft.getText(), (ExpressionNode) visit(ctx.expression().getFirst()), true);
            }
        }
        if (ctx.op != null) {
            if (ctx.op.getText().equals("++") || ctx.op.getText().equals("--")) {
                return new UnaryExprNode(ctx.op.getText(), (ExpressionNode) visit(ctx.expression().get(0)), false);
            } else if (ctx.op.getText().equals("?")) {
                // TernaryExprNode
                return new TernaryExprNode((ExpressionNode) visit(ctx.expression().get(0)),
                        (ExpressionNode) visit(ctx.expression().get(1)),
                        (ExpressionNode) visit(ctx.expression().get(2)));
            } else {
                return new BinaryExprNode(ctx.op.getText(), (ExpressionNode) visit(ctx.expression().get(0)),
                        (ExpressionNode) visit(ctx.expression().get(1)));
            }
        }
        throw new RuntimeException("Unknown expression type");
    }

    @Override
    public ASTNode visitPrimary_expression(MxParser.Primary_expressionContext ctx) {

        if (ctx.formatted_string() != null) {
            // formatted_string
            return visit(ctx.formatted_string());
        } else if (ctx.This() != null) {
            // this
            return new IdentifierNode(ctx.IDENTIFIER().toString());
        } else if (ctx.constant() != null) {
            // constant
            return visit(ctx.constant());
        } else if (ctx.IDENTIFIER() != null && ctx.LeftParen() == null && ctx.RightParen() == null) {
            // IDENTIFIER (variable)
            return new IdentifierNode(ctx.IDENTIFIER().toString());
        } else if (ctx.IDENTIFIER() != null && ctx.LeftParen() != null && ctx.RightParen() != null) {
            // function_call or method_call
            if (ctx.arglist() != null)
                return new FunctionCallNode(ctx.IDENTIFIER().toString(), (ArgListNode) visit(ctx.arglist()));
            else
                return new FunctionCallNode(ctx.IDENTIFIER().toString(), new ArgListNode());
        } else if (ctx.LeftBracket() != null) {
            // array_access
            ArrayAccessNode arrayAccessNode = new ArrayAccessNode((PrimaryExpressionNode) visit(ctx.primary_expression()));
            for (ParseTree child : ctx.children) {
                if (child instanceof MxParser.ExpressionContext) {
                    arrayAccessNode.addExpression((ExpressionNode) visit(child));
                }
            }
        } else if (ctx.Dot() != null) {
            // member_access
            if (ctx.LeftParen() != null) {
                // method_call
                if (ctx.arglist() != null)
                    return new MemberAccessNode((PrimaryExpressionNode) visit(ctx.primary_expression()), ctx.IDENTIFIER().toString(), true, (ArgListNode) visit(ctx.arglist()));
                else
                    return new MemberAccessNode((PrimaryExpressionNode) visit(ctx.primary_expression()), ctx.IDENTIFIER().toString(), true, null);
            } else {
                // member_access
                return new MemberAccessNode((PrimaryExpressionNode) visit(ctx.primary_expression()), ctx.IDENTIFIER().toString(), false, null);
            }
        } else if (ctx.new_expression() != null) {
            // new_expression
            return visit(ctx.new_expression());
        }
        throw new RuntimeException("Unknown primary_expression type");
    }

    @Override
    public ASTNode visitConstant(MxParser.ConstantContext ctx) {
        if (ctx.integerConstant() != null) {
            return new ConstantNode(ctx.integerConstant().toString(), false);
        } else if (ctx.booleanConstant() != null) {
            return new ConstantNode(ctx.booleanConstant().toString(), false);
        } else if (ctx.nullConstant() != null) {
            return new ConstantNode(ctx.nullConstant().toString(), false);
        } else if (ctx.stringConstant() != null) {
            return new ConstantNode(ctx.stringConstant().toString(), false);
        } else if (ctx.arrayConstant() != null) {
            ConstantNode constantNode = new ConstantNode("", true);
            for (ParseTree child : ctx.arrayConstant().children) {
                if (child instanceof MxParser.ConstantContext) {
                    constantNode.addConstant((ConstantNode) visit(child));
                }
            }
        }
        throw new RuntimeException("Unknown constant type");
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

        return node;
    }

    @Override
    public ASTNode visitNew_expression(MxParser.New_expressionContext ctx) {
        if (ctx.array_type() != null) {
            return new NewExprNode((TypeNode) visit(ctx.array_type()), null);
        } else {
            return new NewExprNode(null, ctx.IDENTIFIER().toString());
        }
    }
}
