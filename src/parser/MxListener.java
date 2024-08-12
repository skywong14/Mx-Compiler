// Generated from C:/Users/skywa/IdeaProjects/Mx-Compiler/src/parser/Mx.g4 by ANTLR 4.13.1
package parser;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link MxParser}.
 */
public interface MxListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link MxParser#file_input}.
	 * @param ctx the parse tree
	 */
	void enterFile_input(MxParser.File_inputContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxParser#file_input}.
	 * @param ctx the parse tree
	 */
	void exitFile_input(MxParser.File_inputContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxParser#function_declaration}.
	 * @param ctx the parse tree
	 */
	void enterFunction_declaration(MxParser.Function_declarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxParser#function_declaration}.
	 * @param ctx the parse tree
	 */
	void exitFunction_declaration(MxParser.Function_declarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxParser#class_declaration}.
	 * @param ctx the parse tree
	 */
	void enterClass_declaration(MxParser.Class_declarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxParser#class_declaration}.
	 * @param ctx the parse tree
	 */
	void exitClass_declaration(MxParser.Class_declarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxParser#class_body}.
	 * @param ctx the parse tree
	 */
	void enterClass_body(MxParser.Class_bodyContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxParser#class_body}.
	 * @param ctx the parse tree
	 */
	void exitClass_body(MxParser.Class_bodyContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxParser#constructor_declaration}.
	 * @param ctx the parse tree
	 */
	void enterConstructor_declaration(MxParser.Constructor_declarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxParser#constructor_declaration}.
	 * @param ctx the parse tree
	 */
	void exitConstructor_declaration(MxParser.Constructor_declarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxParser#variable_declaration}.
	 * @param ctx the parse tree
	 */
	void enterVariable_declaration(MxParser.Variable_declarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxParser#variable_declaration}.
	 * @param ctx the parse tree
	 */
	void exitVariable_declaration(MxParser.Variable_declarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxParser#variable_declaration_list}.
	 * @param ctx the parse tree
	 */
	void enterVariable_declaration_list(MxParser.Variable_declaration_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxParser#variable_declaration_list}.
	 * @param ctx the parse tree
	 */
	void exitVariable_declaration_list(MxParser.Variable_declaration_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxParser#single_variable_declaration}.
	 * @param ctx the parse tree
	 */
	void enterSingle_variable_declaration(MxParser.Single_variable_declarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxParser#single_variable_declaration}.
	 * @param ctx the parse tree
	 */
	void exitSingle_variable_declaration(MxParser.Single_variable_declarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxParser#compound_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCompound_stmt(MxParser.Compound_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxParser#compound_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCompound_stmt(MxParser.Compound_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxParser#trailer}.
	 * @param ctx the parse tree
	 */
	void enterTrailer(MxParser.TrailerContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxParser#trailer}.
	 * @param ctx the parse tree
	 */
	void exitTrailer(MxParser.TrailerContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxParser#parameter_list}.
	 * @param ctx the parse tree
	 */
	void enterParameter_list(MxParser.Parameter_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxParser#parameter_list}.
	 * @param ctx the parse tree
	 */
	void exitParameter_list(MxParser.Parameter_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxParser#parameter}.
	 * @param ctx the parse tree
	 */
	void enterParameter(MxParser.ParameterContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxParser#parameter}.
	 * @param ctx the parse tree
	 */
	void exitParameter(MxParser.ParameterContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterStatement(MxParser.StatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitStatement(MxParser.StatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxParser#empty_stmt}.
	 * @param ctx the parse tree
	 */
	void enterEmpty_stmt(MxParser.Empty_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxParser#empty_stmt}.
	 * @param ctx the parse tree
	 */
	void exitEmpty_stmt(MxParser.Empty_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxParser#while_stmt}.
	 * @param ctx the parse tree
	 */
	void enterWhile_stmt(MxParser.While_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxParser#while_stmt}.
	 * @param ctx the parse tree
	 */
	void exitWhile_stmt(MxParser.While_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxParser#for_stmt}.
	 * @param ctx the parse tree
	 */
	void enterFor_stmt(MxParser.For_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxParser#for_stmt}.
	 * @param ctx the parse tree
	 */
	void exitFor_stmt(MxParser.For_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxParser#jump_stmt}.
	 * @param ctx the parse tree
	 */
	void enterJump_stmt(MxParser.Jump_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxParser#jump_stmt}.
	 * @param ctx the parse tree
	 */
	void exitJump_stmt(MxParser.Jump_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxParser#break_stmt}.
	 * @param ctx the parse tree
	 */
	void enterBreak_stmt(MxParser.Break_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxParser#break_stmt}.
	 * @param ctx the parse tree
	 */
	void exitBreak_stmt(MxParser.Break_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxParser#continue_stmt}.
	 * @param ctx the parse tree
	 */
	void enterContinue_stmt(MxParser.Continue_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxParser#continue_stmt}.
	 * @param ctx the parse tree
	 */
	void exitContinue_stmt(MxParser.Continue_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxParser#return_stmt}.
	 * @param ctx the parse tree
	 */
	void enterReturn_stmt(MxParser.Return_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxParser#return_stmt}.
	 * @param ctx the parse tree
	 */
	void exitReturn_stmt(MxParser.Return_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxParser#constant}.
	 * @param ctx the parse tree
	 */
	void enterConstant(MxParser.ConstantContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxParser#constant}.
	 * @param ctx the parse tree
	 */
	void exitConstant(MxParser.ConstantContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxParser#booleanConstant}.
	 * @param ctx the parse tree
	 */
	void enterBooleanConstant(MxParser.BooleanConstantContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxParser#booleanConstant}.
	 * @param ctx the parse tree
	 */
	void exitBooleanConstant(MxParser.BooleanConstantContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxParser#integerConstant}.
	 * @param ctx the parse tree
	 */
	void enterIntegerConstant(MxParser.IntegerConstantContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxParser#integerConstant}.
	 * @param ctx the parse tree
	 */
	void exitIntegerConstant(MxParser.IntegerConstantContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxParser#stringConstant}.
	 * @param ctx the parse tree
	 */
	void enterStringConstant(MxParser.StringConstantContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxParser#stringConstant}.
	 * @param ctx the parse tree
	 */
	void exitStringConstant(MxParser.StringConstantContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxParser#nullConstant}.
	 * @param ctx the parse tree
	 */
	void enterNullConstant(MxParser.NullConstantContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxParser#nullConstant}.
	 * @param ctx the parse tree
	 */
	void exitNullConstant(MxParser.NullConstantContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxParser#arrayConstant}.
	 * @param ctx the parse tree
	 */
	void enterArrayConstant(MxParser.ArrayConstantContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxParser#arrayConstant}.
	 * @param ctx the parse tree
	 */
	void exitArrayConstant(MxParser.ArrayConstantContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxParser#if_stmt}.
	 * @param ctx the parse tree
	 */
	void enterIf_stmt(MxParser.If_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxParser#if_stmt}.
	 * @param ctx the parse tree
	 */
	void exitIf_stmt(MxParser.If_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxParser#expression_stmt}.
	 * @param ctx the parse tree
	 */
	void enterExpression_stmt(MxParser.Expression_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxParser#expression_stmt}.
	 * @param ctx the parse tree
	 */
	void exitExpression_stmt(MxParser.Expression_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxParser#arglist}.
	 * @param ctx the parse tree
	 */
	void enterArglist(MxParser.ArglistContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxParser#arglist}.
	 * @param ctx the parse tree
	 */
	void exitArglist(MxParser.ArglistContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterExpression(MxParser.ExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitExpression(MxParser.ExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxParser#primary_expression}.
	 * @param ctx the parse tree
	 */
	void enterPrimary_expression(MxParser.Primary_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxParser#primary_expression}.
	 * @param ctx the parse tree
	 */
	void exitPrimary_expression(MxParser.Primary_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxParser#new_expression}.
	 * @param ctx the parse tree
	 */
	void enterNew_expression(MxParser.New_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxParser#new_expression}.
	 * @param ctx the parse tree
	 */
	void exitNew_expression(MxParser.New_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxParser#array_type}.
	 * @param ctx the parse tree
	 */
	void enterArray_type(MxParser.Array_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxParser#array_type}.
	 * @param ctx the parse tree
	 */
	void exitArray_type(MxParser.Array_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxParser#formatted_string}.
	 * @param ctx the parse tree
	 */
	void enterFormatted_string(MxParser.Formatted_stringContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxParser#formatted_string}.
	 * @param ctx the parse tree
	 */
	void exitFormatted_string(MxParser.Formatted_stringContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxParser#type}.
	 * @param ctx the parse tree
	 */
	void enterType(MxParser.TypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxParser#type}.
	 * @param ctx the parse tree
	 */
	void exitType(MxParser.TypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxParser#basic_type}.
	 * @param ctx the parse tree
	 */
	void enterBasic_type(MxParser.Basic_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxParser#basic_type}.
	 * @param ctx the parse tree
	 */
	void exitBasic_type(MxParser.Basic_typeContext ctx);
}