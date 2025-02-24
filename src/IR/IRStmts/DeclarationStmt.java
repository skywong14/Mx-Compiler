package IR.IRStmts;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class DeclarationStmt extends IRStmt {
    @Override
    public String getDest() { return null; }

    @Override
    public String toString() {
        String file = "./src/IR/IRStmts/irDeclaration.txt";
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
