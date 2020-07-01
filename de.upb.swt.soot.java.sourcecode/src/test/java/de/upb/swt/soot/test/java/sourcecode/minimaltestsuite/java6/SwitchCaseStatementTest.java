/** @author: Hasitha Rajapakse */
package de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.java6;

import categories.Java8Test;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.MinimalSourceTestSuiteBase;
import java.util.*;
import org.junit.Ignore;
import org.junit.experimental.categories.Category;

@Category(Java8Test.class)
public class SwitchCaseStatementTest extends MinimalSourceTestSuiteBase {

  @Ignore
  public void test() {
    SootMethod method = loadMethod(getMethodSignature("switchCaseStatementEnum"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "r0 := @this: SwitchCaseStatement",
            "$r1 = <SwitchCaseStatement$Color: SwitchCaseStatement$Color RED>",
            "$r2 = \"\"",
            "$r3 = <SwitchCaseStatement$Color: SwitchCaseStatement$Color RED>",
            "if $r1 == $r3 goto label1",
            "$r4 = <SwitchCaseStatement$Color: SwitchCaseStatement$Color GREEN>",
            "if $r1 == $r4 goto label2",
            "goto label3",
            "label1:",
            "$r5 = <SwitchCaseStatement$Color: SwitchCaseStatement$Color RED>",
            "$r2 = \"red\"",
            "goto label4",
            "label2:",
            "$r6 = <SwitchCaseStatement$Color: SwitchCaseStatement$Color GREEN>",
            "$r2 = \"green\"",
            "goto label4",
            "label3:",
            "$r2 = \"invalid\"",
            "goto label4",
            "label4:",
            "return"));
  }

  @Ignore
  public void testSwitchWithInt() {
    // FIXME: [ms] Jimple is not correct
    // 1. multiple goto labels are null
    // 2. default label is missing
    // 3. order of statements is not correct: the assignment of case 2 is after *goto label* and
    // before another label
    // 4. $r1 = null (refers to "String str;" ) is NullType; current state: set it to UnknownType

    SootMethod method = loadMethod(getMethodSignature("switchCaseStatementInt"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "r0 := @this: SwitchCaseStatement",
            "$i0 = 5",
            "$u0 = null",
            "switch($i0)",
            "case 1: goto label1",
            "case 2: goto label2",
            "case 3: goto label3",
            "default: goto label4",
            "label1:",
            "$u0 = \"one\"",
            "goto label5",
            "label2:",
            "$u0 = \"two\"",
            "goto label5",
            "label3:",
            "$u0 = \"three\"",
            "goto label5",
            "label4:",
            "$u0 = \"invalid\"",
            "label5:",
            "return"));
  }

  @Ignore
  public void testSwitchCaseWithoutDefault() {
    // FIXME: [ms] Jimple is not correct: target labels are wrong and jumped code has an offset by 1
    // another stmt
    SootMethod method = loadMethod(getMethodSignature("switchCaseWithoutDefault"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "r0 := @this: SwitchCaseStatement",
            "$i0 = 6",
            "$u0 = null",
            "switch($i0)",
            "case 1: goto label1",
            "case 2: goto label2",
            "case 3: goto label3",
            "default: goto label4",
            "label1:",
            "$u0 = \"one\"",
            "goto label4",
            "label2:",
            "$u0 = \"two\"",
            "goto label4",
            "label3:",
            "$u0 = \"three\"",
            "label4:",
            "return"));
  }

  @Ignore
  public void testSwitchCaseGroupedTargets() {
    // FIXME: [ms] Jimple is not correct; stmt in case as well as the target labels have an offset
    // by one
    SootMethod method = loadMethod(getMethodSignature("switchCaseGroupedTargets"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "r0 := @this: SwitchCaseStatement",
            "$i0 = 7",
            "$u0 = null",
            "switch($i0)",
            "case 1: goto label1",
            "case 2: goto label1",
            "case 3: goto label2",
            "default: goto label3",
            "label1:",
            "$u0 = \"first\"",
            "goto label4",
            "label2:",
            "$u0 = \"second\"",
            "goto label4",
            "label4:",
            "return"));
  }

  @Ignore
  public void testSwitchCaseGroupedTargetsDefault() {
    SootMethod method = loadMethod(getMethodSignature("switchCaseGroupedTargetsDefault"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "r0 := @this: SwitchCaseStatement",
            "$i0 = 8",
            "$u0 = null",
            "switch($i0)",
            "case 1: goto label1",
            "case 2: goto label1",
            "case 3: goto label2",
            "default: goto label3",
            "label1:",
            "$u0 = \"first\"",
            "goto label4",
            "label2:",
            "$u0 = \"second\"",
            "goto label4",
            "label3:",
            "$u0 = \"other\"",
            "label4:",
            "return"));
  }

  public MethodSignature getMethodSignature(String methodName) {
    return identifierFactory.getMethodSignature(
        methodName, getDeclaredClassSignature(), "void", Collections.emptyList());
  }
}
