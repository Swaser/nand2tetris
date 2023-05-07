grammar Jack;

// one class per file only
class : 'class' className '{' classVarDec* subroutineDec* '}';

expressionList : (expression (',' expression)*)?;

subroutineCall : subroutineName '(' expressionList ')'
               | (className|varName) '.' subroutineName '(' expressionList ')'
               ;

stringConst : '"' CHAR_SEQ '"'
            | '"' '"'
            ;

term : NUMBER
     | stringConst
     | keywordConst
     | varName
     | varName '[' expression ']'
     | '(' expression ')'
     | (unaryOp term)
     | subroutineCall;

keywordConst : 'true'|'false'|'null'|'this';

unaryOp : '-'|'~';

op : ('*'|'/')
   | ('+'|'-')
   | ('&'|'|')
   | ('<'|'>')
   | '='
   ;

expression : NUMBER;

letStatement : 'let' varName('[' expression']')? '=' expression;

ifStatement : 'if' '(' expression ')' block ('else' (ifStatement|block)) ;

whileStatement : 'while' '(' expression ')' block;

doStatement : 'do' subroutineCall ';';

returnStatement : 'return' expression? ';';

statement : letStatement
          | ifStatement
          | whileStatement
          | doStatement
          | returnStatement
          ;

type : 'int'|'char'|'boolean'|className;

varDec : 'var' type varName (',' varName)* ';';

blockElement : varDec
             | statement
             ;

block : '{' blockElement* '}';

parameterList : ( (type varName) (',' type varName)* )?;

subroutineDec : ('constructor'|'method'|'field')
                ('void'|type)
                subroutineName
                '(' parameterList ')'
                block;

classVarDec : ('static'|'field') type varName (',' varName)* ';';


className : ID;
varName : ID;
subroutineName : ID;


STATIC : 'static';
FIELD : 'field';
CONSTRUCTOR : 'constructor';
FUNCTION : 'function';
METHOD : 'method';
VAR : 'var';
LET : 'let';
IF : 'if';
WHILE : 'while';
DO : 'do';
RETURN : 'return';
VOID : 'void';
INT : 'int';
CHAR : 'char';
BOOLEAN : 'boolean';

PLUS : '+';
MINUS : '-';
STAR : '*';
SLASH : '/';
AMP : '&';
PIPE : '|';
LT : '<';
GT : '>';
EQUAL : '=';
NEG : '~';
LEFT_BRACE : '{';
RIGHT_BRACE : '}';
LEFT_PAREN : '(';
RIGHT_PAREN : ')';
LEFT_BRACKET : '[';
RIGHT_BRACKET : ']';
DOT : '.';
COMMA : ',';
SEMICOLON : ';';


NUMBER : [0-9]+;

ID : [_a-zA-Z] [_a-zA-Z0-9]*;

CHAR_SEQ : [-_a-zA-Z0-9 \t+"*ç%&/()=[\]{}`'~´^!$£]+;

WS : [ \t\r\n]+ -> skip;
