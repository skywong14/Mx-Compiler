package optimize;

import IR.IRStmts.IRStmt;

import java.util.HashMap;

public class PhiStmt extends IRStmt {
    public String dest;
    public String originDest;
    public String type;
    public HashMap<String, String> val = new HashMap<>(); // blockLabel -> val

    public PhiStmt(String originDest, String type) {
        this.originDest = originDest;
        this.dest = originDest; // todo
        this.type = type;
    }

    public void addVal(String val, String blockLabel) {
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

    @Override
    public int getSpSize() {
        return 1; // todo
    }
}
