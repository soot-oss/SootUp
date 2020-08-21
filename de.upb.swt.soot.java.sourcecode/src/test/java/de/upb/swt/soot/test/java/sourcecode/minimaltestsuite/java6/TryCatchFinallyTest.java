/** @author: Hasitha Rajapakse */
package de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.java6;

import categories.Java8Test;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.MinimalSourceTestSuiteBase;
import java.util.Collections;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(Java8Test.class)
public class TryCatchFinallyTest extends MinimalSourceTestSuiteBase {

  @Test
  public void tryCatch() {
    SootMethod sootMethod = loadMethod(getMethodSignature("tryCatch"));
    assertJimpleStmts(
        sootMethod,
        expectedBodyStmts(
            "r0 := @this: TryCatchFinally",
            "label1:",
            "$r1 = \"\"",
            "$r1 = \"try\"",
            "$r2 = <java.lang.System: java.io.PrintStream out>",
            "label2:",
            "virtualinvoke $r2.<java.io.PrintStream: void println(java.lang.String)>($r1)",
            "goto label4",
            "label3:",
            "$r3 := @caughtexception",
            "$r4 = $r3",
            "$r1 = \"catch\"",
            "$r5 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r5.<java.io.PrintStream: void println(java.lang.String)>($r1)",
            "label4:",
            "return",
            "catch java.lang.Exception from label1 to label2 with label3"));
  }

  @Test
  public void tryCatchFinally() {
    SootMethod sootMethod = loadMethod(getMethodSignature("tryCatchFinally"));
    assertJimpleStmts(
        sootMethod,
        expectedBodyStmts(
            "r0 := @this: TryCatchFinally",
            "label1:",
            "$r1 = \"\"",
            "$r1 = \"try\"",
            "$r2 = <java.lang.System: java.io.PrintStream out>",
            "label2:",
            "virtualinvoke $r2.<java.io.PrintStream: void println(java.lang.String)>($r1)",
            "goto label5",
            "label3:",
            "$r3 := @caughtexception",
            "$r4 = $r3",
            "$r1 = \"catch\"",
            "$r5 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r5.<java.io.PrintStream: void println(java.lang.String)>($r1)",
            "goto label5",
            "label4:",
            "$r6 := @caughtexception",
            "$r1 = \"finally\"",
            "$r7 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r7.<java.io.PrintStream: void println(java.lang.String)>($r1)",
            "throw $r6",
            "label5:",
            "$r1 = \"finally\"",
            "$r8 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r8.<java.io.PrintStream: void println(java.lang.String)>($r1)",
            "return",
            "catch java.lang.Exception from label1 to label2 with label3",
            "catch java.lang.Throwable from label1 to label2 with label4"));
  }

  @Test
  public void tryCatchCombined() {
    SootMethod sootMethod = loadMethod(getMethodSignature("tryCatchCombined"));
    assertJimpleStmts(
        sootMethod,
        expectedBodyStmts(
            "r0 := @this: TryCatchFinally",
            "label1:",
            "$r1 = \"\"",
            "$r1 = \"try\"",
            "$r2 = <java.lang.System: java.io.PrintStream out>",
            "label2:",
            "virtualinvoke $r2.<java.io.PrintStream: void println(java.lang.String)>($r1)",
            "goto label4",
            "label3:",
            "$r3 := @caughtexception",
            "$r4 = $r3",
            "$r1 = \"catch\"",
            "$r5 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r5.<java.io.PrintStream: void println(java.lang.String)>($r1)",
            "label4:",
            "return",
            "catch java.lang.RuntimeException from label1 to label2 with label3",
            "catch java.lang.StackOverflowError from label1 to label2 with label3"));
  }

  @Test
  public void tryCatchFinallyCombined() {
    SootMethod sootMethod = loadMethod(getMethodSignature("tryCatchFinallyCombined"));
    assertJimpleStmts(
        sootMethod,
        expectedBodyStmts(
            "r0 := @this: TryCatchFinally",
            "label1:",
            "$r1 = \"\"",
            "$r1 = \"try\"",
            "$r2 = <java.lang.System: java.io.PrintStream out>",
            "label2:",
            "virtualinvoke $r2.<java.io.PrintStream: void println(java.lang.String)>($r1)",
            "goto label5",
            "label3:",
            "$r3 := @caughtexception",
            "$r4 = $r3",
            "$r1 = \"catch\"",
            "$r5 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r5.<java.io.PrintStream: void println(java.lang.String)>($r1)",
            "goto label5",
            "label4:",
            "$r6 := @caughtexception",
            "$r1 = \"finally\"",
            "$r7 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r7.<java.io.PrintStream: void println(java.lang.String)>($r1)",
            "throw $r6",
            "label5:",
            "$r1 = \"finally\"",
            "$r8 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r8.<java.io.PrintStream: void println(java.lang.String)>($r1)",
            "return",
            "catch java.lang.RuntimeException from label1 to label2 with label3",
            "catch java.lang.StackOverflowError from label1 to label2 with label3",
            "catch java.lang.Throwable from label1 to label2 with label4"));
  }

