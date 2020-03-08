grammar Jimple;

/*
 * Lexer Rules
 */


//  ABSTRACT : 'abstract';
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
  // enable to read old Jimple
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

  fragment DEC_DIGIT : [0-9];
  fragment DEC_CONSTANT : DEC_DIGIT+;

  fragment HEX_DIGIT : DEC_DIGIT | [A-Fa-f];
  fragment HEX_CONSTANT : '0' ('x' | 'X') HEX_DIGIT+;

/*
  fragment ESCAPABLE_CHAR : '\\' | ' ' | '\'' | '.' | '"' | 'n' | 't' | 'r' | 'b' | 'f';
  fragment ESCAPE_CODE : 'u' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT;
  fragment ESCAPE_CHAR : '\\' (ESCAPABLE_CHAR | ESCAPE_CODE);
*/

  // escapes and any char except '\' (92) or '"' (34).
  STRING_CHAR : [^\u0034\u0092] ;


  AT_IDENTIFIER : '@' (('parameter' DEC_DIGIT+ ':') | 'this' ':' | 'caughtexception');

  BOOL_CONSTANT : 'true' | 'false';
  INTEGER_CONSTANT : (DEC_CONSTANT | HEX_CONSTANT ) 'L'?;
  FLOAT_CONSTANT : ((DEC_CONSTANT DOT DEC_CONSTANT) (('e'|'E') (PLUS|MINUS)? DEC_CONSTANT)? ('f'|'F')?)  | ('#' (('-'? 'Infinity') | 'NaN') ('f' | 'F')? ) ;
  STRING_CONSTANT : '"' STRING_CHAR* '"';

  IDENTIFIER: [A-Za-z$_][A-Za-z0-9$._]+;
  LINE_COMMENT : '//' ~('\n'|'\r')* ->skip;
  LONG_COMMENT : '/*' ~('*')* '*'+ ( ~('*' | '/')* ~('*')* '*'+)* '/' -> skip;

  BLANK : [ \t\r\n] ->skip;        // TODO: check problem w/Strings?


 /*
  * Parser Rules
  */

  file:
    importItem* modifier* file_type classname=name extends_clause? implements_clause? L_BRACE member* R_BRACE;

  importItem: 'import' location=name* SEMICOLON;

  modifier : 'abstract' | 'final' | 'native' | 'public' | 'protected' | 'private' | 'static' | 'synchronized' | 'transient' |'volatile' | 'strictfp' | 'enum' | 'annotation';

  file_type : 'class' | 'interface' | 'annotation';

  extends_clause : 'extends' classname=name;

  implements_clause : 'implements' name_list;

  name :
    /*ident*/ IDENTIFIER | '<init>' | '<clinit>';

  name_list :
    /*single*/ name |
    /*multi*/  name COMMA name_list;

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
    /*single*/ parameter=nonvoid_type |
    /*multi*/  parameter=nonvoid_type COMMA parameter_list;

  throws_clause :
    'throws' name_list;

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


  base_type: base_type_no_name | /*class_name*/    classname=name;

  nonvoid_type:
    /*base*/   base_type_no_name array_brackets* |
    /*ident*/  name array_brackets*;

  array_brackets:
    L_BRACKET R_BRACKET;

  method_body:
    /*empty*/ SEMICOLON |
    /*full*/  L_BRACE declaration* statement* catch_clause* R_BRACE;

  declaration:
    jimple_type name_list SEMICOLON;

  jimple_type:
    /*unknown*/ UNKNOWN |
    /*nonvoid*/ nonvoid_type;

    statement:
    /*label*/        label_name COLON stmt SEMICOLON |
                      stmt SEMICOLON;

  stmt :
    /*breakpoint*/   BREAKPOINT |
    /*entermonitor*/ ENTERMONITOR immediate  |
    /*exitmonitor*/  EXITMONITOR immediate  |
    /*switch*/       SWITCH L_PAREN immediate R_PAREN L_BRACE case_stmt+ R_BRACE  |
                     assignments |
    /*if*/           IF bool_expr goto_stmt |
    /*goto*/         goto_stmt |
    /*nop*/          NOP |
    /*ret*/          RET immediate? |
    /*return*/       RETURN immediate? |
    /*throw*/        THROW immediate |
    /*invoke*/       invoke_expr ;

    assignments:
    /*identity*/     local=name COLON_EQUALS AT_IDENTIFIER type  |
    /*identity_no_type*/ local=name COLON_EQUALS AT_IDENTIFIER  |
    /*assign*/       variable EQUALS expression ;

  label_name :
    name;

  case_stmt :
    case_label COLON goto_stmt SEMICOLON;

  case_label :
    /*constant*/ CASE MINUS? INTEGER_CONSTANT |
    /*default*/  DEFAULT;

  goto_stmt :
    'goto' label_name;

  catch_clause :
    'catch' classname=name 'from' label_name 'to' label_name 'with' label_name SEMICOLON;

  expression :
    /*new simple*/  NEW base_type |
    /*new array*/   NEWARRAY L_PAREN nonvoid_type R_PAREN fixed_array_descriptor |
    /*new multi*/   NEWMULTIARRAY L_PAREN base_type R_PAREN array_descriptor+ |
    /*cast*/        L_PAREN nonvoid_type R_PAREN immediate |
    /*instanceof*/  immediate INSTANCEOF nonvoid_type |
    /*invoke*/      invoke_expr |
    /*reference*/   reference |
    /*binop*/       binop_expr |
    /*unop*/        unop_expr |
    /*immediate*/   immediate;

  array_descriptor :
    L_BRACKET immediate? R_BRACKET;

  variable :
    /*reference*/ reference |
    /*local*/     local=name;

  bool_expr :
    /*binop*/ binop_expr |
    /*unop*/  unop_expr;

  invoke_expr :
    /*nonstatic*/ nonstatic_invoke name DOT method_signature L_PAREN arg_list? R_PAREN |
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
    CMPLT class_name=name first=COLON type method_name=name  L_PAREN parameter_list? R_PAREN CMPGT;

  reference :
    /*array*/ name fixed_array_descriptor |
    /*field*/
    /*local*/ name DOT field_signature |
    /*sig*/   field_signature;

  field_signature :
    CMPLT class_signature=name first=COLON type field_name=name CMPGT;

  fixed_array_descriptor :
    L_BRACKET immediate R_BRACKET;

  arg_list :
    /*single*/ immediate |
    /*multi*/  immediate COMMA arg_list;

  immediate :
    /*local*/    local=name |
    /*constant*/ constant;

  constant :
    /*integer*/ MINUS? INTEGER_CONSTANT |
    /*float*/   MINUS? FLOAT_CONSTANT |
    /*string*/  STRING_CONSTANT |
    /*clazz*/   CLASS STRING_CONSTANT |
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

