package sootup.java.bytecode.minimaltestsuite.java7;

import categories.Java8Test;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.bytecode.minimaltestsuite.MinimalBytecodeTestSuiteBase;

/** @author Kaustubh Kelkar */
@Category(Java8Test.class)
public class SwitchCaseStatementWithStringTest extends MinimalBytecodeTestSuiteBase {

  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "switchCaseStatementString", "void", Collections.emptyList());
  }

  public MethodSignature getMethodSignature2() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "switchCaseStatementInt", "void", Collections.emptyList());
  }

  public MethodSignature getMethodSignature3() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "switchCaseStatementEnum", "void", Collections.emptyList());
  }

  /**
   *
   *
   * <pre>
   *  public void switchCaseStatementString() {
   *    String key = "something";
   *    int retVal;
   *    switch ( key ) {
   *    case "one":
   *      retVal = 1;
   *      break;
   *    case "two":
   *      retVal = 2;
   *      break;
   *    case "three":
   *      retVal = 3;
   *      break;
   *    default:
   *      retVal = -1;
   *    }
   *  }
   * </pre>
   */
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

  /**
   *
   *
   * <pre>
   *    public void switchCaseStatementInt() {
   *         int num = 2;
   *         String str;
   *         switch (num) {
   *             case 1:
   *                 str = "number 1 detected";
   *                 break;
   *             case 2:
   *                 str = "number 2 detected";
   *                 break;
   *             case 3:
   *                 str = "number 3 detected";
   *                 break;
   *             default:
   *                 str = "invalid number";
   *                 break;
   *         }
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmts2() {
    return Stream.of(
            "l0 := @this: SwitchCaseStatementWithString",
            "l1 = 2",
            "switch(l1)",
            "case 1: goto label1",
            "case 2: goto label2",
            "case 3: goto label3",
            "default: goto label4",
            "label1:",
            "l2 = \"number 1 detected\"",
            "goto label5",
            "label2:",
            "l2 = \"number 2 detected\"",
            "goto label5",
            "label3:",
            "l2 = \"number 3 detected\"",
            "goto label5",
            "label4:",
            "l2 = \"invalid number\"",
            "label5:",
            "return")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   *       public void switchCaseStatementEnum() {
   *         String color = "RED";
   *         String str = "";
   *         switch (Color.valueOf(color)) {
   *             case RED:
   *                 str = "color red detected";
   *                 break;
   *             case GREEN:
   *                 str = "color green detected";
   *                 break;
   *             default:
   *                 str = "invalid color";
   *                 break;
   *         }
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmts3() {
    return Stream.of(
            "l0 := @this: SwitchCaseStatementWithString",
            "l1 = \"RED\"",
            "l2 = \"\"",
            "$stack3 = <SwitchCaseStatementWithString$1: int[] $SwitchMap$Color>",
            "$stack4 = staticinvoke <Color: Color valueOf(java.lang.String)>(l1)",
            "$stack5 = virtualinvoke $stack4.<Color: int ordinal()>()",
            "$stack6 = $stack3[$stack5]",
            "switch($stack6)",
            "case 1: goto label1",
            "case 2: goto label2",
            "default: goto label3",
            "label1:",
            "l2 = \"color red detected\"",
            "goto label4",
            "label2:",
            "l2 = \"color green detected\"",
            "goto label4",
            "label3:",
            "l2 = \"invalid color\"",
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
