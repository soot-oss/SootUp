grammar Jimpleparser;
/*
 * Lexer Rules
 */
fragment A          : ('A'|'a') ;
fragment S          : ('S'|'s') ;
fragment Y          : ('Y'|'y') ;
fragment H          : ('H'|'h') ;
fragment O          : ('O'|'o') ;
fragment U          : ('U'|'u') ;
fragment T          : ('T'|'t') ;
fragment LOWERCASE  : [a-z] ;
fragment UPPERCASE  : [A-Z] ;
SAYS                : S A Y S ;
SHOUTS              : S H O U T S;
WORD                : (LOWERCASE | UPPERCASE | '_')+ ;
WHITESPACE          : (' ' | '\t') ;
NEWLINE             : ('\r'? '\n' | '\r')+ ;
TEXT                : ~[\])]+ ;

/*
  * Parser Rules
  */
 chat                : line+ EOF ;
 line                : name command message NEWLINE;
 message             : (emoticon | link | color | mention | WORD | WHITESPACE)+ ;
 name                : WORD WHITESPACE;
 command             : (SAYS | SHOUTS) ':' WHITESPACE ;

 emoticon            : ':' '-'? ')'
                     | ':' '-'? '('
                     ;
 link                : '[' TEXT ']' '(' TEXT ')' ;
 color               : '/' WORD '/' message '/';
 mention             : '@' WORD ;


