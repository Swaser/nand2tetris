grammar Jack;

// one class per file only
class : 'class' ID '{' classElement* '}';

classElement : classVarDec
             | subroutineDec
             ;

classVarDec : staticVarDec
            | fieldVarDec
            ;

staticVarDec : 'static' varDec;
fieldVarDec : 'field' varDec;

subroutineDec : (FUNCTION|CONSTRUCTOR|METHOD)
                (VOID|type)
                ID
                '(' (parameter (',' parameter)*)? ')'
                '{'
                localVarDec*
                statement*
                '}';

parameter : type ID;

localVarDec : 'var' varDec;

varDec : type ID (',' ID)* ';';

type : 'int'
     | 'char'
     | 'boolean'
     | ID
     ;

statement : letStatement
          | ifStatement
          | whileStatement
          | doStatement
          | returnStatement
          ;

letStatement : assignArray | assignVariable;
assignVariable : 'let' ID '=' expression ';';
assignArray : 'let' ID '[' expression ']' '=' expression ';';

ifStatement : 'if' '(' expression ')' block ('else' (ifStatement|block))? ;

whileStatement : 'while' '(' expression ')' block;

doStatement : 'do' subroutineCall ';';

returnStatement : 'return' expression? ';';

block : '{' statement* '}';

expression : equality;
equality : comparison (('=='|'!=') comparison)*;
comparison : term ( ('>'|'>='|'<='|'<') term)*;
term    : factor (('-'|'+'|'|') factor)*;  // includes bitwise OR
factor  : unary (('/'|'*'|'&') unary)*;    // includes bitwise AND
unary   : ('!'|'-') unary
        | primary
        ;

primary : '(' expression ')'
        | arrayReferencing
        | subroutineCall
        | ID
        | NUMBER
        | STRING
        | 'true'
        | 'false'
        | 'null'
        | 'this'
        ;

arrayReferencing : ID '[' expression ']';

subroutineCall : callLocal | callRemote;

callLocal : ID '(' expressionList ')';
callRemote : ID '.' ID '(' expressionList ')';

expressionList : (expression (',' expression)*)?;

VOID : 'void';
INT : 'int';
CHAR : 'char';
BOOL : 'boolean';

FUNCTION : 'function';
CONSTRUCTOR : 'constructor';
METHOD : 'method';

NEG : '~';
MINUS : '-';
PLUS : '+';
DIV : '/';
MULT : '*';

AND : '&';
OR : '|';
NOT : '!';

EQUAL : '==';
UNEQUAL : '!=';
LT : '<';
LE : '<=';
GT : '>';
GE : '>=';

TRUE : 'true';
FALSE : 'false';
NULL : 'null';
THIS : 'this';

ID : [_a-zA-Z] [_a-zA-Z0-9]*;
NUMBER : [0-9]+;
STRING : '"' ~["\r\n]* '"';
LINE_COMMENT: '//' ~[\r\n]+ '\r'? '\n' -> skip;
COMMENT : '/*' .*? '*/' -> skip;
WS : [ \t\r\n]+ -> skip;
