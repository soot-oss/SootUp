package de.upb.swt.soot.test.java.bytecode.minimaltestsuite.java7;

import categories.Java8Test;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.bytecode.minimaltestsuite.MinimalBytecodeTestSuiteBase;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author Kaustubh Kelkar */
@Category(Java8Test.class)
public class SwitchCaseStatementWithStringTest extends MinimalBytecodeTestSuiteBase {

  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "switchCaseStatementString", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  /**  <pre>
   * public void switchCaseStatementEnum() {
   * String color = "RED";
   * String str = "";
   * switch (Color.valueOf(color)){
   * case RED:
   * str = "color red detected";
   * break;
   * case GREEN:
   * str = "color green detected";
   * break;
   * default:
   * str = "invalid color";
   * break;
   * }
   * public void switchCaseStatementInt() {
   * int num = 2;
   * String str;
   * switch (num) {
   * case 1:  str = "number 1 detected";
   * break;
   * case 2:  str = "number 2 detected";
   * break;
   * case 3:  str = "number 3 detected";
   * break;
   * default: str = "invalid number";
   * break;
   * }
   *
   * <pre>*/
  /**  <pre>
   * public void switchCaseStatementString() {
   * String key = "something";
   * int retVal;
   * switch ( key ) {
   * case "one":  ;
   * retVal = 1;
   * break;
   * case "two":
   * retVal = 2;
   * break;
   * case "three":
   * retVal = 3;
   * break;
   * default:
   * retVal = -1;
   * }
   *
   * <pre>*/
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "l0 := @this: SwitchCaseStatementWithString",
            "l1 = \"something\"",
            "l3 = l1",
            "l4 = -1",
            "$stack5 = virtualinvoke l3.<java.lang.String: int hashCode()>()",
            "switch($stack5)",
            "case 110182: goto label1",
            "case 115276: goto label2",
            "case 110339486: goto label3",
            "default: goto label4",
            "label1:",
            "$stack9 = virtualinvoke l3.<java.lang.String: boolean equals(java.lang.Object)>(\"one\")",
            "if $stack9 == 0 goto label4",
            "l4 = 0",
            "goto label4",
            "label2:",
            "$stack8 = virtualinvoke l3.<java.lang.String: boolean equals(java.lang.Object)>(\"two\")",
            "if $stack8 == 0 goto label4",
            "l4 = 1",
            "goto label4",
            "label3:",
            "$stack6 = virtualinvoke l3.<java.lang.String: boolean equals(java.lang.Object)>(\"three\")",
            "if $stack6 == 0 goto label4",
            "l4 = 2",
            "label4:",
            "$stack7 = l4",
            "switch($stack7)",
            "case 0: goto label5",
            "case 1: goto label6",
            "case 2: goto label7",
            "default: goto label8",
            "label5:",
            "l2 = 1",
            "goto label9",
            "label6:",
            "l2 = 2",
            "goto label9",
            "label7:",
            "l2 = 3",
            "goto label9",
            "label8:",
            "l2 = -1",
            "label9:",
            "return")
        .collect(Collectors.toList());
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
  }
}
