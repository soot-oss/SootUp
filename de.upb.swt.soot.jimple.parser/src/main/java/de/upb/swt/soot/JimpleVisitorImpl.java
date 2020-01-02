package de.upb.swt.soot;

import de.upb.swt.soot.core.jimple.basic.NoPositionInformation;
import de.upb.swt.soot.core.jimple.common.expr.Expr;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.*;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.jimple.JimpleBaseVisitor;
import de.upb.swt.soot.jimple.JimpleLexer;
import de.upb.swt.soot.jimple.JimpleParser;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.antlr.v4.runtime.*;

class JimpleVisitor {

  public JimpleClassSource parse(String someLangSourceCode) {
    CharStream charStream = new ANTLRInputStream(someLangSourceCode);
    JimpleLexer lexer = new JimpleLexer(charStream);
    TokenStream tokens = new CommonTokenStream(lexer);
    JimpleParser parser = new JimpleParser(tokens);

    ClassVisitor classVisitor = new ClassVisitor();
    JimpleClassSource traverseResult = classVisitor.visit(parser.clazz());
    return traverseResult;
  }

  private static class ClassVisitor extends JimpleBaseVisitor<JimpleClassSource> {
    @Override
    @Nonnull
    public JimpleClassSource visitClazz(@Nonnull JimpleParser.ClazzContext ctx) {

      ClassType clazzSignature;
      ClassType superclazz;
      ClassType outerclazz;
      ClassType extendz;
      Set<ClassType> interfaces;

      Set<Modifier> modifier;
      Set<SootField> fields;

      // TODO: implement
      Position position = NoPositionInformation.getInstance();

      //     List<Method> modifier = ctx.modifier().stream().map().collect(Collectors.toList() );

      String className = ctx.class_name().getText();

      MethodVisitor methodVisitor = new MethodVisitor();
      Set<SootMethod> methods =
          ctx.modifier().stream()
              .map(method -> method.accept(methodVisitor))
              .collect(Collectors.toSet());

      // FIXME null is not valid here.. use own JimpleClassSource
      // AbstractClassSource classSource = new
      // OverridingClassSource(methods,fields,modifier,interfaces, Optional.of(superclazz),
      // Optional.of(outerclazz), position, null );
      return null;
    }
  }

  private static class MethodVisitor extends JimpleBaseVisitor<SootMethod> {
    /*
    @Override
    @Nonnull
    public Method visitMethod(@Nonnull JimpleParser.MethodContext ctx) {
      String methodName = ctx.methodName().getText();
      StmtVisitor instructionVisitor = new StmtVisitor();
      List<Stmt> instructions = ctx.instruction()
              .stream()
              .map(instruction -> instruction.accept(instructionVisitor))
              .collect(Collectors.toList());
      return new SootMethod(methodName, instructions);
    }
    */
  }

  private static class StmtVisitor extends JimpleBaseVisitor<Stmt> {

    /*
        @Override
         @Nonnull
         public Stmt visitStatement(JimpleParser.StatementContext ctx) {
           String methodName = ctx.methodName().getText();
           ExprVisitor instructionVisitor = new ExprVisitor();
           List<Stmt> instructions = ctx.instruction()
                   .stream()
                   .map(instruction -> instruction.accept(instructionVisitor))
                   .collect(Collectors.toList());

           return null;
         }
    */

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
