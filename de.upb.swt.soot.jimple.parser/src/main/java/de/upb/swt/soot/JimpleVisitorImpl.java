package de.upb.swt.soot;

import de.upb.swt.soot.core.IdentifierFactory;
import de.upb.swt.soot.core.frontend.OverridingClassSource;
import de.upb.swt.soot.core.frontend.OverridingMethodSource;
import de.upb.swt.soot.core.frontend.SootClassSource;
import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.StmtPositionInfo;
import de.upb.swt.soot.core.jimple.basic.Trap;
import de.upb.swt.soot.core.jimple.common.expr.Expr;
import de.upb.swt.soot.core.jimple.common.stmt.JNopStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.*;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.signatures.PackageName;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
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

      ClassType superclass = null;
      Set<ClassType> interfaces = null;
      ClassType outerclass = null;

      // position
      Position position =
          new Position(
              ctx.start.getLine(), ctx.start.getCharPositionInLine(), ctx.stop.getLine(), -1);

      // imports
      imports =
          ctx.importItem().stream()
              .map(item -> identifierFactory.getClassType(item.location.getText()))
              .collect(Collectors.toMap(e -> e.getClassName(), e -> e.getPackageName()));

      Set<Modifier> modifierSet =
          ctx.modifier().stream()
              .map(modifierContext -> Modifier.valueOf(modifierContext.getText()))
              .collect(Collectors.toSet());
      EnumSet<Modifier> modifier = EnumSet.copyOf(modifierSet);

      // file_type
      if (ctx.file_type().equals("interface")) {
        modifier.add(Modifier.INTERFACE);
      }
      if (ctx.file_type().equals("annotation")) {
        modifier.add(Modifier.ANNOTATION);
      }

      // class_name
      final String classname = ctx.class_name().getText();
      clazz = getClassType(classname);

      // extends_clause
      if (ctx.extends_clause() != null) {
        superclass = getClassType(ctx.extends_clause().class_name().getText());
      }

      // implements_clause
      if (ctx.implements_clause() != null) {
        interfaces = ctx.implements_clause().accept(new ClassnameVisitor());
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

  private static class ClassnameVisitor extends JimpleBaseVisitor<Set<ClassType>> {

    @Override
    public Set<ClassType> visitImplements_clause(JimpleParser.Implements_clauseContext ctx) {

      Set<ClassType> interfaces = new HashSet();
      JimpleParser.Class_name_listContext class_name_listContextIterator = ctx.class_name_list();
      do {
        interfaces.add(
            identifierFactory.getClassType(class_name_listContextIterator.class_name().getText()));
        class_name_listContextIterator = ctx.class_name_list();
      } while (class_name_listContextIterator != null);

      return interfaces;
    }
  }

  private static class ExceptionListVisitor extends JimpleBaseVisitor<List<ClassType>> {

    @Override
    public List<ClassType> visitThrows_clause(JimpleParser.Throws_clauseContext ctx) {
      List<ClassType> interfaces = new ArrayList();
      JimpleParser.Class_name_listContext class_name_listContextIterator = ctx.class_name_list();
      do {
        interfaces.add(
            identifierFactory.getClassType(class_name_listContextIterator.class_name().getText()));
        class_name_listContextIterator = ctx.class_name_list();
      } while (class_name_listContextIterator != null);

      return interfaces;
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

      Set<Modifier> modifierSet =
          ctx.modifier().stream()
              .map(modifierContext -> Modifier.valueOf(modifierContext.getText()))
              .collect(Collectors.toSet());
      EnumSet<Modifier> modifier = EnumSet.copyOf(modifierSet);

      return new SootField(
          identifierFactory.getFieldSignature(ctx.name().getText(), clazz, ctx.type().getText()),
          modifier);
    }
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
      List<Type> params = ctx.accept(new ParameterListVisitor());
      MethodSignature methodSignature =
          identifierFactory.getMethodSignature(
              ctx.name().getText(), clazz, getType(ctx.type().getText()), params);

      OverridingMethodSource oms = new OverridingMethodSource(methodSignature, b);

      List<ClassType> exceptions = ctx.throws_clause().accept(new ExceptionListVisitor());

      Set<Modifier> modifierSet =
          ctx.modifier().stream()
              .map(modifierContext -> Modifier.valueOf(modifierContext.getText()))
              .collect(Collectors.toSet());
      EnumSet<Modifier> modifier = EnumSet.copyOf(modifierSet);

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
            identifierFactory.getClassType(class_name_listContextIterator.parameter().getText()));
        class_name_listContextIterator = ctx.parameter_list();
      } while (class_name_listContextIterator != null);

      return interfaces;
    }
  }

  private static class StmtVisitor extends JimpleBaseVisitor<Stmt> {

    @Override
    @Nonnull
    public Stmt visitStatement(JimpleParser.StatementContext ctx) {

      // FIXME: statement
      // FIXME: position
      return new JNopStmt(new StmtPositionInfo(ctx.start.getLine()));
    }
  }

  private static class ExprVisitor extends JimpleBaseVisitor<Expr> {
    /*   @Override

    public Expr visitBinop_expr(JimpleParser.Binop_exprContext ctx) {
      // TODO implement
      return new JAddExpr(ctx.left, ctx.right);
    }
    */
  }
}
