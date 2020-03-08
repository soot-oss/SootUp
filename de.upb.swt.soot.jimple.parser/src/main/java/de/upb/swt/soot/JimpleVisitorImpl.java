package de.upb.swt.soot;

import de.upb.swt.soot.core.IdentifierFactory;
import de.upb.swt.soot.core.frontend.OverridingClassSource;
import de.upb.swt.soot.core.frontend.OverridingMethodSource;
import de.upb.swt.soot.core.frontend.SootClassSource;
import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.jimple.Jimple;
import de.upb.swt.soot.core.jimple.basic.*;
import de.upb.swt.soot.core.jimple.common.constant.*;
import de.upb.swt.soot.core.jimple.common.expr.*;
import de.upb.swt.soot.core.jimple.common.stmt.JGotoStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.*;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.signatures.PackageName;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import de.upb.swt.soot.java.core.language.JavaJimple;
import de.upb.swt.soot.jimple.JimpleBaseVisitor;
import de.upb.swt.soot.jimple.JimpleLexer;
import de.upb.swt.soot.jimple.JimpleParser;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.antlr.v4.runtime.*;

class JimpleVisitorImpl {

  static final IdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();
  private static Map<String, PackageName> imports = new HashMap<>();
  private static ClassType clazz = null;
  private static HashMap<String, Stmt> unresolvedGotoStmts = new HashMap<>();
  private static HashMap<String, Stmt> jumpTargets = new HashMap<>();
  private static String addLabel = null;

  private static Type getType(String typename) {
    PackageName packageName = imports.get(typename);
    Type type =
        packageName == null
            ? identifierFactory.getType(typename)
            : identifierFactory.getClassType(typename, packageName.getPackageName());
    return type;
  }

  private static ClassType getClassType(String typename) {
    PackageName packageName = imports.get(typename);
    ClassType type =
        packageName == null
            ? identifierFactory.getClassType(typename)
            : identifierFactory.getClassType(typename, packageName.getPackageName());
    return type;
  }

  public SootClassSource parse(CharStream charStream) {
    JimpleLexer lexer = new JimpleLexer(charStream);
    TokenStream tokens = new CommonTokenStream(lexer);
    JimpleParser parser = new JimpleParser(tokens);

    ClassVisitor classVisitor = new ClassVisitor();
    SootClassSource traverseResult = classVisitor.visit(parser.file());
    return traverseResult;
  }

  private static class ClassVisitor extends JimpleBaseVisitor<SootClassSource> {

    @Override
    @Nonnull
    public SootClassSource visitFile(@Nonnull JimpleParser.FileContext ctx) {

      Set<SootField> fields = new HashSet<>();
      Set<SootMethod> methods = new HashSet<>();
      ClassType superclass;
      Set<ClassType> interfaces;
      // FIXME implement outerclass
      ClassType outerclass = null;

      // position
      Position position =
          new Position(
              ctx.start.getLine(), ctx.start.getCharPositionInLine(), ctx.stop.getLine(), -1);

      // imports
      imports =
          ctx.importItem().stream()
              .filter(item -> item.location != null)
              .map(item -> identifierFactory.getClassType(item.location.getText()))
              .collect(
                  Collectors.toMap(
                      e -> e.getClassName(),
                      e -> e.getPackageName(),
                      (a, b) -> {
                        if (!a.equals(b)) {
                          throw new IllegalStateException(
                              "multiple imports for the same ClassName can not be resolved!");
                        }
                        return b;
                      }));

      EnumSet<Modifier> modifier = getModifiers(ctx.modifier());

      // file_type
      if (ctx.file_type() != null) {
        if (ctx.file_type().getText().equals("interface")) {
          modifier.add(Modifier.INTERFACE);
        }
        if (ctx.file_type().getText().equals("annotation")) {
          modifier.add(Modifier.ANNOTATION);
        }
      }

      // class_name
      if (ctx.classname != null) {
        final String classname = ctx.classname.getText();
        clazz = getClassType(classname);
      }

      // extends_clause
      if (ctx.extends_clause() != null) {
        superclass = getClassType(ctx.extends_clause().classname.getText());
      } else {
        // TODO:
        superclass = null;
      }

      // implements_clause
      if (ctx.implements_clause() != null) {
        interfaces = (Set<ClassType>) ctx.implements_clause().accept(new NameListVisitor());
      } else {
        interfaces = Collections.emptySet();
      }

      // member
      for (int i = 0; i < ctx.member().size(); i++) {
        SootClassMember scm = ctx.member(i).accept(new MemberVisitor());
        if (scm instanceof SootMethod) {
          methods.add((SootMethod) scm);
        } else {
          fields.add((SootField) scm);
        }
      }

      // FIXME
      AnalysisInputLocation inputlocation = null;
      // FIXME
      Path path = Paths.get("abra.kadabra");

      return new OverridingClassSource(
          inputlocation,
          path,
          clazz,
          superclass,
          interfaces,
          outerclass,
          fields,
          methods,
          position,
          modifier);
    }
  }

