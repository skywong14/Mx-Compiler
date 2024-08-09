package semantic;

import org.antlr.v4.runtime.tree.ParseTree;
import parser.MxBaseVisitor;
import parser.MxParser;
import semantic.ASTNodes.*;

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
                program.addFunction((FunctionDeclarationNode) visit(child));
            } else if (child instanceof MxParser.Variable_declarationContext) {
                program.addVariable((VariableDeclarationNode) visit(child));
            } else if (child instanceof MxParser.Class_declarationContext) {
                program.addClass((ClassDeclarationNode) visit(child));
            }
        }

        return program;
    }

    public ASTNode visitFunction_declaration(MxParser.Function_declarationContext ctx) {
        FunctionDeclarationNode function = new FunctionDeclarationNode(ctx.IDENTIFIER().toString());



    }

    @Override
    public ASTNode visitClass_declaration(MxParser.Class_declarationContext ctx) {

    }
}
