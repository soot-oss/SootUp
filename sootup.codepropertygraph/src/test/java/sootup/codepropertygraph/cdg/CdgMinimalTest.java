package sootup.codepropertygraph.cdg;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import sootup.codepropertygraph.CpgTestSuiteBase;
import sootup.codepropertygraph.propertygraph.PropertyGraph;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;

public class CdgMinimalTest extends CpgTestSuiteBase {
  private final ClassType ifElseStatementClass = getClassType("IfElseStatement");
  private final ClassType tryCatchFinallyClass = getClassType("TryCatchFinally");
  private final ClassType switchCaseStatement = getClassType("SwitchCaseStatement");

  @Test
  public void testCdgForIfStatement() {
    String methodName = "ifStatement";
    MethodSignature methodSignature =
        getMethodSignature(
            ifElseStatementClass, methodName, "int", Collections.singletonList("int"));

    assertCdgMethod(methodSignature);
  }

  @Test
  public void testCdgForIfElseStatement() {
    String methodName = "ifElseStatement";
    MethodSignature methodSignature =
        getMethodSignature(
            ifElseStatementClass, methodName, "int", Collections.singletonList("int"));

    assertCdgMethod(methodSignature);
  }

  @Test
  public void testCdgForIfElseIfStatement() {
    String methodName = "ifElseIfStatement";
    MethodSignature methodSignature =
        getMethodSignature(
            ifElseStatementClass, methodName, "int", Collections.singletonList("int"));

    assertCdgMethod(methodSignature);
  }

  @Test
  public void testCdgForIfElseCascadingElseIfStatement() {
    String methodName = "ifElseCascadingElseIfStatement";
    MethodSignature methodSignature =
        getMethodSignature(
            ifElseStatementClass, methodName, "int", Collections.singletonList("int"));

    assertCdgMethod(methodSignature);
  }

  @Test
  public void testCdgForIfElseCascadingElseIfInElseStatement() {
    String methodName = "ifElseCascadingElseIfInElseStatement";
    MethodSignature methodSignature =
        getMethodSignature(
            ifElseStatementClass, methodName, "int", Collections.singletonList("int"));

    assertCdgMethod(methodSignature);
  }

  @Test
  public void testCdgForTryCatch() {
    String methodName = "tryCatch";
    MethodSignature methodSignature =
        getMethodSignature(tryCatchFinallyClass, methodName, "void", Collections.emptyList());

    assertCdgMethod(methodSignature);
  }

  @Test
  public void testCdgForTryCatchNested() {
    String methodName = "tryCatchNested";
    MethodSignature methodSignature =
        getMethodSignature(tryCatchFinallyClass, methodName, "void", Collections.emptyList());

    assertCdgMethod(methodSignature);
  }

  @Test
  public void testCdgForTryCatchFinallyNested() {
    String methodName = "tryCatchFinallyNested";
    MethodSignature methodSignature =
        getMethodSignature(tryCatchFinallyClass, methodName, "void", Collections.emptyList());

    assertCdgMethod(methodSignature);
  }

  @Test
  public void testCdgForTryCatchFinallyNestedInFinally() {
    String methodName = "tryCatchFinallyNestedInFinally";
    MethodSignature methodSignature =
        getMethodSignature(tryCatchFinallyClass, methodName, "void", Collections.emptyList());

    assertCdgMethod(methodSignature);
  }

  @Test
  public void testCdgForTryCatchFinallyCombined() {
    String methodName = "tryCatchFinallyCombined";
    MethodSignature methodSignature =
        getMethodSignature(tryCatchFinallyClass, methodName, "void", Collections.emptyList());

    assertCdgMethod(methodSignature);
  }

  @Test
  public void testCdgForTryCatchFinallyNestedInCatch() {
    String methodName = "tryCatchFinallyNestedInCatch";
    MethodSignature methodSignature =
        getMethodSignature(tryCatchFinallyClass, methodName, "void", Collections.emptyList());

    assertCdgMethod(methodSignature);
  }

  @Test
  public void testCdgForTryCatchFinally() {
    String methodName = "tryCatchFinally";
    MethodSignature methodSignature =
        getMethodSignature(tryCatchFinallyClass, methodName, "void", Collections.emptyList());

    assertCdgMethod(methodSignature);
  }

  @Test
  public void testCdgForTryCatchNestedInCatch() {
    String methodName = "tryCatchNestedInCatch";
    MethodSignature methodSignature =
        getMethodSignature(tryCatchFinallyClass, methodName, "void", Collections.emptyList());

    assertCdgMethod(methodSignature);
  }

  @Test
  public void testCdgForTryCatchCombined() {
    String methodName = "tryCatchCombined";
    MethodSignature methodSignature =
        getMethodSignature(tryCatchFinallyClass, methodName, "void", Collections.emptyList());

    assertCdgMethod(methodSignature);
  }

  @Test
  public void testCdgForSwitchCaseGroupedTargetsDefault() {
    String methodName = "switchCaseGroupedTargetsDefault";
    MethodSignature methodSignature =
        getMethodSignature(switchCaseStatement, methodName, "void", Collections.emptyList());

    assertCdgMethod(methodSignature);
  }

  @Test
  public void testCdgForSwitchWithSwitch() {
    String methodName = "switchWithSwitch";
    MethodSignature methodSignature =
        getMethodSignature(switchCaseStatement, methodName, "void", Collections.emptyList());

    assertCdgMethod(methodSignature);
  }

  @Test
  public void testCdgForSwitchCaseStatementInt() {
    String methodName = "switchCaseStatementInt";
    MethodSignature methodSignature =
        getMethodSignature(switchCaseStatement, methodName, "void", Collections.emptyList());

    assertCdgMethod(methodSignature);
  }

  @Test
  public void testCdgForSwitchCaseGroupedTargets() {
    String methodName = "switchCaseGroupedTargets";
    MethodSignature methodSignature =
        getMethodSignature(switchCaseStatement, methodName, "void", Collections.emptyList());

    assertCdgMethod(methodSignature);
  }

  @Test
  public void testCdgForSwitchCaseStatementEnum() {
    String methodName = "switchCaseStatementEnum";
    MethodSignature methodSignature =
        getMethodSignature(switchCaseStatement, methodName, "void", Collections.emptyList());

    assertCdgMethod(methodSignature);
  }

  @Test
  public void testCdgForSwitchCaseStatementCaseIncludingIf() {
    String methodName = "switchCaseStatementCaseIncludingIf";
    MethodSignature methodSignature =
        getMethodSignature(switchCaseStatement, methodName, "void", Collections.emptyList());

    assertCdgMethod(methodSignature);
  }

  @Test
  public void testCdgForSwitchCaseWithoutDefault() {
    String methodName = "switchCaseWithoutDefault";
    MethodSignature methodSignature =
        getMethodSignature(switchCaseStatement, methodName, "void", Collections.emptyList());

    assertCdgMethod(methodSignature);
  }

  private void assertCdgMethod(MethodSignature methodSignature) {
    Optional<? extends SootMethod> method = getMinimalTestSuiteMethod(methodSignature);
    assertTrue(method.isPresent());

    PropertyGraph cdgGraph = (new CdgCreator()).createGraph(method.get());

    System.out.println(cdgGraph.toDotGraph());
  }
}
