package sootup.tests.exceptions;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import sootup.core.interceptor.BodyInterceptor;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.Value;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.expr.*;
import sootup.core.jimple.common.ref.JArrayRef;
import sootup.core.jimple.common.ref.JInstanceFieldRef;
import sootup.core.jimple.common.ref.JStaticFieldRef;
import sootup.core.jimple.common.ref.Ref;
import sootup.core.jimple.common.stmt.*;
import sootup.core.jimple.javabytecode.stmt.JEnterMonitorStmt;
import sootup.core.jimple.javabytecode.stmt.JExitMonitorStmt;
import sootup.core.model.Body;
import sootup.core.model.SourceType;
import sootup.core.signatures.MethodSignature;
import sootup.core.typehierarchy.TypeHierarchy;
import sootup.core.typehierarchy.ViewTypeHierarchy;
import sootup.core.types.ArrayType;
import sootup.core.types.ClassType;
import sootup.core.types.PrimitiveType;
import sootup.interceptors.*;
import sootup.java.bytecode.frontend.inputlocation.ClassFileBasedAnalysisInputLocation;
import sootup.java.bytecode.frontend.inputlocation.DefaultRuntimeAnalysisInputLocation;
import sootup.java.bytecode.frontend.inputlocation.PathBasedAnalysisInputLocation;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.exceptions.ExceptionInferResult;
import sootup.java.core.exceptions.StmtExceptionAnalyzer;
import sootup.java.core.views.JavaView;

public class StmtExceptionAnalyzerTest {

  JavaIdentifierFactory factory = JavaIdentifierFactory.getInstance();
  ClassType clazzType = factory.getClassType("StmtExceptions");
  String location = "src/test/resources/exceptions/";
  final Path path = Paths.get(location + "StmtExceptions.class");
  List<BodyInterceptor> interceptors =
      Arrays.asList(
          new EmptySwitchEliminator(),
          new CastAndReturnInliner(),
          new Aggregator(),
          new LocalSplitter(),
          new CopyPropagator(),
          new ConstantPropagatorAndFolder(),
          new TypeAssigner());
  PathBasedAnalysisInputLocation inputLocation =
      new ClassFileBasedAnalysisInputLocation(path, "", SourceType.Application, interceptors);
  JavaView view =
      new JavaView(Arrays.asList(inputLocation, new DefaultRuntimeAnalysisInputLocation()));
  TypeHierarchy hierarchy = new ViewTypeHierarchy(view);
  StmtExceptionAnalyzer exceptionAnalyser = new StmtExceptionAnalyzer(hierarchy);

  @Test
  public void testInvokeStmts() {
    MethodSignature methodSignature =
        factory.getMethodSignature(clazzType, "testInvokeStmts", "void", Collections.emptyList());
    Body body = view.getMethod(methodSignature).get().getBody();

    body.getStmts().stream()
        .forEach(
            stmt -> {
              ExceptionInferResult result = exceptionAnalyser.mightThrowImplicitly(stmt);
              if (stmt instanceof JInvokeStmt) {
                if (((JInvokeStmt) stmt).getInvokeExpr().isPresent()) {
                  Expr expr = ((JInvokeStmt) stmt).getInvokeExpr().get();
                  if (expr instanceof JStaticInvokeExpr) {
                    Assertions.assertEquals(1, result.getExceptions().size());
                    Assertions.assertTrue(
                        result
                            .getExceptions()
                            .contains(ExceptionInferResult.ErrorType.INITIALIZATION_ERROR));
                  } else {
                    Assertions.assertEquals(6, result.getExceptions().size());
                    Assertions.assertTrue(
                        result
                            .getExceptions()
                            .contains(ExceptionInferResult.ErrorType.ABSTRACT_METHOD_ERROR));
                    Assertions.assertTrue(
                        result
                            .getExceptions()
                            .contains(ExceptionInferResult.ErrorType.NO_SUCH_METHOD_ERROR));
                    Assertions.assertTrue(
                        result
                            .getExceptions()
                            .contains(ExceptionInferResult.ErrorType.UNSATISFIED_LINK_ERROR));
                    Assertions.assertTrue(
                        result.getExceptions().contains(ExceptionInferResult.ErrorType.VM_ERROR));
                    Assertions.assertTrue(
                        result
                            .getExceptions()
                            .contains(ExceptionInferResult.ErrorType.THREAD_DEATH));
                    Assertions.assertTrue(
                        result
                            .getExceptions()
                            .contains(ExceptionInferResult.ExceptionType.NUll_POINTER_EXCEPTION));
                  }
                }
              }
            });
  }

