package sootup.java.bytecode.minimaltestsuite.java6;

import categories.Java8Test;
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

/** @author Kaustubh Kelkar, Markus Schmidt */
@Category(Java8Test.class)
public class TryCatchFinallyTest extends MinimalBytecodeTestSuiteBase {

  @Test
  public void tryCatch() {
    SootMethod sootMethod = loadMethod(getMethodSignature("tryCatch"));
    assertJimpleStmts(sootMethod, expectedBodyStmtsTryCatch());
  }

  @Test
  public void tryCatchFinally() {
    SootMethod sootMethod = loadMethod(getMethodSignature("tryCatchFinally"));
    assertJimpleStmts(sootMethod, expectedBodyStmtsTryCatchFinally());
  }

  @Test
  public void tryCatchCombined() {
    SootMethod sootMethod = loadMethod(getMethodSignature("tryCatchCombined"));
    assertJimpleStmts(sootMethod, expectedBodyStmtsTryCatchCombined());
  }

  @Test
  public void tryCatchFinallyCombined() {
    SootMethod sootMethod = loadMethod(getMethodSignature("tryCatchFinallyCombined"));
    assertJimpleStmts(sootMethod, expectedBodyStmtsTryCatchFinallyCombined());
  }

  @Test
  public void tryCatchNested() {
    SootMethod sootMethod = loadMethod(getMethodSignature("tryCatchNested"));
    assertJimpleStmts(sootMethod, expectedBodyStmtsTryCatchNested());
  }

  @Test
  public void tryCatchFinallyNested() {
    SootMethod sootMethod = loadMethod(getMethodSignature("tryCatchFinallyNested"));
    assertJimpleStmts(sootMethod, expectedBodyStmtsTryCatchFinallyNested());
  }

  @Test
  public void tryCatchNestedInCatch() {
    SootMethod sootMethod = loadMethod(getMethodSignature("tryCatchNestedInCatch"));
    assertJimpleStmts(sootMethod, expectedBodyStmtsTryCatchNestedInCatch());
  }

  @Test
  public void tryCatchFinallyNestedInCatch() {
    SootMethod sootMethod = loadMethod(getMethodSignature("tryCatchFinallyNestedInCatch"));
    assertJimpleStmts(sootMethod, expectedBodyStmtsTryCatchFinallyNestedInCatch());
  }

  @Test
  public void tryCatchFinallyNestedInFinally() {
    SootMethod sootMethod = loadMethod(getMethodSignature("tryCatchFinallyNestedInFinally"));
    assertJimpleStmts(sootMethod, expectedBodyStmtsTryCatchFinallyNestedInFinally());
  }

