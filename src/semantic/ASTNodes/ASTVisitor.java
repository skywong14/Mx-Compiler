package semantic.ASTNodes;

public interface ASTVisitor {
    // 访问实参列表
    void visit(ArgListNode it);

    // 访问数组类型
    void visit(ArrayAccessNode it);

    // 访问二元表达式
    void visit(BinaryExprNode it);

    // 访问类声明
    void visit(ClassNode it);

    // 访问复合语句
    void visit(CompoundStmtNode it);

    // 访问常量表达式
    void visit(ConstantNode it);

    // 访问构造函数
    void visit(ConstructorNode it);

    // 访问空语句
    void visit(EmptyStmtNode it);

    // 访问表达式
    void visit(ExpressionNode it);

    // 访问表达式语句
    void visit(ExpressionStmtNode it);

    // 访问字段声明
    void visit(FieldDeclarationNode it);

    // 访问格式化字符串
    void visit(FormattedStringNode it);

    // 访问For语句
    void visit(ForStmtNode it);

    // 访问函数调用
    void visit(FunctionCallNode it);

    // 访问函数声明
    void visit(FunctionNode it);

    // 访问标识符
    void visit(IdentifierNode it);

    // 访问条件语句
    void visit(IfStatementNode it);

    // 访问跳转语句
    void visit(JumpStmtNode it);

    // 访问成员访问
    void visit(MemberAccessNode it);

    // 访问新建表达式
    void visit(NewExprNode it);

    // 访问形参列表
    void visit(ParameterListNode it);

    // 访问形参
    void visit(ParameterNode it);

    // 访问PrimaryExpression
    void visit(PrimaryExpressionNode it);

    // 访问根节点
    void visit(ProgramNode it);

    // 访问Statement
    void visit(StatementNode it);

    // 访问三元表达式
    void visit(TernaryExprNode it);

    // 访问类型
    void visit(TypeNode it);

    // 访问一元表达式
    void visit(UnaryExprNode it);

    // 访问变量
    void visit(VariableNode it);

    // 访问变量声明
    void visit(VariableDeclarationNode it);

    // 访问While语句
    void visit(WhileStmtNode it);
}
