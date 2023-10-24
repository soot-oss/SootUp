// Generated from /home/smarkus/workspace/Java/soot-reloaded/sootup.jimple.parser/src/main/antlr4/sootup/jimple/Jimple.g4 by ANTLR 4.10.1
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link JimpleParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface JimpleVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link JimpleParser#banana}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBanana(JimpleParser.BananaContext ctx);
	/**
	 * Visit a parse tree produced by {@link JimpleParser#identifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdentifier(JimpleParser.IdentifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link JimpleParser#integer_constant}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInteger_constant(JimpleParser.Integer_constantContext ctx);
	/**
	 * Visit a parse tree produced by {@link JimpleParser#file}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFile(JimpleParser.FileContext ctx);
	/**
	 * Visit a parse tree produced by {@link JimpleParser#importItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitImportItem(JimpleParser.ImportItemContext ctx);
	/**
	 * Visit a parse tree produced by {@link JimpleParser#modifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitModifier(JimpleParser.ModifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link JimpleParser#file_type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFile_type(JimpleParser.File_typeContext ctx);
	/**
	 * Visit a parse tree produced by {@link JimpleParser#extends_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExtends_clause(JimpleParser.Extends_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link JimpleParser#implements_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitImplements_clause(JimpleParser.Implements_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link JimpleParser#type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitType(JimpleParser.TypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link JimpleParser#type_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitType_list(JimpleParser.Type_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link JimpleParser#member}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMember(JimpleParser.MemberContext ctx);
	/**
	 * Visit a parse tree produced by {@link JimpleParser#field}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitField(JimpleParser.FieldContext ctx);
	/**
	 * Visit a parse tree produced by {@link JimpleParser#method}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMethod(JimpleParser.MethodContext ctx);
	/**
	 * Visit a parse tree produced by {@link JimpleParser#method_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMethod_name(JimpleParser.Method_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link JimpleParser#throws_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitThrows_clause(JimpleParser.Throws_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link JimpleParser#method_body}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMethod_body(JimpleParser.Method_bodyContext ctx);
	/**
	 * Visit a parse tree produced by {@link JimpleParser#method_body_contents}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMethod_body_contents(JimpleParser.Method_body_contentsContext ctx);
	/**
	 * Visit a parse tree produced by {@link JimpleParser#trap_clauses}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTrap_clauses(JimpleParser.Trap_clausesContext ctx);
	/**
	 * Visit a parse tree produced by {@link JimpleParser#statements}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatements(JimpleParser.StatementsContext ctx);
	/**
	 * Visit a parse tree produced by {@link JimpleParser#declarations}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDeclarations(JimpleParser.DeclarationsContext ctx);
	/**
	 * Visit a parse tree produced by {@link JimpleParser#declaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDeclaration(JimpleParser.DeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link JimpleParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatement(JimpleParser.StatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link JimpleParser#stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStmt(JimpleParser.StmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link JimpleParser#assignments}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignments(JimpleParser.AssignmentsContext ctx);
	/**
	 * Visit a parse tree produced by {@link JimpleParser#identity_ref}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdentity_ref(JimpleParser.Identity_refContext ctx);
	/**
	 * Visit a parse tree produced by {@link JimpleParser#case_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCase_stmt(JimpleParser.Case_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link JimpleParser#case_label}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCase_label(JimpleParser.Case_labelContext ctx);
	/**
	 * Visit a parse tree produced by {@link JimpleParser#goto_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGoto_stmt(JimpleParser.Goto_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link JimpleParser#trap_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTrap_clause(JimpleParser.Trap_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link JimpleParser#value}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValue(JimpleParser.ValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link JimpleParser#bool_expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBool_expr(JimpleParser.Bool_exprContext ctx);
	/**
	 * Visit a parse tree produced by {@link JimpleParser#invoke_expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInvoke_expr(JimpleParser.Invoke_exprContext ctx);
	/**
	 * Visit a parse tree produced by {@link JimpleParser#binop_expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBinop_expr(JimpleParser.Binop_exprContext ctx);
	/**
	 * Visit a parse tree produced by {@link JimpleParser#unop_expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnop_expr(JimpleParser.Unop_exprContext ctx);
	/**
	 * Visit a parse tree produced by {@link JimpleParser#method_subsignature}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMethod_subsignature(JimpleParser.Method_subsignatureContext ctx);
	/**
	 * Visit a parse tree produced by {@link JimpleParser#method_signature}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMethod_signature(JimpleParser.Method_signatureContext ctx);
	/**
	 * Visit a parse tree produced by {@link JimpleParser#reference}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReference(JimpleParser.ReferenceContext ctx);
	/**
	 * Visit a parse tree produced by {@link JimpleParser#field_signature}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitField_signature(JimpleParser.Field_signatureContext ctx);
	/**
	 * Visit a parse tree produced by {@link JimpleParser#array_descriptor}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArray_descriptor(JimpleParser.Array_descriptorContext ctx);
	/**
	 * Visit a parse tree produced by {@link JimpleParser#arg_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArg_list(JimpleParser.Arg_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link JimpleParser#immediate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitImmediate(JimpleParser.ImmediateContext ctx);
	/**
	 * Visit a parse tree produced by {@link JimpleParser#constant}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstant(JimpleParser.ConstantContext ctx);
	/**
	 * Visit a parse tree produced by {@link JimpleParser#binop}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBinop(JimpleParser.BinopContext ctx);
	/**
	 * Visit a parse tree produced by {@link JimpleParser#unop}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnop(JimpleParser.UnopContext ctx);
}