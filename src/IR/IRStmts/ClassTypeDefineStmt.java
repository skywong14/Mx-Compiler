package IR.IRStmts;

import semantic.ASTNodes.ClassNode;
import semantic.ASTNodes.FieldDeclarationNode;

import java.util.ArrayList;
import java.util.HashMap;

public class ClassTypeDefineStmt extends IRStmt{
    public String typeName;
    public ClassNode classNode;
    public ArrayList<BasicIRType> fields;
    public HashMap<String, Integer> fieldIndex;

    public ClassTypeDefineStmt(ClassNode classNode) {
        this.classNode = classNode;
        this.typeName = classNode.name;
        classNode.initFieldIndex();
        this.fieldIndex = classNode.getFieldIndex();
        fields = new ArrayList<>();
        for (FieldDeclarationNode field : classNode.getFieldNodes()) {
            for (int i = 0; i < field.getSize(); i++) {
                fields.add(new BasicIRType(field.getType()));
            }
        }
    }

    public int getFieldSize() { return fields.size(); }

    public HashMap<String, Integer> getFieldIndex() { return fieldIndex; }

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder("%class." + typeName + " = type {");
        for (int i = 0; i < fields.size(); i++) {
            ret.append(fields.get(i).toString());
            if (i != fields.size() - 1) ret.append(", ");
        }
        ret.append("}");
        return ret.toString();
    }

    @Override
    public int getSpSize() { return 0; }
}
