package sootup.java.sourcecode.minimaltestsuite.java6;

import categories.Java8Test;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Ignore;
import org.junit.experimental.categories.Category;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.sourcecode.minimaltestsuite.MinimalSourceTestSuiteBase;

/**
 * @author Hasitha Rajapakse
 * @author Kaustubh Kelkar
 */
@Category(Java8Test.class)
public class SwitchCaseStatementTest extends MinimalSourceTestSuiteBase {

  @Ignore
  // FIXME: [ms] only 3 successor flows from switch but 4 needed
  public void switchCaseStatementEnumKey() {
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

  @Ignore
  // FIXME:[ms] buggy jimple from sourcecodefrontend
  public void switchCaseStatementCaseIncludingIf() {
    SootMethod method = loadMethod(getMethodSignature("switchCaseStatementCaseIncludingIf"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "r0 := @this: SwitchCaseStatement",
            "$i0 = 2",
            "$i1 = 0",
            "switch($i0)",
            "case 1: goto label4",
            "case 2: goto [?= null]",
            "case 3: goto [?= null]",
            "default: goto label1",
            "label1:",
            "$i1 = 1",
            "$z0 = $i0 == 666",
            "if $z0 == 0 goto label2",
            "$i1 = 11",
            "goto label3",
            "label2:",
            "$i1 = 12",
            "label3:",
            "goto label5",
            "$i1 = 2",
            "goto label5",
            "$i1 = 3",
            "label4:",
            "goto label5",
            "label5:",
            "return"));
  }

  @Ignore
  // FIXME:[ms] buggy jimple from sourcecodefrontend
  public void switchCaseStatementCaseIncludingSwitch() {
    SootMethod method = loadMethod(getMethodSignature("switchWithSwitch"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "r0 := @this: SwitchCaseStatement",
            "$i0 = 2",
            "$i1 = 0",
            "switch($i0)",
            "case 1: goto label2",
            "case 2: goto [?= null]",
            "case 3: goto [?= null]",
            "default: goto label1",
            "label1:",
            "switch($i0)",
            "case 10: goto label4",
            "case 20: goto [?= null]",
            "default: goto label3",
            "label2:",
            "$i1 = 11",
            "label3:",
            "goto label4",
            "$i1 = 12",
            "goto label4",
            "label4:",
            "goto label8",
            "$i1 = 2",
            "switch($i0)",
            "case 20: goto label6",
            "case 30: goto [?= null]",
            "case 40: goto [?= null]",
            "default: goto label5",
            "$i1 = 220",
            "goto label7",
            "label5:",
            "$i1 = 230",
            "goto label7",
            "$i1 = 240",
            "label6:",
            "goto label7",
            "label7:",
            "goto label8",
            "$i1 = 3",
            "goto label8",
            "label8:",
            "return"));
  }

  public MethodSignature getMethodSignature(String methodName) {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), methodName, "void", Collections.emptyList());
  }

  public List<String> expectedBodyStmtsSwitchCaseStatementEnum() {
    return Stream.of(
            "r0 := @this: SwitchCaseStatement",
            "$r1 = \"RED\"",
            "$r2 = \"\"",
            "$r3 = staticinvoke <SwitchCaseStatement$Color: SwitchCaseStatement$Color valueOf(java.lang.String)>($r1)",
            "$r4 = <SwitchCaseStatement$Color: SwitchCaseStatement$Color RED>",
            "if $r3 == $r4 goto label1",
            "$r5 = <SwitchCaseStatement$Color: SwitchCaseStatement$Color GREEN>",
            "if $r3 == $r5 goto label2",
            "goto label3",
            "label1:",
            "$r6 = <SwitchCaseStatement$Color: SwitchCaseStatement$Color RED>",
            "$r2 = \"color red detected\"",
            "goto label4",
            "label2:",
            "$r7 = <SwitchCaseStatement$Color: SwitchCaseStatement$Color GREEN>",
            "$r2 = \"color green detected\"",
            "goto label4",
            "label3:",
            "$r2 = \"invalid color\"",
            "goto label4",
            "label4:",
            "return")
        .collect(Collectors.toList());
  }
}
