package sootup.codepropertygraph.benchmark;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import org.junit.jupiter.api.Test;
import sootup.codepropertygraph.BenchmarkTestSuiteBase;
import sootup.codepropertygraph.ast.AstCreator;
import sootup.codepropertygraph.propertygraph.PropertyGraph;
import sootup.core.jimple.common.stmt.*;
import sootup.core.model.MethodModifier;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.types.PrimitiveType;
import sootup.core.types.Type;
import sootup.core.types.VoidType;

public class AstBenchmarkTest extends BenchmarkTestSuiteBase {
  private final ClassType ifElseStatementClass = getClassType("IfElseStatement");
  private final ClassType tryCatchFinallyClass = getClassType("TryCatchFinally");
  private final ClassType switchCaseStatementClass = getClassType("SwitchCaseStatement");
  private final ClassType whileLoopClass = getClassType("WhileLoop");


  @Test
  public void testAstForIfStatement() {
    String methodName = "ifStatement";

    MethodSignature methodSignature =
        getMethodSignature(
            ifElseStatementClass, methodName, "int", Collections.singletonList("int"));

    assertAstMethod(
        methodSignature,
        methodName,
        EnumSet.of(MethodModifier.PUBLIC),
        Collections.singletonList(PrimitiveType.IntType.getInstance()),
        null,
        PrimitiveType.IntType.getInstance());
  }

  @Test
  public void testAstForIfElseStatement() {
    String methodName = "ifElseStatement";
    MethodSignature methodSignature =
        getMethodSignature(
            ifElseStatementClass, methodName, "int", Collections.singletonList("int"));

    assertAstMethod(
        methodSignature,
        methodName,
        EnumSet.of(MethodModifier.PUBLIC),
        Collections.singletonList(PrimitiveType.IntType.getInstance()),
        null,
        PrimitiveType.IntType.getInstance());
  }

  @Test
  public void testAstForIfElseIfStatement() {
    String methodName = "ifElseIfStatement";
    MethodSignature methodSignature =
        getMethodSignature(
            ifElseStatementClass, methodName, "int", Collections.singletonList("int"));

    assertAstMethod(
        methodSignature,
        methodName,
        EnumSet.of(MethodModifier.PUBLIC),
        Collections.singletonList(PrimitiveType.IntType.getInstance()),
        null,
        PrimitiveType.IntType.getInstance());
  }

  @Test
  public void testAstForIfElseCascadingStatement() {
    String methodName = "ifElseCascadingStatement";
    MethodSignature methodSignature =
        getMethodSignature(
            ifElseStatementClass, methodName, "int", Collections.singletonList("int"));

    assertAstMethod(
        methodSignature,
        methodName,
        EnumSet.of(MethodModifier.PUBLIC),
        Collections.singletonList(PrimitiveType.IntType.getInstance()),
        null,
        PrimitiveType.IntType.getInstance());
  }

  @Test
  public void testAstForIfElseCascadingElseIfStatement() {
    String methodName = "ifElseCascadingElseIfStatement";
    MethodSignature methodSignature =
        getMethodSignature(
            ifElseStatementClass, methodName, "int", Collections.singletonList("int"));

    assertAstMethod(
        methodSignature,
        methodName,
        EnumSet.of(MethodModifier.PUBLIC),
        Collections.singletonList(PrimitiveType.IntType.getInstance()),
        null,
        PrimitiveType.IntType.getInstance());
  }

  @Test
  public void testAstForIfElseCascadingElseIfInElseStatement() {
    String methodName = "ifElseCascadingElseIfInElseStatement";
    MethodSignature methodSignature =
        getMethodSignature(
            ifElseStatementClass, methodName, "int", Collections.singletonList("int"));

    assertAstMethod(
        methodSignature,
        methodName,
        EnumSet.of(MethodModifier.PUBLIC),
        Collections.singletonList(PrimitiveType.IntType.getInstance()),
        null,
        PrimitiveType.IntType.getInstance());
  }

