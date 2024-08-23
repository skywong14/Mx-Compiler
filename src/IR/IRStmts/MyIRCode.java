package IR.IRStmts;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class MyIRCode extends IRStmt {
    @Override
    public String toString() {
        //将同文件夹下的MyIRCode.ir文本文件转换为字符串返回，打开文件，读入和输出即可
        String file = "MyIRCode.ir";
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
            throw new RuntimeException("IRCode file not found");
        }
        return content;
    }
}