  @Test
  public void testAssignStmt() {
    MethodSignature methodSignature =
        factory.getMethodSignature(clazzType, "testAssignStmts", "void", Collections.emptyList());
    Body body = view.getMethod(methodSignature).get().getBody();

    body.getStmts().stream()
        .forEach(
            stmt -> {
              ExceptionInferResult result = exceptionAnalyser.mightThrowImplicitly(stmt);
              if (stmt instanceof JAssignStmt) {
                Value leftOp = ((JAssignStmt) stmt).getLeftOp();
                Value rightOp = ((JAssignStmt) stmt).getRightOp();
                if (leftOp instanceof Ref) {
                  if (leftOp instanceof JArrayRef) {
                    Assertions.assertEquals(5, result.getExceptions().size());
                    Assertions.assertTrue(
                        result
                            .getExceptions()
                            .contains(
                                ExceptionInferResult.ExceptionType.INDEX_OUT_OF_BOUNDS_EXCEPTION));
                    Assertions.assertTrue(
                        result
                            .getExceptions()
                            .contains(ExceptionInferResult.ExceptionType.NUll_POINTER_EXCEPTION));
                    Assertions.assertTrue(
                        result
                            .getExceptions()
                            .contains(ExceptionInferResult.ExceptionType.ARRAY_STORE_EXCEPTION));
                    Assertions.assertTrue(
                        result.getExceptions().contains(ExceptionInferResult.ErrorType.VM_ERROR));
                    Assertions.assertTrue(
                        result
                            .getExceptions()
                            .contains(ExceptionInferResult.ErrorType.THREAD_DEATH));
                  } else if (leftOp instanceof JInstanceFieldRef) {
                    Assertions.assertEquals(4, result.getExceptions().size());
                    Assertions.assertTrue(
                        result
                            .getExceptions()
                            .contains(ExceptionInferResult.ExceptionType.NUll_POINTER_EXCEPTION));
                    Assertions.assertTrue(
                        result
                            .getExceptions()
                            .contains(ExceptionInferResult.ErrorType.RESOLVE_FIELD_ERROR));
                    Assertions.assertTrue(
                        result.getExceptions().contains(ExceptionInferResult.ErrorType.VM_ERROR));
                    Assertions.assertTrue(
                        result
                            .getExceptions()
                            .contains(ExceptionInferResult.ErrorType.THREAD_DEATH));
                  }
                } else if (rightOp instanceof JNewMultiArrayExpr) {
                  Assertions.assertEquals(4, result.getExceptions().size());
                  Assertions.assertTrue(
                      result
                          .getExceptions()
                          .contains(
                              ExceptionInferResult.ExceptionType.NEGATIVE_ARRAY_SIZE_EXCEPTION));
                  Assertions.assertTrue(
                      result
                          .getExceptions()
                          .contains(ExceptionInferResult.ErrorType.RESOLVE_CLASS_ERROR));
                  Assertions.assertTrue(
                      result.getExceptions().contains(ExceptionInferResult.ErrorType.VM_ERROR));
                  Assertions.assertTrue(
                      result.getExceptions().contains(ExceptionInferResult.ErrorType.THREAD_DEATH));
                } else if (rightOp instanceof JNewArrayExpr
                    || rightOp instanceof JCastExpr
                    || rightOp instanceof JInstanceOfExpr) {
                  Assertions.assertEquals(3, result.getExceptions().size());
                  Assertions.assertTrue(
                      result
                          .getExceptions()
                          .contains(ExceptionInferResult.ErrorType.RESOLVE_CLASS_ERROR));
                  Assertions.assertTrue(
                      result.getExceptions().contains(ExceptionInferResult.ErrorType.VM_ERROR));
                  Assertions.assertTrue(
                      result.getExceptions().contains(ExceptionInferResult.ErrorType.THREAD_DEATH));
                } else if (rightOp instanceof JNewExpr
                    || rightOp instanceof JStaticInvokeExpr
                    || rightOp instanceof JStaticFieldRef) {
                  Assertions.assertEquals(1, result.getExceptions().size());
                  Assertions.assertTrue(
                      result
                          .getExceptions()
                          .contains(ExceptionInferResult.ErrorType.INITIALIZATION_ERROR));
                } else if (rightOp instanceof JVirtualInvokeExpr) {
                  Assertions.assertEquals(6, result.getExceptions().size());
                  Assertions.assertTrue(
                      result
                          .getExceptions()
                          .contains(ExceptionInferResult.ErrorType.ABSTRACT_METHOD_ERROR));
                  Assertions.assertTrue(
                      result
                          .getExceptions()
                          .contains(ExceptionInferResult.ErrorType.NO_SUCH_METHOD_ERROR));
                  Assertions.assertTrue(
                      result
                          .getExceptions()
                          .contains(ExceptionInferResult.ErrorType.UNSATISFIED_LINK_ERROR));
                  Assertions.assertTrue(
                      result.getExceptions().contains(ExceptionInferResult.ErrorType.VM_ERROR));
                  Assertions.assertTrue(
                      result.getExceptions().contains(ExceptionInferResult.ErrorType.THREAD_DEATH));
                  Assertions.assertTrue(
                      result
                          .getExceptions()
                          .contains(ExceptionInferResult.ExceptionType.NUll_POINTER_EXCEPTION));
                } else if (rightOp instanceof JLengthExpr) {
                  Assertions.assertEquals(3, result.getExceptions().size());
                  Assertions.assertTrue(
                      result.getExceptions().contains(ExceptionInferResult.ErrorType.VM_ERROR));
                  Assertions.assertTrue(
                      result.getExceptions().contains(ExceptionInferResult.ErrorType.THREAD_DEATH));
                  Assertions.assertTrue(
                      result
                          .getExceptions()
                          .contains(ExceptionInferResult.ExceptionType.NUll_POINTER_EXCEPTION));
                } else if (rightOp instanceof JArrayRef) {
                  Assertions.assertEquals(4, result.getExceptions().size());
                  Assertions.assertTrue(
                      result.getExceptions().contains(ExceptionInferResult.ErrorType.VM_ERROR));
                  Assertions.assertTrue(
                      result.getExceptions().contains(ExceptionInferResult.ErrorType.THREAD_DEATH));
                  Assertions.assertTrue(
                      result
                          .getExceptions()
                          .contains(ExceptionInferResult.ExceptionType.NUll_POINTER_EXCEPTION));
                  Assertions.assertTrue(
                      result
                          .getExceptions()
                          .contains(
                              ExceptionInferResult.ExceptionType.INDEX_OUT_OF_BOUNDS_EXCEPTION));
                } else if (rightOp instanceof JDivExpr) {
                  Assertions.assertEquals(3, result.getExceptions().size());
                  Assertions.assertTrue(
                      result.getExceptions().contains(ExceptionInferResult.ErrorType.VM_ERROR));
                  Assertions.assertTrue(
                      result.getExceptions().contains(ExceptionInferResult.ErrorType.THREAD_DEATH));
                  Assertions.assertTrue(
                      result
                          .getExceptions()
                          .contains(ExceptionInferResult.ExceptionType.ARITHMETIC_EXCEPTION));
                } else {
                  Assertions.assertEquals(2, result.getExceptions().size());
                  Assertions.assertTrue(
                      result.getExceptions().contains(ExceptionInferResult.ErrorType.VM_ERROR));
                  Assertions.assertTrue(
                      result.getExceptions().contains(ExceptionInferResult.ErrorType.THREAD_DEATH));
                }
              }
            });
  }

