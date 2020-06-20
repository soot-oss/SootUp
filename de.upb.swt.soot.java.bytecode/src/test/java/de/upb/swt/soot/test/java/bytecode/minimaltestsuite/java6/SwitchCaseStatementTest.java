package de.upb.swt.soot.test.java.bytecode.minimaltestsuite.java6;

import categories.Java8Test;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.bytecode.minimaltestsuite.MinimalBytecodeTestSuiteBase;
import java.util.Collections;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author Kaustubh Kelkar */
@Category(Java8Test.class)
public class SwitchCaseStatementTest extends MinimalBytecodeTestSuiteBase {

  @Test
  public void testEnum() {
    SootMethod method = loadMethod(getMethodSignature("switchCaseStatementEnum"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "l0 := @this: SwitchCaseStatement",
            "l1 = <SwitchCaseStatement$Color: SwitchCaseStatement$Color RED>",
            "l2 = \"\"",
            "$stack3 = <SwitchCaseStatement$1: int[] $SwitchMap$SwitchCaseStatement$Color>",
            "$stack4 = virtualinvoke l1.<SwitchCaseStatement$Color: int ordinal()>()",
            "$stack5 = $stack3[$stack4]",
            "switch($stack5)",
            "case 1: goto label1",
            "case 2: goto label2",
            "default: goto label3",
            "label1:",
            "l2 = \"red\"",
            "goto label4",
            "label2:",
            "l2 = \"green\"",
            "goto label4",
            "label3:",
            "l2 = \"invalid\"",
            "label4:",
            "return"));
  }

  @Test
  public void testSwitchInt() {
    SootMethod method = loadMethod(getMethodSignature("switchCaseStatementInt"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "l0 := @this: SwitchCaseStatement",
            "l1 = 5",
            "switch(l1)",
            "case 1: goto label1",
            "case 2: goto label2",
            "case 3: goto label3",
            "default: goto label4",
            "label1:",
            "l2 = \"one\"",
            "goto label5",
            "label2:",
            "l2 = \"two\"",
            "goto label5",
            "label3:",
            "l2 = \"three\"",
            "goto label5",
            "label4:",
            "l2 = \"invalid\"",
            "label5:",
            "return"));
  }

  @Test
  public void testSwitchCaseWithoutDefault() {
    SootMethod method = loadMethod(getMethodSignature("switchCaseWithoutDefault"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "l0 := @this: SwitchCaseStatement",
            "l1 = 6",
            "switch(l1)",
            "case 1: goto label1",
            "case 2: goto label2",
            "case 3: goto label3",
            "default: goto label4",
            "label1:",
            "l2 = \"one\"",
            "goto label4",
            "label2:",
            "l2 = \"two\"",
            "goto label4",
            "label3:",
            "l2 = \"three\"",
            "label4:",
            "return"));
  }

  @Test
  public void testSwitchCaseGroupedTargets() {
    SootMethod method = loadMethod(getMethodSignature("switchCaseGroupedTargets"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "l0 := @this: SwitchCaseStatement",
            "l1 = 7",
            "switch(l1)",
            "case 1: goto label1",
            "case 2: goto label1",
            "case 3: goto label2",
            "default: goto label3",
            "label1:",
            "l2 = \"first\"",
            "goto label3",
            "label2:",
            "l2 = \"second\"",
            "label3:",
            "return"));
  }

  @Test
  public void testSwitchCaseGroupedTargetsDefault() {
    SootMethod method = loadMethod(getMethodSignature("switchCaseGroupedTargetsDefault"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "l0 := @this: SwitchCaseStatement",
            "l1 = 8",
            "switch(l1)",
            "case 1: goto label1",
            "case 2: goto label1",
            "case 3: goto label2",
            "default: goto label3",
            "label1:",
            "l2 = \"first\"",
            "goto label4",
            "label2:",
            "l2 = \"second\"",
            "goto label4",
            "label3:",
            "l2 = \"other\"",
            "label4:",
            "return"));
  }

  public MethodSignature getMethodSignature(String methodName) {
    return identifierFactory.getMethodSignature(
        methodName, getDeclaredClassSignature(), "void", Collections.emptyList());
  }
}