  public MethodSignature getMethodSignature(String methodName) {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), methodName, "void", Collections.emptyList());
  }

  /**
   *
   *
   * <pre>
   *     public void tryCatch() {
   *         String str = "";
   *         try {
   *             str = "try";
   *             System.out.println(str);
   *         } catch (Exception e) {
   *             str = "catch";
   *             System.out.println(str);
   *         }
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsTryCatch() {
    return Stream.of(
            "l0 := @this: TryCatchFinally",
            "l1 = \"\"",
            "label1:",
            "l1 = \"try\"",
            "$stack3 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $stack3.<java.io.PrintStream: void println(java.lang.String)>(l1)",
            "label2:",
            "goto label4",
            "label3:",
            "$stack4 := @caughtexception",
            "l2 = $stack4",
            "l1 = \"catch\"",
            "$stack5 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $stack5.<java.io.PrintStream: void println(java.lang.String)>(l1)",
            "label4:",
            "return",
            "catch java.lang.Exception from label1 to label2 with label3")
        .collect(Collectors.toCollection(ArrayList::new));
  }

  /**
   *
   *
   * <pre>
   *     public void tryCatchFinally() {
   *         String str = "";
   *         try {
   *             str = "try";
   *             System.out.println(str);
   *         } catch (Exception e) {
   *             str = "catch";
   *             System.out.println(str);
   *         } finally {
   *             str = "finally";
   *             System.out.println(str);
   *         }
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsTryCatchFinally() {
    return Stream.of(
            "l0 := @this: TryCatchFinally",
            "l1 = \"\"",
            "label1:",
            "l1 = \"try\"",
            "$stack4 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $stack4.<java.io.PrintStream: void println(java.lang.String)>(l1)",
            "label2:",
            "l1 = \"finally\"",
            "$stack5 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $stack5.<java.io.PrintStream: void println(java.lang.String)>(l1)",
            "goto label6",
            "label3:",
            "$stack8 := @caughtexception",
            "l2 = $stack8",
            "l1 = \"catch\"",
            "$stack9 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $stack9.<java.io.PrintStream: void println(java.lang.String)>(l1)",
            "label4:",
            "l1 = \"finally\"",
            "$stack10 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $stack10.<java.io.PrintStream: void println(java.lang.String)>(l1)",
            "goto label6",
            "label5:",
            "$stack6 := @caughtexception",
            "l3 = $stack6",
            "l1 = \"finally\"",
            "$stack7 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $stack7.<java.io.PrintStream: void println(java.lang.String)>(l1)",
            "throw l3",
            "label6:",
            "return",
            "catch java.lang.Exception from label1 to label2 with label3",
            "catch java.lang.Throwable from label1 to label2 with label5",
            "catch java.lang.Throwable from label3 to label4 with label5")
        .collect(Collectors.toCollection(ArrayList::new));
  }

  /**
   *
   *
   * <pre>
   *     public void tryCatchCombined() {
   *         String str = "";
   *         try {
   *             str = "try";
   *             System.out.println(str);
   *         } catch (RuntimeException | StackOverflowError e) {
   *             str = "catch";
   *             System.out.println(str);
   *         }
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsTryCatchCombined() {
    return Stream.of(
            "l0 := @this: TryCatchFinally",
            "l1 = \"\"",
            "label1:",
            "l1 = \"try\"",
            "$stack3 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $stack3.<java.io.PrintStream: void println(java.lang.String)>(l1)",
            "label2:",
            "goto label4",
            "label3:",
            "$stack4 := @caughtexception",
            "l2 = $stack4",
            "l1 = \"catch\"",
            "$stack5 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $stack5.<java.io.PrintStream: void println(java.lang.String)>(l1)",
            "label4:",
            "return",
            "catch java.lang.RuntimeException from label1 to label2 with label3",
            "catch java.lang.StackOverflowError from label1 to label2 with label3")
        .collect(Collectors.toCollection(ArrayList::new));
  }

  /**
   *
   *
   * <pre>
   *     public void tryCatchFinallyCombined() {
   *         String str = "";
   *         try {
   *             str = "try";
   *             System.out.println(str);
   *         } catch (RuntimeException | StackOverflowError e) {
   *             str = "catch";
   *             System.out.println(str);
   *         } finally {
   *             str = "finally";
   *             System.out.println(str);
   *         }
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsTryCatchFinallyCombined() {
    return Stream.of(
            "l0 := @this: TryCatchFinally",
            "l1 = \"\"",
            "label1:",
            "l1 = \"try\"",
            "$stack4 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $stack4.<java.io.PrintStream: void println(java.lang.String)>(l1)",
            "label2:",
            "l1 = \"finally\"",
            "$stack5 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $stack5.<java.io.PrintStream: void println(java.lang.String)>(l1)",
            "goto label6",
            "label3:",
            "$stack8 := @caughtexception",
            "l2 = $stack8",
            "l1 = \"catch\"",
            "$stack9 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $stack9.<java.io.PrintStream: void println(java.lang.String)>(l1)",
            "label4:",
            "l1 = \"finally\"",
            "$stack10 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $stack10.<java.io.PrintStream: void println(java.lang.String)>(l1)",
            "goto label6",
            "label5:",
            "$stack6 := @caughtexception",
            "l3 = $stack6",
            "l1 = \"finally\"",
            "$stack7 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $stack7.<java.io.PrintStream: void println(java.lang.String)>(l1)",
            "throw l3",
            "label6:",
            "return",
            "catch java.lang.RuntimeException from label1 to label2 with label3",
            "catch java.lang.StackOverflowError from label1 to label2 with label3",
            "catch java.lang.Throwable from label1 to label2 with label5",
            "catch java.lang.Throwable from label3 to label4 with label5")
        .collect(Collectors.toCollection(ArrayList::new));
  }

  /**
   *
   *
   * <pre>
   *     public void tryCatchNested() {
   *         String str = "";
   *         try {
   *             str = "1try";
   *             System.out.println(str);
   *             try {
   *                 str = "2try";
   *                 System.out.println(str);
   *             } catch (Exception e) {
   *                 str = "2catch";
   *                 System.out.println(str);
   *             }
   *         } catch (Exception e) {
   *             str = "1catch";
   *             System.out.println(str);
   *         }
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsTryCatchNested() {
    return Stream.of(
            "l0 := @this: TryCatchFinally",
            "l1 = \"\"",
            "label1:",
            "l1 = \"1try\"",
            "$stack3 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $stack3.<java.io.PrintStream: void println(java.lang.String)>(l1)",
            "label2:",
            "l1 = \"2try\"",
            "$stack4 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $stack4.<java.io.PrintStream: void println(java.lang.String)>(l1)",
            "label3:",
            "goto label7",
            "label4:",
            "$stack5 := @caughtexception",
            "l2 = $stack5",
            "l1 = \"1catch\"",
            "$stack6 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $stack6.<java.io.PrintStream: void println(java.lang.String)>(l1)",
            "label5:",
            "return",
            "label6:",
            "$stack7 := @caughtexception",
            "l2 = $stack7",
            "l1 = \"2catch\"",
            "$stack8 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $stack8.<java.io.PrintStream: void println(java.lang.String)>(l1)",
            "label7:",
            "goto label5",
            "catch java.lang.Exception from label1 to label2 with label4",
            "catch java.lang.Exception from label2 to label3 with label6",
            "catch java.lang.Exception from label3 to label4 with label4",
            "catch java.lang.Exception from label6 to label7 with label4")
        .collect(Collectors.toCollection(ArrayList::new));
  }

  /**
   *
   *
   * <pre>
   *     public void tryCatchFinallyNested() {
   *         String str = "";
   *         try {
   *             str = "1try";
   *             System.out.println(str);
   *             try {
   *                 str = "2try";
   *                 System.out.println(str);
   *             } catch (Exception e) {
   *                 str = "2catch";
   *                 System.out.println(str);
   *             }
   *         } catch (Exception e) {
   *             str = "1catch";
   *             System.out.println(str);
   *         }finally {
   *             str = "1finally";
   *             System.out.println(str);
   *         }
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsTryCatchFinallyNested() {
    return Stream.of(
            "l0 := @this: TryCatchFinally",
            "l1 = \"\"",
            "label1:",
            "l1 = \"1try\"",
            "$stack4 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $stack4.<java.io.PrintStream: void println(java.lang.String)>(l1)",
            "label2:",
            "l1 = \"2try\"",
            "$stack5 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $stack5.<java.io.PrintStream: void println(java.lang.String)>(l1)",
            "label3:",
            "goto label8",
            "label4:",
            "$stack9 := @caughtexception",
            "l2 = $stack9",
            "l1 = \"1catch\"",
            "$stack10 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $stack10.<java.io.PrintStream: void println(java.lang.String)>(l1)",
            "label5:",
            "l1 = \"1finally\"",
            "$stack11 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $stack11.<java.io.PrintStream: void println(java.lang.String)>(l1)",
            "goto label9",
            "label6:",
            "$stack7 := @caughtexception",
            "l3 = $stack7",
            "l1 = \"1finally\"",
            "$stack8 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $stack8.<java.io.PrintStream: void println(java.lang.String)>(l1)",
            "throw l3",
            "label7:",
            "$stack12 := @caughtexception",
            "l2 = $stack12",
            "l1 = \"2catch\"",
            "$stack13 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $stack13.<java.io.PrintStream: void println(java.lang.String)>(l1)",
            "label8:",
            "l1 = \"1finally\"",
            "$stack6 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $stack6.<java.io.PrintStream: void println(java.lang.String)>(l1)",
            "goto label9",
            "label9:",
            "return",
            "catch java.lang.Exception from label1 to label2 with label4",
            "catch java.lang.Throwable from label1 to label5 with label6",
            "catch java.lang.Exception from label2 to label3 with label7",
            "catch java.lang.Exception from label3 to label4 with label4",
            "catch java.lang.Exception from label7 to label8 with label4",
            "catch java.lang.Throwable from label7 to label8 with label6")
        .collect(Collectors.toCollection(ArrayList::new));
  }

  /**
   *
   *
   * <pre>
   *     public void tryCatchNestedInCatch() {
   *         String str = "";
   *         try {
   *             str = "1try";
   *             System.out.println(str);
   *         } catch (Exception e) {
   *             str = "1catch";
   *             System.out.println(str);
   *             try {
   *                 str = "2try";
   *                 System.out.println(str);
   *             } catch (Exception ex) {
   *                 str = "2catch";
   *                 System.out.println(str);
   *             }
   *         }
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsTryCatchNestedInCatch() {
    return Stream.of(
            "l0 := @this: TryCatchFinally",
            "l1 = \"\"",
            "label1:",
            "l1 = \"1try\"",
            "$stack4 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $stack4.<java.io.PrintStream: void println(java.lang.String)>(l1)",
            "label2:",
            "goto label7",
            "label3:",
            "$stack7 := @caughtexception",
            "l2 = $stack7",
            "l1 = \"1catch\"",
            "$stack8 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $stack8.<java.io.PrintStream: void println(java.lang.String)>(l1)",
            "label4:",
            "l1 = \"2try\"",
            "$stack9 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $stack9.<java.io.PrintStream: void println(java.lang.String)>(l1)",
            "label5:",
            "goto label7",
            "label6:",
            "$stack5 := @caughtexception",
            "l3 = $stack5",
            "l1 = \"2catch\"",
            "$stack6 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $stack6.<java.io.PrintStream: void println(java.lang.String)>(l1)",
            "label7:",
            "return",
            "catch java.lang.Exception from label1 to label2 with label3",
            "catch java.lang.Exception from label4 to label5 with label6")
        .collect(Collectors.toCollection(ArrayList::new));
  }

  /**
   *
   *
   * <pre>
   *     public void tryCatchFinallyNestedInCatch() {
   *         String str = "";
   *         try {
   *             str = "1try";
   *             System.out.println(str);
   *         } catch (Exception e) {
   *             str = "1catch";
   *             System.out.println(str);
   *             try {
   *                 str = "2try";
   *                 System.out.println(str);
   *             } catch (Exception ex) {
   *                 str = "2catch";
   *                 System.out.println(str);
   *             }
   *         }finally {
   *             str = "1finally";
   *             System.out.println(str);
   *         }
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsTryCatchFinallyNestedInCatch() {
    return Stream.of(
            "l0 := @this: TryCatchFinally",
            "l1 = \"\"",
            "label01:",
            "l1 = \"1try\"",
            "$stack5 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $stack5.<java.io.PrintStream: void println(java.lang.String)>(l1)",
            "label02:",
            "l1 = \"1finally\"",
            "$stack6 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $stack6.<java.io.PrintStream: void println(java.lang.String)>(l1)",
            "goto label10",
            "label03:",
            "$stack12 := @caughtexception",
            "l2 = $stack12",
            "l1 = \"1catch\"",
            "$stack13 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $stack13.<java.io.PrintStream: void println(java.lang.String)>(l1)",
            "label04:",
            "l1 = \"2try\"",
            "$stack14 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $stack14.<java.io.PrintStream: void println(java.lang.String)>(l1)",
            "label05:",
            "goto label09",
            "label06:",
            "$stack7 := @caughtexception",
            "l4 = $stack7",
            "label07:",
            "l1 = \"1finally\"",
            "$stack8 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $stack8.<java.io.PrintStream: void println(java.lang.String)>(l1)",
            "throw l4",
            "label08:",
            "$stack9 := @caughtexception",
            "l3 = $stack9",
            "l1 = \"2catch\"",
            "$stack10 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $stack10.<java.io.PrintStream: void println(java.lang.String)>(l1)",
            "label09:",
            "l1 = \"1finally\"",
            "$stack11 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $stack11.<java.io.PrintStream: void println(java.lang.String)>(l1)",
            "goto label10",
            "label10:",
            "return",
            "catch java.lang.Exception from label01 to label02 with label03",
            "catch java.lang.Throwable from label01 to label02 with label06",
            "catch java.lang.Throwable from label03 to label07 with label06",
            "catch java.lang.Exception from label04 to label05 with label08",
            "catch java.lang.Throwable from label08 to label09 with label06")
        .collect(Collectors.toCollection(ArrayList::new));
  }

  /**
   *
   *
   * <pre>
   *     public void tryCatchFinallyNestedInFinally() {
   *         String str = "";
   *         try {
   *             str = "1try";
   *             System.out.println(str);
   *         } catch (Exception e) {
   *             str = "1catch";
   *             System.out.println(str);
   *         }finally {
   *             str = "1finally";
   *             System.out.println(str);
   *             try {
   *                 str = "2try";
   *                 System.out.println(str);
   *             } catch (Exception e) {
   *                 str = "2catch";
   *                 System.out.println(str);
   *             }
   *         }
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsTryCatchFinallyNestedInFinally() {
    return Stream.of(
            "l0 := @this: TryCatchFinally",
            "l1 = \"\"",
            "label01:",
            "l1 = \"1try\"",
            "$stack5 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $stack5.<java.io.PrintStream: void println(java.lang.String)>(l1)",
            "label02:",
            "l1 = \"1finally\"",
            "$stack6 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $stack6.<java.io.PrintStream: void println(java.lang.String)>(l1)",
            "label03:",
            "l1 = \"2try\"",
            "$stack7 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $stack7.<java.io.PrintStream: void println(java.lang.String)>(l1)",
            "label04:",
            "goto label16",
            "label05:",
            "$stack19 := @caughtexception",
            "l2 = $stack19",
            "l1 = \"2catch\"",
            "$stack20 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $stack20.<java.io.PrintStream: void println(java.lang.String)>(l1)",
            "goto label16",
            "label06:",
            "$stack15 := @caughtexception",
            "l2 = $stack15",
            "l1 = \"1catch\"",
            "$stack16 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $stack16.<java.io.PrintStream: void println(java.lang.String)>(l1)",
            "label07:",
            "l1 = \"1finally\"",
            "$stack17 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $stack17.<java.io.PrintStream: void println(java.lang.String)>(l1)",
            "label08:",
            "l1 = \"2try\"",
            "$stack18 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $stack18.<java.io.PrintStream: void println(java.lang.String)>(l1)",
            "label09:",
            "goto label16",
            "label10:",
            "$stack13 := @caughtexception",
            "l2 = $stack13",
            "l1 = \"2catch\"",
            "$stack14 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $stack14.<java.io.PrintStream: void println(java.lang.String)>(l1)",
            "goto label16",
            "label11:",
            "$stack10 := @caughtexception",
            "l3 = $stack10",
            "l1 = \"1finally\"",
            "$stack11 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $stack11.<java.io.PrintStream: void println(java.lang.String)>(l1)",
            "label12:",
            "l1 = \"2try\"",
            "$stack12 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $stack12.<java.io.PrintStream: void println(java.lang.String)>(l1)",
            "label13:",
            "goto label15",
            "label14:",
            "$stack8 := @caughtexception",
            "l4 = $stack8",
            "l1 = \"2catch\"",
            "$stack9 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $stack9.<java.io.PrintStream: void println(java.lang.String)>(l1)",
            "label15:",
            "throw l3",
            "label16:",
            "return",
            "catch java.lang.Exception from label01 to label02 with label06",
            "catch java.lang.Throwable from label01 to label02 with label11",
            "catch java.lang.Exception from label03 to label04 with label05",
            "catch java.lang.Throwable from label06 to label07 with label11",
            "catch java.lang.Exception from label08 to label09 with label10",
            "catch java.lang.Exception from label12 to label13 with label14")
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
