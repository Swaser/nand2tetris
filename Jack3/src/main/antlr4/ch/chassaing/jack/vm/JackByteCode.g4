grammar JackByteCode;



push : 'push' segment ;

segment : 'arg'
        | 'local'
        | 'this'
        | 'that'
        | 'pointer'
        ;

INT : [0-9]+ ;

ID : [_]+ [a-zA-Z0-9] [_a-zA-Z0-9]*
   | [a-zA-Z] [_a-zA-Z0-9]*;