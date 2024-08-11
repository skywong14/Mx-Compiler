package semantic.ASTNodes;

public interface ASTVisitor {
    // 访问根节点
    void visit(ProgramNode it);

    // 访问类声明
    void visit(ClassNode it);

    // 访问构造函数
    void visit(ConstructorNode it);

    // 访问函数声明
    void visit(FunctionNode it);

    // 访问变量声明
    void visit(VariableNode it);

    // 访问复合语句
    void visit(CompoundStmtNode it);

    // 访问跳转语句
    void visit(JumpStmtNode it);

    // 访问条件语句
    void visit(IfStatementNode it);

    // 访问常量表达式
    void visit(ConstantNode it);

    // 访问表达式
    void visit(ExpressionNode it);

    // 访问主表达式
    void visit(PrimaryExpressionNode it);

    // 访问新建表达式
    void visit(NewExpressionNode it);

    // 访问数组类型
    void visit(ArrayTypeNode it);

    // 访问格式化字符串
    void visit(FormattedStringNode it);

    // 访问基本类型
    void visit(BasicTypeNode it);

    // 访问形参
    void visit(ParameterNode it);
    // 访问形参列表
    void visit(ParameterListNode it);

    // 访问实参
    void visit(ArgNode it);
    // 访问实参列表
    void visit(ArgListNode it);

    // 访问标识符
    void visit(IdentifierNode it);

    // 访问常量类型
    void visit(BooleanConstantNode it);
    void visit(IntegerConstantNode it);
    void visit(StringConstantNode it);
    void visit(NullConstantNode it);
    void visit(ArrayConstantNode it);

    // 访问类型
    void visit(TypeNode it);

    // EmptyStmt
    void visit(EmptyStmt it);
}
