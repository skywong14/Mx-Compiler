package IR.IRStmts;

import java.util.HashMap;

public class PhiStmt extends IRStmt {
    public String dest;
    public String originDest;
    public String type;
    public HashMap<String, String> val = new HashMap<>(); // blockLabel -> val

    public PhiStmt(String originDest, String type) {
        this.originDest = originDest;
        this.dest = null;
        this.type = type;
    }

    public void setDest(String dest) {
        this.dest = dest;
    }

    public void addVal(String val, String blockLabel) {
        this.val.put(blockLabel, val);
    }

    public void addEmptyVal(String blockLabel) {
        String val;
        if (type.equals("i32")) {
            val = "0";
        } else if (type.equals("i1")) {
            val = "false";
        } else {
            val = "null";
        }
        this.val.put(blockLabel, val);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(dest).append(" = phi ").append(type).append(" ");
        boolean flag = false;
        for (String v : val.keySet()) {
            if (flag) sb.append(", ");
            flag = true;
            sb.append("[ ").append(val.get(v)).append(", %").append(v).append(" ] ");
        }
        return sb.toString();
    }
}