  private static class NameListVisitor extends JimpleBaseVisitor<Collection<ClassType>> {

    public List<ClassType> visitThrows_clause(JimpleParser.Throws_clauseContext ctx) {
      List<ClassType> list = new ArrayList();
      iterate(list, ctx.name_list());
      return list;
    }

    @Override
    public Set<ClassType> visitImplements_clause(JimpleParser.Implements_clauseContext ctx) {
      Set<ClassType> interfaces = new HashSet();
      iterate(interfaces, ctx.name_list());
      return interfaces;
    }

    public Collection<ClassType> iterate(
        Collection<ClassType> list, JimpleParser.Name_listContext ctx) {
      JimpleParser.Name_listContext name_listContextIterator = ctx.name_list();

      while (name_listContextIterator != null) {
        if (name_listContextIterator.name() == null) {
          break;
        }
        list.add(identifierFactory.getClassType(name_listContextIterator.name().getText()));
        name_listContextIterator = name_listContextIterator.name_list();
      }
      return list;
    }
  }

  private static class MemberVisitor extends JimpleBaseVisitor<SootClassMember> {
    @Override
    public SootClassMember visitMember(JimpleParser.MemberContext ctx) {
      SootClassMember member = null;
      if (ctx.method() != null) {
        member = ctx.accept(new MethodVisitor());
      }
      if (ctx.field() != null) {
        member = ctx.accept(new FieldVisitor());
      }
      assert (member != null);
      return member;
    }
  }

  private static class FieldVisitor extends JimpleBaseVisitor<SootField> {

    @Override
    public SootField visitField(JimpleParser.FieldContext ctx) {

      EnumSet<Modifier> modifier = getModifiers(ctx.modifier());

      return new SootField(
          identifierFactory.getFieldSignature(ctx.name().getText(), clazz, ctx.type().getText()),
          modifier);
    }
  }

  private static EnumSet<Modifier> getModifiers(List<JimpleParser.ModifierContext> modifier) {
    Set<Modifier> modifierSet =
        modifier.stream()
            .map(modifierContext -> Modifier.valueOf(modifierContext.getText().toUpperCase()))
            .collect(Collectors.toSet());
    return modifierSet.isEmpty() ? EnumSet.noneOf(Modifier.class) : EnumSet.copyOf(modifierSet);
  }

  private static class MethodVisitor extends JimpleBaseVisitor<SootMethod> {

    @Override
    @Nonnull
    public SootMethod visitMethod(@Nonnull JimpleParser.MethodContext ctx) {
      StmtVisitor stmtVisitor = new StmtVisitor();
      List<Stmt> stmts =
          ctx.method_body().statement().stream()
              .map(instruction -> instruction.accept(stmtVisitor))
              .collect(Collectors.toList());

      // TODO
      Set<Local> locals = new HashSet<>();

      // TODO
      List<Trap> traps = new ArrayList<>();

      Position position =
          new Position(
              ctx.start.getLine(),
              ctx.start.getCharPositionInLine(),
              ctx.stop.getLine(),
              ctx.stop.getCharPositionInLine());

      Body b = new Body(locals, traps, stmts, position);
      List<Type> params =
          ctx.parameter_list() == null
              ? Collections.emptyList()
              : ctx.parameter_list().accept(new ParameterListVisitor());
      MethodSignature methodSignature =
          identifierFactory.getMethodSignature(
              ctx.name().getText(), clazz, getType(ctx.type().getText()), params);

      OverridingMethodSource oms = new OverridingMethodSource(methodSignature, b);

      List<ClassType> exceptions =
          ctx.throws_clause() == null
              ? Collections.emptyList()
              : (List<ClassType>) ctx.throws_clause().accept(new NameListVisitor());

      EnumSet<Modifier> modifier = getModifiers(ctx.modifier());

      // TODO: associate labels with goto boxes
      for (Map.Entry<String, Stmt> item : unresolvedGotoStmts.entrySet()) {
        final Stmt stmt = jumpTargets.get(item.getKey());
        if (stmt != null) {

          final Stmt value = item.getValue();
          if (value instanceof JGotoStmt) {
            JGotoStmt.$Accessor.setTarget((JGotoStmt) value, stmt);
          }

        } else {
          // TODO: choose a better Exception
          throw new RuntimeException(
              "dont jump into the space. target Stmt not found i.e. no label: " + item.getKey());
        }
      }

      return new SootMethod(oms, methodSignature, modifier, exceptions);
    }
  }

