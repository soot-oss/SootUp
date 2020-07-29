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

  // FIXME: [ms] sourcecodefrontend does not add Traps yet to connect unexceptional flows with
  // traphandlers!

  @Test
  public void tryCatch() {
    SootMethod sootMethod = loadMethod(getMethodSignature("tryCatch"));
    assertJimpleStmts(
        sootMethod,
        expectedBodyStmts(
            "r0 := @this: TryCatchFinally",
            "$r1 = \"\"",
            "$r1 = \"try\"",
            "$r2 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r2.<java.io.PrintStream: void println(java.lang.String)>($r1)",
            "goto label1",
            "$r3 := @caughtexception",
            "$r4 = $r3",
            "$r1 = \"catch\"",
            "$r5 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r5.<java.io.PrintStream: void println(java.lang.String)>($r1)",
            "label1:",
            "return"));
  }

  @Test
  public void tryCatchFinally() {
    SootMethod sootMethod = loadMethod(getMethodSignature("tryCatchFinally"));
    assertJimpleStmts(
        sootMethod,
        expectedBodyStmts(
            "r0 := @this: TryCatchFinally",
            "$r1 = \"\"",
            "$r1 = \"try\"",
            "$r2 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r2.<java.io.PrintStream: void println(java.lang.String)>($r1)",
            "goto label1",
            "$r3 := @caughtexception",
            "$r4 = $r3",
            "$r1 = \"catch\"",
            "$r5 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r5.<java.io.PrintStream: void println(java.lang.String)>($r1)",
            "goto label1",
            "$r6 := @caughtexception",
            "$r1 = \"finally\"",
            "$r7 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r7.<java.io.PrintStream: void println(java.lang.String)>($r1)",
            "throw $r6",
            "label1:",
            "$r1 = \"finally\"",
            "$r8 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r8.<java.io.PrintStream: void println(java.lang.String)>($r1)",
            "return"));
  }

  @Test
  public void tryCatchCombined() {
    SootMethod sootMethod = loadMethod(getMethodSignature("tryCatchCombined"));
    assertJimpleStmts(
        sootMethod,
        expectedBodyStmts(
            "r0 := @this: TryCatchFinally",
            "$r1 = \"\"",
            "$r1 = \"try\"",
            "$r2 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r2.<java.io.PrintStream: void println(java.lang.String)>($r1)",
            "goto label1",
            "$r3 := @caughtexception",
            "$r4 = $r3",
            "$r1 = \"catch\"",
            "$r5 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r5.<java.io.PrintStream: void println(java.lang.String)>($r1)",
            "label1:",
            "return"));
  }

  @Test
  public void tryCatchFinallyCombined() {
    SootMethod sootMethod = loadMethod(getMethodSignature("tryCatchFinallyCombined"));
    assertJimpleStmts(
        sootMethod,
        expectedBodyStmts(
            "r0 := @this: TryCatchFinally",
            "$r1 = \"\"",
            "$r1 = \"try\"",
            "$r2 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r2.<java.io.PrintStream: void println(java.lang.String)>($r1)",
            "goto label1",
            "$r3 := @caughtexception",
            "$r4 = $r3",
            "$r1 = \"catch\"",
            "$r5 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r5.<java.io.PrintStream: void println(java.lang.String)>($r1)",
            "goto label1",
            "$r6 := @caughtexception",
            "$r1 = \"finally\"",
            "$r7 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r7.<java.io.PrintStream: void println(java.lang.String)>($r1)",
            "throw $r6",
            "label1:",
            "$r1 = \"finally\"",
            "$r8 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r8.<java.io.PrintStream: void println(java.lang.String)>($r1)",
            "return"));
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
            "$r1 = \"2try\"",
            "$r3 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r3.<java.io.PrintStream: void println(java.lang.String)>($r1)",
            "goto label1",
            "$r4 := @caughtexception",
            "$r5 = $r4",
            "$r1 = \"2catch\"",
            "$r6 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r6.<java.io.PrintStream: void println(java.lang.String)>($r1)",
            "label1:",
            "goto label2",
            "$r7 := @caughtexception",
            "$r8 = $r7",
            "$r1 = \"1catch\"",
            "$r9 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r9.<java.io.PrintStream: void println(java.lang.String)>($r1)",
            "label2:",
            "return"));
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
            "$r1 = \"2try\"",
            "$r3 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r3.<java.io.PrintStream: void println(java.lang.String)>($r1)",
            "goto label1",
            "$r4 := @caughtexception",
            "$r5 = $r4",
            "$r1 = \"2catch\"",
            "$r6 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r6.<java.io.PrintStream: void println(java.lang.String)>($r1)",
            "label1:",
            "goto label2",
            "$r7 := @caughtexception",
            "$r8 = $r7",
            "$r1 = \"1catch\"",
            "$r9 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r9.<java.io.PrintStream: void println(java.lang.String)>($r1)",
            "goto label2",
            "$r10 := @caughtexception",
            "$r1 = \"1finally\"",
            "$r11 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r11.<java.io.PrintStream: void println(java.lang.String)>($r1)",
            "throw $r10",
            "label2:",
            "$r1 = \"1finally\"",
            "$r12 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r12.<java.io.PrintStream: void println(java.lang.String)>($r1)",
            "return"));
  }

  @Test
  public void tryCatchNestedInCatch() {
    SootMethod sootMethod = loadMethod(getMethodSignature("tryCatchNestedInCatch"));
    assertJimpleStmts(
        sootMethod,
        expectedBodyStmts(
            "r0 := @this: TryCatchFinally",
            "$r1 = \"\"",
            "$r1 = \"1try\"",
            "$r2 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r2.<java.io.PrintStream: void println(java.lang.String)>($r1)",
            "goto label1",
            "$r3 := @caughtexception",
            "$r4 = $r3",
            "$r1 = \"1catch\"",
            "$r5 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r5.<java.io.PrintStream: void println(java.lang.String)>($r1)",
            "$r1 = \"2try\"",
            "$r6 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r6.<java.io.PrintStream: void println(java.lang.String)>($r1)",
            "goto label1",
            "$r7 := @caughtexception",
            "$r8 = $r7",
            "$r1 = \"2catch\"",
            "$r9 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r9.<java.io.PrintStream: void println(java.lang.String)>($r1)",
            "label1:",
            "return"));
  }

  @Test
  public void tryCatchFinallyNestedInCatch() {
    SootMethod sootMethod = loadMethod(getMethodSignature("tryCatchFinallyNestedInCatch"));
    assertJimpleStmts(
        sootMethod,
        expectedBodyStmts(
            "r0 := @this: TryCatchFinally",
            "$r1 = \"\"",
            "$r1 = \"1try\"",
            "$r2 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r2.<java.io.PrintStream: void println(java.lang.String)>($r1)",
            "goto label2",
            "$r3 := @caughtexception",
            "$r4 = $r3",
            "$r1 = \"1catch\"",
            "$r5 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r5.<java.io.PrintStream: void println(java.lang.String)>($r1)",
            "goto label1",
            "$r6 := @caughtexception",
            "$r1 = \"1finally\"",
            "$r7 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r7.<java.io.PrintStream: void println(java.lang.String)>($r1)",
            "throw $r6",
            "label1:",
            "$r1 = \"2try\"",
            "$r8 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r8.<java.io.PrintStream: void println(java.lang.String)>($r1)",
            "goto label2",
            "$r9 := @caughtexception",
            "$r10 = $r9",
            "$r1 = \"2catch\"",
            "$r11 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r11.<java.io.PrintStream: void println(java.lang.String)>($r1)",
            "label2:",
            "$r1 = \"1finally\"",
            "$r12 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r12.<java.io.PrintStream: void println(java.lang.String)>($r1)",
            "return"));
  }

  @Test
  public void tryCatchFinallyNestedInFinally() {
    SootMethod sootMethod = loadMethod(getMethodSignature("tryCatchFinallyNestedInFinally"));
    assertJimpleStmts(
        sootMethod,
        expectedBodyStmts(
            "r0 := @this: TryCatchFinally",
            "$r1 = \"\"",
            "$r1 = \"1try\"",
            "$r2 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r2.<java.io.PrintStream: void println(java.lang.String)>($r1)",
            "goto label2",
            "$r3 := @caughtexception",
            "$r4 = $r3",
            "$r1 = \"1catch\"",
            "$r5 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r5.<java.io.PrintStream: void println(java.lang.String)>($r1)",
            "goto label2",
            "$r6 := @caughtexception",
            "$r1 = \"1finally\"",
            "$r7 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r7.<java.io.PrintStream: void println(java.lang.String)>($r1)",
            "$r1 = \"2try\"",
            "$r8 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r8.<java.io.PrintStream: void println(java.lang.String)>($r1)",
            "goto label1",
            "$r9 := @caughtexception",
            "$r10 = $r9",
            "$r1 = \"2catch\"",
            "$r11 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r11.<java.io.PrintStream: void println(java.lang.String)>($r1)",
            "label1:",
            "throw $r6",
            "label2:",
            "$r1 = \"1finally\"",
            "$r12 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r12.<java.io.PrintStream: void println(java.lang.String)>($r1)",
            "$r1 = \"2try\"",
            "$r13 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r13.<java.io.PrintStream: void println(java.lang.String)>($r1)",
            "goto label3",
            "$r14 := @caughtexception",
            "$r15 = $r14",
            "$r1 = \"2catch\"",
            "$r16 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r16.<java.io.PrintStream: void println(java.lang.String)>($r1)",
            "label3:",
            "return"));
  }

  public MethodSignature getMethodSignature(String methodName) {
    return identifierFactory.getMethodSignature(
        methodName, getDeclaredClassSignature(), "void", Collections.emptyList());
  }
}
