grammar Calc;

prog: stat+;

stat: expr NEWLINE            # print
    | ID '=' expr NEWLINE     # assign
    | NEWLINE                 # blank
    ;

expr: expr op=('*'|'/') expr  # MulDiv
    | expr op=('+'|'-') expr  # AddSub
    | INT                     # num
    | ID                      # var
    | '(' expr ')'            # parens
    ;

MUL : '*';
DIV : '/';
ADD : '+';
SUB : '-';

ID: [a-zA-Z]+;
INT : [0-9]+;
NEWLINE : '\r'? '\n';
WS : [ \t]+ -> skip;