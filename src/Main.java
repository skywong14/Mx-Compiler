import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import parser.MxLexer;
import parser.MxParser;
import semantic.*;
import semantic.ASTNodes.ASTNode;
import semantic.ASTNodes.ProgramNode;
import semantic.error.Error;

import java.io.FileInputStream;
import java.io.InputStream;

public class Main {
    public static void main(String[] args) throws Exception{
        String name = "test.mx";
        InputStream input = new FileInputStream(name);
        try {
            // 词法分析器，将输入流转换为字符流
            MxLexer lexer = new MxLexer(CharStreams.fromStream(input));
//            lexer.removeErrorListeners();
            lexer.addErrorListener(new MxErrorListener());

            // 语法分析器，将词法分析器的输出转换为语法树
            MxParser parser = new MxParser(new CommonTokenStream(lexer));
//            parser.removeErrorListeners();
            parser.addErrorListener(new MxErrorListener());

            // 解析程序，生成语法树的根节点
            ParseTree parseTreeRoot = parser.file_input();

            // 构建AST
            ASTBuilder astBuilder = new ASTBuilder();
            ASTNode programNode = astBuilder.visit(parseTreeRoot);

            // collect symbol
            ScopeManager scopeManager = new ScopeManager();
            GlobalScope globalScope = (GlobalScope) scopeManager.getCurrentScope();
            ((ProgramNode)programNode).collectSymbol(globalScope);

            // 语义分析
           new SemanticChecker(scopeManager).visit((ProgramNode) programNode);


            // success
            System.out.println("Success");
        } catch (Exception e) {
            // error
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
