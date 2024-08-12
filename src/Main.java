import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import parser.MxLexer;
import parser.MxParser;
import semantic.ASTBuilder;
import semantic.ASTNodes.ASTNode;
import semantic.ASTNodes.ProgramNode;
import semantic.GlobalScope;
import semantic.MxErrorListener;
import semantic.error.Error;

import java.io.FileInputStream;
import java.io.InputStream;

public class Main {
    public static void main(String[] args) throws Exception{
//        String currentDir = System.getProperty("user.dir");
//        System.out.println("Current working directory: " + currentDir);
        String name = "test.mx";
        InputStream input = new FileInputStream(name);
        try {
            // 创建全局作用域
            GlobalScope globalScope = new GlobalScope();

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

            // 收集符号
            ((ProgramNode)programNode).collectSymbol(globalScope);

            // 语义分析


            // success
            System.out.println("Success");
        } catch (Exception e) {
            // error
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
