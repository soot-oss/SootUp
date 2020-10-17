grammar Jimple;

/*
 * Lexer Rules
 */

  LINE_COMMENT : '//' ~('\n'|'\r')* ->skip;
  LONG_COMMENT : '/*' ~('*')* '*'+ ( ~('*' | '/')* ~('*')* '*'+)* '/' -> skip;

  STRING_CONSTANT : '"' STRING_CHAR* '"';

  CLASS : 'class';
  EXTENDS : 'extends';
  IMPLEMENTS : 'implements';

  BREAKPOINT : 'breakpoint';
  CASE : 'case';
  CATCH : 'catch';
  CMP : 'cmp';
  CMPG : 'cmpg';
  CMPL : 'cmpl';
  DEFAULT : 'default';
  ENTERMONITOR : 'entermonitor';
  EXITMONITOR : 'exitmonitor';
  GOTO : 'goto';
  IF : 'if';
  INSTANCEOF : 'instanceof';
  LENGTHOF : 'lengthof';
  SWITCH : 'lookupswitch' | 'tableswitch' | 'switch';
  NEG : 'neg';
  NEWARRAY : 'newarray';
  NEWMULTIARRAY : 'newmultiarray';
  NEW : 'new';
  NOP : 'nop';
  RETURN : 'return';
  RET : 'ret';

  fragment SPECIALINVOKE : 'specialinvoke';
  fragment VIRTUALINVOKE : 'virtualinvoke';
  fragment INTERFACEINVOKE : 'interfaceinvoke';
  NONSTATIC_INVOKE: SPECIALINVOKE | VIRTUALINVOKE | INTERFACEINVOKE;

  STATICINVOKE : 'staticinvoke';
  DYNAMICINVOKE : 'dynamicinvoke';

  THROWS : 'throws';
  THROW : 'throw';
  NULL : 'null';
  FROM : 'from';
  TO : 'to';
  WITH : 'with';

  COMMA : ',';
  L_BRACE : '{';
  R_BRACE : '}';
  SEMICOLON : ';';
  L_BRACKET : '[';
  R_BRACKET : ']';
  L_PAREN : '(';
  R_PAREN : ')';
  COLON : ':';
  DOT : '.';
  EQUALS : '=';
  COLON_EQUALS : ':=';
  AND : '&';
  OR : '|';
  XOR : '^';
  MOD : '%';
  CMPEQ : '==';
  CMPNE : '!=';
  CMPGT : '>';
  CMPGE : '>=';
  CMPLT : '<';
  CMPLE : '<=';
  SHL : '<<';
  SHR : '>>';
  USHR : '>>>';
  PLUS : '+';
  MINUS : '-';
  MULT : '*';
  DIV : '/';


  BOOL_CONSTANT :
    'true' | 'false';

  FLOAT_CONSTANT :
    (PLUS|MINUS)? ((DEC_CONSTANT DOT DEC_CONSTANT) (('e'|'E') (PLUS|MINUS)? DEC_CONSTANT)? ('f'|'F')?)  | ('#' (('-'? 'Infinity') | 'NaN') ('f' | 'F')? ) ;

  DEC_CONSTANT :
    DEC_DIGIT+;
  fragment DEC_DIGIT :
    [0-9];
  fragment HEX_DIGIT :
    [0-9A-Fa-f];
  HEX_CONSTANT :
    '0' ('x' | 'X') HEX_DIGIT+;

  fragment ESCAPABLE_CHAR :
    '\\' | ' ' | '\'' | '.' | '"' | 'n' | 't' | 'r' | 'b' | 'f';
  fragment ESCAPE_CHAR :
    '\\' (ESCAPABLE_CHAR | 'u' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT );

  // escapes and any char except '\' (92) or '"' (34).
  fragment STRING_CHAR :  ESCAPE_CHAR | ~('\\' | '"') ;

  IDENTIFIER:
    (([A-Za-z$_] | ESCAPE_CHAR) ( (ESCAPE_CHAR | [A-Za-z0-9$_]) | '.' (ESCAPE_CHAR | [A-Za-z0-9$_]) )*) | ('"' ~('\n' | '\r' | '"') '"');

  BLANK :
    [ \t\r\n] ->skip;


  CATCHERROR:
    . ;

 /*
  * Parser Rules
  */

  integer_constant :
    (PLUS|MINUS)? (DEC_CONSTANT | HEX_CONSTANT ) 'L'?;

  file:
    importItem* modifier* file_type classname=IDENTIFIER extends_clause? implements_clause? L_BRACE member* R_BRACE;

  importItem:
    'import' location=IDENTIFIER SEMICOLON;

  modifier :
    'abstract' | 'final' | 'native' | 'public' | 'protected' | 'private' | 'static' | 'synchronized' | 'transient' |'volatile' | 'strictfp' | 'enum';

  file_type :
    'class' | 'interface' | 'annotation';

  extends_clause :
    'extends' classname=IDENTIFIER;

  implements_clause :
    'implements' type_list;

  type:
    IDENTIFIER (L_BRACKET R_BRACKET)*;


  type_list:
    type (COMMA type)*;

  member :
   field | method;

  field :
    modifier* type IDENTIFIER SEMICOLON;

  method :
    modifier* type method_name L_PAREN type_list? R_PAREN throws_clause? method_body;

  method_name :
    '<init>' | '<clinit>' | IDENTIFIER;

  throws_clause :
    'throws' type_list;

  method_body :
    /*empty*/    SEMICOLON |
    /*full*/     L_BRACE declaration* statement* trap_clause* R_BRACE;

  declaration :
    type arg_list SEMICOLON;

  statement :
    (label_name=IDENTIFIER COLON)? stmt SEMICOLON;

  stmt :
    assignments |
    IF bool_expr goto_stmt |
    goto_stmt |
    invoke_expr |
    RETURN immediate? |
    SWITCH L_PAREN immediate R_PAREN L_BRACE case_stmt+ R_BRACE |
    RET immediate? |
    THROW immediate |
    ENTERMONITOR immediate |
    EXITMONITOR immediate |
    NOP |
    BREAKPOINT;

  assignments :
    /*identity*/     local=IDENTIFIER COLON_EQUALS identity_ref |
    /*assign*/       (reference | local=IDENTIFIER) EQUALS value ;

  identity_ref :
    '@parameter' parameter_idx=DEC_CONSTANT ':' type | '@this:' type | caught='@caughtexception';

  case_stmt :
    case_label COLON goto_stmt SEMICOLON;

  case_label :
    CASE integer_constant |
    DEFAULT;

  goto_stmt :
    GOTO label_name=IDENTIFIER;

  trap_clause :
    CATCH exceptiontype=IDENTIFIER FROM from=IDENTIFIER TO to=IDENTIFIER WITH with=IDENTIFIER SEMICOLON;

  value :
    /*immediate*/      immediate |
    /*reference*/      reference |
    /*new primitive*/  NEW base_type=IDENTIFIER |
    /*new array*/   NEWARRAY L_PAREN array_type=type R_PAREN array_descriptor |
    /*new multi*/   NEWMULTIARRAY L_PAREN multiarray_type=IDENTIFIER R_PAREN (L_BRACKET immediate? R_BRACKET)+ |
    /*cast*/        L_PAREN nonvoid_cast=type R_PAREN op=immediate |
    /*instanceof*/  op=immediate INSTANCEOF nonvoid_type=type |
    /*binop*/       binop_expr |
    /*invoke*/      invoke_expr |
    /*unop*/        unop_expr ;

  bool_expr :
    /*binop*/ binop_expr |
    /*unop*/  unop_expr;

  invoke_expr :
    /*nonstatic*/ nonstaticinvoke=NONSTATIC_INVOKE local_name=IDENTIFIER DOT method_signature L_PAREN arg_list? R_PAREN |
    /*static*/    staticinvoke=STATICINVOKE method_signature L_PAREN arg_list? R_PAREN |
    /*dynamic*/   dynamicinvoke=DYNAMICINVOKE name=STRING_CONSTANT CMPLT unnamed_method_name=type L_PAREN parameter_list=type_list? R_PAREN CMPGT L_PAREN dyn_args=arg_list? R_PAREN
                                                                                              bsm=method_signature L_PAREN staticargs=arg_list? R_PAREN;

  binop_expr :
    left=immediate binop right=immediate;

  unop_expr :
    unop immediate;

  method_signature :
    CMPLT class_name=IDENTIFIER COLON type method_name L_PAREN type_list? R_PAREN CMPGT;

  reference :
    /*array*/ IDENTIFIER array_descriptor |
    /*field*/
    /*instance*/ IDENTIFIER DOT field_signature |
    /*static*/   field_signature;

  field_signature :
    CMPLT classname=IDENTIFIER COLON type fieldname=IDENTIFIER CMPGT;

  array_descriptor :
    L_BRACKET immediate R_BRACKET;

  arg_list :
    immediate (COMMA immediate)*;

  immediate :
    /*local*/    local=IDENTIFIER |
    /*constant*/ constant;

  constant :
    /*boolean*/ BOOL_CONSTANT |
    /*integer*/ integer_constant |
    /*float*/   FLOAT_CONSTANT |
    /*string*/  STRING_CONSTANT |
    /*clazz*/   CLASS STRING_CONSTANT |
    /*null*/    NULL;

  binop :
    /*and*/   AND |
    /*or*/    OR |
    /*xor*/   XOR |
    /*cmp*/   CMP |
    /*cmpg*/  CMPG |
    /*cmpl*/  CMPL |
    /*cmpeq*/ CMPEQ |
    /*cmpne*/ CMPNE |
    /*cmpgt*/ CMPGT |
    /*cmpge*/ CMPGE |
    /*cmplt*/ CMPLT |
    /*cmple*/ CMPLE |
    /*shl*/   SHL |
    /*shr*/   SHR |
    /*ushr*/  USHR |
    /*plus*/  PLUS |
    /*minus*/ MINUS |
    /*mult*/  MULT |
    /*div*/   DIV |
    /*mod*/   MOD;


  unop :
     LENGTHOF | NEG;

