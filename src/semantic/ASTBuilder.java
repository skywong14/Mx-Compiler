package semantic;

import org.antlr.v4.runtime.tree.ParseTree;
import parser.MxBaseVisitor;
import parser.MxParser;
import semantic.ASTNodes.*;

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
                program.addVariable((VariableNode) visit(child));
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
            if (child instanceof MxParser.Variable_declarationContext) {
                classNode.addVariable((VariableNode) visit(child));
            } else if (child instanceof MxParser.Function_declarationContext) {
                classNode.addConstructor((ConstructorNode) visit(child));
            } else if (child instanceof MxParser.Constructor_declarationContext) {
                classNode.addMethod((FunctionNode) visit(child));
            }
        }
        return classNode;
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
            return visit(ctx.expression_stmt().expression());
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
        } else {
            return new EmptyStmt(); // emptyStmt
        }
    }

    @Override
    public ASTNode visitIf_stmt(MxParser.If_stmtContext ctx) {
        if (ctx.Else() == null) {
            return new IfStatementNode(
                    (ExpressionNode) visit(ctx.expression()),
                    (CompoundStmtNode) visit(ctx.statement(0)),
                    null);
        } else {
            return new IfStatementNode(
                    (ExpressionNode) visit(ctx.expression()),
                    (CompoundStmtNode) visit(ctx.statement(0)),
                    (CompoundStmtNode) visit(ctx.statement(1)));
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
}
