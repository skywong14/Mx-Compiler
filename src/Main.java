import ASM.ASMBuilder;
import IR.IRBuilder;
import optimize.IRCode;
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

    public static String commentType(String s) {
        StringBuilder sb = new StringBuilder();
        String[] lines = s.split("\n");
        for (String line : lines) {
            sb.append("# ").append(line).append("\n");
        }
        return sb.toString();
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

            // collect symbol
            ScopeManager scopeManager = new ScopeManager((ProgramNode) programNode);
            GlobalScope globalScope = (GlobalScope) scopeManager.getCurrentScope();
            ((ProgramNode)programNode).collectSymbol(globalScope);

            // 语义分析
            new SemanticChecker(scopeManager).visit((ProgramNode) programNode);

            // 常量折叠
//            new ASTOptimizer().visit((ProgramNode) programNode);

            // 生成IR
            IRBuilder ir = new IRBuilder((ProgramNode) programNode);
            ir.visitProgramNode((ProgramNode) programNode);

            // transfer IR form, for the upcoming optimization
            IRCode irCode = new IRCode();
            ir.toString(irCode);

            // optimization
             irCode.optimize();

            // print IR
//            System.out.println(irCode.toString());
//            System.out.println(commentType(irCode.toString()));

            // erase phi
            irCode.erasePhi();

            // print IR
            System.out.println(commentType(irCode.toString()));

            // asm
             printBuiltin();

            ASMBuilder asmBuilder = new ASMBuilder(irCode);
            asmBuilder.build();
            System.out.println(asmBuilder.toString());
        } catch (Exception e) {
//            System.out.println("Error: " + e.getMessage());
            System.out.println(e.getMessage().split("\\[")[1].split("]")[0]);
//             e.printStackTrace();
            System.exit(2);
        }
    }
}
// export PATH="/usr/local/opt/bin:$PATH"  # optional