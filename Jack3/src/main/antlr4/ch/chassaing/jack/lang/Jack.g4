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

varDec : type ID (',' ID)* ';';

type : 'int'     # intType
     | 'char'    # charType
     | 'boolean' # boolType
     | ID        # userType
     ;

subroutineDec : ('function'|'constructor'|'method') ('void'|type) ID ';';

INT : 'int';
CHAR : 'char';
BOOL : 'boolean';

ID : [a-zA-Z]+;
LINE_COMMENT: '//' .*? '\r'? '\n' -> skip;
COMMENT : '/*' .*? '*/' -> skip;
WS : [ \t\r\n]+ -> skip;