  private static class ParameterListVisitor extends JimpleBaseVisitor<List<Type>> {
    @Override
    public List<Type> visitParameter_list(JimpleParser.Parameter_listContext ctx) {
      List<Type> interfaces = new ArrayList<>();
      JimpleParser.Parameter_listContext class_name_listContextIterator = ctx;
      do {
        interfaces.add(
            identifierFactory.getClassType(class_name_listContextIterator.parameter.getText()));
        class_name_listContextIterator = ctx.parameter_list();
      } while (class_name_listContextIterator != null);

      return interfaces;
    }
  }

  private static class StmtVisitor extends JimpleBaseVisitor<Stmt> {

    @Override
    public Stmt visitStatement(JimpleParser.StatementContext ctx) {
      Stmt stmt = ctx.stmt().accept(new StmtVisitor());
      if (ctx.label_name() != null) {
        jumpTargets.put(ctx.label_name().getText(), stmt);
      }
      return stmt;
    }

    @Override
    @Nonnull
    public Stmt visitStmt(JimpleParser.StmtContext ctx) {

      StmtPositionInfo pos = new StmtPositionInfo(ctx.start.getLine());

      if (ctx.BREAKPOINT() != null) {
        return Jimple.newBreakpointStmt(pos);
      } else if (ctx.ENTERMONITOR() != null) {
        return Jimple.newEnterMonitorStmt(ctx.immediate().accept(new ValueVisitor()), pos);
      } else if (ctx.EXITMONITOR() != null) {
        return Jimple.newExitMonitorStmt(ctx.immediate().accept(new ValueVisitor()), pos);
      } else if (ctx.SWITCH() != null) {
        if (ctx.SWITCH().getText().charAt(0) == 't') {
          //        return Jimple.newTableSwitchStmt();
        } else {
          //          return Jimple.newLookupSwitchStmt();
        }
      } else if (ctx.assignments() != null) {
        if (ctx.assignments().EQUALS() == null) {
          if (ctx.assignments().type() != null) {
            // TODO: WHAT :D
            ctx.assignments().type();
            //    return Jimple.newIdentityStmt( ctx.assignments().local_name().accept(new
            // ValueVisitor()), , pos);
          } else {
            ctx.assignments().local.accept(new ValueVisitor());
            //     return Jimple.newIdentityStmt(,,pos);
          }
        } else {
          return Jimple.newAssignStmt(
              ctx.assignments().variable().accept(new ValueVisitor()),
              ctx.assignments().expression().accept(new ValueVisitor()),
              pos);
        }

      } else if (ctx.IF() != null) {
        JStmtBox target = (JStmtBox) Jimple.newStmtBox(null);
        final Stmt stmt = Jimple.newIfStmt(ctx.bool_expr().accept(new ValueVisitor()), target, pos);
        unresolvedGotoStmts.put(ctx.goto_stmt().label_name().getText(), stmt);
        return stmt;
      } else if (ctx.goto_stmt() != null) {
        JStmtBox target = (JStmtBox) Jimple.newStmtBox(null);
        final Stmt stmt = Jimple.newGotoStmt(target, pos);
        unresolvedGotoStmts.put(ctx.goto_stmt().label_name().getText(), stmt);
        return stmt;
      } else if (ctx.NOP() != null) {
        return Jimple.newNopStmt(pos);
      } else if (ctx.RET() != null) {
        return Jimple.newRetStmt(ctx.immediate().accept(new ValueVisitor()), pos);
      } else if (ctx.RETURN() != null) {
        if (ctx.immediate() == null) {
          return Jimple.newReturnVoidStmt(pos);
        } else {
          return Jimple.newReturnStmt(ctx.immediate().accept(new ValueVisitor()), pos);
        }
      } else if (ctx.THROW() != null) {
        return Jimple.newThrowStmt(ctx.immediate().accept(new ValueVisitor()), pos);
      } else if (ctx.invoke_expr() != null) {
        // TODO
        // return Jimple.newSpecialInvokeExpr();
      }
      throw new RuntimeException("Unknown Stmt");
    }
  }

