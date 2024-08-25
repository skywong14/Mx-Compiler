package IR.IRStmts;

import semantic.ASTNodes.ClassNode;
import semantic.ASTNodes.FieldDeclarationNode;

import java.util.ArrayList;
import java.util.HashMap;

public class ClassTypeDefineStmt extends IRStmt{
    public String typeName;
    public ClassNode classNode;
    public ArrayList<BasicIRType> fields = new ArrayList<>();
    public HashMap<String, Integer> fieldIndex = new HashMap<>();

    public ClassTypeDefineStmt(ClassNode classNode) {
        this.classNode = classNode;
        this.typeName = classNode.name;
        classNode.initFieldIndex();
        this.fieldIndex = classNode.getFieldIndex();
        for (FieldDeclarationNode field : classNode.getFieldNodes()) {
            for (int i = 0; i < field.getSize(); i++) {
                fields.add(new BasicIRType(field.getType()));
            }
        }
    }

    public HashMap<String, Integer> getFieldIndex() { return fieldIndex; }

    @Override
    public String toString() {
//        return "%class." + typeName + " = type {" + fields.stream().map(IRType::toString).reduce((a, b) -> a + ", " + b).orElse("") + "}";
        StringBuilder ret = new StringBuilder("%class." + typeName + " = type {");
        for (int i = 0; i < fields.size(); i++) {
            ret.append(fields.get(i).toString());
            if (i != fields.size() - 1) ret.append(", ");
        }
        ret.append("}");
        return ret.toString();
    }

}
