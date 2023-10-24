// Generated from /home/smarkus/workspace/Java/soot-reloaded/sootup.jimple.parser/src/main/antlr4/sootup/jimple/Jimple.g4 by ANTLR 4.10.1
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link JimpleParser}.
 */
public interface JimpleListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link JimpleParser#banana}.
	 * @param ctx the parse tree
	 */
	void enterBanana(JimpleParser.BananaContext ctx);
	/**
	 * Exit a parse tree produced by {@link JimpleParser#banana}.
	 * @param ctx the parse tree
	 */
	void exitBanana(JimpleParser.BananaContext ctx);
	/**
	 * Enter a parse tree produced by {@link JimpleParser#identifier}.
	 * @param ctx the parse tree
	 */
	void enterIdentifier(JimpleParser.IdentifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link JimpleParser#identifier}.
	 * @param ctx the parse tree
	 */
	void exitIdentifier(JimpleParser.IdentifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link JimpleParser#integer_constant}.
	 * @param ctx the parse tree
	 */
	void enterInteger_constant(JimpleParser.Integer_constantContext ctx);
	/**
	 * Exit a parse tree produced by {@link JimpleParser#integer_constant}.
	 * @param ctx the parse tree
	 */
	void exitInteger_constant(JimpleParser.Integer_constantContext ctx);
	/**
	 * Enter a parse tree produced by {@link JimpleParser#file}.
	 * @param ctx the parse tree
	 */
	void enterFile(JimpleParser.FileContext ctx);
	/**
	 * Exit a parse tree produced by {@link JimpleParser#file}.
	 * @param ctx the parse tree
	 */
	void exitFile(JimpleParser.FileContext ctx);
	/**
	 * Enter a parse tree produced by {@link JimpleParser#importItem}.
	 * @param ctx the parse tree
	 */
	void enterImportItem(JimpleParser.ImportItemContext ctx);
	/**
	 * Exit a parse tree produced by {@link JimpleParser#importItem}.
	 * @param ctx the parse tree
	 */
	void exitImportItem(JimpleParser.ImportItemContext ctx);
	/**
	 * Enter a parse tree produced by {@link JimpleParser#modifier}.
	 * @param ctx the parse tree
	 */
	void enterModifier(JimpleParser.ModifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link JimpleParser#modifier}.
	 * @param ctx the parse tree
	 */
	void exitModifier(JimpleParser.ModifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link JimpleParser#file_type}.
	 * @param ctx the parse tree
	 */
	void enterFile_type(JimpleParser.File_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link JimpleParser#file_type}.
	 * @param ctx the parse tree
	 */
	void exitFile_type(JimpleParser.File_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link JimpleParser#extends_clause}.
	 * @param ctx the parse tree
	 */
	void enterExtends_clause(JimpleParser.Extends_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link JimpleParser#extends_clause}.
	 * @param ctx the parse tree
	 */
	void exitExtends_clause(JimpleParser.Extends_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link JimpleParser#implements_clause}.
	 * @param ctx the parse tree
	 */
	void enterImplements_clause(JimpleParser.Implements_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link JimpleParser#implements_clause}.
	 * @param ctx the parse tree
	 */
	void exitImplements_clause(JimpleParser.Implements_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link JimpleParser#type}.
	 * @param ctx the parse tree
	 */
	void enterType(JimpleParser.TypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link JimpleParser#type}.
	 * @param ctx the parse tree
	 */
	void exitType(JimpleParser.TypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link JimpleParser#type_list}.
	 * @param ctx the parse tree
	 */
	void enterType_list(JimpleParser.Type_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link JimpleParser#type_list}.
	 * @param ctx the parse tree
	 */
	void exitType_list(JimpleParser.Type_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link JimpleParser#member}.
	 * @param ctx the parse tree
	 */
	void enterMember(JimpleParser.MemberContext ctx);
	/**
	 * Exit a parse tree produced by {@link JimpleParser#member}.
	 * @param ctx the parse tree
	 */
	void exitMember(JimpleParser.MemberContext ctx);
	/**
	 * Enter a parse tree produced by {@link JimpleParser#field}.
	 * @param ctx the parse tree
	 */
	void enterField(JimpleParser.FieldContext ctx);
	/**
	 * Exit a parse tree produced by {@link JimpleParser#field}.
	 * @param ctx the parse tree
	 */
	void exitField(JimpleParser.FieldContext ctx);
	/**
	 * Enter a parse tree produced by {@link JimpleParser#method}.
	 * @param ctx the parse tree
	 */
	void enterMethod(JimpleParser.MethodContext ctx);
	/**
	 * Exit a parse tree produced by {@link JimpleParser#method}.
	 * @param ctx the parse tree
	 */
	void exitMethod(JimpleParser.MethodContext ctx);
	/**
	 * Enter a parse tree produced by {@link JimpleParser#method_name}.
	 * @param ctx the parse tree
	 */
	void enterMethod_name(JimpleParser.Method_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link JimpleParser#method_name}.
	 * @param ctx the parse tree
	 */
	void exitMethod_name(JimpleParser.Method_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link JimpleParser#throws_clause}.
	 * @param ctx the parse tree
	 */
	void enterThrows_clause(JimpleParser.Throws_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link JimpleParser#throws_clause}.
	 * @param ctx the parse tree
	 */
	void exitThrows_clause(JimpleParser.Throws_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link JimpleParser#method_body}.
	 * @param ctx the parse tree
	 */
	void enterMethod_body(JimpleParser.Method_bodyContext ctx);
	/**
	 * Exit a parse tree produced by {@link JimpleParser#method_body}.
	 * @param ctx the parse tree
	 */
	void exitMethod_body(JimpleParser.Method_bodyContext ctx);
	/**
	 * Enter a parse tree produced by {@link JimpleParser#method_body_contents}.
	 * @param ctx the parse tree
	 */
	void enterMethod_body_contents(JimpleParser.Method_body_contentsContext ctx);
	/**
	 * Exit a parse tree produced by {@link JimpleParser#method_body_contents}.
	 * @param ctx the parse tree
	 */
	void exitMethod_body_contents(JimpleParser.Method_body_contentsContext ctx);
	/**
	 * Enter a parse tree produced by {@link JimpleParser#trap_clauses}.
	 * @param ctx the parse tree
	 */
	void enterTrap_clauses(JimpleParser.Trap_clausesContext ctx);
	/**
	 * Exit a parse tree produced by {@link JimpleParser#trap_clauses}.
	 * @param ctx the parse tree
	 */
	void exitTrap_clauses(JimpleParser.Trap_clausesContext ctx);
	/**
	 * Enter a parse tree produced by {@link JimpleParser#statements}.
	 * @param ctx the parse tree
	 */
	void enterStatements(JimpleParser.StatementsContext ctx);
	/**
	 * Exit a parse tree produced by {@link JimpleParser#statements}.
	 * @param ctx the parse tree
	 */
	void exitStatements(JimpleParser.StatementsContext ctx);
	/**
	 * Enter a parse tree produced by {@link JimpleParser#declarations}.
	 * @param ctx the parse tree
	 */
	void enterDeclarations(JimpleParser.DeclarationsContext ctx);
	/**
	 * Exit a parse tree produced by {@link JimpleParser#declarations}.
	 * @param ctx the parse tree
	 */
	void exitDeclarations(JimpleParser.DeclarationsContext ctx);
	/**
	 * Enter a parse tree produced by {@link JimpleParser#declaration}.
	 * @param ctx the parse tree
	 */
	void enterDeclaration(JimpleParser.DeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link JimpleParser#declaration}.
	 * @param ctx the parse tree
	 */
	void exitDeclaration(JimpleParser.DeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link JimpleParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterStatement(JimpleParser.StatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link JimpleParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitStatement(JimpleParser.StatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link JimpleParser#stmt}.
	 * @param ctx the parse tree
	 */
	void enterStmt(JimpleParser.StmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link JimpleParser#stmt}.
	 * @param ctx the parse tree
	 */
	void exitStmt(JimpleParser.StmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link JimpleParser#assignments}.
	 * @param ctx the parse tree
	 */
	void enterAssignments(JimpleParser.AssignmentsContext ctx);
	/**
	 * Exit a parse tree produced by {@link JimpleParser#assignments}.
	 * @param ctx the parse tree
	 */
	void exitAssignments(JimpleParser.AssignmentsContext ctx);
	/**
	 * Enter a parse tree produced by {@link JimpleParser#identity_ref}.
	 * @param ctx the parse tree
	 */
	void enterIdentity_ref(JimpleParser.Identity_refContext ctx);
	/**
	 * Exit a parse tree produced by {@link JimpleParser#identity_ref}.
	 * @param ctx the parse tree
	 */
	void exitIdentity_ref(JimpleParser.Identity_refContext ctx);
	/**
	 * Enter a parse tree produced by {@link JimpleParser#case_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCase_stmt(JimpleParser.Case_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link JimpleParser#case_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCase_stmt(JimpleParser.Case_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link JimpleParser#case_label}.
	 * @param ctx the parse tree
	 */
	void enterCase_label(JimpleParser.Case_labelContext ctx);
	/**
	 * Exit a parse tree produced by {@link JimpleParser#case_label}.
	 * @param ctx the parse tree
	 */
	void exitCase_label(JimpleParser.Case_labelContext ctx);
	/**
	 * Enter a parse tree produced by {@link JimpleParser#goto_stmt}.
	 * @param ctx the parse tree
	 */
	void enterGoto_stmt(JimpleParser.Goto_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link JimpleParser#goto_stmt}.
	 * @param ctx the parse tree
	 */
	void exitGoto_stmt(JimpleParser.Goto_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link JimpleParser#trap_clause}.
	 * @param ctx the parse tree
	 */
	void enterTrap_clause(JimpleParser.Trap_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link JimpleParser#trap_clause}.
	 * @param ctx the parse tree
	 */
	void exitTrap_clause(JimpleParser.Trap_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link JimpleParser#value}.
	 * @param ctx the parse tree
	 */
	void enterValue(JimpleParser.ValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link JimpleParser#value}.
	 * @param ctx the parse tree
	 */
	void exitValue(JimpleParser.ValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link JimpleParser#bool_expr}.
	 * @param ctx the parse tree
	 */
	void enterBool_expr(JimpleParser.Bool_exprContext ctx);
	/**
	 * Exit a parse tree produced by {@link JimpleParser#bool_expr}.
	 * @param ctx the parse tree
	 */
	void exitBool_expr(JimpleParser.Bool_exprContext ctx);
	/**
	 * Enter a parse tree produced by {@link JimpleParser#invoke_expr}.
	 * @param ctx the parse tree
	 */
	void enterInvoke_expr(JimpleParser.Invoke_exprContext ctx);
	/**
	 * Exit a parse tree produced by {@link JimpleParser#invoke_expr}.
	 * @param ctx the parse tree
	 */
	void exitInvoke_expr(JimpleParser.Invoke_exprContext ctx);
	/**
	 * Enter a parse tree produced by {@link JimpleParser#binop_expr}.
	 * @param ctx the parse tree
	 */
	void enterBinop_expr(JimpleParser.Binop_exprContext ctx);
	/**
	 * Exit a parse tree produced by {@link JimpleParser#binop_expr}.
	 * @param ctx the parse tree
	 */
	void exitBinop_expr(JimpleParser.Binop_exprContext ctx);
	/**
	 * Enter a parse tree produced by {@link JimpleParser#unop_expr}.
	 * @param ctx the parse tree
	 */
	void enterUnop_expr(JimpleParser.Unop_exprContext ctx);
	/**
	 * Exit a parse tree produced by {@link JimpleParser#unop_expr}.
	 * @param ctx the parse tree
	 */
	void exitUnop_expr(JimpleParser.Unop_exprContext ctx);
	/**
	 * Enter a parse tree produced by {@link JimpleParser#method_subsignature}.
	 * @param ctx the parse tree
	 */
	void enterMethod_subsignature(JimpleParser.Method_subsignatureContext ctx);
	/**
	 * Exit a parse tree produced by {@link JimpleParser#method_subsignature}.
	 * @param ctx the parse tree
	 */
	void exitMethod_subsignature(JimpleParser.Method_subsignatureContext ctx);
	/**
	 * Enter a parse tree produced by {@link JimpleParser#method_signature}.
	 * @param ctx the parse tree
	 */
	void enterMethod_signature(JimpleParser.Method_signatureContext ctx);
	/**
	 * Exit a parse tree produced by {@link JimpleParser#method_signature}.
	 * @param ctx the parse tree
	 */
	void exitMethod_signature(JimpleParser.Method_signatureContext ctx);
	/**
	 * Enter a parse tree produced by {@link JimpleParser#reference}.
	 * @param ctx the parse tree
	 */
	void enterReference(JimpleParser.ReferenceContext ctx);
	/**
	 * Exit a parse tree produced by {@link JimpleParser#reference}.
	 * @param ctx the parse tree
	 */
	void exitReference(JimpleParser.ReferenceContext ctx);
	/**
	 * Enter a parse tree produced by {@link JimpleParser#field_signature}.
	 * @param ctx the parse tree
	 */
	void enterField_signature(JimpleParser.Field_signatureContext ctx);
	/**
	 * Exit a parse tree produced by {@link JimpleParser#field_signature}.
	 * @param ctx the parse tree
	 */
	void exitField_signature(JimpleParser.Field_signatureContext ctx);
	/**
	 * Enter a parse tree produced by {@link JimpleParser#array_descriptor}.
	 * @param ctx the parse tree
	 */
	void enterArray_descriptor(JimpleParser.Array_descriptorContext ctx);
	/**
	 * Exit a parse tree produced by {@link JimpleParser#array_descriptor}.
	 * @param ctx the parse tree
	 */
	void exitArray_descriptor(JimpleParser.Array_descriptorContext ctx);
	/**
	 * Enter a parse tree produced by {@link JimpleParser#arg_list}.
	 * @param ctx the parse tree
	 */
	void enterArg_list(JimpleParser.Arg_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link JimpleParser#arg_list}.
	 * @param ctx the parse tree
	 */
	void exitArg_list(JimpleParser.Arg_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link JimpleParser#immediate}.
	 * @param ctx the parse tree
	 */
	void enterImmediate(JimpleParser.ImmediateContext ctx);
	/**
	 * Exit a parse tree produced by {@link JimpleParser#immediate}.
	 * @param ctx the parse tree
	 */
	void exitImmediate(JimpleParser.ImmediateContext ctx);
	/**
	 * Enter a parse tree produced by {@link JimpleParser#constant}.
	 * @param ctx the parse tree
	 */
	void enterConstant(JimpleParser.ConstantContext ctx);
	/**
	 * Exit a parse tree produced by {@link JimpleParser#constant}.
	 * @param ctx the parse tree
	 */
	void exitConstant(JimpleParser.ConstantContext ctx);
	/**
	 * Enter a parse tree produced by {@link JimpleParser#binop}.
	 * @param ctx the parse tree
	 */
	void enterBinop(JimpleParser.BinopContext ctx);
	/**
	 * Exit a parse tree produced by {@link JimpleParser#binop}.
	 * @param ctx the parse tree
	 */
	void exitBinop(JimpleParser.BinopContext ctx);
	/**
	 * Enter a parse tree produced by {@link JimpleParser#unop}.
	 * @param ctx the parse tree
	 */
	void enterUnop(JimpleParser.UnopContext ctx);
	/**
	 * Exit a parse tree produced by {@link JimpleParser#unop}.
	 * @param ctx the parse tree
	 */
	void exitUnop(JimpleParser.UnopContext ctx);
}