grammar Jimple;

/*
 * Lexer Rules
 */

  LINE_COMMENT : '//' ~('\n'|'\r')* ->skip;
  LONG_COMMENT : '/*' ~('*')* '*'+ ( ~('*' | '/')* ~('*')* '*'+)*? '/' -> skip;

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
  QUOTE : '\'';


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
    '\\' | ' ' | '\'' | '"' | '.'  | 'n' | 't' | 'r' | 'b' | 'f';
  fragment ESCAPE_CHAR :
    '\\' (ESCAPABLE_CHAR | 'u' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT );

  // escapes and any char except \ (92) or " (34) and '.
  fragment STRING_CHAR :  ESCAPE_CHAR | ~('\\' | '"' | '\'') ;

  IDENTIFIER:
    (([\p{L}$_] | ESCAPE_CHAR | QUOTE) ( (ESCAPE_CHAR | [\p{L}0-9$_] | QUOTE) | '.' (ESCAPE_CHAR | [\p{L}0-9$_] | QUOTE) )*);

  BLANK :
    [ \t\r\n] ->skip;

  // UNKNOWN_TOKEN : . ;

 /*
  * Parser Rules
  */
  identifier:
    IDENTIFIER ;

  integer_constant :
    (PLUS|MINUS)? (DEC_CONSTANT | HEX_CONSTANT ) 'L'?;

  file:
    importItem* class_modifier* file_type classname=IDENTIFIER extends_clause? implements_clause? L_BRACE member* R_BRACE EOF;

  importItem:
    'import' location=identifier SEMICOLON;

  common_modifier :
    'final' | 'public' | 'protected' | 'private' | 'static' | 'enum'| 'synthetic';

  class_modifier :
   common_modifier | 'abstract' | 'super';

  method_modifier :
   common_modifier | 'abstract' | 'native' | 'synchronized' | 'varargs'| 'bridge' | 'strictfp';

  field_modifier :
   common_modifier  | 'transient' | 'volatile';

  file_type :
    CLASS | 'interface' | 'annotation interface';

  extends_clause :
    EXTENDS classname=identifier;

  implements_clause :
    IMPLEMENTS type_list;

  type:
    identifier (L_BRACKET R_BRACKET)*;


  type_list:
    type (COMMA type)*;

  member :
   field | method;

  field :
    field_modifier* type identifier SEMICOLON;

  method :
    method_modifier* method_subsignature throws_clause? method_body;

  method_name :
    '<init>' | '<clinit>' | identifier;

  throws_clause :
    'throws' type_list;

  method_body :
    /*empty*/    SEMICOLON |
    /*full*/     L_BRACE method_body_contents R_BRACE;

  method_body_contents:
    declarations statements trap_clauses;

  trap_clauses :
    trap_clause*;

  statements :
    statement*;

  declarations :
      declaration*;

  declaration :
    type arg_list SEMICOLON;

  statement :
    (label_name=identifier COLON)? stmt SEMICOLON;

  stmt :
    assignments |
    (IF bool_expr)? goto_stmt |
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
    /*identity*/     local=identifier COLON_EQUALS identity_ref |
    /*assign*/       (reference | local=identifier) EQUALS value ;

  identity_ref :
    '@parameter' parameter_idx=DEC_CONSTANT ':' type | '@this:' type | caught='@caughtexception';

  case_stmt :
    case_label COLON goto_stmt SEMICOLON;

  case_label :
    CASE integer_constant |
    DEFAULT;

  goto_stmt :
    GOTO label_name=identifier;

  trap_clause :
    CATCH exceptiontype=identifier FROM from=identifier TO to=identifier WITH with=identifier SEMICOLON;

  value :
    /*immediate*/      immediate |
    /*reference*/      reference |
    /*new primitive*/  NEW base_type=identifier |
    /*new array*/   NEWARRAY L_PAREN array_type=type R_PAREN array_descriptor |
    /*new multi*/   NEWMULTIARRAY L_PAREN multiarray_type=type R_PAREN (L_BRACKET immediate? R_BRACKET)+ |
    /*cast*/        L_PAREN nonvoid_cast=type R_PAREN op=immediate |
    /*instanceof*/  op=immediate INSTANCEOF nonvoid_type=type |
    /*binop*/       binop_expr |
    /*invoke*/      invoke_expr |
    /*unop*/        unop_expr ;

  bool_expr :
    /*binop*/ binop_expr |
    /*unop*/  unop_expr;

  invoke_expr :
    /*nonstatic*/ nonstaticinvoke=NONSTATIC_INVOKE local_name=identifier DOT method_signature L_PAREN arg_list? R_PAREN |
    /*static*/    staticinvoke=STATICINVOKE method_signature L_PAREN arg_list? R_PAREN |
    /*dynamic*/   dynamicinvoke=DYNAMICINVOKE unnamed_method_name=STRING_CONSTANT CMPLT name=type L_PAREN parameter_list=type_list? R_PAREN CMPGT L_PAREN dyn_args=arg_list? R_PAREN
                                                                                              bsm=method_signature L_PAREN staticargs=arg_list? R_PAREN;

  binop_expr :
    left=immediate binop right=immediate;

  unop_expr :
    unop immediate;

  method_subsignature:
   type method_name L_PAREN type_list? R_PAREN;

  method_signature :
    CMPLT class_name=identifier COLON method_subsignature CMPGT;


  reference :
    /*array*/ identifier array_descriptor |
    /*field*/
    /*instance*/ identifier DOT field_signature |
    /*static*/   field_signature;

  field_signature :
    CMPLT classname=identifier COLON type fieldname=identifier CMPGT;

  array_descriptor :
    L_BRACKET immediate R_BRACKET;

  arg_list :
    immediate (COMMA immediate)*;

  immediate :
    /*local*/    local=identifier |
    /*constant*/ constant;

  constant :
    /*boolean*/ BOOL_CONSTANT |
    /*integer*/ integer_constant |
    /*float*/   FLOAT_CONSTANT |
    /*string*/  STRING_CONSTANT |
    /*clazz*/   CLASS STRING_CONSTANT |
    /*null*/    NULL |
                methodhandle='handle:' method_signature |
                methodtype='methodtype:' method_subsignature ;

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

