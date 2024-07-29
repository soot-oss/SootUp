package sootup.codepropertygraph.benchmark;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import sootup.codepropertygraph.BenchmarkTestSuiteBase;
import sootup.codepropertygraph.ddg.DdgCreator;
import sootup.codepropertygraph.propertygraph.PropertyGraph;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;

public class DdgBenchmarkTest extends BenchmarkTestSuiteBase {
  private final ClassType ifElseStatementClass = getClassType("IfElseStatement");
  private final ClassType tryCatchFinallyClass = getClassType("TryCatchFinally");
  private ClassType switchCaseStatement = getClassType("SwitchCaseStatement");
  private final ClassType whileLoopClass = getClassType("WhileLoop");

  @Test
  public void testDdgForIfStatement() {
    String methodName = "ifStatement";
    MethodSignature methodSignature =
        getMethodSignature(
            ifElseStatementClass, methodName, "int", Collections.singletonList("int"));

    assertDdgMethod(methodSignature);
  }

  @Test
  public void testDdgForIfElseStatement() {
    String methodName = "ifElseStatement";
    MethodSignature methodSignature =
        getMethodSignature(
            ifElseStatementClass, methodName, "int", Collections.singletonList("int"));

    assertDdgMethod(methodSignature);
  }

  @Test
  public void testDdgForIfElseIfStatement() {
    String methodName = "ifElseIfStatement";
    MethodSignature methodSignature =
        getMethodSignature(
            ifElseStatementClass, methodName, "int", Collections.singletonList("int"));

    assertDdgMethod(methodSignature);
  }

  @Test
  public void testDdgForIfElseCascadingElseIfStatement() {
    String methodName = "ifElseCascadingElseIfStatement";
    MethodSignature methodSignature =
        getMethodSignature(
            ifElseStatementClass, methodName, "int", Collections.singletonList("int"));

    assertDdgMethod(methodSignature);
  }

  @Test
  public void testDdgForIfElseCascadingElseIfInElseStatement() {
    String methodName = "ifElseCascadingElseIfInElseStatement";
    MethodSignature methodSignature =
        getMethodSignature(
            ifElseStatementClass, methodName, "int", Collections.singletonList("int"));

    assertDdgMethod(methodSignature);
  }

  @Test
  public void testDdgForTryCatch() {
    String methodName = "tryCatch";
    MethodSignature methodSignature =
        getMethodSignature(tryCatchFinallyClass, methodName, "void", Collections.emptyList());

    assertDdgMethod(methodSignature);
  }

  @Test
  public void testDdgForTryCatchNested() {
    String methodName = "tryCatchNested";
    MethodSignature methodSignature =
        getMethodSignature(tryCatchFinallyClass, methodName, "void", Collections.emptyList());

    assertDdgMethod(methodSignature);
  }

  @Test
  public void testDdgForTryCatchFinallyNested() {
    String methodName = "tryCatchFinallyNested";
    MethodSignature methodSignature =
        getMethodSignature(tryCatchFinallyClass, methodName, "void", Collections.emptyList());

    assertDdgMethod(methodSignature);
  }

  @Test
  public void testDdgForTryCatchFinallyNestedInFinally() {
    String methodName = "tryCatchFinallyNestedInFinally";
    MethodSignature methodSignature =
        getMethodSignature(tryCatchFinallyClass, methodName, "void", Collections.emptyList());

    assertDdgMethod(methodSignature);
  }

  @Test
  public void testDdgForTryCatchFinallyCombined() {
    String methodName = "tryCatchFinallyCombined";
    MethodSignature methodSignature =
        getMethodSignature(tryCatchFinallyClass, methodName, "void", Collections.emptyList());

    assertDdgMethod(methodSignature);
  }

  @Test
  public void testDdgForTryCatchFinallyNestedInCatch() {
    String methodName = "tryCatchFinallyNestedInCatch";
    MethodSignature methodSignature =
        getMethodSignature(tryCatchFinallyClass, methodName, "void", Collections.emptyList());

    assertDdgMethod(methodSignature);
  }

  @Test
  public void testDdgForTryCatchFinally() {
    String methodName = "tryCatchFinally";
    MethodSignature methodSignature =
        getMethodSignature(tryCatchFinallyClass, methodName, "void", Collections.emptyList());

    assertDdgMethod(methodSignature);
  }

  @Test
  public void testDdgForTryCatchNestedInCatch() {
    String methodName = "tryCatchNestedInCatch";
    MethodSignature methodSignature =
        getMethodSignature(tryCatchFinallyClass, methodName, "void", Collections.emptyList());

    assertDdgMethod(methodSignature);
  }

  @Test
  public void testDdgForTryCatchCombined() {
    String methodName = "tryCatchCombined";
    MethodSignature methodSignature =
        getMethodSignature(tryCatchFinallyClass, methodName, "void", Collections.emptyList());

    assertDdgMethod(methodSignature);
  }

  @Test
  public void testDdgForSwitchCaseGroupedTargetsDefault() {
    String methodName = "switchCaseGroupedTargetsDefault";
    MethodSignature methodSignature =
        getMethodSignature(switchCaseStatement, methodName, "void", Collections.emptyList());

    assertDdgMethod(methodSignature);
  }

  @Test
  public void testDdgForSwitchWithSwitch() {
    String methodName = "switchWithSwitch";
    MethodSignature methodSignature =
        getMethodSignature(switchCaseStatement, methodName, "void", Collections.emptyList());

    assertDdgMethod(methodSignature);
  }

  @Test
  public void testDdgForSwitchCaseStatementInt() {
    String methodName = "switchCaseStatementInt";
    MethodSignature methodSignature =
        getMethodSignature(switchCaseStatement, methodName, "void", Collections.emptyList());

    assertDdgMethod(methodSignature);
  }

  @Test
  public void testDdgForSwitchCaseGroupedTargets() {
    String methodName = "switchCaseGroupedTargets";
    MethodSignature methodSignature =
        getMethodSignature(switchCaseStatement, methodName, "void", Collections.emptyList());

    assertDdgMethod(methodSignature);
  }

  @Test
  public void testDdgForSwitchCaseStatementEnum() {
    String methodName = "switchCaseStatementEnum";
    MethodSignature methodSignature =
        getMethodSignature(switchCaseStatement, methodName, "void", Collections.emptyList());

    assertDdgMethod(methodSignature);
  }

  @Test
  public void testDdgForSwitchCaseStatementCaseIncludingIf() {
    String methodName = "switchCaseStatementCaseIncludingIf";
    MethodSignature methodSignature =
        getMethodSignature(switchCaseStatement, methodName, "void", Collections.emptyList());

    assertDdgMethod(methodSignature);
  }

  @Test
  public void testDdgForSwitchCaseWithoutDefault() {
    String methodName = "switchCaseWithoutDefault";
    MethodSignature methodSignature =
        getMethodSignature(switchCaseStatement, methodName, "void", Collections.emptyList());

    assertDdgMethod(methodSignature);
  }

  @Test
  public void testDdgForWhileLoop() {
    String methodName = "whileLoop";
    MethodSignature methodSignature =
        getMethodSignature(whileLoopClass, methodName, "void", Collections.emptyList());

    assertDdgMethod(methodSignature);
  }

  private void assertDdgMethod(MethodSignature methodSignature) {
    Optional<? extends SootMethod> method = getMinimalTestSuiteMethod(methodSignature);
    assertTrue(method.isPresent());

    PropertyGraph ddgGraph = (new DdgCreator()).createGraph(method.get());

    System.out.println(ddgGraph.toDotGraph());
  }
}
