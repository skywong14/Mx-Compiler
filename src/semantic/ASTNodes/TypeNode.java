package semantic.ASTNodes;

import IR.IRStmts.ArrayIRType;
import IR.IRStmts.BasicIRType;
import IR.IRStmts.IRType;
import semantic.Type;

import java.util.ArrayList;

public class TypeNode extends ASTNode {
    private Type type;
    private ArrayList<ExpressionNode> expressions; // size of array

    public TypeNode(Type type_) {
        this.type = type_;
        this.expressions = new ArrayList<>();
    }

    public void addExpression(ExpressionNode expression) {
        expressions.add(expression);
    }

    public Type getType() { return type; }
    public ArrayList<ExpressionNode> getExpressions() { return expressions; }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    // IR
    /*
    public IRType toIRType() {
        if (type.is_array) {
            int dim = expressions.size();
            IRType retType;
            if (dim == type.dimension) {
               retType = new BasicIRType(type.baseType);
            } else {
                retType = new BasicIRType("ptr");
            }
            for (int i = dim; i >= 0; i--) {
                retType = new ArrayIRType(retType, expressions.get(i).toInt());
            }
        } else {
            return new BasicIRType(type.baseType);
        }
    }
    */
}
