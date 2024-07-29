package sootup.codepropertygraph.benchmark;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import sootup.codepropertygraph.BenchmarkTestSuiteBase;
import sootup.codepropertygraph.ast.AstCreator;
import sootup.codepropertygraph.cdg.CdgCreator;
import sootup.codepropertygraph.cfg.CfgCreator;
import sootup.codepropertygraph.cpg.CpgCreator;
import sootup.codepropertygraph.ddg.DdgCreator;
import sootup.codepropertygraph.propertygraph.PropertyGraph;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;

public class CpgBenchmarkTest extends BenchmarkTestSuiteBase {
  private final ClassType ifElseStatementClass = getClassType("IfElseStatement");
  private final ClassType tryCatchFinallyClass = getClassType("TryCatchFinally");
  private final ClassType switchCaseStatementClass = getClassType("SwitchCaseStatement");
  private final ClassType whileLoopClass = getClassType("WhileLoop");

  @Test
  public void testCpgForIfStatement() {
    String methodName = "ifStatement";
    MethodSignature methodSignature =
        getMethodSignature(
            ifElseStatementClass, methodName, "int", Collections.singletonList("int"));

    assertCpgMethod(methodSignature);
  }

  @Test
  public void testCpgForIfElseStatement() {
    String methodName = "ifElseStatement";
    MethodSignature methodSignature =
        getMethodSignature(
            ifElseStatementClass, methodName, "int", Collections.singletonList("int"));

    assertCpgMethod(methodSignature);
  }

  @Test
  public void testCpgForIfElseIfStatement() {
    String methodName = "ifElseIfStatement";
    MethodSignature methodSignature =
        getMethodSignature(
            ifElseStatementClass, methodName, "int", Collections.singletonList("int"));

    assertCpgMethod(methodSignature);
  }

  @Test
  public void testCpgForIfElseCascadingStatement() {
    String methodName = "ifElseCascadingStatement";
    MethodSignature methodSignature =
        getMethodSignature(
            ifElseStatementClass, methodName, "int", Collections.singletonList("int"));

    assertCpgMethod(methodSignature);
  }

  @Test
  public void testCpgForIfElseCascadingElseIfStatement() {
    String methodName = "ifElseCascadingElseIfStatement";
    MethodSignature methodSignature =
        getMethodSignature(
            ifElseStatementClass, methodName, "int", Collections.singletonList("int"));

    assertCpgMethod(methodSignature);
  }

  @Test
  public void testCpgForIfElseCascadingElseIfInElseStatement() {
    String methodName = "ifElseCascadingElseIfInElseStatement";
    MethodSignature methodSignature =
        getMethodSignature(
            ifElseStatementClass, methodName, "int", Collections.singletonList("int"));

    assertCpgMethod(methodSignature);
  }

  @Test
  public void testCpgForTryCatch() {
    String methodName = "tryCatch";
    MethodSignature methodSignature =
        getMethodSignature(tryCatchFinallyClass, methodName, "void", Collections.emptyList());

    assertCpgMethod(methodSignature);
  }

  @Test
  public void testCpgForTryCatchNested() {
    String methodName = "tryCatchNested";
    MethodSignature methodSignature =
        getMethodSignature(tryCatchFinallyClass, methodName, "void", Collections.emptyList());

    assertCpgMethod(methodSignature);
  }

  @Test
  public void testCpgForTryCatchFinallyNested() {
    String methodName = "tryCatchFinallyNested";
    MethodSignature methodSignature =
        getMethodSignature(tryCatchFinallyClass, methodName, "void", Collections.emptyList());

    assertCpgMethod(methodSignature);
  }

  @Test
  public void testCpgForTryCatchFinallyNestedInFinally() {
    String methodName = "tryCatchFinallyNestedInFinally";
    MethodSignature methodSignature =
        getMethodSignature(tryCatchFinallyClass, methodName, "void", Collections.emptyList());

    assertCpgMethod(methodSignature);
  }

  @Test
  public void testCpgForTryCatchFinallyCombined() {
    String methodName = "tryCatchFinallyCombined";
    MethodSignature methodSignature =
        getMethodSignature(tryCatchFinallyClass, methodName, "void", Collections.emptyList());

    assertCpgMethod(methodSignature);
  }