  @Test
  public void testAstForTryCatch() {
    String methodName = "tryCatch";
    MethodSignature methodSignature =
        getMethodSignature(tryCatchFinallyClass, methodName, "void", Collections.emptyList());

    assertAstMethod(
        methodSignature,
        methodName,
        EnumSet.of(MethodModifier.PUBLIC),
        Collections.emptyList(),
        null,
        VoidType.getInstance());
  }

  @Test
  public void testAstForTryCatchNested() {
    String methodName = "tryCatchNested";
    MethodSignature methodSignature =
        getMethodSignature(tryCatchFinallyClass, methodName, "void", Collections.emptyList());

    assertAstMethod(
        methodSignature,
        methodName,
        EnumSet.of(MethodModifier.PUBLIC),
        Collections.emptyList(),
        null,
        VoidType.getInstance());
  }

  @Test
  public void testAstForTryCatchFinallyNested() {
    String methodName = "tryCatchFinallyNested";
    MethodSignature methodSignature =
        getMethodSignature(tryCatchFinallyClass, methodName, "void", Collections.emptyList());

    assertAstMethod(
        methodSignature,
        methodName,
        EnumSet.of(MethodModifier.PUBLIC),
        Collections.emptyList(),
        null,
        VoidType.getInstance());
  }

  @Test
  public void testAstForTryCatchFinallyNestedInFinally() {
    String methodName = "tryCatchFinallyNestedInFinally";
    MethodSignature methodSignature =
        getMethodSignature(tryCatchFinallyClass, methodName, "void", Collections.emptyList());

    assertAstMethod(
        methodSignature,
        methodName,
        EnumSet.of(MethodModifier.PUBLIC),
        Collections.emptyList(),
        null,
        VoidType.getInstance());
  }

  @Test
  public void testAstForTryCatchFinallyCombined() {
    String methodName = "tryCatchFinallyCombined";
    MethodSignature methodSignature =
        getMethodSignature(tryCatchFinallyClass, methodName, "void", Collections.emptyList());

    assertAstMethod(
        methodSignature,
        methodName,
        EnumSet.of(MethodModifier.PUBLIC),
        Collections.emptyList(),
        null,
        VoidType.getInstance());
  }

  @Test
  public void testAstForTryCatchFinallyNestedInCatch() {
    String methodName = "tryCatchFinallyNestedInCatch";
    MethodSignature methodSignature =
        getMethodSignature(tryCatchFinallyClass, methodName, "void", Collections.emptyList());

    assertAstMethod(
        methodSignature,
        methodName,
        EnumSet.of(MethodModifier.PUBLIC),
        Collections.emptyList(),
        null,
        VoidType.getInstance());
  }

  @Test
  public void testAstForTryCatchFinally() {
    String methodName = "tryCatchFinally";
    MethodSignature methodSignature =
        getMethodSignature(tryCatchFinallyClass, methodName, "void", Collections.emptyList());

    assertAstMethod(
        methodSignature,
        methodName,
        EnumSet.of(MethodModifier.PUBLIC),
        Collections.emptyList(),
        null,
        VoidType.getInstance());
  }

  @Test
  public void testAstForTryCatchNestedInCatch() {
    String methodName = "tryCatchNestedInCatch";
    MethodSignature methodSignature =
        getMethodSignature(tryCatchFinallyClass, methodName, "void", Collections.emptyList());

    assertAstMethod(
        methodSignature,
        methodName,
        EnumSet.of(MethodModifier.PUBLIC),
        Collections.emptyList(),
        null,
        VoidType.getInstance());
  }

  @Test
  public void testAstForTryCatchCombined() {
    String methodName = "tryCatchCombined";
    MethodSignature methodSignature =
        getMethodSignature(tryCatchFinallyClass, methodName, "void", Collections.emptyList());

    assertAstMethod(
        methodSignature,
        methodName,
        EnumSet.of(MethodModifier.PUBLIC),
        Collections.emptyList(),
        null,
        VoidType.getInstance());
  }

