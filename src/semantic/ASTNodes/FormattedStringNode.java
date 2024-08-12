package semantic.ASTNodes;

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

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
