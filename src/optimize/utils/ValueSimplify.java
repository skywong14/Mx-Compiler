package optimize.utils;

public class ValueSimplify {
    public int resolveValue(String name) {
        return switch (name) {
            case "true" -> 1;
            case "false", "null" -> 0;
            default -> Integer.parseInt(name);
        };
    }
    public int simplifyBinaryStmt(String operator, int val1, int val2) {
        return switch (operator) {
            case "add" -> val1 + val2;
            case "sub" -> val1 - val2;
            case "mul" -> val1 * val2;
            case "sdiv" -> val1 / val2;
            case "srem" -> val1 % val2;
            case "and" -> val1 & val2;
            case "or" -> val1 | val2;
            case "xor" -> val1 ^ val2;
            case "shl" -> val1 << val2;
            case "ashr" -> val1 >> val2;
            case "icmp eq" -> val1 == val2 ? 1 : 0;
            case "icmp ne" -> val1 != val2 ? 1 : 0;
            case "icmp slt" -> val1 < val2 ? 1 : 0;
            case "icmp sgt" -> val1 > val2 ? 1 : 0;
            case "icmp sle" -> val1 <= val2 ? 1 : 0;
            case "icmp sge" -> val1 >= val2 ? 1 : 0;
            default -> 0;
        };
    }
    public int simplifyUnaryStmt(String operator, int val) {
        return switch (operator) {
            case "!" -> val^1;
            case "~" -> ~val;
            case "-" -> -val;
            case "++" -> val + 1;
            case "--" -> val - 1;
            default -> 0;
        };
    }
}
