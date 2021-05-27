package de.upb.swt.soot.test.java.bytecode.minimaltestsuite.java14;

import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.bytecode.minimaltestsuite.MinimalBytecodeTestSuiteBase;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;

/** @author Bastian Haverkamp */
public class SwitchExprWithoutBreakTest extends MinimalBytecodeTestSuiteBase {

  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "switchSomething", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  /**
   *
   *
   * <pre>
   *     void switchSomething() {
   *     int k = 5;
   *     String s = "";
   *
   *     // new arrow syntax, will not fall through
   *     s = switch (k) {
   *       case 1 -> "single";
   *       case 2, 3 -> "double";
   *       default -> "somethingElse";
   *     };
   *
   *     // new arrow syntax + code block with new yield statement
   *     s = switch (k) {
   *       case 1 -> {
   *         yield "single";
   *       }
   *       case 2, 3 -> "double";
   *       default -> "somethingElse";
   *     };
   *
   *     // old syntax with new yield statement
   *     s = switch(k) {
   *       case 1:
   *         yield "no fall through";
   *       case 2,3:
   *         yield "still no fall through";
   *       default: {
   *         yield "we will not fall through";
   *       }
   *     };
   *
   *     System.out.println(s);
   *   }
   * </pre>
   */
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "l0 := @this: SwitchExprWithoutBreak",
            "l1 = 5",
            "l2 = \"\"",
            "switch(l1)",
            "case 1: goto label01",
            "case 2: goto label02",
            "case 3: goto label02",
            "default: goto label03",
            "label01:",
            "$stack8 = \"single\"",
            "goto label04",
            "label02:",
            "$stack8 = \"double\"",
            "goto label04",
            "label03:",
            "$stack8 = \"somethingElse\"",
            "label04:",
            "l2 = $stack8",
            "$stack9 = l1",
            "switch($stack9)",
            "case 1: goto label05",
            "case 2: goto label06",
            "case 3: goto label06",
            "default: goto label07",
            "label05:",
            "$stack6 = \"single\"",
            "goto label08",
            "label06:",
            "$stack6 = \"double\"",
            "goto label08",
            "label07:",
            "$stack6 = \"somethingElse\"",
            "label08:",
            "l2 = $stack6",
            "$stack7 = l1",
            "switch($stack7)",
            "case 1: goto label09",
            "case 2: goto label10",
            "case 3: goto label10",
            "default: goto label11",
            "label09:",
            "$stack4 = \"no fall through\"",
            "goto label12",
            "label10:",
            "$stack4 = \"still no fall through\"",
            "goto label12",
            "label11:",
            "$stack4 = \"we will not fall through\"",
            "label12:",
            "l2 = $stack4",
            "$stack3 = <java.lang.System: java.io.PrintStream out>",
            "$stack5 = l2",
            "virtualinvoke $stack3.<java.io.PrintStream: void println(java.lang.String)>($stack5)",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
  }
}
