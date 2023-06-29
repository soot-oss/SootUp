package sootup.java.bytecode.minimaltestsuite.java14;

import categories.Java9Test;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.bytecode.minimaltestsuite.MinimalBytecodeTestSuiteBase;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.types.JavaClassType;

/** @author Bastian Haverkamp */
@Category(Java9Test.class)
public class SwitchExprWithYieldTest extends MinimalBytecodeTestSuiteBase {

  @Override
  public JavaClassType getDeclaredClassSignature() {
    return JavaIdentifierFactory.getInstance().getClassType("SwitchExprWithYieldTest");
  }

  @Override
  public MethodSignature getMethodSignature() {
    System.out.println(getDeclaredClassSignature());
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
   *     System.out.println(s);
   *   }
   * </pre>
   */
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "l0 := @this: SwitchExprWithYieldTest",
            "l1 = 5",
            "l2 = \"\"",
            "switch(l1)",
            "case 1: goto label01",
            "case 2: goto label02",
            "case 3: goto label02",
            "default: goto label03",
            "label01:",
            "$stack11 = \"single\"",
            "goto label04",
            "label02:",
            "$stack11 = \"double\"",
            "goto label04",
            "label03:",
            "$stack11 = \"somethingElse\"",
            "label04:",
            "l2 = $stack11",
            "$stack12 = l1",
            "switch($stack12)",
            "case 1: goto label05",
            "case 2: goto label06",
            "case 3: goto label06",
            "default: goto label07",
            "label05:",
            "$stack9 = \"single\"",
            "goto label08",
            "label06:",
            "$stack9 = \"double\"",
            "goto label08",
            "label07:",
            "$stack9 = \"somethingElse\"",
            "label08:",
            "l2 = $stack9",
            "$stack10 = l1",
            "switch($stack10)",
            "case 1: goto label09",
            "case 2: goto label10",
            "case 3: goto label10",
            "default: goto label11",
            "label09:",
            "$stack7 = \"no fall through\"",
            "goto label12",
            "label10:",
            "$stack7 = \"still no fall through\"",
            "goto label12",
            "label11:",
            "$stack7 = \"we will not fall through\"",
            "label12:",
            "l2 = $stack7",
            "$stack8 = l1",
            "switch($stack8)",
            "case 1: goto label13",
            "default: goto label14",
            "label13:",
            "l2 = dynamicinvoke \"makeConcatWithConstants\" <java.lang.String (java.lang.String)>(l2) <java.lang.invoke.StringConcatFactory: java.lang.invoke.CallSite makeConcatWithConstants(java.lang.invoke.MethodHandles$Lookup,java.lang.String,java.lang.invoke.MethodType,java.lang.String,java.lang.Object[])>(\"\\u0001single\")",
            "label14:",
            "$stack4 = l2",
            "$stack5 = dynamicinvoke \"makeConcatWithConstants\" <java.lang.String (java.lang.String)>(l2) <java.lang.invoke.StringConcatFactory: java.lang.invoke.CallSite makeConcatWithConstants(java.lang.invoke.MethodHandles$Lookup,java.lang.String,java.lang.invoke.MethodType,java.lang.String,java.lang.Object[])>(\"\\u0001somethingElse\")",
            "$stack3 = <java.lang.System: java.io.PrintStream out>",
            "$stack6 = l2",
            "virtualinvoke $stack3.<java.io.PrintStream: void println(java.lang.String)>($stack6)",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
  }
}
