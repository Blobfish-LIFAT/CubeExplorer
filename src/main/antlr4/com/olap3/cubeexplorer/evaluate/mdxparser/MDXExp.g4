grammar MDXExp;

// [Measure].[Un truc - (moyen)]*[Measure].[Chose]

/*
    Parser rules
*/
start : LPAREN expression RPAREN;

expression : measure OP measure | expression OP expression | expression OP measure | measure OP expression | LPAREN measure OP measure RPAREN;

Validtext : TEXT+(TEXT|TIRET|WS|LPAREN|RPAREN)*;

Atom : '[' Validtext ']';

measure : Atom'.'Atom;

/*
    Lexer rules
*/
fragment FRENCH : [éèàêôûâ'];
fragment LOWERCASE  : [a-z] ;
fragment UPPERCASE  : [A-Z] ;
TEXT : (LOWERCASE | UPPERCASE | FRENCH )+ ;
OP : ('+' | '-' | '/' | '*');
TIRET: '_' | '-';
LPAREN : '(';
RPAREN : ')';
WS : ' ' -> skip;