  private static class ValueVisitor extends JimpleBaseVisitor<Value> {
    @Override
    public Constant visitConstant(JimpleParser.ConstantContext ctx) {

      if (ctx.INTEGER_CONSTANT() != null) {
        return IntConstant.getInstance(Integer.valueOf(ctx.INTEGER_CONSTANT().getText()));
      } else if (ctx.FLOAT_CONSTANT() != null) {
        return FloatConstant.getInstance(Float.valueOf(ctx.FLOAT_CONSTANT().getText()));
      } else if (ctx.CLASS() != null) {
        final String text = ctx.CLASS().getText();
        return JavaJimple.getInstance().newStringConstant(text.substring(1, text.length() - 1));
      } else if (ctx.STRING_CONSTANT() != null) {
        final String text = ctx.STRING_CONSTANT().getText();
        return JavaJimple.getInstance().newStringConstant(text.substring(1, text.length() - 1));
      } else if (ctx.NULL() != null) {
        return NullConstant.getInstance();
      }
      // TODO
      throw new RuntimeException("Unknown Constant");
    }

    @Override
    public Expr visitReference(JimpleParser.ReferenceContext ctx) {
      // TODO
      return null;
    }

    @Override
    public Expr visitBool_expr(JimpleParser.Bool_exprContext ctx) {
      // TODO
      return null;
    }

    @Override
    public Expr visitInvoke_expr(JimpleParser.Invoke_exprContext ctx) {
      // TODO
      return null;
    }

    @Override
    public Expr visitUnop_expr(JimpleParser.Unop_exprContext ctx) {
      Value value = ctx.immediate().accept(new ValueVisitor());
      if (ctx.unop().NEG() != null) {
        return Jimple.newNegExpr(value);
      } else {
        return Jimple.newLengthExpr(value);
      }
    }

    @Override
    public AbstractBinopExpr visitBinop_expr(JimpleParser.Binop_exprContext ctx) {

      Value left = ctx.left.accept(new ValueVisitor());
      Value right = ctx.right.accept(new ValueVisitor());

      JimpleParser.BinopContext binopctx = ctx.op;

      if (binopctx.AND() != null) {
        return new JAndExpr(left, right);
      } else if (binopctx.OR() != null) {
        return new JOrExpr(left, right);
      } else if (binopctx.MOD() != null) {
        return new JRemExpr(left, right);
      } else if (binopctx.CMP() != null) {
        return new JCmpExpr(left, right);
      } else if (binopctx.CMPG() != null) {
        return new JCmpgExpr(left, right);
      } else if (binopctx.CMPL() != null) {
        return new JCmplExpr(left, right);
      } else if (binopctx.CMPEQ() != null) {
        return new JEqExpr(left, right);
      } else if (binopctx.CMPNE() != null) {
        return new JNeExpr(left, right);
      } else if (binopctx.CMPGT() != null) {
        return new JGtExpr(left, right);
      } else if (binopctx.CMPGE() != null) {
        return new JGeExpr(left, right);
      } else if (binopctx.CMPLT() != null) {
        return new JLtExpr(left, right);
      } else if (binopctx.CMPLE() != null) {
        return new JLeExpr(left, right);
      } else if (binopctx.SHL() != null) {
        return new JShlExpr(left, right);
      } else if (binopctx.SHR() != null) {
        return new JShrExpr(left, right);
      } else if (binopctx.USHR() != null) {
        return new JUshrExpr(left, right);
      } else if (binopctx.PLUS() != null) {
        return new JAddExpr(left, right);
      } else if (binopctx.MINUS() != null) {
        return new JSubExpr(left, right);
      } else if (binopctx.MULT() != null) {
        return new JMulExpr(left, right);
      } else if (binopctx.DIV() != null) {
        return new JDivExpr(left, right);
      }
      throw new RuntimeException("Unknown BinOp");
    }
  }
}