  @Test
  public void testCpgForTryCatchFinallyNestedInCatch() {
    String methodName = "tryCatchFinallyNestedInCatch";
    MethodSignature methodSignature =
        getMethodSignature(tryCatchFinallyClass, methodName, "void", Collections.emptyList());

    assertCpgMethod(methodSignature);
  }

  @Test
  public void testCpgForTryCatchFinally() {
    String methodName = "tryCatchFinally";
    MethodSignature methodSignature =
        getMethodSignature(tryCatchFinallyClass, methodName, "void", Collections.emptyList());

    assertCpgMethod(methodSignature);
  }

  @Test
  public void testCpgForTryCatchNestedInCatch() {
    String methodName = "tryCatchNestedInCatch";
    MethodSignature methodSignature =
        getMethodSignature(tryCatchFinallyClass, methodName, "void", Collections.emptyList());

    assertCpgMethod(methodSignature);
  }

  @Test
  public void testCpgForTryCatchCombined() {
    String methodName = "tryCatchCombined";
    MethodSignature methodSignature =
        getMethodSignature(tryCatchFinallyClass, methodName, "void", Collections.emptyList());

    assertCpgMethod(methodSignature);
  }

  @Test
  public void testCpgForSwitchCaseGroupedTargetsDefault() {
    String methodName = "switchCaseGroupedTargetsDefault";
    MethodSignature methodSignature =
        getMethodSignature(switchCaseStatementClass, methodName, "void", Collections.emptyList());

    assertCpgMethod(methodSignature);
  }

  @Test
  public void testCpgForSwitchWithSwitch() {
    String methodName = "switchWithSwitch";
    MethodSignature methodSignature =
        getMethodSignature(switchCaseStatementClass, methodName, "void", Collections.emptyList());

    assertCpgMethod(methodSignature);
  }

  @Test
  public void testCpgForSwitchCaseStatementInt() {
    String methodName = "switchCaseStatementInt";
    MethodSignature methodSignature =
        getMethodSignature(switchCaseStatementClass, methodName, "void", Collections.emptyList());

    assertCpgMethod(methodSignature);
  }

  @Test
  public void testCpgForSwitchCaseGroupedTargets() {
    String methodName = "switchCaseGroupedTargets";
    MethodSignature methodSignature =
        getMethodSignature(switchCaseStatementClass, methodName, "void", Collections.emptyList());

    assertCpgMethod(methodSignature);
  }

  @Test
  public void testCpgForSwitchCaseStatementEnum() {
    String methodName = "switchCaseStatementEnum";
    MethodSignature methodSignature =
        getMethodSignature(switchCaseStatementClass, methodName, "void", Collections.emptyList());

    assertCpgMethod(methodSignature);
  }

  @Test
  public void testCpgForSwitchCaseStatementCaseIncludingIf() {
    String methodName = "switchCaseStatementCaseIncludingIf";
    MethodSignature methodSignature =
        getMethodSignature(switchCaseStatementClass, methodName, "void", Collections.emptyList());

    assertCpgMethod(methodSignature);
  }

  @Test
  public void testCpgForSwitchCaseWithoutDefault() {
    String methodName = "switchCaseWithoutDefault";
    MethodSignature methodSignature =
        getMethodSignature(switchCaseStatementClass, methodName, "void", Collections.emptyList());

    assertCpgMethod(methodSignature);
  }

  @Test
  public void testCpgForWhileLoop() {
    String methodName = "whileLoop";
    MethodSignature methodSignature =
        getMethodSignature(whileLoopClass, methodName, "void", Collections.emptyList());

    assertCpgMethod(methodSignature);
  }

  private void assertCpgMethod(MethodSignature methodSignature) {
    Optional<? extends SootMethod> method = getMinimalTestSuiteMethod(methodSignature);
    assertTrue(method.isPresent());

    PropertyGraph cpgGraph =
        (new CpgCreator(new AstCreator(), new CfgCreator(), new CdgCreator(), new DdgCreator()))
            .createCpg(method.get());

    System.out.println(cpgGraph.toDotGraph());
  }
}
