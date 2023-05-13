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
localVarDec : 'var' varDec;

varDec : type ID (',' ID)* ';';

type : 'int'
     | 'char'
     | 'boolean'
     | ID
     ;

subroutineDec : (FUNCTION|CONSTRUCTOR|METHOD)
                (VOID|type)
                ID
                '(' (parameter (',' parameter)*)? ')'
                block;

parameter : type ID;

block : '{' '}';

VOID : 'void';
INT : 'int';
CHAR : 'char';
BOOL : 'boolean';

FUNCTION : 'function';
CONSTRUCTOR : 'constructor';
METHOD : 'method';

ID : ([a-zA-Z]|'_'+ [a-zA-Z0-9]) [_a-zA-Z0-9]*;
LINE_COMMENT: '//' ~[\r\n]+ '\r'? '\n' -> skip;
COMMENT : '/*' .*? '*/' -> skip;
WS : [ \t\r\n]+ -> skip;
