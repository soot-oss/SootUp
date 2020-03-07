grammar Jimple;

/*
 * Lexer Rules
 */

  fragment DEC_DIGIT : [0-9];
  fragment DEC_CONSTANT : DEC_DIGIT+;

  fragment HEX_DIGIT : DEC_DIGIT | [A-Fa-f];
  fragment HEX_CONSTANT : '0' ('x' | 'X') HEX_DIGIT+;

  fragment OCT_DIGIT : [0-7];
  fragment OCT_CONSTANT : '0' OCT_DIGIT+;

  fragment QUOTE : '\'';

  fragment ESCAPABLE_CHAR : '\\' | ' ' | QUOTE | '.' | '"' | 'n' | 't' | 'r' | 'b' | 'f';
  fragment ESCAPE_CODE : 'u' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT;
  fragment ESCAPE_CHAR : '\\' (ESCAPABLE_CHAR | ESCAPE_CODE);

  fragment NOT_CR_LF : [^\u0010\u0013];
  fragment NOT_STAR : [^*];
  fragment NOT_STAR_SLASH : [^*\\];

  fragment SIMPLE_ID_CHAR : [a-zA-Z] | DEC_DIGIT | '_' | '$';

  fragment FIRST_ID_CHAR : [a-zA-Z] | '_' | '$';

  fragment QUOTABLE_CHAR : NOT_CR_LF | '\'' ;

  // escapes and any char except '\' (92) or '"' (34).
  fragment STRING_CHAR : ESCAPE_CHAR | [^\u0034\u0092] ;

  fragment LINE_COMMENT : '//' NOT_CR_LF*;
  fragment LONG_COMMENT : '/*' NOT_STAR* '*'+ (NOT_STAR_SLASH NOT_STAR* '*'+)* '/';

  BLANK : [ \t\r\n];        // ->skipt --->problem w/Strings ;)


  ABSTRACT : 'abstract';
  FINAL : 'final';
  NATIVE : 'native';
  PUBLIC : 'public';
  PROTECTED : 'protected';
  PRIVATE : 'private';
  STATIC : 'static';
  SYNCHRONIZED : 'synchronized';
  TRANSIENT : 'transient';
  VOLATILE : 'volatile';
  STRICTFP : 'strictfp';
  ENUM : 'enum';
  ANNOTATION : 'annotation';

  CLASS : 'class';
  INTERFACE : 'interface';

  VOID : 'void';
  BOOLEAN : 'boolean';
  BYTE : 'byte';
  SHORT : 'short';
  CHAR : 'char';
  INT : 'int';
  LONG : 'long';
  FLOAT : 'float';
  DOUBLE : 'double';
  NULL_TYPE : 'null_type';
  UNKNOWN : 'unknown';

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
  INTERFACEINVOKE : 'interfaceinvoke';
  LENGTHOF : 'lengthof';
  // possibility to read old Jimple
  SWITCH : 'lookupswitch' | 'tableswitch' | 'switch';
  NEG : 'neg';
  NEW : 'new';
  NEWARRAY : 'newarray';
  NEWMULTIARRAY : 'newmultiarray';
  NOP : 'nop';
  RET : 'ret';
  RETURN : 'return';
  SPECIALINVOKE : 'specialinvoke';
  STATICINVOKE : 'staticinvoke';
  DYNAMICINVOKE : 'dynamicinvoke';
  THROW : 'throw';
  THROWS : 'throws';
  VIRTUALINVOKE : 'virtualinvoke';
  NULL : 'null';
  FROM : 'from';
  TO : 'to';
  WITH : 'with';
  CLS : 'cls';

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


    /* FIXME: generify - this is java specific */
  FULL_IDENTIFIER :
      ((FIRST_ID_CHAR | ESCAPE_CHAR) (SIMPLE_ID_CHAR | ESCAPE_CHAR)* DOT)+  (FIRST_ID_CHAR | ESCAPE_CHAR) (SIMPLE_ID_CHAR | ESCAPE_CHAR)*;
  QUOTED_NAME : QUOTE QUOTABLE_CHAR+ QUOTE;
  IDENTIFIER :
      (FIRST_ID_CHAR | ESCAPE_CHAR) (SIMPLE_ID_CHAR | ESCAPE_CHAR)* | '<clinit>' | '<init>';

  AT_IDENTIFIER : '@' (('parameter' DEC_DIGIT+ ':') | 'this' ':' | 'caughtexception');

  BOOL_CONSTANT : 'true' | 'false';
  INTEGER_CONSTANT : (DEC_CONSTANT | HEX_CONSTANT | OCT_CONSTANT) 'L'?;
  FLOAT_CONSTANT : ((DEC_CONSTANT DOT DEC_CONSTANT) (('e'|'E') (PLUS|MINUS)? DEC_CONSTANT)? ('f'|'F')?)  | ('#' (('-'? 'Infinity') | 'NaN') ('f' | 'F')? ) ;
  STRING_CONSTANT : '"' STRING_CHAR* '"';



 /*
  * Parser Rules
  */

  file:
    importItem* modifier* file_type class_name extends_clause? implements_clause? L_BRACE member* R_BRACE;

  importItem: 'import' location=STRING_CONSTANT ';';

  modifier : 'abstract' | 'final' | 'native' | 'public' | 'protected' | 'private' | 'static' | 'synchronized' | 'transient' |'volatile' | 'strictfp' | 'enum' | 'annotation';

  file_type : 'class' | 'interface' | 'annotation';

  extends_clause : 'extends' class_name;

  implements_clause :    'implements' class_name_list;

  name_list :
    /*single*/ name |
    /*multi*/  name COMMA name_list;

  class_name_list :
    /*class_name_single*/ class_name |
    /*class_name_multi*/  class_name COMMA class_name_list;

  member:
        field | method;

  field:
        modifier* type name SEMICOLON;

  method:
     modifier* type name L_PAREN parameter_list? R_PAREN throws_clause? method_body;

  type :
    /*void*/   'void' |
    /*novoid*/ nonvoid_type;

  parameter_list :
    /*single*/ parameter |
    /*multi*/  parameter COMMA parameter_list;

  parameter :
    nonvoid_type;

  throws_clause :
    'throws' class_name_list;




  base_type_no_name :
    /*boolean*/ BOOLEAN |
    /*byte*/    BYTE |
    /*char*/    CHAR |
    /*short*/   SHORT |
    /*int*/     INT |
    /*long*/    LONG |
    /*float*/   FLOAT |
    /*double*/  DOUBLE |
    /*null*/    NULL_TYPE;


  base_type :
                    base_type_no_name |
    /*class_name*/    class_name;

  nonvoid_type :
    /*base*/   base_type_no_name array_brackets* |
    /*quoted*/ QUOTED_NAME array_brackets* |
    /*ident*/  IDENTIFIER array_brackets* |
    /*full_ident*/ FULL_IDENTIFIER array_brackets*;





  array_brackets :
    L_BRACKET R_BRACKET;

  method_body :
    /*empty*/ SEMICOLON |
    /*full*/  L_BRACE declaration* statement* catch_clause* R_BRACE;

  declaration :
    jimple_type local_name_list SEMICOLON;

  jimple_type :
    /*unknown*/ UNKNOWN |
    /*nonvoid*/ nonvoid_type;

  local_name :
    name;




  local_name_list :
    /*single*/ local_name |
    /*multi*/  local_name COMMA local_name_list;

  statement :
    /*label*/        label_name COLON |
    /*breakpoint*/   BREAKPOINT SEMICOLON |
    /*entermonitor*/ ENTERMONITOR immediate SEMICOLON |
    /*exitmonitor*/  EXITMONITOR immediate SEMICOLON |
    /*switch*/       SWITCH L_PAREN immediate R_PAREN L_BRACE case_stmt+ R_BRACE SEMICOLON |
                     assignments |
    /*if*/           IF bool_expr goto_stmt |
    /*goto*/         goto_stmt |
    /*nop*/          NOP SEMICOLON |
    /*ret*/          RET immediate? SEMICOLON |
    /*return*/       RETURN immediate? SEMICOLON |
    /*throw*/        THROW immediate SEMICOLON |
    /*invoke*/       invoke_expr SEMICOLON;

    assignments:
    /*identity*/     local_name COLON_EQUALS AT_IDENTIFIER type SEMICOLON |
    /*identity_no_type*/  local_name COLON_EQUALS AT_IDENTIFIER SEMICOLON |
    /*assign*/       variable EQUALS expression SEMICOLON;

  label_name :
    IDENTIFIER;

  case_stmt :
    case_label COLON goto_stmt;

  case_label :
    /*constant*/ CASE MINUS? INTEGER_CONSTANT |
    /*default*/  DEFAULT;

  goto_stmt :
    'goto' label_name SEMICOLON;

  catch_clause :
    'catch' class_name 'from' label_name 'to' label_name 'with' label_name SEMICOLON;

  expression :
    /*new*/         new_expr |
    /*cast*/        L_PAREN nonvoid_type R_PAREN immediate |
    /*instanceof*/  immediate INSTANCEOF nonvoid_type |
    /*invoke*/      invoke_expr |
    /*reference*/   reference |
    /*binop*/       binop_expr |
    /*unop*/        unop_expr |
    /*immediate*/   immediate;

  new_expr :
    /*simple*/ NEW base_type |
    /*array*/  NEWARRAY L_PAREN nonvoid_type R_PAREN fixed_array_descriptor |
    /*multi*/  NEWMULTIARRAY L_PAREN base_type R_PAREN array_descriptor+;

  array_descriptor :
    L_BRACKET immediate? R_BRACKET;

  variable :
    /*reference*/ reference |
    /*local*/     local_name;

  bool_expr :
    /*binop*/ binop_expr |
    /*unop*/  unop_expr;

  invoke_expr :
    /*nonstatic*/ nonstatic_invoke local_name DOT method_signature L_PAREN arg_list? R_PAREN |
    /*static*/    STATICINVOKE method_signature L_PAREN arg_list? R_PAREN |
    /*dynamic*/   DYNAMICINVOKE STRING_CONSTANT dynmethod=unnamed_method_signature firstl=L_PAREN dynargs=arg_list? firstr=R_PAREN
                                              bsm=method_signature L_PAREN staticargs=arg_list? R_PAREN;

  binop_expr :
    left=immediate op=binop right=immediate;

  unop_expr :
    unop immediate;

  nonstatic_invoke :
    /*special*/   SPECIALINVOKE |
    /*virtual*/   VIRTUALINVOKE |
    /*interface*/ INTERFACEINVOKE;

  unnamed_method_signature :
    CMPLT type L_PAREN parameter_list? R_PAREN CMPGT;

  method_signature :
    CMPLT class_name2=class_name first=COLON type method_name=name  L_PAREN parameter_list? R_PAREN CMPGT;

  reference :
    /*array*/ array_ref |
    /*field*/ field_ref;

  array_ref :
    /*ident*/ IDENTIFIER fixed_array_descriptor |
    /*quoted*/ QUOTED_NAME fixed_array_descriptor;

  field_ref :
    /*local*/ local_name DOT field_signature |
    /*sig*/   field_signature;

  field_signature :
    CMPLT class_signature=class_name first=COLON type field_name=name CMPGT;

  fixed_array_descriptor :
    L_BRACKET immediate R_BRACKET;

  arg_list :
    /*single*/ immediate |
    /*multi*/  immediate COMMA arg_list;

  immediate :
    /*local*/    local_name |
    /*constant*/ constant;

  constant :
    /*integer*/ MINUS? INTEGER_CONSTANT |
    /*float*/   MINUS? FLOAT_CONSTANT |
    /*string*/  STRING_CONSTANT |
    /*clazz*/   id=CLASS STRING_CONSTANT |
    /*null*/    NULL;

  binop :
    /*and*/   AND |
    /*or*/    OR |
    /*xor*/   XOR |
    /*mod*/   MOD |
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
    /*div*/   DIV;

  unop :
    /*lengthof*/ LENGTHOF |
    /*neg*/      NEG;

class_name :
    /*quoted*/ QUOTED_NAME |
    /*ident*/  IDENTIFIER |
    /*full_ident*/ FULL_IDENTIFIER;

name :
    /*quoted*/ QUOTED_NAME |
    /*ident*/  IDENTIFIER;