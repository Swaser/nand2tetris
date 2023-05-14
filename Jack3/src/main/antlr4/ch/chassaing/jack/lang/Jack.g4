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
                block;

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

letStatement : 'let' ID '=' expression ';';

ifStatement : 'if' '(' expression ')' block ('else' (ifStatement|block))? ;

whileStatement : 'while' '(' expression ')' block;

doStatement : 'do' callSubroutine ';';

returnStatement : 'return' expression? ';';

block : '{' (localVarDec|statement)* '}';

expression : 'exp';

unary : ('!'|'-') unary
      | primary
      ;

primary : NUMBER
        | STRING
        | subroutineCall
        | 'true'
        | 'false'
        | 'null'
        | 'this'
        | '(' expression ')';

subroutineCall : 'call';

VOID : 'void';
INT : 'int';
CHAR : 'char';
BOOL : 'boolean';

FUNCTION : 'function';
CONSTRUCTOR : 'constructor';
METHOD : 'method';

ID : [_a-zA-Z] [_a-zA-Z0-9]*;
NUMBER : [0-9]+;
STRING : '"' ~["\r\n]* '"';
LINE_COMMENT: '//' ~[\r\n]+ '\r'? '\n' -> skip;
COMMENT : '/*' .*? '*/' -> skip;
WS : [ \t\r\n]+ -> skip;