  @Test
  public void testMonitorStmts() {
    MethodSignature methodSignature =
        factory.getMethodSignature(clazzType, "testMonitorStmts", "void", Collections.emptyList());
    Body body = view.getMethod(methodSignature).get().getBody();

    body.getStmts().stream()
        .forEach(
            stmt -> {
              ExceptionInferResult result = exceptionAnalyser.mightThrowImplicitly(stmt);
              if (stmt instanceof JEnterMonitorStmt) {
                Assertions.assertEquals(3, result.getExceptions().size());
                Assertions.assertTrue(
                    result.getExceptions().contains(ExceptionInferResult.ErrorType.VM_ERROR));
                Assertions.assertTrue(
                    result.getExceptions().contains(ExceptionInferResult.ErrorType.THREAD_DEATH));
                Assertions.assertTrue(
                    result
                        .getExceptions()
                        .contains(ExceptionInferResult.ExceptionType.NUll_POINTER_EXCEPTION));
              } else if (stmt instanceof JExitMonitorStmt) {
                Assertions.assertEquals(4, result.getExceptions().size());
                Assertions.assertTrue(
                    result.getExceptions().contains(ExceptionInferResult.ErrorType.VM_ERROR));
                Assertions.assertTrue(
                    result.getExceptions().contains(ExceptionInferResult.ErrorType.THREAD_DEATH));
                Assertions.assertTrue(
                    result
                        .getExceptions()
                        .contains(ExceptionInferResult.ExceptionType.NUll_POINTER_EXCEPTION));
                Assertions.assertTrue(
                    result
                        .getExceptions()
                        .contains(
                            ExceptionInferResult.ExceptionType.ILLEGAL_MONITOR_STATE_EXCEPTION));
              } else if (stmt instanceof JThrowStmt) {
                result =
                    exceptionAnalyser.mightThrowExplicitly(
                        (JThrowStmt) stmt, body.getControlFlowGraph());
                Assertions.assertEquals(1, result.getExceptions().size());
                Assertions.assertTrue(
                    result.getExceptions().contains(ExceptionInferResult.ExceptionType.THROWABLE));
              }
            });
  }

