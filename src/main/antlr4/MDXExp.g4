grammar MDXExp;
/*
    Parser rules
*/
expression : measure OP measure;

validtext : TEXT+(TEXT|TIRET|WS|LPAREN|RPAREN)*;

atom : '[' validtext ']';

measure : atom'.'atom | LPAREN measure OP measure RPAREN;

/*
    Lexer rules
*/
fragment FRENCH : [éèàêôûâ];
fragment LOWERCASE  : [a-z] ;
fragment UPPERCASE  : [A-Z] ;
TEXT : (LOWERCASE | UPPERCASE | FRENCH )+ ;
OP : ('+' | '-' | '/' | '*');
TIRET: '_' | '-';
LPAREN : '(';
RPAREN : ')';
WS : ' ' -> skip;
