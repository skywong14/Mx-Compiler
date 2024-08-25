package IR.IRStmts;

public class PhiStmt extends IRStmt{
    public String dest;
    public BasicIRType type;
    public String[] values;
    public String[] labels;

    public PhiStmt(BasicIRType type, String[] values, String[] labels, String dest) {
        this.dest = dest;
        this.type = type;
        this.values = values;
        this.labels = labels;
    }

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder(dest + " = phi " + type.typeName + " ");
        for (int i = 0; i < values.length; i++) {
            ret.append("[ ").append(values[i]).append(", ").append(labels[i]).append(" ]");
            if (i != values.length - 1) ret.append(", ");
        }
        return ret.toString();
    }
}
