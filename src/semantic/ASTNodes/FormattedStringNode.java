package semantic.ASTNodes;

import semantic.ScopeManager;
import semantic.Type;

import java.util.ArrayList;

public class FormattedStringNode extends PrimaryExpressionNode{
    private ArrayList<String> plain_strings;
    private ArrayList<ExpressionNode> expressions;

    public FormattedStringNode(String plain_string) {
        plain_strings = new ArrayList<>();
        expressions = new ArrayList<>();
        plain_strings.add(plain_string);
    }
    public void addPlainString(String plain_string) {
        plain_strings.add(plain_string);
    }
    public void addExpression(ExpressionNode expression) {
        expressions.add(expression);
    }

    public ArrayList<String> getPlainStrings() {
        return plain_strings;
    }
    public ArrayList<ExpressionNode> getExpressions() {
        return expressions;
    }

    @Override
    public Type deduceType(ScopeManager scopeManager) {
        return new Type("string");
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
