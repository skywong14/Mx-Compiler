grammar Mx;

file_input: (class_declaration | function_declaration | variable_declaration)* EOF ;

function_declaration : type IDENTIFIER trailer compound_stmt ;

class_declaration : 'class' IDENTIFIER class_body ';' ;
class_body: '{' (field_declration | function_declaration | constructor_declaration)* '}';

constructor_declaration : IDENTIFIER '(' ')' compound_stmt ;

field_declration : type IDENTIFIER (',' IDENTIFIER)* ';' ;
variable_declaration : type variable_declaration_list ';' ;
variable_declaration_list : single_variable_declaration (',' single_variable_declaration)* ;
single_variable_declaration : IDENTIFIER ('=' expression)? ;

compound_stmt : '{' statement* '}' ; //  '{' and '}': new_scope

trailer: '(' (parameter_list)? ')' ;

parameter_list: parameter (',' parameter)* ;
parameter: type IDENTIFIER ;

statement
    : empty_stmt
    | expression_stmt
    | compound_stmt
    | if_stmt // if and if-else
    | while_stmt
    | for_stmt
    | jump_stmt // break, continue, return
    | variable_declaration // variable declaration
    ;
empty_stmt: ';';

while_stmt: 'while' '(' expression ')' statement;
for_stmt: 'for' '(' (empty_stmt | expression_stmt | variable_declaration) (contdition_expr = expression)? ';' (step_expr = expression)? ')' statement;


jump_stmt: break_stmt | continue_stmt | return_stmt;
break_stmt: 'break' ';';
continue_stmt: 'continue' ';';
return_stmt: 'return' (expression)? ';';

constant
    : booleanConstant
    | integerConstant
    | stringConstant
    | nullConstant
    | arrayConstant
    ;

booleanConstant : 'true' | 'false' ;
integerConstant : INTEGER_CONSTANT ;
stringConstant : STRING_LITERAL ;
nullConstant : 'null' ;
arrayConstant : '{' (constant (',' constant)*)? '}' ;

if_stmt : 'if' '(' expression ')' statement ('else' statement)?;

expression_stmt : expression ';' ;

arglist : expression (',' expression)*;

//--------------------expression--------------------
expression
    : primary_expression                                        # expression_primary
    | IDENTIFIER '(' arglist? ')'                               # primary_function_call
    | expression ('[' expression ']')+                          # array_access
    | expression '.' IDENTIFIER                                 # member_access
    | expression '.' IDENTIFIER '(' arglist? ')'                # member_function_call
    | '(' expression ')'                                        # expression_parenthesis
    | expression op = ('++' | '--')                             # unaryExpression
    | <assoc = right> opLeft = ('++' | '--') expression         # unaryExpression
    | <assoc = right> opLeft = ('~' | '!') expression           # unaryExpression
    | <assoc = right> opLeft = ('-' | '+') expression           # unaryExpression
    | expression op = ('*' | '/' | '%') expression              # binaryExpression
    | expression op = ('+' | '-') expression                    # binaryExpression
    | expression op = ('<<' | '>>') expression                  # binaryExpression
    | expression op = ('<' | '>' | '<=' | '>=') expression      # binaryExpression
    | expression op = ('==' | '!=') expression                  # binaryExpression
    | expression op = '&' expression                            # binaryExpression
    | expression op = '^' expression                            # binaryExpression
    | expression op = '|' expression                            # binaryExpression
    | expression op = '&&' expression                           # binaryExpression
    | expression op = '||' expression                           # binaryExpression
    | <assoc = right> expression op = '?' expression ':' expression # ternaryExpression
    | <assoc = right> expression op = '=' expression                # assignExpression
    ;
primary_expression
    : formatted_string              # primary_formatted_string
    | 'this'                        # primary_this
    | new_expression                # primary_new
    | constant                      # primary_constant
    | IDENTIFIER                    # primary_identifier
    ;
new_expression
    : 'new' new_array_type
    | 'new' IDENTIFIER ('(' ')')?
    ;

new_array_type
    : basic_type ('[' expression? ']')+
    ;

array_type
    : basic_type ('[' expression ']')+ ( '[' ']' )*
    | basic_type ('[' ']')+
    ;
//--------------------------------------------------
formatted_string
    :  Formatted_string_plain
    |  Formatted_string_begin expression ( Formatted_string_middle expression )* Formatted_string_end
    ;
Formatted_string_begin: 'f"' Formatted_string_character* '$' ;
Formatted_string_middle: '$' Formatted_string_character* '$' ;
Formatted_string_end: '$' Formatted_string_character* '"' ;
Formatted_string_plain
    : 'f"' Formatted_string_character* '"'
    ;
fragment Formatted_string_character
    : PRINTABLE_CHAR_WITHOUT_DOLLER | ESC | '$$'
    ;
type
    : array_type
    | basic_type
    ;

basic_type
    : BASIC_TYPE
    | IDENTIFIER ; //class type

BASIC_TYPE : Int | Bool | Void | String;
Class: 'class' ;
Int: 'int' ;
Bool: 'bool' ;
Void: 'void' ;
String: 'string' ;
If : 'if' ;
Else: 'else' ;
For : 'for' ;
While : 'while' ;
This : 'this' ;
Return : 'return' ;
Break : 'break' ;
Continue : 'continue' ;
LeftParen : '(';
RightParen : ')';
LeftBracket : '[';
RightBracket : ']';
LeftBrace : '{';
RightBrace : '}';
Dot : '.';

Less : '<';
LessEqual : '<=';
Greater : '>';
GreaterEqual : '>=';
LeftShift : '<<';
RightShift : '>>';

Plus : '+';
Minus : '-';

And : '&';
Or : '|';
AndAnd : '&&';
OrOr : '||';
Caret : '^';
Not : '!';
Tilde : '~';

Question : '?';
Colon : ':';
Semi : ';';
Comma : ',';

Assign : '=';
Equal : '==';
NotEqual : '!=';

INTEGER_CONSTANT : [1-9][0-9]* | '0' ;
STRING_LITERAL : '"' (PRINTABLE_CHAR | ESC)* '"' ;
IDENTIFIER: [a-zA-Z][a-zA-Z0-9_]* ;

//排除了双引号 ("，\u0022)、反斜杠 (\，\u005C) 和美元符号 ($，\u0024)。
fragment PRINTABLE_CHAR
             :   [\u0020-\u0021]         // Space to !
             |   [\u0023-\u0024]         // # to $
             |   [\u0025-\u005B]         // % to [
             |   [\u005D-\u007E]         // ] to ~
             ;
fragment PRINTABLE_CHAR_WITHOUT_DOLLER
             :   [\u0020-\u0021]         // Space to !
             |   [\u0023]                // #
             |   [\u0025-\u005B]         // % to [
             |   [\u005D-\u007E]         // ] to ~
             ;
fragment ESC
    : '\\n'  // 换行符
    | '\\\\' // 反斜杠
    | '\\"'  // 双引号
    ;

// Comments and whitespace
WS : [ \t]+ -> skip ; // 空白字符
NEWLINE : ('\r''\n'? |'\n' ) -> skip ;
BLOCK_COMMENT: '/*' .*? '*/' -> skip ; // 多行注释
LINE_COMMENT : '//' ~[\r\n]* -> skip; // 单行注释