  @Test
  public void tryCatchNested() {
    SootMethod sootMethod = loadMethod(getMethodSignature("tryCatchNested"));
    assertJimpleStmts(
        sootMethod,
        expectedBodyStmts(
            "r0 := @this: TryCatchFinally",
            "$r1 = \"\"",
            "$r1 = \"1try\"",
            "$r2 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r2.<java.io.PrintStream: void println(java.lang.String)>($r1)",
            "label1:",
            "$r1 = \"2try\"",
            "$r3 = <java.lang.System: java.io.PrintStream out>",
            "label2:",
            "virtualinvoke $r3.<java.io.PrintStream: void println(java.lang.String)>($r1)",
            "goto label4",
            "label3:",
            "$r4 := @caughtexception",
            "$r5 = $r4",
            "$r1 = \"2catch\"",
            "$r6 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r6.<java.io.PrintStream: void println(java.lang.String)>($r1)",
            "label4:",
            "goto label6",
            "label5:",
            "$r7 := @caughtexception",
            "$r8 = $r7",
            "$r1 = \"1catch\"",
            "$r9 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r9.<java.io.PrintStream: void println(java.lang.String)>($r1)",
            "label6:",
            "return",
            "catch java.lang.Exception from label1 to label2 with label3",
            "catch java.lang.Exception from label1 to label2 with label5"));
  }

  @Test
  public void tryCatchFinallyNested() {
    SootMethod sootMethod = loadMethod(getMethodSignature("tryCatchFinallyNested"));
    assertJimpleStmts(
        sootMethod,
        expectedBodyStmts(
            "r0 := @this: TryCatchFinally",
            "$r1 = \"\"",
            "$r1 = \"1try\"",
            "$r2 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r2.<java.io.PrintStream: void println(java.lang.String)>($r1)",
            "label1:",
            "$r1 = \"2try\"",
            "$r3 = <java.lang.System: java.io.PrintStream out>",
            "label2:",
            "virtualinvoke $r3.<java.io.PrintStream: void println(java.lang.String)>($r1)",
            "goto label4",
            "label3:",
            "$r4 := @caughtexception",
            "$r5 = $r4",
            "$r1 = \"2catch\"",
            "$r6 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r6.<java.io.PrintStream: void println(java.lang.String)>($r1)",
            "label4:",
            "goto label7",
            "label5:",
            "$r7 := @caughtexception",
            "$r8 = $r7",
            "$r1 = \"1catch\"",
            "$r9 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r9.<java.io.PrintStream: void println(java.lang.String)>($r1)",
            "goto label7",
            "label6:",
            "$r10 := @caughtexception",
            "$r1 = \"1finally\"",
            "$r11 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r11.<java.io.PrintStream: void println(java.lang.String)>($r1)",
            "throw $r10",
            "label7:",
            "$r1 = \"1finally\"",
            "$r12 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r12.<java.io.PrintStream: void println(java.lang.String)>($r1)",
            "return",
            "catch java.lang.Exception from label1 to label2 with label3",
            "catch java.lang.Exception from label1 to label2 with label5",
            "catch java.lang.Throwable from label1 to label2 with label6"));
  }

  @Test
  public void tryCatchNestedInCatch() {
    SootMethod sootMethod = loadMethod(getMethodSignature("tryCatchNestedInCatch"));
    assertJimpleStmts(
        sootMethod,
        expectedBodyStmts(
            "r0 := @this: TryCatchFinally",
            "label1:",
            "$r1 = \"\"",
            "$r1 = \"1try\"",
            "$r2 = <java.lang.System: java.io.PrintStream out>",
            "label2:",
            "virtualinvoke $r2.<java.io.PrintStream: void println(java.lang.String)>($r1)",
            "goto label7",
            "label3:",
            "$r3 := @caughtexception",
            "$r4 = $r3",
            "$r1 = \"1catch\"",
            "$r5 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r5.<java.io.PrintStream: void println(java.lang.String)>($r1)",
            "label4:",
            "$r1 = \"2try\"",
            "$r6 = <java.lang.System: java.io.PrintStream out>",
            "label5:",
            "virtualinvoke $r6.<java.io.PrintStream: void println(java.lang.String)>($r1)",
            "goto label7",
            "label6:",
            "$r7 := @caughtexception",
            "$r8 = $r7",
            "$r1 = \"2catch\"",
            "$r9 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r9.<java.io.PrintStream: void println(java.lang.String)>($r1)",
            "label7:",
            "return",
            "catch java.lang.Exception from label1 to label2 with label3",
            "catch java.lang.Exception from label4 to label5 with label6"));
  }

