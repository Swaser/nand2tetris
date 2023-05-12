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

type : 'int'     # intType
     | 'char'    # charType
     | 'boolean' # boolType
     | ID        # userType
     ;

subroutineDec : (FUNCTION|CONSTRUCTOR|METHOD) (VOID|type) ID parameterList block;

parameterList : '(' ')';

block : '{' '}';

VOID : 'void';
INT : 'int';
CHAR : 'char';
BOOL : 'boolean';

FUNCTION : 'function';
CONSTRUCTOR : 'constructor';
METHOD : 'method';

ID : [a-zA-Z]+;
LINE_COMMENT: '//' ~[\r\n]+ '\r'? '\n' -> skip;
COMMENT : '/*' .*? '*/' -> skip;
WS : [ \t\r\n]+ -> skip;
