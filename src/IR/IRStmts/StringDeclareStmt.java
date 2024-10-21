package IR.IRStmts;

public class StringDeclareStmt extends IRStmt{
    public String dest;
    public String value;
    public String printValue;
    public int length;

    public static String convertEscapedString(String str) {
        StringBuilder result = new StringBuilder();
        int length = str.length();

        for (int i = 0; i < length; i++) {
            char currentChar = str.charAt(i);

            if (currentChar == '\\' && i + 1 < length) {
                char nextChar = str.charAt(i + 1);
                switch (nextChar) {
                    case 'n':
                        result.append('\n'); // 转换为换行符
                        i++; // 跳过下一个字符，因为它是转义字符的一部分
                        break;
                    case '\\':
                        result.append('\\'); // 转换为反斜杠
                        i++;
                        break;
                    case '\"':
                        result.append('\"'); // 转换为双引号
                        i++;
                        break;
                    default:
                        result.append(currentChar); // 如果是未知的转义序列，则保持原样
                        break;
                }
            } else {
                result.append(currentChar); // 非转义字符，直接追加
            }
        }

        return result.toString();
    }

    public static String convertString(String str) {
        StringBuilder llvmStr = new StringBuilder();
        for (char c : str.toCharArray()) {
            switch (c) {
                case '\\':
                    llvmStr.append("\\\\");
                    break;
                case '\"':
                    llvmStr.append("\\22"); // 双引号 '"' 的 ASCII 是 34，对应的十六进制是 0x22
                    break;
                case '\n':
                    llvmStr.append("\\0A"); // 换行符 '\n' 的 ASCII 是 10，对应的十六进制是 0x0A
                    break;
                default:
                    llvmStr.append(c); // 其他字符直接追加
                    break;
            }
        }
        // 生成 LLVM IR 声明
        return llvmStr.toString();
    }

    public static String convert2riscv(String str){
        // 替换 \\22 为 \\\"
        String riscvString = str.replace("\\22", "\\\"");
        // 替换 \\0A 为换行符 \\n
        riscvString = riscvString.replace("\\0A", "\\n");
        return riscvString;
    }

    public StringDeclareStmt(String value, String dest) {
        this.dest = dest;
        this.value = convertEscapedString(value);
        this.length = this.value.length() + 1;
        this.printValue = convertString(this.value);
    }

    // <dest> = private unnamed_addr constant [4 x i8] c"value\00"
    @Override
    public String toString() {
        return dest + " = private unnamed_addr constant [" + length + " x i8] c\"" + printValue + "\\00\"";
    }
}
