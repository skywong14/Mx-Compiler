// Generated from C:/Users/skywa/IdeaProjects/Mx-Compiler/src/parser/Mx.g4 by ANTLR 4.13.1
package parser;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link MxParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface MxVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link MxParser#file_input}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFile_input(MxParser.File_inputContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#function_declaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunction_declaration(MxParser.Function_declarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#class_declaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClass_declaration(MxParser.Class_declarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#class_body}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClass_body(MxParser.Class_bodyContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#constructor_declaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstructor_declaration(MxParser.Constructor_declarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#variable_declaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariable_declaration(MxParser.Variable_declarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#variable_declaration_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariable_declaration_list(MxParser.Variable_declaration_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#single_variable_declaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSingle_variable_declaration(MxParser.Single_variable_declarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#compound_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCompound_stmt(MxParser.Compound_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#trailer}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTrailer(MxParser.TrailerContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#parameter_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParameter_list(MxParser.Parameter_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#parameter}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParameter(MxParser.ParameterContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatement(MxParser.StatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#empty_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEmpty_stmt(MxParser.Empty_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#while_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhile_stmt(MxParser.While_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#for_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFor_stmt(MxParser.For_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#jump_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJump_stmt(MxParser.Jump_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#break_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBreak_stmt(MxParser.Break_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#continue_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitContinue_stmt(MxParser.Continue_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#return_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReturn_stmt(MxParser.Return_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#constant}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstant(MxParser.ConstantContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#booleanConstant}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBooleanConstant(MxParser.BooleanConstantContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#integerConstant}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIntegerConstant(MxParser.IntegerConstantContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#stringConstant}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStringConstant(MxParser.StringConstantContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#nullConstant}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNullConstant(MxParser.NullConstantContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#arrayConstant}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArrayConstant(MxParser.ArrayConstantContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#if_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIf_stmt(MxParser.If_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#expression_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression_stmt(MxParser.Expression_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#arglist}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArglist(MxParser.ArglistContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression(MxParser.ExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#primary_expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimary_expression(MxParser.Primary_expressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#new_expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNew_expression(MxParser.New_expressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#array_type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArray_type(MxParser.Array_typeContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#formatted_string}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFormatted_string(MxParser.Formatted_stringContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitType(MxParser.TypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#basic_type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBasic_type(MxParser.Basic_typeContext ctx);
}