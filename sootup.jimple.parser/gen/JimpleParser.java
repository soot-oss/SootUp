// Generated from /home/smarkus/workspace/Java/soot-reloaded/sootup.jimple.parser/src/main/antlr4/sootup/jimple/Jimple.g4 by ANTLR 4.10.1
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class JimpleParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.10.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		T__9=10, T__10=11, T__11=12, T__12=13, T__13=14, T__14=15, T__15=16, T__16=17, 
		T__17=18, T__18=19, T__19=20, T__20=21, T__21=22, T__22=23, T__23=24, 
		T__24=25, LINE_COMMENT=26, LONG_COMMENT=27, STRING_CONSTANT=28, CLASS=29, 
		EXTENDS=30, IMPLEMENTS=31, BREAKPOINT=32, CASE=33, CATCH=34, CMP=35, CMPG=36, 
		CMPL=37, DEFAULT=38, ENTERMONITOR=39, EXITMONITOR=40, GOTO=41, IF=42, 
		INSTANCEOF=43, LENGTHOF=44, SWITCH=45, NEG=46, NEWARRAY=47, NEWMULTIARRAY=48, 
		NEW=49, NOP=50, RETURN=51, RET=52, NONSTATIC_INVOKE=53, STATICINVOKE=54, 
		DYNAMICINVOKE=55, THROWS=56, THROW=57, NULL=58, FROM=59, TO=60, WITH=61, 
		COMMA=62, L_BRACE=63, R_BRACE=64, SEMICOLON=65, L_BRACKET=66, R_BRACKET=67, 
		L_PAREN=68, R_PAREN=69, COLON=70, DOT=71, EQUALS=72, COLON_EQUALS=73, 
		AND=74, OR=75, XOR=76, MOD=77, CMPEQ=78, CMPNE=79, CMPGT=80, CMPGE=81, 
		CMPLT=82, CMPLE=83, SHL=84, SHR=85, USHR=86, PLUS=87, MINUS=88, MULT=89, 
		DIV=90, BOOL_CONSTANT=91, FLOAT_CONSTANT=92, DEC_CONSTANT=93, HEX_CONSTANT=94, 
		IDENTIFIER=95, BLANK=96;
	public static final int
		RULE_banana = 0, RULE_identifier = 1, RULE_integer_constant = 2, RULE_file = 3, 
		RULE_importItem = 4, RULE_modifier = 5, RULE_file_type = 6, RULE_extends_clause = 7, 
		RULE_implements_clause = 8, RULE_type = 9, RULE_type_list = 10, RULE_member = 11, 
		RULE_field = 12, RULE_method = 13, RULE_method_name = 14, RULE_throws_clause = 15, 
		RULE_method_body = 16, RULE_method_body_contents = 17, RULE_trap_clauses = 18, 
		RULE_statements = 19, RULE_declarations = 20, RULE_declaration = 21, RULE_statement = 22, 
		RULE_stmt = 23, RULE_assignments = 24, RULE_identity_ref = 25, RULE_case_stmt = 26, 
		RULE_case_label = 27, RULE_goto_stmt = 28, RULE_trap_clause = 29, RULE_value = 30, 
		RULE_bool_expr = 31, RULE_invoke_expr = 32, RULE_binop_expr = 33, RULE_unop_expr = 34, 
		RULE_method_subsignature = 35, RULE_method_signature = 36, RULE_reference = 37, 
		RULE_field_signature = 38, RULE_array_descriptor = 39, RULE_arg_list = 40, 
		RULE_immediate = 41, RULE_constant = 42, RULE_binop = 43, RULE_unop = 44;
	private static String[] makeRuleNames() {
		return new String[] {
			"banana", "identifier", "integer_constant", "file", "importItem", "modifier", 
			"file_type", "extends_clause", "implements_clause", "type", "type_list", 
			"member", "field", "method", "method_name", "throws_clause", "method_body", 
			"method_body_contents", "trap_clauses", "statements", "declarations", 
			"declaration", "statement", "stmt", "assignments", "identity_ref", "case_stmt", 
			"case_label", "goto_stmt", "trap_clause", "value", "bool_expr", "invoke_expr", 
			"binop_expr", "unop_expr", "method_subsignature", "method_signature", 
			"reference", "field_signature", "array_descriptor", "arg_list", "immediate", 
			"constant", "binop", "unop"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'override'", "'overrides'", "'L'", "'import'", "'abstract'", "'final'", 
			"'native'", "'public'", "'protected'", "'private'", "'static'", "'synchronized'", 
			"'transient'", "'volatile'", "'strictfp'", "'enum'", "'interface'", "'annotation interface'", 
			"'<init>'", "'<clinit>'", "'@parameter'", "'@this:'", "'@caughtexception'", 
			"'handle:'", "'methodtype:'", null, null, null, "'class'", "'extends'", 
			"'implements'", "'breakpoint'", "'case'", "'catch'", "'cmp'", "'cmpg'", 
			"'cmpl'", "'default'", "'entermonitor'", "'exitmonitor'", "'goto'", "'if'", 
			"'instanceof'", "'lengthof'", null, "'neg'", "'newarray'", "'newmultiarray'", 
			"'new'", "'nop'", "'return'", "'ret'", null, "'staticinvoke'", "'dynamicinvoke'", 
			"'throws'", "'throw'", "'null'", "'from'", "'to'", "'with'", "','", "'{'", 
			"'}'", "';'", "'['", "']'", "'('", "')'", "':'", "'.'", "'='", "':='", 
			"'&'", "'|'", "'^'", "'%'", "'=='", "'!='", "'>'", "'>='", "'<'", "'<='", 
			"'<<'", "'>>'", "'>>>'", "'+'", "'-'", "'*'", "'/'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, "LINE_COMMENT", "LONG_COMMENT", "STRING_CONSTANT", "CLASS", 
			"EXTENDS", "IMPLEMENTS", "BREAKPOINT", "CASE", "CATCH", "CMP", "CMPG", 
			"CMPL", "DEFAULT", "ENTERMONITOR", "EXITMONITOR", "GOTO", "IF", "INSTANCEOF", 
			"LENGTHOF", "SWITCH", "NEG", "NEWARRAY", "NEWMULTIARRAY", "NEW", "NOP", 
			"RETURN", "RET", "NONSTATIC_INVOKE", "STATICINVOKE", "DYNAMICINVOKE", 
			"THROWS", "THROW", "NULL", "FROM", "TO", "WITH", "COMMA", "L_BRACE", 
			"R_BRACE", "SEMICOLON", "L_BRACKET", "R_BRACKET", "L_PAREN", "R_PAREN", 
			"COLON", "DOT", "EQUALS", "COLON_EQUALS", "AND", "OR", "XOR", "MOD", 
			"CMPEQ", "CMPNE", "CMPGT", "CMPGE", "CMPLT", "CMPLE", "SHL", "SHR", "USHR", 
			"PLUS", "MINUS", "MULT", "DIV", "BOOL_CONSTANT", "FLOAT_CONSTANT", "DEC_CONSTANT", 
			"HEX_CONSTANT", "IDENTIFIER", "BLANK"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "Jimple.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public JimpleParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	public static class BananaContext extends ParserRuleContext {
		public BananaContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_banana; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).enterBanana(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).exitBanana(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JimpleVisitor ) return ((JimpleVisitor<? extends T>)visitor).visitBanana(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BananaContext banana() throws RecognitionException {
		BananaContext _localctx = new BananaContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_banana);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(90);
			_la = _input.LA(1);
			if ( !(_la==T__0 || _la==T__1) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class IdentifierContext extends ParserRuleContext {
		public TerminalNode STRING_CONSTANT() { return getToken(JimpleParser.STRING_CONSTANT, 0); }
		public TerminalNode IDENTIFIER() { return getToken(JimpleParser.IDENTIFIER, 0); }
		public IdentifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_identifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).enterIdentifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).exitIdentifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JimpleVisitor ) return ((JimpleVisitor<? extends T>)visitor).visitIdentifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IdentifierContext identifier() throws RecognitionException {
		IdentifierContext _localctx = new IdentifierContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_identifier);
		try {
			setState(97);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,0,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(92);
				match(STRING_CONSTANT);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(93);
				match(STRING_CONSTANT);
				setState(94);
				matchWildcard();
				setState(95);
				match(IDENTIFIER);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(96);
				match(IDENTIFIER);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Integer_constantContext extends ParserRuleContext {
		public TerminalNode DEC_CONSTANT() { return getToken(JimpleParser.DEC_CONSTANT, 0); }
		public TerminalNode HEX_CONSTANT() { return getToken(JimpleParser.HEX_CONSTANT, 0); }
		public TerminalNode PLUS() { return getToken(JimpleParser.PLUS, 0); }
		public TerminalNode MINUS() { return getToken(JimpleParser.MINUS, 0); }
		public Integer_constantContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_integer_constant; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).enterInteger_constant(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).exitInteger_constant(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JimpleVisitor ) return ((JimpleVisitor<? extends T>)visitor).visitInteger_constant(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Integer_constantContext integer_constant() throws RecognitionException {
		Integer_constantContext _localctx = new Integer_constantContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_integer_constant);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(100);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==PLUS || _la==MINUS) {
				{
				setState(99);
				_la = _input.LA(1);
				if ( !(_la==PLUS || _la==MINUS) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
			}

			setState(102);
			_la = _input.LA(1);
			if ( !(_la==DEC_CONSTANT || _la==HEX_CONSTANT) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(104);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__2) {
				{
				setState(103);
				match(T__2);
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FileContext extends ParserRuleContext {
		public IdentifierContext classname;
		public File_typeContext file_type() {
			return getRuleContext(File_typeContext.class,0);
		}
		public TerminalNode L_BRACE() { return getToken(JimpleParser.L_BRACE, 0); }
		public TerminalNode R_BRACE() { return getToken(JimpleParser.R_BRACE, 0); }
		public TerminalNode EOF() { return getToken(JimpleParser.EOF, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public List<ImportItemContext> importItem() {
			return getRuleContexts(ImportItemContext.class);
		}
		public ImportItemContext importItem(int i) {
			return getRuleContext(ImportItemContext.class,i);
		}
		public List<ModifierContext> modifier() {
			return getRuleContexts(ModifierContext.class);
		}
		public ModifierContext modifier(int i) {
			return getRuleContext(ModifierContext.class,i);
		}
		public Extends_clauseContext extends_clause() {
			return getRuleContext(Extends_clauseContext.class,0);
		}
		public Implements_clauseContext implements_clause() {
			return getRuleContext(Implements_clauseContext.class,0);
		}
		public List<MemberContext> member() {
			return getRuleContexts(MemberContext.class);
		}
		public MemberContext member(int i) {
			return getRuleContext(MemberContext.class,i);
		}
		public FileContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_file; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).enterFile(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).exitFile(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JimpleVisitor ) return ((JimpleVisitor<? extends T>)visitor).visitFile(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FileContext file() throws RecognitionException {
		FileContext _localctx = new FileContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_file);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(109);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__3) {
				{
				{
				setState(106);
				importItem();
				}
				}
				setState(111);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(115);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__4) | (1L << T__5) | (1L << T__6) | (1L << T__7) | (1L << T__8) | (1L << T__9) | (1L << T__10) | (1L << T__11) | (1L << T__12) | (1L << T__13) | (1L << T__14) | (1L << T__15))) != 0)) {
				{
				{
				setState(112);
				modifier();
				}
				}
				setState(117);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(118);
			file_type();
			setState(119);
			((FileContext)_localctx).classname = identifier();
			setState(121);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==EXTENDS) {
				{
				setState(120);
				extends_clause();
				}
			}

			setState(124);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==IMPLEMENTS) {
				{
				setState(123);
				implements_clause();
				}
			}

			setState(126);
			match(L_BRACE);
			setState(130);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__4) | (1L << T__5) | (1L << T__6) | (1L << T__7) | (1L << T__8) | (1L << T__9) | (1L << T__10) | (1L << T__11) | (1L << T__12) | (1L << T__13) | (1L << T__14) | (1L << T__15) | (1L << STRING_CONSTANT))) != 0) || _la==IDENTIFIER) {
				{
				{
				setState(127);
				member();
				}
				}
				setState(132);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(133);
			match(R_BRACE);
			setState(134);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ImportItemContext extends ParserRuleContext {
		public IdentifierContext location;
		public TerminalNode SEMICOLON() { return getToken(JimpleParser.SEMICOLON, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public ImportItemContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_importItem; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).enterImportItem(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).exitImportItem(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JimpleVisitor ) return ((JimpleVisitor<? extends T>)visitor).visitImportItem(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ImportItemContext importItem() throws RecognitionException {
		ImportItemContext _localctx = new ImportItemContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_importItem);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(136);
			match(T__3);
			setState(137);
			((ImportItemContext)_localctx).location = identifier();
			setState(138);
			match(SEMICOLON);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ModifierContext extends ParserRuleContext {
		public ModifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_modifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).enterModifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).exitModifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JimpleVisitor ) return ((JimpleVisitor<? extends T>)visitor).visitModifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ModifierContext modifier() throws RecognitionException {
		ModifierContext _localctx = new ModifierContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_modifier);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(140);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__4) | (1L << T__5) | (1L << T__6) | (1L << T__7) | (1L << T__8) | (1L << T__9) | (1L << T__10) | (1L << T__11) | (1L << T__12) | (1L << T__13) | (1L << T__14) | (1L << T__15))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class File_typeContext extends ParserRuleContext {
		public TerminalNode CLASS() { return getToken(JimpleParser.CLASS, 0); }
		public File_typeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_file_type; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).enterFile_type(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).exitFile_type(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JimpleVisitor ) return ((JimpleVisitor<? extends T>)visitor).visitFile_type(this);
			else return visitor.visitChildren(this);
		}
	}

	public final File_typeContext file_type() throws RecognitionException {
		File_typeContext _localctx = new File_typeContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_file_type);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(142);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__16) | (1L << T__17) | (1L << CLASS))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Extends_clauseContext extends ParserRuleContext {
		public IdentifierContext classname;
		public TerminalNode EXTENDS() { return getToken(JimpleParser.EXTENDS, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public Extends_clauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_extends_clause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).enterExtends_clause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).exitExtends_clause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JimpleVisitor ) return ((JimpleVisitor<? extends T>)visitor).visitExtends_clause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Extends_clauseContext extends_clause() throws RecognitionException {
		Extends_clauseContext _localctx = new Extends_clauseContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_extends_clause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(144);
			match(EXTENDS);
			setState(145);
			((Extends_clauseContext)_localctx).classname = identifier();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Implements_clauseContext extends ParserRuleContext {
		public TerminalNode IMPLEMENTS() { return getToken(JimpleParser.IMPLEMENTS, 0); }
		public Type_listContext type_list() {
			return getRuleContext(Type_listContext.class,0);
		}
		public Implements_clauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_implements_clause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).enterImplements_clause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).exitImplements_clause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JimpleVisitor ) return ((JimpleVisitor<? extends T>)visitor).visitImplements_clause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Implements_clauseContext implements_clause() throws RecognitionException {
		Implements_clauseContext _localctx = new Implements_clauseContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_implements_clause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(147);
			match(IMPLEMENTS);
			setState(148);
			type_list();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TypeContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public List<TerminalNode> L_BRACKET() { return getTokens(JimpleParser.L_BRACKET); }
		public TerminalNode L_BRACKET(int i) {
			return getToken(JimpleParser.L_BRACKET, i);
		}
		public List<TerminalNode> R_BRACKET() { return getTokens(JimpleParser.R_BRACKET); }
		public TerminalNode R_BRACKET(int i) {
			return getToken(JimpleParser.R_BRACKET, i);
		}
		public TypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_type; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).enterType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).exitType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JimpleVisitor ) return ((JimpleVisitor<? extends T>)visitor).visitType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypeContext type() throws RecognitionException {
		TypeContext _localctx = new TypeContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_type);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(150);
			identifier();
			setState(155);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==L_BRACKET) {
				{
				{
				setState(151);
				match(L_BRACKET);
				setState(152);
				match(R_BRACKET);
				}
				}
				setState(157);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Type_listContext extends ParserRuleContext {
		public List<TypeContext> type() {
			return getRuleContexts(TypeContext.class);
		}
		public TypeContext type(int i) {
			return getRuleContext(TypeContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(JimpleParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(JimpleParser.COMMA, i);
		}
		public Type_listContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_type_list; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).enterType_list(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).exitType_list(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JimpleVisitor ) return ((JimpleVisitor<? extends T>)visitor).visitType_list(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Type_listContext type_list() throws RecognitionException {
		Type_listContext _localctx = new Type_listContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_type_list);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(158);
			type();
			setState(163);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(159);
				match(COMMA);
				setState(160);
				type();
				}
				}
				setState(165);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class MemberContext extends ParserRuleContext {
		public FieldContext field() {
			return getRuleContext(FieldContext.class,0);
		}
		public MethodContext method() {
			return getRuleContext(MethodContext.class,0);
		}
		public MemberContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_member; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).enterMember(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).exitMember(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JimpleVisitor ) return ((JimpleVisitor<? extends T>)visitor).visitMember(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MemberContext member() throws RecognitionException {
		MemberContext _localctx = new MemberContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_member);
		try {
			setState(168);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,10,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(166);
				field();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(167);
				method();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FieldContext extends ParserRuleContext {
		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode SEMICOLON() { return getToken(JimpleParser.SEMICOLON, 0); }
		public List<ModifierContext> modifier() {
			return getRuleContexts(ModifierContext.class);
		}
		public ModifierContext modifier(int i) {
			return getRuleContext(ModifierContext.class,i);
		}
		public FieldContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_field; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).enterField(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).exitField(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JimpleVisitor ) return ((JimpleVisitor<? extends T>)visitor).visitField(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FieldContext field() throws RecognitionException {
		FieldContext _localctx = new FieldContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_field);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(173);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__4) | (1L << T__5) | (1L << T__6) | (1L << T__7) | (1L << T__8) | (1L << T__9) | (1L << T__10) | (1L << T__11) | (1L << T__12) | (1L << T__13) | (1L << T__14) | (1L << T__15))) != 0)) {
				{
				{
				setState(170);
				modifier();
				}
				}
				setState(175);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(176);
			type();
			setState(177);
			identifier();
			setState(178);
			match(SEMICOLON);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class MethodContext extends ParserRuleContext {
		public Method_subsignatureContext method_subsignature() {
			return getRuleContext(Method_subsignatureContext.class,0);
		}
		public Method_bodyContext method_body() {
			return getRuleContext(Method_bodyContext.class,0);
		}
		public List<ModifierContext> modifier() {
			return getRuleContexts(ModifierContext.class);
		}
		public ModifierContext modifier(int i) {
			return getRuleContext(ModifierContext.class,i);
		}
		public Throws_clauseContext throws_clause() {
			return getRuleContext(Throws_clauseContext.class,0);
		}
		public MethodContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_method; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).enterMethod(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).exitMethod(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JimpleVisitor ) return ((JimpleVisitor<? extends T>)visitor).visitMethod(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MethodContext method() throws RecognitionException {
		MethodContext _localctx = new MethodContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_method);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(183);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__4) | (1L << T__5) | (1L << T__6) | (1L << T__7) | (1L << T__8) | (1L << T__9) | (1L << T__10) | (1L << T__11) | (1L << T__12) | (1L << T__13) | (1L << T__14) | (1L << T__15))) != 0)) {
				{
				{
				setState(180);
				modifier();
				}
				}
				setState(185);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(186);
			method_subsignature();
			setState(188);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==THROWS) {
				{
				setState(187);
				throws_clause();
				}
			}

			setState(190);
			method_body();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Method_nameContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public Method_nameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_method_name; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).enterMethod_name(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).exitMethod_name(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JimpleVisitor ) return ((JimpleVisitor<? extends T>)visitor).visitMethod_name(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Method_nameContext method_name() throws RecognitionException {
		Method_nameContext _localctx = new Method_nameContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_method_name);
		try {
			setState(195);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__18:
				enterOuterAlt(_localctx, 1);
				{
				setState(192);
				match(T__18);
				}
				break;
			case T__19:
				enterOuterAlt(_localctx, 2);
				{
				setState(193);
				match(T__19);
				}
				break;
			case STRING_CONSTANT:
			case IDENTIFIER:
				enterOuterAlt(_localctx, 3);
				{
				setState(194);
				identifier();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Throws_clauseContext extends ParserRuleContext {
		public TerminalNode THROWS() { return getToken(JimpleParser.THROWS, 0); }
		public Type_listContext type_list() {
			return getRuleContext(Type_listContext.class,0);
		}
		public Throws_clauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_throws_clause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).enterThrows_clause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).exitThrows_clause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JimpleVisitor ) return ((JimpleVisitor<? extends T>)visitor).visitThrows_clause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Throws_clauseContext throws_clause() throws RecognitionException {
		Throws_clauseContext _localctx = new Throws_clauseContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_throws_clause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(197);
			match(THROWS);
			setState(198);
			type_list();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Method_bodyContext extends ParserRuleContext {
		public TerminalNode SEMICOLON() { return getToken(JimpleParser.SEMICOLON, 0); }
		public TerminalNode L_BRACE() { return getToken(JimpleParser.L_BRACE, 0); }
		public Method_body_contentsContext method_body_contents() {
			return getRuleContext(Method_body_contentsContext.class,0);
		}
		public TerminalNode R_BRACE() { return getToken(JimpleParser.R_BRACE, 0); }
		public Method_bodyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_method_body; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).enterMethod_body(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).exitMethod_body(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JimpleVisitor ) return ((JimpleVisitor<? extends T>)visitor).visitMethod_body(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Method_bodyContext method_body() throws RecognitionException {
		Method_bodyContext _localctx = new Method_bodyContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_method_body);
		try {
			setState(205);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case SEMICOLON:
				enterOuterAlt(_localctx, 1);
				{
				setState(200);
				match(SEMICOLON);
				}
				break;
			case L_BRACE:
				enterOuterAlt(_localctx, 2);
				{
				setState(201);
				match(L_BRACE);
				setState(202);
				method_body_contents();
				setState(203);
				match(R_BRACE);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Method_body_contentsContext extends ParserRuleContext {
		public DeclarationsContext declarations() {
			return getRuleContext(DeclarationsContext.class,0);
		}
		public StatementsContext statements() {
			return getRuleContext(StatementsContext.class,0);
		}
		public Trap_clausesContext trap_clauses() {
			return getRuleContext(Trap_clausesContext.class,0);
		}
		public Method_body_contentsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_method_body_contents; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).enterMethod_body_contents(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).exitMethod_body_contents(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JimpleVisitor ) return ((JimpleVisitor<? extends T>)visitor).visitMethod_body_contents(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Method_body_contentsContext method_body_contents() throws RecognitionException {
		Method_body_contentsContext _localctx = new Method_body_contentsContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_method_body_contents);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(207);
			declarations();
			setState(208);
			statements();
			setState(209);
			trap_clauses();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Trap_clausesContext extends ParserRuleContext {
		public List<Trap_clauseContext> trap_clause() {
			return getRuleContexts(Trap_clauseContext.class);
		}
		public Trap_clauseContext trap_clause(int i) {
			return getRuleContext(Trap_clauseContext.class,i);
		}
		public Trap_clausesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_trap_clauses; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).enterTrap_clauses(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).exitTrap_clauses(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JimpleVisitor ) return ((JimpleVisitor<? extends T>)visitor).visitTrap_clauses(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Trap_clausesContext trap_clauses() throws RecognitionException {
		Trap_clausesContext _localctx = new Trap_clausesContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_trap_clauses);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(214);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==CATCH) {
				{
				{
				setState(211);
				trap_clause();
				}
				}
				setState(216);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class StatementsContext extends ParserRuleContext {
		public List<StatementContext> statement() {
			return getRuleContexts(StatementContext.class);
		}
		public StatementContext statement(int i) {
			return getRuleContext(StatementContext.class,i);
		}
		public StatementsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_statements; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).enterStatements(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).exitStatements(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JimpleVisitor ) return ((JimpleVisitor<? extends T>)visitor).visitStatements(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StatementsContext statements() throws RecognitionException {
		StatementsContext _localctx = new StatementsContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_statements);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(220);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << STRING_CONSTANT) | (1L << BREAKPOINT) | (1L << ENTERMONITOR) | (1L << EXITMONITOR) | (1L << GOTO) | (1L << IF) | (1L << SWITCH) | (1L << NOP) | (1L << RETURN) | (1L << RET) | (1L << NONSTATIC_INVOKE) | (1L << STATICINVOKE) | (1L << DYNAMICINVOKE) | (1L << THROW))) != 0) || _la==CMPLT || _la==IDENTIFIER) {
				{
				{
				setState(217);
				statement();
				}
				}
				setState(222);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DeclarationsContext extends ParserRuleContext {
		public List<DeclarationContext> declaration() {
			return getRuleContexts(DeclarationContext.class);
		}
		public DeclarationContext declaration(int i) {
			return getRuleContext(DeclarationContext.class,i);
		}
		public DeclarationsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_declarations; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).enterDeclarations(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).exitDeclarations(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JimpleVisitor ) return ((JimpleVisitor<? extends T>)visitor).visitDeclarations(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DeclarationsContext declarations() throws RecognitionException {
		DeclarationsContext _localctx = new DeclarationsContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_declarations);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(226);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,18,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(223);
					declaration();
					}
					} 
				}
				setState(228);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,18,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DeclarationContext extends ParserRuleContext {
		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}
		public Arg_listContext arg_list() {
			return getRuleContext(Arg_listContext.class,0);
		}
		public TerminalNode SEMICOLON() { return getToken(JimpleParser.SEMICOLON, 0); }
		public DeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_declaration; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).enterDeclaration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).exitDeclaration(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JimpleVisitor ) return ((JimpleVisitor<? extends T>)visitor).visitDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DeclarationContext declaration() throws RecognitionException {
		DeclarationContext _localctx = new DeclarationContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_declaration);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(229);
			type();
			setState(230);
			arg_list();
			setState(231);
			match(SEMICOLON);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class StatementContext extends ParserRuleContext {
		public IdentifierContext label_name;
		public StmtContext stmt() {
			return getRuleContext(StmtContext.class,0);
		}
		public TerminalNode SEMICOLON() { return getToken(JimpleParser.SEMICOLON, 0); }
		public TerminalNode COLON() { return getToken(JimpleParser.COLON, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public StatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).enterStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).exitStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JimpleVisitor ) return ((JimpleVisitor<? extends T>)visitor).visitStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StatementContext statement() throws RecognitionException {
		StatementContext _localctx = new StatementContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_statement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(236);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,19,_ctx) ) {
			case 1:
				{
				setState(233);
				((StatementContext)_localctx).label_name = identifier();
				setState(234);
				match(COLON);
				}
				break;
			}
			setState(238);
			stmt();
			setState(239);
			match(SEMICOLON);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class StmtContext extends ParserRuleContext {
		public AssignmentsContext assignments() {
			return getRuleContext(AssignmentsContext.class,0);
		}
		public Goto_stmtContext goto_stmt() {
			return getRuleContext(Goto_stmtContext.class,0);
		}
		public TerminalNode IF() { return getToken(JimpleParser.IF, 0); }
		public Bool_exprContext bool_expr() {
			return getRuleContext(Bool_exprContext.class,0);
		}
		public Invoke_exprContext invoke_expr() {
			return getRuleContext(Invoke_exprContext.class,0);
		}
		public TerminalNode RETURN() { return getToken(JimpleParser.RETURN, 0); }
		public ImmediateContext immediate() {
			return getRuleContext(ImmediateContext.class,0);
		}
		public TerminalNode SWITCH() { return getToken(JimpleParser.SWITCH, 0); }
		public TerminalNode L_PAREN() { return getToken(JimpleParser.L_PAREN, 0); }
		public TerminalNode R_PAREN() { return getToken(JimpleParser.R_PAREN, 0); }
		public TerminalNode L_BRACE() { return getToken(JimpleParser.L_BRACE, 0); }
		public TerminalNode R_BRACE() { return getToken(JimpleParser.R_BRACE, 0); }
		public List<Case_stmtContext> case_stmt() {
			return getRuleContexts(Case_stmtContext.class);
		}
		public Case_stmtContext case_stmt(int i) {
			return getRuleContext(Case_stmtContext.class,i);
		}
		public TerminalNode RET() { return getToken(JimpleParser.RET, 0); }
		public TerminalNode THROW() { return getToken(JimpleParser.THROW, 0); }
		public TerminalNode ENTERMONITOR() { return getToken(JimpleParser.ENTERMONITOR, 0); }
		public TerminalNode EXITMONITOR() { return getToken(JimpleParser.EXITMONITOR, 0); }
		public TerminalNode NOP() { return getToken(JimpleParser.NOP, 0); }
		public TerminalNode BREAKPOINT() { return getToken(JimpleParser.BREAKPOINT, 0); }
		public StmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_stmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).enterStmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).exitStmt(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JimpleVisitor ) return ((JimpleVisitor<? extends T>)visitor).visitStmt(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StmtContext stmt() throws RecognitionException {
		StmtContext _localctx = new StmtContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_stmt);
		int _la;
		try {
			setState(276);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case STRING_CONSTANT:
			case CMPLT:
			case IDENTIFIER:
				enterOuterAlt(_localctx, 1);
				{
				setState(241);
				assignments();
				}
				break;
			case GOTO:
			case IF:
				enterOuterAlt(_localctx, 2);
				{
				setState(244);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==IF) {
					{
					setState(242);
					match(IF);
					setState(243);
					bool_expr();
					}
				}

				setState(246);
				goto_stmt();
				}
				break;
			case NONSTATIC_INVOKE:
			case STATICINVOKE:
			case DYNAMICINVOKE:
				enterOuterAlt(_localctx, 3);
				{
				setState(247);
				invoke_expr();
				}
				break;
			case RETURN:
				enterOuterAlt(_localctx, 4);
				{
				setState(248);
				match(RETURN);
				setState(250);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__23) | (1L << T__24) | (1L << STRING_CONSTANT) | (1L << CLASS) | (1L << NULL))) != 0) || ((((_la - 87)) & ~0x3f) == 0 && ((1L << (_la - 87)) & ((1L << (PLUS - 87)) | (1L << (MINUS - 87)) | (1L << (BOOL_CONSTANT - 87)) | (1L << (FLOAT_CONSTANT - 87)) | (1L << (DEC_CONSTANT - 87)) | (1L << (HEX_CONSTANT - 87)) | (1L << (IDENTIFIER - 87)))) != 0)) {
					{
					setState(249);
					immediate();
					}
				}

				}
				break;
			case SWITCH:
				enterOuterAlt(_localctx, 5);
				{
				setState(252);
				match(SWITCH);
				setState(253);
				match(L_PAREN);
				setState(254);
				immediate();
				setState(255);
				match(R_PAREN);
				setState(256);
				match(L_BRACE);
				setState(258); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(257);
					case_stmt();
					}
					}
					setState(260); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==CASE || _la==DEFAULT );
				setState(262);
				match(R_BRACE);
				}
				break;
			case RET:
				enterOuterAlt(_localctx, 6);
				{
				setState(264);
				match(RET);
				setState(266);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__23) | (1L << T__24) | (1L << STRING_CONSTANT) | (1L << CLASS) | (1L << NULL))) != 0) || ((((_la - 87)) & ~0x3f) == 0 && ((1L << (_la - 87)) & ((1L << (PLUS - 87)) | (1L << (MINUS - 87)) | (1L << (BOOL_CONSTANT - 87)) | (1L << (FLOAT_CONSTANT - 87)) | (1L << (DEC_CONSTANT - 87)) | (1L << (HEX_CONSTANT - 87)) | (1L << (IDENTIFIER - 87)))) != 0)) {
					{
					setState(265);
					immediate();
					}
				}

				}
				break;
			case THROW:
				enterOuterAlt(_localctx, 7);
				{
				setState(268);
				match(THROW);
				setState(269);
				immediate();
				}
				break;
			case ENTERMONITOR:
				enterOuterAlt(_localctx, 8);
				{
				setState(270);
				match(ENTERMONITOR);
				setState(271);
				immediate();
				}
				break;
			case EXITMONITOR:
				enterOuterAlt(_localctx, 9);
				{
				setState(272);
				match(EXITMONITOR);
				setState(273);
				immediate();
				}
				break;
			case NOP:
				enterOuterAlt(_localctx, 10);
				{
				setState(274);
				match(NOP);
				}
				break;
			case BREAKPOINT:
				enterOuterAlt(_localctx, 11);
				{
				setState(275);
				match(BREAKPOINT);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AssignmentsContext extends ParserRuleContext {
		public IdentifierContext local;
		public TerminalNode COLON_EQUALS() { return getToken(JimpleParser.COLON_EQUALS, 0); }
		public Identity_refContext identity_ref() {
			return getRuleContext(Identity_refContext.class,0);
		}
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode EQUALS() { return getToken(JimpleParser.EQUALS, 0); }
		public ValueContext value() {
			return getRuleContext(ValueContext.class,0);
		}
		public ReferenceContext reference() {
			return getRuleContext(ReferenceContext.class,0);
		}
		public AssignmentsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_assignments; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).enterAssignments(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).exitAssignments(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JimpleVisitor ) return ((JimpleVisitor<? extends T>)visitor).visitAssignments(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AssignmentsContext assignments() throws RecognitionException {
		AssignmentsContext _localctx = new AssignmentsContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_assignments);
		try {
			setState(289);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,26,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(278);
				((AssignmentsContext)_localctx).local = identifier();
				setState(279);
				match(COLON_EQUALS);
				setState(280);
				identity_ref();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(284);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,25,_ctx) ) {
				case 1:
					{
					setState(282);
					reference();
					}
					break;
				case 2:
					{
					setState(283);
					((AssignmentsContext)_localctx).local = identifier();
					}
					break;
				}
				setState(286);
				match(EQUALS);
				setState(287);
				value();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Identity_refContext extends ParserRuleContext {
		public Token parameter_idx;
		public Token caught;
		public TerminalNode COLON() { return getToken(JimpleParser.COLON, 0); }
		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}
		public TerminalNode DEC_CONSTANT() { return getToken(JimpleParser.DEC_CONSTANT, 0); }
		public Identity_refContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_identity_ref; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).enterIdentity_ref(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).exitIdentity_ref(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JimpleVisitor ) return ((JimpleVisitor<? extends T>)visitor).visitIdentity_ref(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Identity_refContext identity_ref() throws RecognitionException {
		Identity_refContext _localctx = new Identity_refContext(_ctx, getState());
		enterRule(_localctx, 50, RULE_identity_ref);
		try {
			setState(298);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__20:
				enterOuterAlt(_localctx, 1);
				{
				setState(291);
				match(T__20);
				setState(292);
				((Identity_refContext)_localctx).parameter_idx = match(DEC_CONSTANT);
				setState(293);
				match(COLON);
				setState(294);
				type();
				}
				break;
			case T__21:
				enterOuterAlt(_localctx, 2);
				{
				setState(295);
				match(T__21);
				setState(296);
				type();
				}
				break;
			case T__22:
				enterOuterAlt(_localctx, 3);
				{
				setState(297);
				((Identity_refContext)_localctx).caught = match(T__22);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Case_stmtContext extends ParserRuleContext {
		public Case_labelContext case_label() {
			return getRuleContext(Case_labelContext.class,0);
		}
		public TerminalNode COLON() { return getToken(JimpleParser.COLON, 0); }
		public Goto_stmtContext goto_stmt() {
			return getRuleContext(Goto_stmtContext.class,0);
		}
		public TerminalNode SEMICOLON() { return getToken(JimpleParser.SEMICOLON, 0); }
		public Case_stmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_case_stmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).enterCase_stmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).exitCase_stmt(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JimpleVisitor ) return ((JimpleVisitor<? extends T>)visitor).visitCase_stmt(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Case_stmtContext case_stmt() throws RecognitionException {
		Case_stmtContext _localctx = new Case_stmtContext(_ctx, getState());
		enterRule(_localctx, 52, RULE_case_stmt);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(300);
			case_label();
			setState(301);
			match(COLON);
			setState(302);
			goto_stmt();
			setState(303);
			match(SEMICOLON);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Case_labelContext extends ParserRuleContext {
		public TerminalNode CASE() { return getToken(JimpleParser.CASE, 0); }
		public Integer_constantContext integer_constant() {
			return getRuleContext(Integer_constantContext.class,0);
		}
		public TerminalNode DEFAULT() { return getToken(JimpleParser.DEFAULT, 0); }
		public Case_labelContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_case_label; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).enterCase_label(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).exitCase_label(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JimpleVisitor ) return ((JimpleVisitor<? extends T>)visitor).visitCase_label(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Case_labelContext case_label() throws RecognitionException {
		Case_labelContext _localctx = new Case_labelContext(_ctx, getState());
		enterRule(_localctx, 54, RULE_case_label);
		try {
			setState(308);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case CASE:
				enterOuterAlt(_localctx, 1);
				{
				setState(305);
				match(CASE);
				setState(306);
				integer_constant();
				}
				break;
			case DEFAULT:
				enterOuterAlt(_localctx, 2);
				{
				setState(307);
				match(DEFAULT);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Goto_stmtContext extends ParserRuleContext {
		public IdentifierContext label_name;
		public TerminalNode GOTO() { return getToken(JimpleParser.GOTO, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public Goto_stmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_goto_stmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).enterGoto_stmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).exitGoto_stmt(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JimpleVisitor ) return ((JimpleVisitor<? extends T>)visitor).visitGoto_stmt(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Goto_stmtContext goto_stmt() throws RecognitionException {
		Goto_stmtContext _localctx = new Goto_stmtContext(_ctx, getState());
		enterRule(_localctx, 56, RULE_goto_stmt);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(310);
			match(GOTO);
			setState(311);
			((Goto_stmtContext)_localctx).label_name = identifier();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Trap_clauseContext extends ParserRuleContext {
		public IdentifierContext exceptiontype;
		public IdentifierContext from;
		public IdentifierContext to;
		public IdentifierContext with;
		public TerminalNode CATCH() { return getToken(JimpleParser.CATCH, 0); }
		public TerminalNode FROM() { return getToken(JimpleParser.FROM, 0); }
		public TerminalNode TO() { return getToken(JimpleParser.TO, 0); }
		public TerminalNode WITH() { return getToken(JimpleParser.WITH, 0); }
		public TerminalNode SEMICOLON() { return getToken(JimpleParser.SEMICOLON, 0); }
		public List<IdentifierContext> identifier() {
			return getRuleContexts(IdentifierContext.class);
		}
		public IdentifierContext identifier(int i) {
			return getRuleContext(IdentifierContext.class,i);
		}
		public Trap_clauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_trap_clause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).enterTrap_clause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).exitTrap_clause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JimpleVisitor ) return ((JimpleVisitor<? extends T>)visitor).visitTrap_clause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Trap_clauseContext trap_clause() throws RecognitionException {
		Trap_clauseContext _localctx = new Trap_clauseContext(_ctx, getState());
		enterRule(_localctx, 58, RULE_trap_clause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(313);
			match(CATCH);
			setState(314);
			((Trap_clauseContext)_localctx).exceptiontype = identifier();
			setState(315);
			match(FROM);
			setState(316);
			((Trap_clauseContext)_localctx).from = identifier();
			setState(317);
			match(TO);
			setState(318);
			((Trap_clauseContext)_localctx).to = identifier();
			setState(319);
			match(WITH);
			setState(320);
			((Trap_clauseContext)_localctx).with = identifier();
			setState(321);
			match(SEMICOLON);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ValueContext extends ParserRuleContext {
		public IdentifierContext base_type;
		public TypeContext array_type;
		public TypeContext multiarray_type;
		public TypeContext nonvoid_cast;
		public ImmediateContext op;
		public TypeContext nonvoid_type;
		public List<ImmediateContext> immediate() {
			return getRuleContexts(ImmediateContext.class);
		}
		public ImmediateContext immediate(int i) {
			return getRuleContext(ImmediateContext.class,i);
		}
		public ReferenceContext reference() {
			return getRuleContext(ReferenceContext.class,0);
		}
		public TerminalNode NEW() { return getToken(JimpleParser.NEW, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode NEWARRAY() { return getToken(JimpleParser.NEWARRAY, 0); }
		public TerminalNode L_PAREN() { return getToken(JimpleParser.L_PAREN, 0); }
		public TerminalNode R_PAREN() { return getToken(JimpleParser.R_PAREN, 0); }
		public Array_descriptorContext array_descriptor() {
			return getRuleContext(Array_descriptorContext.class,0);
		}
		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}
		public TerminalNode NEWMULTIARRAY() { return getToken(JimpleParser.NEWMULTIARRAY, 0); }
		public List<TerminalNode> L_BRACKET() { return getTokens(JimpleParser.L_BRACKET); }
		public TerminalNode L_BRACKET(int i) {
			return getToken(JimpleParser.L_BRACKET, i);
		}
		public List<TerminalNode> R_BRACKET() { return getTokens(JimpleParser.R_BRACKET); }
		public TerminalNode R_BRACKET(int i) {
			return getToken(JimpleParser.R_BRACKET, i);
		}
		public TerminalNode INSTANCEOF() { return getToken(JimpleParser.INSTANCEOF, 0); }
		public Binop_exprContext binop_expr() {
			return getRuleContext(Binop_exprContext.class,0);
		}
		public Invoke_exprContext invoke_expr() {
			return getRuleContext(Invoke_exprContext.class,0);
		}
		public Unop_exprContext unop_expr() {
			return getRuleContext(Unop_exprContext.class,0);
		}
		public ValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_value; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).enterValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).exitValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JimpleVisitor ) return ((JimpleVisitor<? extends T>)visitor).visitValue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ValueContext value() throws RecognitionException {
		ValueContext _localctx = new ValueContext(_ctx, getState());
		enterRule(_localctx, 60, RULE_value);
		int _la;
		try {
			setState(358);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,31,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(323);
				immediate();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(324);
				reference();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(325);
				match(NEW);
				setState(326);
				((ValueContext)_localctx).base_type = identifier();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(327);
				match(NEWARRAY);
				setState(328);
				match(L_PAREN);
				setState(329);
				((ValueContext)_localctx).array_type = type();
				setState(330);
				match(R_PAREN);
				setState(331);
				array_descriptor();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(333);
				match(NEWMULTIARRAY);
				setState(334);
				match(L_PAREN);
				setState(335);
				((ValueContext)_localctx).multiarray_type = type();
				setState(336);
				match(R_PAREN);
				setState(342); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(337);
					match(L_BRACKET);
					setState(339);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__23) | (1L << T__24) | (1L << STRING_CONSTANT) | (1L << CLASS) | (1L << NULL))) != 0) || ((((_la - 87)) & ~0x3f) == 0 && ((1L << (_la - 87)) & ((1L << (PLUS - 87)) | (1L << (MINUS - 87)) | (1L << (BOOL_CONSTANT - 87)) | (1L << (FLOAT_CONSTANT - 87)) | (1L << (DEC_CONSTANT - 87)) | (1L << (HEX_CONSTANT - 87)) | (1L << (IDENTIFIER - 87)))) != 0)) {
						{
						setState(338);
						immediate();
						}
					}

					setState(341);
					match(R_BRACKET);
					}
					}
					setState(344); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==L_BRACKET );
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(346);
				match(L_PAREN);
				setState(347);
				((ValueContext)_localctx).nonvoid_cast = type();
				setState(348);
				match(R_PAREN);
				setState(349);
				((ValueContext)_localctx).op = immediate();
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(351);
				((ValueContext)_localctx).op = immediate();
				setState(352);
				match(INSTANCEOF);
				setState(353);
				((ValueContext)_localctx).nonvoid_type = type();
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(355);
				binop_expr();
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(356);
				invoke_expr();
				}
				break;
			case 10:
				enterOuterAlt(_localctx, 10);
				{
				setState(357);
				unop_expr();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Bool_exprContext extends ParserRuleContext {
		public Binop_exprContext binop_expr() {
			return getRuleContext(Binop_exprContext.class,0);
		}
		public Unop_exprContext unop_expr() {
			return getRuleContext(Unop_exprContext.class,0);
		}
		public Bool_exprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_bool_expr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).enterBool_expr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).exitBool_expr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JimpleVisitor ) return ((JimpleVisitor<? extends T>)visitor).visitBool_expr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Bool_exprContext bool_expr() throws RecognitionException {
		Bool_exprContext _localctx = new Bool_exprContext(_ctx, getState());
		enterRule(_localctx, 62, RULE_bool_expr);
		try {
			setState(362);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__23:
			case T__24:
			case STRING_CONSTANT:
			case CLASS:
			case NULL:
			case PLUS:
			case MINUS:
			case BOOL_CONSTANT:
			case FLOAT_CONSTANT:
			case DEC_CONSTANT:
			case HEX_CONSTANT:
			case IDENTIFIER:
				enterOuterAlt(_localctx, 1);
				{
				setState(360);
				binop_expr();
				}
				break;
			case LENGTHOF:
			case NEG:
				enterOuterAlt(_localctx, 2);
				{
				setState(361);
				unop_expr();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Invoke_exprContext extends ParserRuleContext {
		public Token nonstaticinvoke;
		public IdentifierContext local_name;
		public Token staticinvoke;
		public Token dynamicinvoke;
		public IdentifierContext unnamed_method_name;
		public TypeContext name;
		public Type_listContext parameter_list;
		public Arg_listContext dyn_args;
		public Method_signatureContext bsm;
		public Arg_listContext staticargs;
		public TerminalNode DOT() { return getToken(JimpleParser.DOT, 0); }
		public Method_signatureContext method_signature() {
			return getRuleContext(Method_signatureContext.class,0);
		}
		public List<TerminalNode> L_PAREN() { return getTokens(JimpleParser.L_PAREN); }
		public TerminalNode L_PAREN(int i) {
			return getToken(JimpleParser.L_PAREN, i);
		}
		public List<TerminalNode> R_PAREN() { return getTokens(JimpleParser.R_PAREN); }
		public TerminalNode R_PAREN(int i) {
			return getToken(JimpleParser.R_PAREN, i);
		}
		public TerminalNode NONSTATIC_INVOKE() { return getToken(JimpleParser.NONSTATIC_INVOKE, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public List<Arg_listContext> arg_list() {
			return getRuleContexts(Arg_listContext.class);
		}
		public Arg_listContext arg_list(int i) {
			return getRuleContext(Arg_listContext.class,i);
		}
		public TerminalNode STATICINVOKE() { return getToken(JimpleParser.STATICINVOKE, 0); }
		public TerminalNode CMPLT() { return getToken(JimpleParser.CMPLT, 0); }
		public TerminalNode CMPGT() { return getToken(JimpleParser.CMPGT, 0); }
		public TerminalNode DYNAMICINVOKE() { return getToken(JimpleParser.DYNAMICINVOKE, 0); }
		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}
		public Type_listContext type_list() {
			return getRuleContext(Type_listContext.class,0);
		}
		public Invoke_exprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_invoke_expr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).enterInvoke_expr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).exitInvoke_expr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JimpleVisitor ) return ((JimpleVisitor<? extends T>)visitor).visitInvoke_expr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Invoke_exprContext invoke_expr() throws RecognitionException {
		Invoke_exprContext _localctx = new Invoke_exprContext(_ctx, getState());
		enterRule(_localctx, 64, RULE_invoke_expr);
		int _la;
		try {
			setState(404);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case NONSTATIC_INVOKE:
				enterOuterAlt(_localctx, 1);
				{
				setState(364);
				((Invoke_exprContext)_localctx).nonstaticinvoke = match(NONSTATIC_INVOKE);
				setState(365);
				((Invoke_exprContext)_localctx).local_name = identifier();
				setState(366);
				match(DOT);
				setState(367);
				method_signature();
				setState(368);
				match(L_PAREN);
				setState(370);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__23) | (1L << T__24) | (1L << STRING_CONSTANT) | (1L << CLASS) | (1L << NULL))) != 0) || ((((_la - 87)) & ~0x3f) == 0 && ((1L << (_la - 87)) & ((1L << (PLUS - 87)) | (1L << (MINUS - 87)) | (1L << (BOOL_CONSTANT - 87)) | (1L << (FLOAT_CONSTANT - 87)) | (1L << (DEC_CONSTANT - 87)) | (1L << (HEX_CONSTANT - 87)) | (1L << (IDENTIFIER - 87)))) != 0)) {
					{
					setState(369);
					arg_list();
					}
				}

				setState(372);
				match(R_PAREN);
				}
				break;
			case STATICINVOKE:
				enterOuterAlt(_localctx, 2);
				{
				setState(374);
				((Invoke_exprContext)_localctx).staticinvoke = match(STATICINVOKE);
				setState(375);
				method_signature();
				setState(376);
				match(L_PAREN);
				setState(378);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__23) | (1L << T__24) | (1L << STRING_CONSTANT) | (1L << CLASS) | (1L << NULL))) != 0) || ((((_la - 87)) & ~0x3f) == 0 && ((1L << (_la - 87)) & ((1L << (PLUS - 87)) | (1L << (MINUS - 87)) | (1L << (BOOL_CONSTANT - 87)) | (1L << (FLOAT_CONSTANT - 87)) | (1L << (DEC_CONSTANT - 87)) | (1L << (HEX_CONSTANT - 87)) | (1L << (IDENTIFIER - 87)))) != 0)) {
					{
					setState(377);
					arg_list();
					}
				}

				setState(380);
				match(R_PAREN);
				}
				break;
			case DYNAMICINVOKE:
				enterOuterAlt(_localctx, 3);
				{
				setState(382);
				((Invoke_exprContext)_localctx).dynamicinvoke = match(DYNAMICINVOKE);
				setState(383);
				((Invoke_exprContext)_localctx).unnamed_method_name = identifier();
				setState(384);
				match(CMPLT);
				setState(385);
				((Invoke_exprContext)_localctx).name = type();
				setState(386);
				match(L_PAREN);
				setState(388);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==STRING_CONSTANT || _la==IDENTIFIER) {
					{
					setState(387);
					((Invoke_exprContext)_localctx).parameter_list = type_list();
					}
				}

				setState(390);
				match(R_PAREN);
				setState(391);
				match(CMPGT);
				setState(392);
				match(L_PAREN);
				setState(394);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__23) | (1L << T__24) | (1L << STRING_CONSTANT) | (1L << CLASS) | (1L << NULL))) != 0) || ((((_la - 87)) & ~0x3f) == 0 && ((1L << (_la - 87)) & ((1L << (PLUS - 87)) | (1L << (MINUS - 87)) | (1L << (BOOL_CONSTANT - 87)) | (1L << (FLOAT_CONSTANT - 87)) | (1L << (DEC_CONSTANT - 87)) | (1L << (HEX_CONSTANT - 87)) | (1L << (IDENTIFIER - 87)))) != 0)) {
					{
					setState(393);
					((Invoke_exprContext)_localctx).dyn_args = arg_list();
					}
				}

				setState(396);
				match(R_PAREN);
				setState(397);
				((Invoke_exprContext)_localctx).bsm = method_signature();
				setState(398);
				match(L_PAREN);
				setState(400);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__23) | (1L << T__24) | (1L << STRING_CONSTANT) | (1L << CLASS) | (1L << NULL))) != 0) || ((((_la - 87)) & ~0x3f) == 0 && ((1L << (_la - 87)) & ((1L << (PLUS - 87)) | (1L << (MINUS - 87)) | (1L << (BOOL_CONSTANT - 87)) | (1L << (FLOAT_CONSTANT - 87)) | (1L << (DEC_CONSTANT - 87)) | (1L << (HEX_CONSTANT - 87)) | (1L << (IDENTIFIER - 87)))) != 0)) {
					{
					setState(399);
					((Invoke_exprContext)_localctx).staticargs = arg_list();
					}
				}

				setState(402);
				match(R_PAREN);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Binop_exprContext extends ParserRuleContext {
		public ImmediateContext left;
		public ImmediateContext right;
		public BinopContext binop() {
			return getRuleContext(BinopContext.class,0);
		}
		public List<ImmediateContext> immediate() {
			return getRuleContexts(ImmediateContext.class);
		}
		public ImmediateContext immediate(int i) {
			return getRuleContext(ImmediateContext.class,i);
		}
		public Binop_exprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_binop_expr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).enterBinop_expr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).exitBinop_expr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JimpleVisitor ) return ((JimpleVisitor<? extends T>)visitor).visitBinop_expr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Binop_exprContext binop_expr() throws RecognitionException {
		Binop_exprContext _localctx = new Binop_exprContext(_ctx, getState());
		enterRule(_localctx, 66, RULE_binop_expr);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(406);
			((Binop_exprContext)_localctx).left = immediate();
			setState(407);
			binop();
			setState(408);
			((Binop_exprContext)_localctx).right = immediate();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Unop_exprContext extends ParserRuleContext {
		public UnopContext unop() {
			return getRuleContext(UnopContext.class,0);
		}
		public ImmediateContext immediate() {
			return getRuleContext(ImmediateContext.class,0);
		}
		public Unop_exprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_unop_expr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).enterUnop_expr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).exitUnop_expr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JimpleVisitor ) return ((JimpleVisitor<? extends T>)visitor).visitUnop_expr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Unop_exprContext unop_expr() throws RecognitionException {
		Unop_exprContext _localctx = new Unop_exprContext(_ctx, getState());
		enterRule(_localctx, 68, RULE_unop_expr);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(410);
			unop();
			setState(411);
			immediate();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Method_subsignatureContext extends ParserRuleContext {
		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}
		public Method_nameContext method_name() {
			return getRuleContext(Method_nameContext.class,0);
		}
		public TerminalNode L_PAREN() { return getToken(JimpleParser.L_PAREN, 0); }
		public TerminalNode R_PAREN() { return getToken(JimpleParser.R_PAREN, 0); }
		public Type_listContext type_list() {
			return getRuleContext(Type_listContext.class,0);
		}
		public Method_subsignatureContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_method_subsignature; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).enterMethod_subsignature(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).exitMethod_subsignature(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JimpleVisitor ) return ((JimpleVisitor<? extends T>)visitor).visitMethod_subsignature(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Method_subsignatureContext method_subsignature() throws RecognitionException {
		Method_subsignatureContext _localctx = new Method_subsignatureContext(_ctx, getState());
		enterRule(_localctx, 70, RULE_method_subsignature);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(413);
			type();
			setState(414);
			method_name();
			setState(415);
			match(L_PAREN);
			setState(417);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==STRING_CONSTANT || _la==IDENTIFIER) {
				{
				setState(416);
				type_list();
				}
			}

			setState(419);
			match(R_PAREN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Method_signatureContext extends ParserRuleContext {
		public IdentifierContext class_name;
		public TerminalNode CMPLT() { return getToken(JimpleParser.CMPLT, 0); }
		public TerminalNode COLON() { return getToken(JimpleParser.COLON, 0); }
		public Method_subsignatureContext method_subsignature() {
			return getRuleContext(Method_subsignatureContext.class,0);
		}
		public TerminalNode CMPGT() { return getToken(JimpleParser.CMPGT, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public Method_signatureContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_method_signature; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).enterMethod_signature(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).exitMethod_signature(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JimpleVisitor ) return ((JimpleVisitor<? extends T>)visitor).visitMethod_signature(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Method_signatureContext method_signature() throws RecognitionException {
		Method_signatureContext _localctx = new Method_signatureContext(_ctx, getState());
		enterRule(_localctx, 72, RULE_method_signature);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(421);
			match(CMPLT);
			setState(422);
			((Method_signatureContext)_localctx).class_name = identifier();
			setState(423);
			match(COLON);
			setState(424);
			method_subsignature();
			setState(425);
			match(CMPGT);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ReferenceContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public Array_descriptorContext array_descriptor() {
			return getRuleContext(Array_descriptorContext.class,0);
		}
		public TerminalNode DOT() { return getToken(JimpleParser.DOT, 0); }
		public Field_signatureContext field_signature() {
			return getRuleContext(Field_signatureContext.class,0);
		}
		public ReferenceContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_reference; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).enterReference(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).exitReference(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JimpleVisitor ) return ((JimpleVisitor<? extends T>)visitor).visitReference(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ReferenceContext reference() throws RecognitionException {
		ReferenceContext _localctx = new ReferenceContext(_ctx, getState());
		enterRule(_localctx, 74, RULE_reference);
		try {
			setState(435);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,40,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(427);
				identifier();
				setState(428);
				array_descriptor();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(430);
				identifier();
				setState(431);
				match(DOT);
				setState(432);
				field_signature();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(434);
				field_signature();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Field_signatureContext extends ParserRuleContext {
		public IdentifierContext classname;
		public IdentifierContext fieldname;
		public TerminalNode CMPLT() { return getToken(JimpleParser.CMPLT, 0); }
		public TerminalNode COLON() { return getToken(JimpleParser.COLON, 0); }
		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}
		public TerminalNode CMPGT() { return getToken(JimpleParser.CMPGT, 0); }
		public List<IdentifierContext> identifier() {
			return getRuleContexts(IdentifierContext.class);
		}
		public IdentifierContext identifier(int i) {
			return getRuleContext(IdentifierContext.class,i);
		}
		public Field_signatureContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_field_signature; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).enterField_signature(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).exitField_signature(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JimpleVisitor ) return ((JimpleVisitor<? extends T>)visitor).visitField_signature(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Field_signatureContext field_signature() throws RecognitionException {
		Field_signatureContext _localctx = new Field_signatureContext(_ctx, getState());
		enterRule(_localctx, 76, RULE_field_signature);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(437);
			match(CMPLT);
			setState(438);
			((Field_signatureContext)_localctx).classname = identifier();
			setState(439);
			match(COLON);
			setState(440);
			type();
			setState(441);
			((Field_signatureContext)_localctx).fieldname = identifier();
			setState(442);
			match(CMPGT);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Array_descriptorContext extends ParserRuleContext {
		public TerminalNode L_BRACKET() { return getToken(JimpleParser.L_BRACKET, 0); }
		public ImmediateContext immediate() {
			return getRuleContext(ImmediateContext.class,0);
		}
		public TerminalNode R_BRACKET() { return getToken(JimpleParser.R_BRACKET, 0); }
		public Array_descriptorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_array_descriptor; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).enterArray_descriptor(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).exitArray_descriptor(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JimpleVisitor ) return ((JimpleVisitor<? extends T>)visitor).visitArray_descriptor(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Array_descriptorContext array_descriptor() throws RecognitionException {
		Array_descriptorContext _localctx = new Array_descriptorContext(_ctx, getState());
		enterRule(_localctx, 78, RULE_array_descriptor);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(444);
			match(L_BRACKET);
			setState(445);
			immediate();
			setState(446);
			match(R_BRACKET);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Arg_listContext extends ParserRuleContext {
		public List<ImmediateContext> immediate() {
			return getRuleContexts(ImmediateContext.class);
		}
		public ImmediateContext immediate(int i) {
			return getRuleContext(ImmediateContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(JimpleParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(JimpleParser.COMMA, i);
		}
		public Arg_listContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_arg_list; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).enterArg_list(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).exitArg_list(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JimpleVisitor ) return ((JimpleVisitor<? extends T>)visitor).visitArg_list(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Arg_listContext arg_list() throws RecognitionException {
		Arg_listContext _localctx = new Arg_listContext(_ctx, getState());
		enterRule(_localctx, 80, RULE_arg_list);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(448);
			immediate();
			setState(453);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(449);
				match(COMMA);
				setState(450);
				immediate();
				}
				}
				setState(455);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ImmediateContext extends ParserRuleContext {
		public IdentifierContext local;
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public ConstantContext constant() {
			return getRuleContext(ConstantContext.class,0);
		}
		public ImmediateContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_immediate; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).enterImmediate(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).exitImmediate(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JimpleVisitor ) return ((JimpleVisitor<? extends T>)visitor).visitImmediate(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ImmediateContext immediate() throws RecognitionException {
		ImmediateContext _localctx = new ImmediateContext(_ctx, getState());
		enterRule(_localctx, 82, RULE_immediate);
		try {
			setState(458);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,42,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(456);
				((ImmediateContext)_localctx).local = identifier();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(457);
				constant();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ConstantContext extends ParserRuleContext {
		public Token methodhandle;
		public Token methodtype;
		public TerminalNode BOOL_CONSTANT() { return getToken(JimpleParser.BOOL_CONSTANT, 0); }
		public Integer_constantContext integer_constant() {
			return getRuleContext(Integer_constantContext.class,0);
		}
		public TerminalNode FLOAT_CONSTANT() { return getToken(JimpleParser.FLOAT_CONSTANT, 0); }
		public TerminalNode STRING_CONSTANT() { return getToken(JimpleParser.STRING_CONSTANT, 0); }
		public TerminalNode CLASS() { return getToken(JimpleParser.CLASS, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode NULL() { return getToken(JimpleParser.NULL, 0); }
		public Method_signatureContext method_signature() {
			return getRuleContext(Method_signatureContext.class,0);
		}
		public Method_subsignatureContext method_subsignature() {
			return getRuleContext(Method_subsignatureContext.class,0);
		}
		public ConstantContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_constant; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).enterConstant(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).exitConstant(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JimpleVisitor ) return ((JimpleVisitor<? extends T>)visitor).visitConstant(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ConstantContext constant() throws RecognitionException {
		ConstantContext _localctx = new ConstantContext(_ctx, getState());
		enterRule(_localctx, 84, RULE_constant);
		try {
			setState(471);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case BOOL_CONSTANT:
				enterOuterAlt(_localctx, 1);
				{
				setState(460);
				match(BOOL_CONSTANT);
				}
				break;
			case PLUS:
			case MINUS:
			case DEC_CONSTANT:
			case HEX_CONSTANT:
				enterOuterAlt(_localctx, 2);
				{
				setState(461);
				integer_constant();
				}
				break;
			case FLOAT_CONSTANT:
				enterOuterAlt(_localctx, 3);
				{
				setState(462);
				match(FLOAT_CONSTANT);
				}
				break;
			case STRING_CONSTANT:
				enterOuterAlt(_localctx, 4);
				{
				setState(463);
				match(STRING_CONSTANT);
				}
				break;
			case CLASS:
				enterOuterAlt(_localctx, 5);
				{
				setState(464);
				match(CLASS);
				setState(465);
				identifier();
				}
				break;
			case NULL:
				enterOuterAlt(_localctx, 6);
				{
				setState(466);
				match(NULL);
				}
				break;
			case T__23:
				enterOuterAlt(_localctx, 7);
				{
				setState(467);
				((ConstantContext)_localctx).methodhandle = match(T__23);
				setState(468);
				method_signature();
				}
				break;
			case T__24:
				enterOuterAlt(_localctx, 8);
				{
				setState(469);
				((ConstantContext)_localctx).methodtype = match(T__24);
				setState(470);
				method_subsignature();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class BinopContext extends ParserRuleContext {
		public TerminalNode AND() { return getToken(JimpleParser.AND, 0); }
		public TerminalNode OR() { return getToken(JimpleParser.OR, 0); }
		public TerminalNode XOR() { return getToken(JimpleParser.XOR, 0); }
		public TerminalNode CMP() { return getToken(JimpleParser.CMP, 0); }
		public TerminalNode CMPG() { return getToken(JimpleParser.CMPG, 0); }
		public TerminalNode CMPL() { return getToken(JimpleParser.CMPL, 0); }
		public TerminalNode CMPEQ() { return getToken(JimpleParser.CMPEQ, 0); }
		public TerminalNode CMPNE() { return getToken(JimpleParser.CMPNE, 0); }
		public TerminalNode CMPGT() { return getToken(JimpleParser.CMPGT, 0); }
		public TerminalNode CMPGE() { return getToken(JimpleParser.CMPGE, 0); }
		public TerminalNode CMPLT() { return getToken(JimpleParser.CMPLT, 0); }
		public TerminalNode CMPLE() { return getToken(JimpleParser.CMPLE, 0); }
		public TerminalNode SHL() { return getToken(JimpleParser.SHL, 0); }
		public TerminalNode SHR() { return getToken(JimpleParser.SHR, 0); }
		public TerminalNode USHR() { return getToken(JimpleParser.USHR, 0); }
		public TerminalNode PLUS() { return getToken(JimpleParser.PLUS, 0); }
		public TerminalNode MINUS() { return getToken(JimpleParser.MINUS, 0); }
		public TerminalNode MULT() { return getToken(JimpleParser.MULT, 0); }
		public TerminalNode DIV() { return getToken(JimpleParser.DIV, 0); }
		public TerminalNode MOD() { return getToken(JimpleParser.MOD, 0); }
		public BinopContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_binop; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).enterBinop(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).exitBinop(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JimpleVisitor ) return ((JimpleVisitor<? extends T>)visitor).visitBinop(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BinopContext binop() throws RecognitionException {
		BinopContext _localctx = new BinopContext(_ctx, getState());
		enterRule(_localctx, 86, RULE_binop);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(473);
			_la = _input.LA(1);
			if ( !(((((_la - 35)) & ~0x3f) == 0 && ((1L << (_la - 35)) & ((1L << (CMP - 35)) | (1L << (CMPG - 35)) | (1L << (CMPL - 35)) | (1L << (AND - 35)) | (1L << (OR - 35)) | (1L << (XOR - 35)) | (1L << (MOD - 35)) | (1L << (CMPEQ - 35)) | (1L << (CMPNE - 35)) | (1L << (CMPGT - 35)) | (1L << (CMPGE - 35)) | (1L << (CMPLT - 35)) | (1L << (CMPLE - 35)) | (1L << (SHL - 35)) | (1L << (SHR - 35)) | (1L << (USHR - 35)) | (1L << (PLUS - 35)) | (1L << (MINUS - 35)) | (1L << (MULT - 35)) | (1L << (DIV - 35)))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class UnopContext extends ParserRuleContext {
		public TerminalNode LENGTHOF() { return getToken(JimpleParser.LENGTHOF, 0); }
		public TerminalNode NEG() { return getToken(JimpleParser.NEG, 0); }
		public UnopContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_unop; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).enterUnop(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JimpleListener ) ((JimpleListener)listener).exitUnop(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JimpleVisitor ) return ((JimpleVisitor<? extends T>)visitor).visitUnop(this);
			else return visitor.visitChildren(this);
		}
	}

	public final UnopContext unop() throws RecognitionException {
		UnopContext _localctx = new UnopContext(_ctx, getState());
		enterRule(_localctx, 88, RULE_unop);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(475);
			_la = _input.LA(1);
			if ( !(_la==LENGTHOF || _la==NEG) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\u0004\u0001`\u01de\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002"+
		"\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b\u0002"+
		"\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007\u000f"+
		"\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002\u0012\u0007\u0012"+
		"\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014\u0002\u0015\u0007\u0015"+
		"\u0002\u0016\u0007\u0016\u0002\u0017\u0007\u0017\u0002\u0018\u0007\u0018"+
		"\u0002\u0019\u0007\u0019\u0002\u001a\u0007\u001a\u0002\u001b\u0007\u001b"+
		"\u0002\u001c\u0007\u001c\u0002\u001d\u0007\u001d\u0002\u001e\u0007\u001e"+
		"\u0002\u001f\u0007\u001f\u0002 \u0007 \u0002!\u0007!\u0002\"\u0007\"\u0002"+
		"#\u0007#\u0002$\u0007$\u0002%\u0007%\u0002&\u0007&\u0002\'\u0007\'\u0002"+
		"(\u0007(\u0002)\u0007)\u0002*\u0007*\u0002+\u0007+\u0002,\u0007,\u0001"+
		"\u0000\u0001\u0000\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0003\u0001b\b\u0001\u0001\u0002\u0003\u0002e\b\u0002\u0001\u0002"+
		"\u0001\u0002\u0003\u0002i\b\u0002\u0001\u0003\u0005\u0003l\b\u0003\n\u0003"+
		"\f\u0003o\t\u0003\u0001\u0003\u0005\u0003r\b\u0003\n\u0003\f\u0003u\t"+
		"\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0003\u0003z\b\u0003\u0001"+
		"\u0003\u0003\u0003}\b\u0003\u0001\u0003\u0001\u0003\u0005\u0003\u0081"+
		"\b\u0003\n\u0003\f\u0003\u0084\t\u0003\u0001\u0003\u0001\u0003\u0001\u0003"+
		"\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0005\u0001\u0005"+
		"\u0001\u0006\u0001\u0006\u0001\u0007\u0001\u0007\u0001\u0007\u0001\b\u0001"+
		"\b\u0001\b\u0001\t\u0001\t\u0001\t\u0005\t\u009a\b\t\n\t\f\t\u009d\t\t"+
		"\u0001\n\u0001\n\u0001\n\u0005\n\u00a2\b\n\n\n\f\n\u00a5\t\n\u0001\u000b"+
		"\u0001\u000b\u0003\u000b\u00a9\b\u000b\u0001\f\u0005\f\u00ac\b\f\n\f\f"+
		"\f\u00af\t\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\r\u0005\r\u00b6\b\r"+
		"\n\r\f\r\u00b9\t\r\u0001\r\u0001\r\u0003\r\u00bd\b\r\u0001\r\u0001\r\u0001"+
		"\u000e\u0001\u000e\u0001\u000e\u0003\u000e\u00c4\b\u000e\u0001\u000f\u0001"+
		"\u000f\u0001\u000f\u0001\u0010\u0001\u0010\u0001\u0010\u0001\u0010\u0001"+
		"\u0010\u0003\u0010\u00ce\b\u0010\u0001\u0011\u0001\u0011\u0001\u0011\u0001"+
		"\u0011\u0001\u0012\u0005\u0012\u00d5\b\u0012\n\u0012\f\u0012\u00d8\t\u0012"+
		"\u0001\u0013\u0005\u0013\u00db\b\u0013\n\u0013\f\u0013\u00de\t\u0013\u0001"+
		"\u0014\u0005\u0014\u00e1\b\u0014\n\u0014\f\u0014\u00e4\t\u0014\u0001\u0015"+
		"\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0016\u0001\u0016\u0001\u0016"+
		"\u0003\u0016\u00ed\b\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0017"+
		"\u0001\u0017\u0001\u0017\u0003\u0017\u00f5\b\u0017\u0001\u0017\u0001\u0017"+
		"\u0001\u0017\u0001\u0017\u0003\u0017\u00fb\b\u0017\u0001\u0017\u0001\u0017"+
		"\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0004\u0017\u0103\b\u0017"+
		"\u000b\u0017\f\u0017\u0104\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017"+
		"\u0003\u0017\u010b\b\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017"+
		"\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0003\u0017\u0115\b\u0017"+
		"\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018"+
		"\u0003\u0018\u011d\b\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0003\u0018"+
		"\u0122\b\u0018\u0001\u0019\u0001\u0019\u0001\u0019\u0001\u0019\u0001\u0019"+
		"\u0001\u0019\u0001\u0019\u0003\u0019\u012b\b\u0019\u0001\u001a\u0001\u001a"+
		"\u0001\u001a\u0001\u001a\u0001\u001a\u0001\u001b\u0001\u001b\u0001\u001b"+
		"\u0003\u001b\u0135\b\u001b\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001d"+
		"\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001d"+
		"\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001e\u0001\u001e\u0001\u001e"+
		"\u0001\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0001\u001e"+
		"\u0001\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0001\u001e"+
		"\u0001\u001e\u0003\u001e\u0154\b\u001e\u0001\u001e\u0004\u001e\u0157\b"+
		"\u001e\u000b\u001e\f\u001e\u0158\u0001\u001e\u0001\u001e\u0001\u001e\u0001"+
		"\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0001"+
		"\u001e\u0001\u001e\u0001\u001e\u0003\u001e\u0167\b\u001e\u0001\u001f\u0001"+
		"\u001f\u0003\u001f\u016b\b\u001f\u0001 \u0001 \u0001 \u0001 \u0001 \u0001"+
		" \u0003 \u0173\b \u0001 \u0001 \u0001 \u0001 \u0001 \u0001 \u0003 \u017b"+
		"\b \u0001 \u0001 \u0001 \u0001 \u0001 \u0001 \u0001 \u0001 \u0003 \u0185"+
		"\b \u0001 \u0001 \u0001 \u0001 \u0003 \u018b\b \u0001 \u0001 \u0001 \u0001"+
		" \u0003 \u0191\b \u0001 \u0001 \u0003 \u0195\b \u0001!\u0001!\u0001!\u0001"+
		"!\u0001\"\u0001\"\u0001\"\u0001#\u0001#\u0001#\u0001#\u0003#\u01a2\b#"+
		"\u0001#\u0001#\u0001$\u0001$\u0001$\u0001$\u0001$\u0001$\u0001%\u0001"+
		"%\u0001%\u0001%\u0001%\u0001%\u0001%\u0001%\u0003%\u01b4\b%\u0001&\u0001"+
		"&\u0001&\u0001&\u0001&\u0001&\u0001&\u0001\'\u0001\'\u0001\'\u0001\'\u0001"+
		"(\u0001(\u0001(\u0005(\u01c4\b(\n(\f(\u01c7\t(\u0001)\u0001)\u0003)\u01cb"+
		"\b)\u0001*\u0001*\u0001*\u0001*\u0001*\u0001*\u0001*\u0001*\u0001*\u0001"+
		"*\u0001*\u0003*\u01d8\b*\u0001+\u0001+\u0001,\u0001,\u0001,\u0000\u0000"+
		"-\u0000\u0002\u0004\u0006\b\n\f\u000e\u0010\u0012\u0014\u0016\u0018\u001a"+
		"\u001c\u001e \"$&(*,.02468:<>@BDFHJLNPRTVX\u0000\u0007\u0001\u0000\u0001"+
		"\u0002\u0001\u0000WX\u0001\u0000]^\u0001\u0000\u0005\u0010\u0002\u0000"+
		"\u0011\u0012\u001d\u001d\u0002\u0000#%JZ\u0002\u0000,,..\u01f8\u0000Z"+
		"\u0001\u0000\u0000\u0000\u0002a\u0001\u0000\u0000\u0000\u0004d\u0001\u0000"+
		"\u0000\u0000\u0006m\u0001\u0000\u0000\u0000\b\u0088\u0001\u0000\u0000"+
		"\u0000\n\u008c\u0001\u0000\u0000\u0000\f\u008e\u0001\u0000\u0000\u0000"+
		"\u000e\u0090\u0001\u0000\u0000\u0000\u0010\u0093\u0001\u0000\u0000\u0000"+
		"\u0012\u0096\u0001\u0000\u0000\u0000\u0014\u009e\u0001\u0000\u0000\u0000"+
		"\u0016\u00a8\u0001\u0000\u0000\u0000\u0018\u00ad\u0001\u0000\u0000\u0000"+
		"\u001a\u00b7\u0001\u0000\u0000\u0000\u001c\u00c3\u0001\u0000\u0000\u0000"+
		"\u001e\u00c5\u0001\u0000\u0000\u0000 \u00cd\u0001\u0000\u0000\u0000\""+
		"\u00cf\u0001\u0000\u0000\u0000$\u00d6\u0001\u0000\u0000\u0000&\u00dc\u0001"+
		"\u0000\u0000\u0000(\u00e2\u0001\u0000\u0000\u0000*\u00e5\u0001\u0000\u0000"+
		"\u0000,\u00ec\u0001\u0000\u0000\u0000.\u0114\u0001\u0000\u0000\u00000"+
		"\u0121\u0001\u0000\u0000\u00002\u012a\u0001\u0000\u0000\u00004\u012c\u0001"+
		"\u0000\u0000\u00006\u0134\u0001\u0000\u0000\u00008\u0136\u0001\u0000\u0000"+
		"\u0000:\u0139\u0001\u0000\u0000\u0000<\u0166\u0001\u0000\u0000\u0000>"+
		"\u016a\u0001\u0000\u0000\u0000@\u0194\u0001\u0000\u0000\u0000B\u0196\u0001"+
		"\u0000\u0000\u0000D\u019a\u0001\u0000\u0000\u0000F\u019d\u0001\u0000\u0000"+
		"\u0000H\u01a5\u0001\u0000\u0000\u0000J\u01b3\u0001\u0000\u0000\u0000L"+
		"\u01b5\u0001\u0000\u0000\u0000N\u01bc\u0001\u0000\u0000\u0000P\u01c0\u0001"+
		"\u0000\u0000\u0000R\u01ca\u0001\u0000\u0000\u0000T\u01d7\u0001\u0000\u0000"+
		"\u0000V\u01d9\u0001\u0000\u0000\u0000X\u01db\u0001\u0000\u0000\u0000Z"+
		"[\u0007\u0000\u0000\u0000[\u0001\u0001\u0000\u0000\u0000\\b\u0005\u001c"+
		"\u0000\u0000]^\u0005\u001c\u0000\u0000^_\t\u0000\u0000\u0000_b\u0005_"+
		"\u0000\u0000`b\u0005_\u0000\u0000a\\\u0001\u0000\u0000\u0000a]\u0001\u0000"+
		"\u0000\u0000a`\u0001\u0000\u0000\u0000b\u0003\u0001\u0000\u0000\u0000"+
		"ce\u0007\u0001\u0000\u0000dc\u0001\u0000\u0000\u0000de\u0001\u0000\u0000"+
		"\u0000ef\u0001\u0000\u0000\u0000fh\u0007\u0002\u0000\u0000gi\u0005\u0003"+
		"\u0000\u0000hg\u0001\u0000\u0000\u0000hi\u0001\u0000\u0000\u0000i\u0005"+
		"\u0001\u0000\u0000\u0000jl\u0003\b\u0004\u0000kj\u0001\u0000\u0000\u0000"+
		"lo\u0001\u0000\u0000\u0000mk\u0001\u0000\u0000\u0000mn\u0001\u0000\u0000"+
		"\u0000ns\u0001\u0000\u0000\u0000om\u0001\u0000\u0000\u0000pr\u0003\n\u0005"+
		"\u0000qp\u0001\u0000\u0000\u0000ru\u0001\u0000\u0000\u0000sq\u0001\u0000"+
		"\u0000\u0000st\u0001\u0000\u0000\u0000tv\u0001\u0000\u0000\u0000us\u0001"+
		"\u0000\u0000\u0000vw\u0003\f\u0006\u0000wy\u0003\u0002\u0001\u0000xz\u0003"+
		"\u000e\u0007\u0000yx\u0001\u0000\u0000\u0000yz\u0001\u0000\u0000\u0000"+
		"z|\u0001\u0000\u0000\u0000{}\u0003\u0010\b\u0000|{\u0001\u0000\u0000\u0000"+
		"|}\u0001\u0000\u0000\u0000}~\u0001\u0000\u0000\u0000~\u0082\u0005?\u0000"+
		"\u0000\u007f\u0081\u0003\u0016\u000b\u0000\u0080\u007f\u0001\u0000\u0000"+
		"\u0000\u0081\u0084\u0001\u0000\u0000\u0000\u0082\u0080\u0001\u0000\u0000"+
		"\u0000\u0082\u0083\u0001\u0000\u0000\u0000\u0083\u0085\u0001\u0000\u0000"+
		"\u0000\u0084\u0082\u0001\u0000\u0000\u0000\u0085\u0086\u0005@\u0000\u0000"+
		"\u0086\u0087\u0005\u0000\u0000\u0001\u0087\u0007\u0001\u0000\u0000\u0000"+
		"\u0088\u0089\u0005\u0004\u0000\u0000\u0089\u008a\u0003\u0002\u0001\u0000"+
		"\u008a\u008b\u0005A\u0000\u0000\u008b\t\u0001\u0000\u0000\u0000\u008c"+
		"\u008d\u0007\u0003\u0000\u0000\u008d\u000b\u0001\u0000\u0000\u0000\u008e"+
		"\u008f\u0007\u0004\u0000\u0000\u008f\r\u0001\u0000\u0000\u0000\u0090\u0091"+
		"\u0005\u001e\u0000\u0000\u0091\u0092\u0003\u0002\u0001\u0000\u0092\u000f"+
		"\u0001\u0000\u0000\u0000\u0093\u0094\u0005\u001f\u0000\u0000\u0094\u0095"+
		"\u0003\u0014\n\u0000\u0095\u0011\u0001\u0000\u0000\u0000\u0096\u009b\u0003"+
		"\u0002\u0001\u0000\u0097\u0098\u0005B\u0000\u0000\u0098\u009a\u0005C\u0000"+
		"\u0000\u0099\u0097\u0001\u0000\u0000\u0000\u009a\u009d\u0001\u0000\u0000"+
		"\u0000\u009b\u0099\u0001\u0000\u0000\u0000\u009b\u009c\u0001\u0000\u0000"+
		"\u0000\u009c\u0013\u0001\u0000\u0000\u0000\u009d\u009b\u0001\u0000\u0000"+
		"\u0000\u009e\u00a3\u0003\u0012\t\u0000\u009f\u00a0\u0005>\u0000\u0000"+
		"\u00a0\u00a2\u0003\u0012\t\u0000\u00a1\u009f\u0001\u0000\u0000\u0000\u00a2"+
		"\u00a5\u0001\u0000\u0000\u0000\u00a3\u00a1\u0001\u0000\u0000\u0000\u00a3"+
		"\u00a4\u0001\u0000\u0000\u0000\u00a4\u0015\u0001\u0000\u0000\u0000\u00a5"+
		"\u00a3\u0001\u0000\u0000\u0000\u00a6\u00a9\u0003\u0018\f\u0000\u00a7\u00a9"+
		"\u0003\u001a\r\u0000\u00a8\u00a6\u0001\u0000\u0000\u0000\u00a8\u00a7\u0001"+
		"\u0000\u0000\u0000\u00a9\u0017\u0001\u0000\u0000\u0000\u00aa\u00ac\u0003"+
		"\n\u0005\u0000\u00ab\u00aa\u0001\u0000\u0000\u0000\u00ac\u00af\u0001\u0000"+
		"\u0000\u0000\u00ad\u00ab\u0001\u0000\u0000\u0000\u00ad\u00ae\u0001\u0000"+
		"\u0000\u0000\u00ae\u00b0\u0001\u0000\u0000\u0000\u00af\u00ad\u0001\u0000"+
		"\u0000\u0000\u00b0\u00b1\u0003\u0012\t\u0000\u00b1\u00b2\u0003\u0002\u0001"+
		"\u0000\u00b2\u00b3\u0005A\u0000\u0000\u00b3\u0019\u0001\u0000\u0000\u0000"+
		"\u00b4\u00b6\u0003\n\u0005\u0000\u00b5\u00b4\u0001\u0000\u0000\u0000\u00b6"+
		"\u00b9\u0001\u0000\u0000\u0000\u00b7\u00b5\u0001\u0000\u0000\u0000\u00b7"+
		"\u00b8\u0001\u0000\u0000\u0000\u00b8\u00ba\u0001\u0000\u0000\u0000\u00b9"+
		"\u00b7\u0001\u0000\u0000\u0000\u00ba\u00bc\u0003F#\u0000\u00bb\u00bd\u0003"+
		"\u001e\u000f\u0000\u00bc\u00bb\u0001\u0000\u0000\u0000\u00bc\u00bd\u0001"+
		"\u0000\u0000\u0000\u00bd\u00be\u0001\u0000\u0000\u0000\u00be\u00bf\u0003"+
		" \u0010\u0000\u00bf\u001b\u0001\u0000\u0000\u0000\u00c0\u00c4\u0005\u0013"+
		"\u0000\u0000\u00c1\u00c4\u0005\u0014\u0000\u0000\u00c2\u00c4\u0003\u0002"+
		"\u0001\u0000\u00c3\u00c0\u0001\u0000\u0000\u0000\u00c3\u00c1\u0001\u0000"+
		"\u0000\u0000\u00c3\u00c2\u0001\u0000\u0000\u0000\u00c4\u001d\u0001\u0000"+
		"\u0000\u0000\u00c5\u00c6\u00058\u0000\u0000\u00c6\u00c7\u0003\u0014\n"+
		"\u0000\u00c7\u001f\u0001\u0000\u0000\u0000\u00c8\u00ce\u0005A\u0000\u0000"+
		"\u00c9\u00ca\u0005?\u0000\u0000\u00ca\u00cb\u0003\"\u0011\u0000\u00cb"+
		"\u00cc\u0005@\u0000\u0000\u00cc\u00ce\u0001\u0000\u0000\u0000\u00cd\u00c8"+
		"\u0001\u0000\u0000\u0000\u00cd\u00c9\u0001\u0000\u0000\u0000\u00ce!\u0001"+
		"\u0000\u0000\u0000\u00cf\u00d0\u0003(\u0014\u0000\u00d0\u00d1\u0003&\u0013"+
		"\u0000\u00d1\u00d2\u0003$\u0012\u0000\u00d2#\u0001\u0000\u0000\u0000\u00d3"+
		"\u00d5\u0003:\u001d\u0000\u00d4\u00d3\u0001\u0000\u0000\u0000\u00d5\u00d8"+
		"\u0001\u0000\u0000\u0000\u00d6\u00d4\u0001\u0000\u0000\u0000\u00d6\u00d7"+
		"\u0001\u0000\u0000\u0000\u00d7%\u0001\u0000\u0000\u0000\u00d8\u00d6\u0001"+
		"\u0000\u0000\u0000\u00d9\u00db\u0003,\u0016\u0000\u00da\u00d9\u0001\u0000"+
		"\u0000\u0000\u00db\u00de\u0001\u0000\u0000\u0000\u00dc\u00da\u0001\u0000"+
		"\u0000\u0000\u00dc\u00dd\u0001\u0000\u0000\u0000\u00dd\'\u0001\u0000\u0000"+
		"\u0000\u00de\u00dc\u0001\u0000\u0000\u0000\u00df\u00e1\u0003*\u0015\u0000"+
		"\u00e0\u00df\u0001\u0000\u0000\u0000\u00e1\u00e4\u0001\u0000\u0000\u0000"+
		"\u00e2\u00e0\u0001\u0000\u0000\u0000\u00e2\u00e3\u0001\u0000\u0000\u0000"+
		"\u00e3)\u0001\u0000\u0000\u0000\u00e4\u00e2\u0001\u0000\u0000\u0000\u00e5"+
		"\u00e6\u0003\u0012\t\u0000\u00e6\u00e7\u0003P(\u0000\u00e7\u00e8\u0005"+
		"A\u0000\u0000\u00e8+\u0001\u0000\u0000\u0000\u00e9\u00ea\u0003\u0002\u0001"+
		"\u0000\u00ea\u00eb\u0005F\u0000\u0000\u00eb\u00ed\u0001\u0000\u0000\u0000"+
		"\u00ec\u00e9\u0001\u0000\u0000\u0000\u00ec\u00ed\u0001\u0000\u0000\u0000"+
		"\u00ed\u00ee\u0001\u0000\u0000\u0000\u00ee\u00ef\u0003.\u0017\u0000\u00ef"+
		"\u00f0\u0005A\u0000\u0000\u00f0-\u0001\u0000\u0000\u0000\u00f1\u0115\u0003"+
		"0\u0018\u0000\u00f2\u00f3\u0005*\u0000\u0000\u00f3\u00f5\u0003>\u001f"+
		"\u0000\u00f4\u00f2\u0001\u0000\u0000\u0000\u00f4\u00f5\u0001\u0000\u0000"+
		"\u0000\u00f5\u00f6\u0001\u0000\u0000\u0000\u00f6\u0115\u00038\u001c\u0000"+
		"\u00f7\u0115\u0003@ \u0000\u00f8\u00fa\u00053\u0000\u0000\u00f9\u00fb"+
		"\u0003R)\u0000\u00fa\u00f9\u0001\u0000\u0000\u0000\u00fa\u00fb\u0001\u0000"+
		"\u0000\u0000\u00fb\u0115\u0001\u0000\u0000\u0000\u00fc\u00fd\u0005-\u0000"+
		"\u0000\u00fd\u00fe\u0005D\u0000\u0000\u00fe\u00ff\u0003R)\u0000\u00ff"+
		"\u0100\u0005E\u0000\u0000\u0100\u0102\u0005?\u0000\u0000\u0101\u0103\u0003"+
		"4\u001a\u0000\u0102\u0101\u0001\u0000\u0000\u0000\u0103\u0104\u0001\u0000"+
		"\u0000\u0000\u0104\u0102\u0001\u0000\u0000\u0000\u0104\u0105\u0001\u0000"+
		"\u0000\u0000\u0105\u0106\u0001\u0000\u0000\u0000\u0106\u0107\u0005@\u0000"+
		"\u0000\u0107\u0115\u0001\u0000\u0000\u0000\u0108\u010a\u00054\u0000\u0000"+
		"\u0109\u010b\u0003R)\u0000\u010a\u0109\u0001\u0000\u0000\u0000\u010a\u010b"+
		"\u0001\u0000\u0000\u0000\u010b\u0115\u0001\u0000\u0000\u0000\u010c\u010d"+
		"\u00059\u0000\u0000\u010d\u0115\u0003R)\u0000\u010e\u010f\u0005\'\u0000"+
		"\u0000\u010f\u0115\u0003R)\u0000\u0110\u0111\u0005(\u0000\u0000\u0111"+
		"\u0115\u0003R)\u0000\u0112\u0115\u00052\u0000\u0000\u0113\u0115\u0005"+
		" \u0000\u0000\u0114\u00f1\u0001\u0000\u0000\u0000\u0114\u00f4\u0001\u0000"+
		"\u0000\u0000\u0114\u00f7\u0001\u0000\u0000\u0000\u0114\u00f8\u0001\u0000"+
		"\u0000\u0000\u0114\u00fc\u0001\u0000\u0000\u0000\u0114\u0108\u0001\u0000"+
		"\u0000\u0000\u0114\u010c\u0001\u0000\u0000\u0000\u0114\u010e\u0001\u0000"+
		"\u0000\u0000\u0114\u0110\u0001\u0000\u0000\u0000\u0114\u0112\u0001\u0000"+
		"\u0000\u0000\u0114\u0113\u0001\u0000\u0000\u0000\u0115/\u0001\u0000\u0000"+
		"\u0000\u0116\u0117\u0003\u0002\u0001\u0000\u0117\u0118\u0005I\u0000\u0000"+
		"\u0118\u0119\u00032\u0019\u0000\u0119\u0122\u0001\u0000\u0000\u0000\u011a"+
		"\u011d\u0003J%\u0000\u011b\u011d\u0003\u0002\u0001\u0000\u011c\u011a\u0001"+
		"\u0000\u0000\u0000\u011c\u011b\u0001\u0000\u0000\u0000\u011d\u011e\u0001"+
		"\u0000\u0000\u0000\u011e\u011f\u0005H\u0000\u0000\u011f\u0120\u0003<\u001e"+
		"\u0000\u0120\u0122\u0001\u0000\u0000\u0000\u0121\u0116\u0001\u0000\u0000"+
		"\u0000\u0121\u011c\u0001\u0000\u0000\u0000\u01221\u0001\u0000\u0000\u0000"+
		"\u0123\u0124\u0005\u0015\u0000\u0000\u0124\u0125\u0005]\u0000\u0000\u0125"+
		"\u0126\u0005F\u0000\u0000\u0126\u012b\u0003\u0012\t\u0000\u0127\u0128"+
		"\u0005\u0016\u0000\u0000\u0128\u012b\u0003\u0012\t\u0000\u0129\u012b\u0005"+
		"\u0017\u0000\u0000\u012a\u0123\u0001\u0000\u0000\u0000\u012a\u0127\u0001"+
		"\u0000\u0000\u0000\u012a\u0129\u0001\u0000\u0000\u0000\u012b3\u0001\u0000"+
		"\u0000\u0000\u012c\u012d\u00036\u001b\u0000\u012d\u012e\u0005F\u0000\u0000"+
		"\u012e\u012f\u00038\u001c\u0000\u012f\u0130\u0005A\u0000\u0000\u01305"+
		"\u0001\u0000\u0000\u0000\u0131\u0132\u0005!\u0000\u0000\u0132\u0135\u0003"+
		"\u0004\u0002\u0000\u0133\u0135\u0005&\u0000\u0000\u0134\u0131\u0001\u0000"+
		"\u0000\u0000\u0134\u0133\u0001\u0000\u0000\u0000\u01357\u0001\u0000\u0000"+
		"\u0000\u0136\u0137\u0005)\u0000\u0000\u0137\u0138\u0003\u0002\u0001\u0000"+
		"\u01389\u0001\u0000\u0000\u0000\u0139\u013a\u0005\"\u0000\u0000\u013a"+
		"\u013b\u0003\u0002\u0001\u0000\u013b\u013c\u0005;\u0000\u0000\u013c\u013d"+
		"\u0003\u0002\u0001\u0000\u013d\u013e\u0005<\u0000\u0000\u013e\u013f\u0003"+
		"\u0002\u0001\u0000\u013f\u0140\u0005=\u0000\u0000\u0140\u0141\u0003\u0002"+
		"\u0001\u0000\u0141\u0142\u0005A\u0000\u0000\u0142;\u0001\u0000\u0000\u0000"+
		"\u0143\u0167\u0003R)\u0000\u0144\u0167\u0003J%\u0000\u0145\u0146\u0005"+
		"1\u0000\u0000\u0146\u0167\u0003\u0002\u0001\u0000\u0147\u0148\u0005/\u0000"+
		"\u0000\u0148\u0149\u0005D\u0000\u0000\u0149\u014a\u0003\u0012\t\u0000"+
		"\u014a\u014b\u0005E\u0000\u0000\u014b\u014c\u0003N\'\u0000\u014c\u0167"+
		"\u0001\u0000\u0000\u0000\u014d\u014e\u00050\u0000\u0000\u014e\u014f\u0005"+
		"D\u0000\u0000\u014f\u0150\u0003\u0012\t\u0000\u0150\u0156\u0005E\u0000"+
		"\u0000\u0151\u0153\u0005B\u0000\u0000\u0152\u0154\u0003R)\u0000\u0153"+
		"\u0152\u0001\u0000\u0000\u0000\u0153\u0154\u0001\u0000\u0000\u0000\u0154"+
		"\u0155\u0001\u0000\u0000\u0000\u0155\u0157\u0005C\u0000\u0000\u0156\u0151"+
		"\u0001\u0000\u0000\u0000\u0157\u0158\u0001\u0000\u0000\u0000\u0158\u0156"+
		"\u0001\u0000\u0000\u0000\u0158\u0159\u0001\u0000\u0000\u0000\u0159\u0167"+
		"\u0001\u0000\u0000\u0000\u015a\u015b\u0005D\u0000\u0000\u015b\u015c\u0003"+
		"\u0012\t\u0000\u015c\u015d\u0005E\u0000\u0000\u015d\u015e\u0003R)\u0000"+
		"\u015e\u0167\u0001\u0000\u0000\u0000\u015f\u0160\u0003R)\u0000\u0160\u0161"+
		"\u0005+\u0000\u0000\u0161\u0162\u0003\u0012\t\u0000\u0162\u0167\u0001"+
		"\u0000\u0000\u0000\u0163\u0167\u0003B!\u0000\u0164\u0167\u0003@ \u0000"+
		"\u0165\u0167\u0003D\"\u0000\u0166\u0143\u0001\u0000\u0000\u0000\u0166"+
		"\u0144\u0001\u0000\u0000\u0000\u0166\u0145\u0001\u0000\u0000\u0000\u0166"+
		"\u0147\u0001\u0000\u0000\u0000\u0166\u014d\u0001\u0000\u0000\u0000\u0166"+
		"\u015a\u0001\u0000\u0000\u0000\u0166\u015f\u0001\u0000\u0000\u0000\u0166"+
		"\u0163\u0001\u0000\u0000\u0000\u0166\u0164\u0001\u0000\u0000\u0000\u0166"+
		"\u0165\u0001\u0000\u0000\u0000\u0167=\u0001\u0000\u0000\u0000\u0168\u016b"+
		"\u0003B!\u0000\u0169\u016b\u0003D\"\u0000\u016a\u0168\u0001\u0000\u0000"+
		"\u0000\u016a\u0169\u0001\u0000\u0000\u0000\u016b?\u0001\u0000\u0000\u0000"+
		"\u016c\u016d\u00055\u0000\u0000\u016d\u016e\u0003\u0002\u0001\u0000\u016e"+
		"\u016f\u0005G\u0000\u0000\u016f\u0170\u0003H$\u0000\u0170\u0172\u0005"+
		"D\u0000\u0000\u0171\u0173\u0003P(\u0000\u0172\u0171\u0001\u0000\u0000"+
		"\u0000\u0172\u0173\u0001\u0000\u0000\u0000\u0173\u0174\u0001\u0000\u0000"+
		"\u0000\u0174\u0175\u0005E\u0000\u0000\u0175\u0195\u0001\u0000\u0000\u0000"+
		"\u0176\u0177\u00056\u0000\u0000\u0177\u0178\u0003H$\u0000\u0178\u017a"+
		"\u0005D\u0000\u0000\u0179\u017b\u0003P(\u0000\u017a\u0179\u0001\u0000"+
		"\u0000\u0000\u017a\u017b\u0001\u0000\u0000\u0000\u017b\u017c\u0001\u0000"+
		"\u0000\u0000\u017c\u017d\u0005E\u0000\u0000\u017d\u0195\u0001\u0000\u0000"+
		"\u0000\u017e\u017f\u00057\u0000\u0000\u017f\u0180\u0003\u0002\u0001\u0000"+
		"\u0180\u0181\u0005R\u0000\u0000\u0181\u0182\u0003\u0012\t\u0000\u0182"+
		"\u0184\u0005D\u0000\u0000\u0183\u0185\u0003\u0014\n\u0000\u0184\u0183"+
		"\u0001\u0000\u0000\u0000\u0184\u0185\u0001\u0000\u0000\u0000\u0185\u0186"+
		"\u0001\u0000\u0000\u0000\u0186\u0187\u0005E\u0000\u0000\u0187\u0188\u0005"+
		"P\u0000\u0000\u0188\u018a\u0005D\u0000\u0000\u0189\u018b\u0003P(\u0000"+
		"\u018a\u0189\u0001\u0000\u0000\u0000\u018a\u018b\u0001\u0000\u0000\u0000"+
		"\u018b\u018c\u0001\u0000\u0000\u0000\u018c\u018d\u0005E\u0000\u0000\u018d"+
		"\u018e\u0003H$\u0000\u018e\u0190\u0005D\u0000\u0000\u018f\u0191\u0003"+
		"P(\u0000\u0190\u018f\u0001\u0000\u0000\u0000\u0190\u0191\u0001\u0000\u0000"+
		"\u0000\u0191\u0192\u0001\u0000\u0000\u0000\u0192\u0193\u0005E\u0000\u0000"+
		"\u0193\u0195\u0001\u0000\u0000\u0000\u0194\u016c\u0001\u0000\u0000\u0000"+
		"\u0194\u0176\u0001\u0000\u0000\u0000\u0194\u017e\u0001\u0000\u0000\u0000"+
		"\u0195A\u0001\u0000\u0000\u0000\u0196\u0197\u0003R)\u0000\u0197\u0198"+
		"\u0003V+\u0000\u0198\u0199\u0003R)\u0000\u0199C\u0001\u0000\u0000\u0000"+
		"\u019a\u019b\u0003X,\u0000\u019b\u019c\u0003R)\u0000\u019cE\u0001\u0000"+
		"\u0000\u0000\u019d\u019e\u0003\u0012\t\u0000\u019e\u019f\u0003\u001c\u000e"+
		"\u0000\u019f\u01a1\u0005D\u0000\u0000\u01a0\u01a2\u0003\u0014\n\u0000"+
		"\u01a1\u01a0\u0001\u0000\u0000\u0000\u01a1\u01a2\u0001\u0000\u0000\u0000"+
		"\u01a2\u01a3\u0001\u0000\u0000\u0000\u01a3\u01a4\u0005E\u0000\u0000\u01a4"+
		"G\u0001\u0000\u0000\u0000\u01a5\u01a6\u0005R\u0000\u0000\u01a6\u01a7\u0003"+
		"\u0002\u0001\u0000\u01a7\u01a8\u0005F\u0000\u0000\u01a8\u01a9\u0003F#"+
		"\u0000\u01a9\u01aa\u0005P\u0000\u0000\u01aaI\u0001\u0000\u0000\u0000\u01ab"+
		"\u01ac\u0003\u0002\u0001\u0000\u01ac\u01ad\u0003N\'\u0000\u01ad\u01b4"+
		"\u0001\u0000\u0000\u0000\u01ae\u01af\u0003\u0002\u0001\u0000\u01af\u01b0"+
		"\u0005G\u0000\u0000\u01b0\u01b1\u0003L&\u0000\u01b1\u01b4\u0001\u0000"+
		"\u0000\u0000\u01b2\u01b4\u0003L&\u0000\u01b3\u01ab\u0001\u0000\u0000\u0000"+
		"\u01b3\u01ae\u0001\u0000\u0000\u0000\u01b3\u01b2\u0001\u0000\u0000\u0000"+
		"\u01b4K\u0001\u0000\u0000\u0000\u01b5\u01b6\u0005R\u0000\u0000\u01b6\u01b7"+
		"\u0003\u0002\u0001\u0000\u01b7\u01b8\u0005F\u0000\u0000\u01b8\u01b9\u0003"+
		"\u0012\t\u0000\u01b9\u01ba\u0003\u0002\u0001\u0000\u01ba\u01bb\u0005P"+
		"\u0000\u0000\u01bbM\u0001\u0000\u0000\u0000\u01bc\u01bd\u0005B\u0000\u0000"+
		"\u01bd\u01be\u0003R)\u0000\u01be\u01bf\u0005C\u0000\u0000\u01bfO\u0001"+
		"\u0000\u0000\u0000\u01c0\u01c5\u0003R)\u0000\u01c1\u01c2\u0005>\u0000"+
		"\u0000\u01c2\u01c4\u0003R)\u0000\u01c3\u01c1\u0001\u0000\u0000\u0000\u01c4"+
		"\u01c7\u0001\u0000\u0000\u0000\u01c5\u01c3\u0001\u0000\u0000\u0000\u01c5"+
		"\u01c6\u0001\u0000\u0000\u0000\u01c6Q\u0001\u0000\u0000\u0000\u01c7\u01c5"+
		"\u0001\u0000\u0000\u0000\u01c8\u01cb\u0003\u0002\u0001\u0000\u01c9\u01cb"+
		"\u0003T*\u0000\u01ca\u01c8\u0001\u0000\u0000\u0000\u01ca\u01c9\u0001\u0000"+
		"\u0000\u0000\u01cbS\u0001\u0000\u0000\u0000\u01cc\u01d8\u0005[\u0000\u0000"+
		"\u01cd\u01d8\u0003\u0004\u0002\u0000\u01ce\u01d8\u0005\\\u0000\u0000\u01cf"+
		"\u01d8\u0005\u001c\u0000\u0000\u01d0\u01d1\u0005\u001d\u0000\u0000\u01d1"+
		"\u01d8\u0003\u0002\u0001\u0000\u01d2\u01d8\u0005:\u0000\u0000\u01d3\u01d4"+
		"\u0005\u0018\u0000\u0000\u01d4\u01d8\u0003H$\u0000\u01d5\u01d6\u0005\u0019"+
		"\u0000\u0000\u01d6\u01d8\u0003F#\u0000\u01d7\u01cc\u0001\u0000\u0000\u0000"+
		"\u01d7\u01cd\u0001\u0000\u0000\u0000\u01d7\u01ce\u0001\u0000\u0000\u0000"+
		"\u01d7\u01cf\u0001\u0000\u0000\u0000\u01d7\u01d0\u0001\u0000\u0000\u0000"+
		"\u01d7\u01d2\u0001\u0000\u0000\u0000\u01d7\u01d3\u0001\u0000\u0000\u0000"+
		"\u01d7\u01d5\u0001\u0000\u0000\u0000\u01d8U\u0001\u0000\u0000\u0000\u01d9"+
		"\u01da\u0007\u0005\u0000\u0000\u01daW\u0001\u0000\u0000\u0000\u01db\u01dc"+
		"\u0007\u0006\u0000\u0000\u01dcY\u0001\u0000\u0000\u0000,adhmsy|\u0082"+
		"\u009b\u00a3\u00a8\u00ad\u00b7\u00bc\u00c3\u00cd\u00d6\u00dc\u00e2\u00ec"+
		"\u00f4\u00fa\u0104\u010a\u0114\u011c\u0121\u012a\u0134\u0153\u0158\u0166"+
		"\u016a\u0172\u017a\u0184\u018a\u0190\u0194\u01a1\u01b3\u01c5\u01ca\u01d7";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}