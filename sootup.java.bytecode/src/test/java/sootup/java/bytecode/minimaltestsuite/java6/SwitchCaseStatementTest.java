package sootup.java.bytecode.minimaltestsuite.java6;

import categories.Java8Test;
import java.util.Collections;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.bytecode.minimaltestsuite.MinimalBytecodeTestSuiteBase;

/** @author Kaustubh Kelkar */
@Category(Java8Test.class)
public class SwitchCaseStatementTest extends MinimalBytecodeTestSuiteBase {

  @Test
  public void switchCaseStatementEnum() {
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

  @Test
  public void switchCaseStatementCaseIncludingIf() {
    SootMethod method = loadMethod(getMethodSignature("switchCaseStatementCaseIncludingIf"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "l0 := @this: SwitchCaseStatement",
            "l1 = 2",
            "switch(l1)",
            "case 1: goto label1",
            "case 2: goto label3",
            "case 3: goto label4",
            "default: goto label5",
            "label1:",
            "l2 = 1",
            "if l1 != 666 goto label2",
            "l2 = 11",
            "goto label6",
            "label2:",
            "l2 = 12",
            "goto label6",
            "label3:",
            "l2 = 2",
            "goto label6",
            "label4:",
            "l2 = 3",
            "goto label6",
            "label5:",
            "l2 = -1",
            "label6:",
            "return"));
  }

  @Test
  public void switchCaseStatementCaseIncludingSwitch() {
    SootMethod method = loadMethod(getMethodSignature("switchWithSwitch"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "l0 := @this: SwitchCaseStatement",
            "l1 = 2",
            "switch(l1)",
            "case 1: goto label01",
            "case 2: goto label05",
            "case 3: goto label10",
            "default: goto label11",
            "label01:",
            "switch(l1)",
            "case 10: goto label02",
            "case 20: goto label03",
            "default: goto label04",
            "label02:",
            "l2 = 11",
            "goto label04",
            "label03:",
            "l2 = 12",
            "label04:",
            "goto label12",
            "label05:",
            "l2 = 2",
            "switch(l1)",
            "case 20: goto label06",
            "case 30: goto label07",
            "case 40: goto label08",
            "default: goto label09",
            "label06:",
            "l2 = 220",
            "goto label09",
            "label07:",
            "l2 = 230",
            "goto label09",
            "label08:",
            "l2 = 240",
            "label09:",
            "goto label12",
            "label10:",
            "l2 = 3",
            "goto label12",
            "label11:",
            "l2 = -1",
            "label12:",
            "return"));
  }

  public MethodSignature getMethodSignature(String methodName) {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), methodName, "void", Collections.emptyList());
  }
}