  @Test
  public void tryCatchFinallyNestedInCatch() {
    SootMethod sootMethod = loadMethod(getMethodSignature("tryCatchFinallyNestedInCatch"));
    assertJimpleStmts(
        sootMethod,
        expectedBodyStmts(
            "r0 := @this: TryCatchFinally",
            "label1:",
            "$r1 = \"\"",
            "$r1 = \"1try\"",
            "$r2 = <java.lang.System: java.io.PrintStream out>",
            "label2:",
            "virtualinvoke $r2.<java.io.PrintStream: void println(java.lang.String)>($r1)",
            "goto label6",
            "label3:",
            "$r3 := @caughtexception",
            "$r4 = $r3",
            "$r1 = \"1catch\"",
            "$r5 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r5.<java.io.PrintStream: void println(java.lang.String)>($r1)",
            "goto label7",
            "label4:",
            "$r6 := @caughtexception",
            "$r1 = \"1finally\"",
            "$r7 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r7.<java.io.PrintStream: void println(java.lang.String)>($r1)",
            "throw $r6",
            "label5:",
            "$r9 := @caughtexception",
            "$r10 = $r9",
            "$r1 = \"2catch\"",
            "$r11 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r11.<java.io.PrintStream: void println(java.lang.String)>($r1)",
            "label6:",
            "$r1 = \"1finally\"",
            "$r12 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r12.<java.io.PrintStream: void println(java.lang.String)>($r1)",
            "return",
            "label7:",
            "$r1 = \"2try\"",
            "$r8 = <java.lang.System: java.io.PrintStream out>",
            "label8:",
            "virtualinvoke $r8.<java.io.PrintStream: void println(java.lang.String)>($r1)",
            "goto label6",
            "catch java.lang.Exception from label1 to label2 with label3",
            "catch java.lang.Throwable from label1 to label2 with label4",
            "catch java.lang.Exception from label7 to label8 with label5"));
  }

  @Test
  public void tryCatchFinallyNestedInFinally() {
    SootMethod sootMethod = loadMethod(getMethodSignature("tryCatchFinallyNestedInFinally"));
    assertJimpleStmts(
        sootMethod,
        expectedBodyStmts(
            "r0 := @this: TryCatchFinally",
            "label01:",
            "$r1 = \"\"",
            "$r1 = \"1try\"",
            "$r2 = <java.lang.System: java.io.PrintStream out>",
            "label02:",
            "virtualinvoke $r2.<java.io.PrintStream: void println(java.lang.String)>($r1)",
            "goto label11",
            "label03:",
            "$r3 := @caughtexception",
            "$r4 = $r3",
            "$r1 = \"1catch\"",
            "$r5 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r5.<java.io.PrintStream: void println(java.lang.String)>($r1)",
            "goto label11",
            "label04:",
            "$r6 := @caughtexception",
            "$r1 = \"1finally\"",
            "$r7 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r7.<java.io.PrintStream: void println(java.lang.String)>($r1)",
            "label05:",
            "$r1 = \"2try\"",
            "$r8 = <java.lang.System: java.io.PrintStream out>",
            "label06:",
            "virtualinvoke $r8.<java.io.PrintStream: void println(java.lang.String)>($r1)",
            "goto label08",
            "label07:",
            "$r9 := @caughtexception",
            "$r10 = $r9",
            "$r1 = \"2catch\"",
            "$r11 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r11.<java.io.PrintStream: void println(java.lang.String)>($r1)",
            "label08:",
            "throw $r6",
            "label09:",
            "$r14 := @caughtexception",
            "$r15 = $r14",
            "$r1 = \"2catch\"",
            "$r16 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r16.<java.io.PrintStream: void println(java.lang.String)>($r1)",
            "label10:",
            "return",
            "label11:",
            "$r1 = \"1finally\"",
            "$r12 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r12.<java.io.PrintStream: void println(java.lang.String)>($r1)",
            "label12:",
            "$r1 = \"2try\"",
            "$r13 = <java.lang.System: java.io.PrintStream out>",
            "label13:",
            "virtualinvoke $r13.<java.io.PrintStream: void println(java.lang.String)>($r1)",
            "goto label10",
            "catch java.lang.Exception from label01 to label02 with label03",
            "catch java.lang.Throwable from label01 to label02 with label04",
            "catch java.lang.Throwable from label05 to label06 with label07",
            "catch java.lang.Exception from label12 to label13 with label09"));
  }

  public MethodSignature getMethodSignature(String methodName) {
    return identifierFactory.getMethodSignature(
        methodName, getDeclaredClassSignature(), "void", Collections.emptyList());
  }
}