  @Test
  public void testNewArrayExprWithPrimitiveBaseType() {
    StmtPositionInfo noPositionInfo = StmtPositionInfo.getNoStmtPositionInfo();
    Local arr = Jimple.newLocal("arr", new ArrayType(PrimitiveType.getInt(), 1));
    Local size = Jimple.newLocal("size", PrimitiveType.getInt());
    Set<ClassType> defaultExceptions =
        new HashSet<>(
            Arrays.asList(
                ExceptionInferResult.ErrorType.VM_ERROR,
                ExceptionInferResult.ErrorType.THREAD_DEATH));

    // arr = new int[4]: a non-negative constant size cannot throw a NegativeArraySizeException and
    // a primitive base type needs no class resolution, so only the default result remains.
    Stmt constantSize =
        Jimple.newAssignStmt(
            arr,
            new JNewArrayExpr(PrimitiveType.getInt(), IntConstant.getInstance(4), factory),
            noPositionInfo);
    Assertions.assertEquals(
        defaultExceptions, exceptionAnalyser.mightThrowImplicitly(constantSize).getExceptions());

    // arr = new int[-1]: a negative constant size always throws a NegativeArraySizeException.
    Set<ClassType> withNegativeArraySize = new HashSet<>(defaultExceptions);
    withNegativeArraySize.add(ExceptionInferResult.ExceptionType.NEGATIVE_ARRAY_SIZE_EXCEPTION);
    Stmt negativeConstantSize =
        Jimple.newAssignStmt(
            arr,
            new JNewArrayExpr(PrimitiveType.getInt(), IntConstant.getInstance(-1), factory),
            noPositionInfo);
    Assertions.assertEquals(
        withNegativeArraySize,
        exceptionAnalyser.mightThrowImplicitly(negativeConstantSize).getExceptions());

    // arr = new int[size]: an unknown size might be negative.
    Stmt localSize =
        Jimple.newAssignStmt(
            arr, new JNewArrayExpr(PrimitiveType.getInt(), size, factory), noPositionInfo);
    Assertions.assertEquals(
        withNegativeArraySize, exceptionAnalyser.mightThrowImplicitly(localSize).getExceptions());
  }

  @Test
  public void testNewArrayExprWithReferenceBaseType() {
    StmtPositionInfo noPositionInfo = StmtPositionInfo.getNoStmtPositionInfo();
    ClassType integerType = factory.getClassType("java.lang.Integer");
    Local arr = Jimple.newLocal("arr", new ArrayType(integerType, 1));
    Set<ClassType> expected =
        new HashSet<>(
            Arrays.asList(
                ExceptionInferResult.ErrorType.VM_ERROR,
                ExceptionInferResult.ErrorType.THREAD_DEATH,
                ExceptionInferResult.ErrorType.RESOLVE_CLASS_ERROR));

    // arr = new Integer[4]: a reference base type has to be resolved.
    Stmt constantSize =
        Jimple.newAssignStmt(
            arr,
            new JNewArrayExpr(integerType, IntConstant.getInstance(4), factory),
            noPositionInfo);
    Assertions.assertEquals(
        expected, exceptionAnalyser.mightThrowImplicitly(constantSize).getExceptions());
  }
}
