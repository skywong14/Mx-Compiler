package IR.IRStmts;

import IR.IRBuilder;

import java.util.ArrayList;
import java.util.HashMap;

public class StructIRType extends IRType {
    public ArrayList<IRType> memberType = new ArrayList<>();
    public HashMap<String, Integer> memberMap = new HashMap<>();
    public boolean hasConstructor = false;

    @Override
    public void generateIR(IRBuilder builder) {

    }
}
