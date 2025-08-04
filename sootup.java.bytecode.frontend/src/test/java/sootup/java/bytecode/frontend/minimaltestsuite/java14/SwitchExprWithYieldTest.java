package sootup.java.bytecode.frontend.minimaltestsuite.java14;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.bytecode.frontend.minimaltestsuite.MinimalBytecodeTestSuiteBase;
import sootup.java.core.types.JavaClassType;

/**
 * @author Bastian Haverkamp
 */
public class SwitchExprWithYieldTest extends MinimalBytecodeTestSuiteBase {

  @Override
  public JavaClassType getDeclaredClassSignature() {
    return identifierFactory.getClassType("SwitchExprWithYieldTest");
  }

  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "switchSomething", "void", Collections.emptyList());
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
   *     switch (k) {
   *       case 1:
   *         s += "single";
   *       default:
   *         s += "somethingElse";
   *     };
   *
   *     System.out.println(s);
   *   }
   * </pre>
   */
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "this := @this: SwitchExprWithYieldTest",
            "l1 = 5",
            "l2 = \"\"",
            "switch(l1)",
            "case 1: goto label01",
            "case 2: goto label02",
            "case 3: goto label02",
            "default: goto label03",
            "label01:",
            "l2 = \"single\"",
            "goto label04",
            "label02:",
            "l2 = \"double\"",
            "goto label04",
            "label03:",
            "l2 = \"somethingElse\"",
            "label04:",
            "switch(l1)",
            "case 1: goto label05",
            "case 2: goto label06",
            "case 3: goto label06",
            "default: goto label07",
            "label05:",
            "l2 = \"single\"",
            "goto label08",
            "label06:",
            "l2 = \"double\"",
            "goto label08",
            "label07:",
            "l2 = \"somethingElse\"",
            "label08:",
            "switch(l1)",
            "case 1: goto label09",
            "case 2: goto label10",
            "case 3: goto label10",
            "default: goto label11",
            "label09:",
            "l2 = \"no fall through\"",
            "goto label12",
            "label10:",
            "l2 = \"still no fall through\"",
            "goto label12",
            "label11:",
            "l2 = \"we will not fall through\"",
            "label12:",
            "switch(l1)",
            "case 1: goto label13",
            "default: goto label14",
            "label13:",
            "l2 = dynamicinvoke \"makeConcatWithConstants\" <java.lang.String (java.lang.String)>(l2) <java.lang.invoke.StringConcatFactory: java.lang.invoke.CallSite makeConcatWithConstants(java.lang.invoke.MethodHandles$Lookup,java.lang.String,java.lang.invoke.MethodType,java.lang.String,java.lang.Object[])>(\"\\u0001single\")",
            "label14:",
            "l2 = dynamicinvoke \"makeConcatWithConstants\" <java.lang.String (java.lang.String)>(l2) <java.lang.invoke.StringConcatFactory: java.lang.invoke.CallSite makeConcatWithConstants(java.lang.invoke.MethodHandles$Lookup,java.lang.String,java.lang.invoke.MethodType,java.lang.String,java.lang.Object[])>(\"\\u0001somethingElse\")",
            "$stack3 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $stack3.<java.io.PrintStream: void println(java.lang.String)>(l2)",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
  }
}
