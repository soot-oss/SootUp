package sootup.codepropertygraph.cfg;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import sootup.codepropertygraph.CpgTestSuiteBase;
import sootup.codepropertygraph.propertygraph.PropertyGraph;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;

public class CfgMinimalTest extends CpgTestSuiteBase {
  private final ClassType ifElseStatementClass = getClassType("IfElseStatement");
  private final ClassType tryCatchFinallyClass = getClassType("TryCatchFinally");
  private final ClassType switchCaseStatement = getClassType("SwitchCaseStatement");

  @Test
  public void testCfgForIfStatement() {
    String methodName = "ifStatement";
    MethodSignature methodSignature =
        getMethodSignature(
            ifElseStatementClass, methodName, "int", Collections.singletonList("int"));

    assertCfgMethod(methodSignature);
  }

  @Test
  public void testCfgForIfElseStatement() {
    String methodName = "ifElseStatement";
    MethodSignature methodSignature =
        getMethodSignature(
            ifElseStatementClass, methodName, "int", Collections.singletonList("int"));

    assertCfgMethod(methodSignature);
  }

  @Test
  public void testCfgForIfElseIfStatement() {
    String methodName = "ifElseIfStatement";
    MethodSignature methodSignature =
        getMethodSignature(
            ifElseStatementClass, methodName, "int", Collections.singletonList("int"));

    assertCfgMethod(methodSignature);
  }

  @Test
  public void testCfgForIfElseCascadingStatement() {
    String methodName = "ifElseCascadingStatement";
    MethodSignature methodSignature =
        getMethodSignature(
            ifElseStatementClass, methodName, "int", Collections.singletonList("int"));

    assertCfgMethod(methodSignature);
  }

  @Test
  public void testCfgForIfElseCascadingElseIfStatement() {
    String methodName = "ifElseCascadingElseIfStatement";
    MethodSignature methodSignature =
        getMethodSignature(
            ifElseStatementClass, methodName, "int", Collections.singletonList("int"));

    assertCfgMethod(methodSignature);
  }

  @Test
  public void testCfgForIfElseCascadingElseIfInElseStatement() {
    String methodName = "ifElseCascadingElseIfInElseStatement";
    MethodSignature methodSignature =
        getMethodSignature(
            ifElseStatementClass, methodName, "int", Collections.singletonList("int"));

    assertCfgMethod(methodSignature);
  }

  @Test
  public void testCfgForTryCatch() {
    String methodName = "tryCatch";
    MethodSignature methodSignature =
        getMethodSignature(tryCatchFinallyClass, methodName, "void", Collections.emptyList());

    assertCfgMethod(methodSignature);
  }

  @Test
  public void testCfgForTryCatchNested() {
    String methodName = "tryCatchNested";
    MethodSignature methodSignature =
        getMethodSignature(tryCatchFinallyClass, methodName, "void", Collections.emptyList());

    assertCfgMethod(methodSignature);
  }

  @Test
  public void testCfgForTryCatchFinallyNested() {
    String methodName = "tryCatchFinallyNested";
    MethodSignature methodSignature =
        getMethodSignature(tryCatchFinallyClass, methodName, "void", Collections.emptyList());

    assertCfgMethod(methodSignature);
  }

  @Test
  public void testCfgForTryCatchFinallyNestedInFinally() {
    String methodName = "tryCatchFinallyNestedInFinally";
    MethodSignature methodSignature =
        getMethodSignature(tryCatchFinallyClass, methodName, "void", Collections.emptyList());

    assertCfgMethod(methodSignature);
  }

  @Test
  public void testCfgForTryCatchFinallyCombined() {
    String methodName = "tryCatchFinallyCombined";
    MethodSignature methodSignature =
        getMethodSignature(tryCatchFinallyClass, methodName, "void", Collections.emptyList());

    assertCfgMethod(methodSignature);
  }

  @Test
  public void testCfgForTryCatchFinallyNestedInCatch() {
    String methodName = "tryCatchFinallyNestedInCatch";
    MethodSignature methodSignature =
        getMethodSignature(tryCatchFinallyClass, methodName, "void", Collections.emptyList());

    assertCfgMethod(methodSignature);
  }

  @Test
  public void testCfgForTryCatchFinally() {
    String methodName = "tryCatchFinally";
    MethodSignature methodSignature =
        getMethodSignature(tryCatchFinallyClass, methodName, "void", Collections.emptyList());

    assertCfgMethod(methodSignature);
  }

  @Test
  public void testCfgForTryCatchNestedInCatch() {
    String methodName = "tryCatchNestedInCatch";
    MethodSignature methodSignature =
        getMethodSignature(tryCatchFinallyClass, methodName, "void", Collections.emptyList());

    assertCfgMethod(methodSignature);
  }

  @Test
  public void testCfgForTryCatchCombined() {
    String methodName = "tryCatchCombined";
    MethodSignature methodSignature =
        getMethodSignature(tryCatchFinallyClass, methodName, "void", Collections.emptyList());

    assertCfgMethod(methodSignature);
  }

  @Test
  public void testCfgForSwitchCaseGroupedTargetsDefault() {
    String methodName = "switchCaseGroupedTargetsDefault";
    MethodSignature methodSignature =
            getMethodSignature(switchCaseStatement, methodName, "void", Collections.emptyList());

    assertCfgMethod(methodSignature);
  }

  @Test
  public void testCfgForSwitchWithSwitch() {
    String methodName = "switchWithSwitch";
    MethodSignature methodSignature =
            getMethodSignature(switchCaseStatement, methodName, "void", Collections.emptyList());

    assertCfgMethod(methodSignature);
  }

  @Test
  public void testCfgForSwitchCaseStatementInt() {
    String methodName = "switchCaseStatementInt";
    MethodSignature methodSignature =
            getMethodSignature(switchCaseStatement, methodName, "void", Collections.emptyList());

    assertCfgMethod(methodSignature);
  }

  @Test
  public void testCfgForSwitchCaseGroupedTargets() {
    String methodName = "switchCaseGroupedTargets";
    MethodSignature methodSignature =
            getMethodSignature(switchCaseStatement, methodName, "void", Collections.emptyList());

    assertCfgMethod(methodSignature);
  }

  @Test
  public void testCfgForSwitchCaseStatementEnum() {
    String methodName = "switchCaseStatementEnum";
    MethodSignature methodSignature =
            getMethodSignature(switchCaseStatement, methodName, "void", Collections.emptyList());

    assertCfgMethod(methodSignature);
  }

  @Test
  public void testCfgForSwitchCaseStatementCaseIncludingIf() {
    String methodName = "switchCaseStatementCaseIncludingIf";
    MethodSignature methodSignature =
            getMethodSignature(switchCaseStatement, methodName, "void", Collections.emptyList());

    assertCfgMethod(methodSignature);
  }

  @Test
  public void testCfgForSwitchCaseWithoutDefault() {
    String methodName = "switchCaseWithoutDefault";
    MethodSignature methodSignature =
            getMethodSignature(switchCaseStatement, methodName, "void", Collections.emptyList());

    assertCfgMethod(methodSignature);
  }

  private void assertCfgMethod(MethodSignature methodSignature) {
    Optional<? extends SootMethod> method = getMinimalTestSuiteMethod(methodSignature);
    assertTrue(method.isPresent());

    PropertyGraph cfgGraph = new CfgCreator().createGraph(method.get());

    System.out.println(cfgGraph.toDotGraph());
  }
}
