/**
 * @author: Markus Schmidt
 * @author: Hasitha Rajapakse
 */
package sootup.java.sourcecode.minimaltestsuite.java7;

import categories.Java8Test;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.sourcecode.minimaltestsuite.MinimalSourceTestSuiteBase;

@Category(Java8Test.class)
public class SwitchCaseStatementWithStringTest extends MinimalSourceTestSuiteBase {

  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "switchCaseStatementString", "void", Collections.emptyList());
  }

  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: SwitchCaseStatementWithString",
            "$r1 = \"something\"",
            "$i0 = 0",
            "$i1 = \"one\"",
            "if $r1 == $i1 goto label3",
            "$i2 = \"two\"",
            "if $r1 == $i2 goto label2",
            "$i3 = \"three\"",
            "if $r1 == $i3 goto label1",
            "goto label4",
            "label1:",
            "$i0 = 3",
            "goto label5",
            "label2:",
            "$i0 = 2",
            "goto label5",
            "label3:",
            "$i0 = 1",
            "goto label5",
            "label4:",
            "$i4 = 0 - 1",
            "$i0 = $i4",
            "label5:",
            "return")
        .collect(Collectors.toList());
  }

  public MethodSignature getMethodSignature2() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "switchCaseStatementInt", "void", Collections.emptyList());
  }

  public List<String> expectedBodyStmts2() {
    return Stream.of(
            "r0 := @this: SwitchCaseStatementWithString",
            "$i0 = 2",
            "$u0 = null",
            "switch($i0)",
            "case 1: goto label1",
            "case 2: goto label2",
            "case 3: goto label3",
            "default: goto label4",
            "label1:",
            "$u0 = \"number 1 detected\"",
            "goto label6",
            "label2:",
            "$u0 = \"number 2 detected\"",
            "goto label6",
            "label3:",
            "$u0 = \"number 3 detected\"",
            "goto label6",
            "label4:",
            "goto label5",
            "label5:",
            "$u0 = \"invalid number\"",
            "goto label6",
            "label6:",
            "return")
        .collect(Collectors.toList());
  }

  public MethodSignature getMethodSignature3() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "switchCaseStatementEnum", "void", Collections.emptyList());
  }

  public List<String> expectedBodyStmts3() {
    return Stream.of(
            "r0 := @this: SwitchCaseStatementWithString",
            "$r1 = \"RED\"",
            "$r2 = \"\"",
            "$r3 = staticinvoke <Color: Color valueOf(java.lang.String)>($r1)",
            "$r4 = <Color: Color RED>",
            "if $r3 == $r4 goto label2",
            "$r5 = <Color: Color GREEN>",
            "if $r3 == $r5 goto label1",
            "goto label3",
            "label1:",
            "$r7 = <Color: Color GREEN>",
            "$r2 = \"color green detected\"",
            "goto label4",
            "label2:",
            "$r6 = <Color: Color RED>",
            "$r2 = \"color red detected\"",
            "goto label4",
            "label3:",
            "$r2 = \"invalid color\"",
            "goto label4",
            "label4:",
            "return")
        .collect(Collectors.toList());
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());

    SootMethod method2 = loadMethod(getMethodSignature2());
    assertJimpleStmts(method2, expectedBodyStmts2());

    SootMethod method3 = loadMethod(getMethodSignature3());
    assertJimpleStmts(method3, expectedBodyStmts3());
  }
}
