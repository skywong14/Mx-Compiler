import ASM.ASMBuilder;
import IR.IRBuilder;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import parser.MxLexer;
import parser.MxParser;
import semantic.*;
import semantic.ASTNodes.ASTNode;
import semantic.ASTNodes.ProgramNode;

import java.io.*;

public class Main {
    private static void debug(String msg) {
//        System.out.println("Main: " + msg);
    }

    public static void printBuiltin() {
        String file = "./src/IR/builtin/builtin.s";
        String content = "";
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine();
            while (line != null) {
                content += line + "\n";
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("builtin file not found");
        }
        System.out.println(content);
    }

    public static void main(String[] args) throws Exception{
        InputStream input = System.in;
        try {
            // 词法分析器，将输入流转换为字符流
            MxLexer lexer = new MxLexer(CharStreams.fromStream(input));
            lexer.addErrorListener(new MxErrorListener());

            // 语法分析器，将词法分析器的输出转换为语法树
            MxParser parser = new MxParser(new CommonTokenStream(lexer));
            parser.addErrorListener(new MxErrorListener());

            // 解析程序，生成语法树的根节点
            ParseTree parseTreeRoot = parser.file_input();

            // 构建AST
            ASTBuilder astBuilder = new ASTBuilder();
            ASTNode programNode = astBuilder.visit(parseTreeRoot);

            debug("AST built successfully");

            // collect symbol
            ScopeManager scopeManager = new ScopeManager((ProgramNode) programNode);
            GlobalScope globalScope = (GlobalScope) scopeManager.getCurrentScope();
            ((ProgramNode)programNode).collectSymbol(globalScope);

            // 语义分析
            new SemanticChecker(scopeManager).visit((ProgramNode) programNode);

            // 常量折叠
//            new ASTOptimizer().visit((ProgramNode) programNode);

            // 生成IR
            debug("begin to generate IR");
            IRBuilder ir = new IRBuilder((ProgramNode) programNode);
            ir.visitProgramNode((ProgramNode) programNode);

            // print ir.toString()
            ir.toString();

            printBuiltin();

            // asm
            ASMBuilder asmBuilder = new ASMBuilder(ir);
            asmBuilder.visitProgram();
            System.out.println(asmBuilder.toString());

            // success
            debug("Success!");
        } catch (Exception e) {
//            System.out.println("Error: " + e.getMessage());
            System.out.println(e.getMessage().split("\\[")[1].split("]")[0]);
//             e.printStackTrace();
            System.exit(2);
        }
    }
}
// export PATH="/usr/local/opt/bin:$PATH"  # optional