  @Test
  public void testAstForSwitchCaseGroupedTargetsDefault() {
    String methodName = "switchCaseGroupedTargetsDefault";
    MethodSignature methodSignature =
        getMethodSignature(switchCaseStatementClass, methodName, "void", Collections.emptyList());

    assertAstMethod(
        methodSignature,
        methodName,
        EnumSet.of(MethodModifier.PUBLIC),
        Collections.emptyList(),
        null,
        VoidType.getInstance());
  }

  @Test
  public void testAstForSwitchWithSwitch() {
    String methodName = "switchWithSwitch";
    MethodSignature methodSignature =
        getMethodSignature(switchCaseStatementClass, methodName, "void", Collections.emptyList());

    assertAstMethod(
        methodSignature,
        methodName,
        EnumSet.of(MethodModifier.PUBLIC),
        Collections.emptyList(),
        null,
        VoidType.getInstance());
  }

  @Test
  public void testAstForSwitchCaseStatementInt() {
    String methodName = "switchCaseStatementInt";
    MethodSignature methodSignature =
        getMethodSignature(switchCaseStatementClass, methodName, "void", Collections.emptyList());

    assertAstMethod(
        methodSignature,
        methodName,
        EnumSet.of(MethodModifier.PUBLIC),
        Collections.emptyList(),
        null,
        VoidType.getInstance());
  }

  @Test
  public void testAstForSwitchCaseGroupedTargets() {
    String methodName = "switchCaseGroupedTargets";
    MethodSignature methodSignature =
        getMethodSignature(switchCaseStatementClass, methodName, "void", Collections.emptyList());

    assertAstMethod(
        methodSignature,
        methodName,
        EnumSet.of(MethodModifier.PUBLIC),
        Collections.emptyList(),
        null,
        VoidType.getInstance());
  }

  @Test
  public void testAstForSwitchCaseStatementEnum() {
    String methodName = "switchCaseStatementEnum";
    MethodSignature methodSignature =
        getMethodSignature(switchCaseStatementClass, methodName, "void", Collections.emptyList());

    assertAstMethod(
        methodSignature,
        methodName,
        EnumSet.of(MethodModifier.PUBLIC),
        Collections.emptyList(),
        null,
        VoidType.getInstance());
  }

  @Test
  public void testAstForSwitchCaseStatementCaseIncludingIf() {
    String methodName = "switchCaseStatementCaseIncludingIf";
    MethodSignature methodSignature =
        getMethodSignature(switchCaseStatementClass, methodName, "void", Collections.emptyList());

    assertAstMethod(
        methodSignature,
        methodName,
        EnumSet.of(MethodModifier.PUBLIC),
        Collections.emptyList(),
        null,
        VoidType.getInstance());
  }

  @Test
  public void testAstForSwitchCaseWithoutDefault() {
    String methodName = "switchCaseWithoutDefault";
    MethodSignature methodSignature =
        getMethodSignature(switchCaseStatementClass, methodName, "void", Collections.emptyList());

    assertAstMethod(
        methodSignature,
        methodName,
        EnumSet.of(MethodModifier.PUBLIC),
        Collections.emptyList(),
        null,
        VoidType.getInstance());
  }

  @Test
  public void testAstForWhileLoop() {
    String methodName = "whileLoop";
    MethodSignature methodSignature =
            getMethodSignature(whileLoopClass, methodName, "void", Collections.emptyList());

    assertAstMethod(
            methodSignature,
            methodName,
            EnumSet.of(MethodModifier.PUBLIC),
            Collections.emptyList(),
            null,
            VoidType.getInstance());
  }

  private void assertAstMethod(
      MethodSignature methodSignature,
      String expectedName,
      Set<MethodModifier> expectedModifiers,
      List<Type> expectedParameterTypes,
      List<Stmt> expectedBodyStmts,
      Type expectedReturnType) {
    Optional<? extends SootMethod> method = getMinimalTestSuiteMethod(methodSignature);
    assertTrue(method.isPresent());

    PropertyGraph astGraph = (new AstCreator()).createGraph(method.get());

    System.out.println(astGraph.toDotGraph());
  }
}
