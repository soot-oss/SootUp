package sootup.java.bytecode.minimaltestsuite.java6;

import categories.Java8Test;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.model.SootMethod;
import sootup.java.bytecode.minimaltestsuite.MinimalBytecodeTestSuiteBase;

@Category(Java8Test.class)
public class LocalMergingTest extends MinimalBytecodeTestSuiteBase {
  @Test
  public void test() {
    SootMethod methodConstant =
        loadMethod(
            identifierFactory.getMethodSignature(
                getDeclaredClassSignature(),
                "localMergingWithConstant",
                "void",
                Collections.singletonList("int")));
    assertJimpleStmts(methodConstant, expectedBodyStmtsConstant());

    SootMethod methodOtherLocal =
        loadMethod(
            identifierFactory.getMethodSignature(
                getDeclaredClassSignature(),
                "localMergingWithOtherLocal",
                "void",
                Collections.singletonList("int")));
    assertJimpleStmts(methodOtherLocal, expectedBodyStmtsOtherLocal());

    SootMethod methodDuplicateValue =
        loadMethod(
            identifierFactory.getMethodSignature(
                getDeclaredClassSignature(),
                "localMergingWithDuplicateValue",
                "void",
                Collections.singletonList("int")));
    assertJimpleStmts(methodDuplicateValue, expectedBodyStmtsDuplicateValue());
  }

  /**
   *
   *
   * <pre>
   * public void localMergingWithConstant(int n) {
   *     String a = "one";
   *     // The branch returns either a local or a constant.
   *     // Because of the divergence neither `a` nor `"two"` should be inlined,
   *     // but a stack local variable should be created for holding the result of the branch.
   *     System.out.println(n == 1 ? a : "two");
   * }
   * </pre>
   */
  public List<String> expectedBodyStmtsConstant() {
    return Stream.of(
            "$l0 := @this: LocalMerging",
            "$l1 := @parameter0: int",
            "$l2 = \"one\"",
            "$stack3 = <java.lang.System: java.io.PrintStream out>",
            "if $l1 != 1 goto label1",
            "$stack4 = $l2",
            "goto label2",
            "label1:",
            "$stack4 = \"two\"",
            "label2:",
            "virtualinvoke $stack3.<java.io.PrintStream: void println(java.lang.String)>($stack4)",
            "return")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   * public void localMergingWithOtherLocal(int n) {
   *     String a = "one";
   *     String b = "two";
   *     // The branch returns either a local or a different local.
   *     // Because of the divergence neither `a` nor `b` should be inlined,
   *     // but a stack local variable should be created for holding the result of the branch.
   *     System.out.println(n == 1 ? a : b);
   * }
   * </pre>
   */
  public List<String> expectedBodyStmtsOtherLocal() {
    return Stream.of(
            "$l0 := @this: LocalMerging",
            "$l1 := @parameter0: int",
            "$l2 = \"one\"",
            "$l3 = \"two\"",
            "$stack4 = <java.lang.System: java.io.PrintStream out>",
            "if $l1 != 1 goto label1",
            "$stack5 = $l2",
            "goto label2",
            "label1:",
            "$stack5 = $l3",
            "label2:",
            "virtualinvoke $stack4.<java.io.PrintStream: void println(java.lang.String)>($stack5)",
            "return")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   * public void localMergingWithDuplicateValue(int n) {
   *     String a = "one";
   *     // One of the branches for the first argument contains the constant "two"
   *     // and the second argument also contains the constant "two".
   *     // This test ensures that when the first argument gets replaced by a stack local,
   *     // the second argument isn't replaced as well.
   *     System.setProperty(n == 1 ? a : "two", "two");
   * }
   * </pre>
   */
  public List<String> expectedBodyStmtsDuplicateValue() {
    return Stream.of(
            "$l0 := @this: LocalMerging",
            "$l1 := @parameter0: int",
            "$l2 = \"one\"",
            "if $l1 != 1 goto label1",
            "$stack3 = $l2",
            "goto label2",
            "label1:",
            "$stack3 = \"two\"",
            "label2:",
            "staticinvoke <java.lang.System: java.lang.String setProperty(java.lang.String,java.lang.String)>($stack3, \"two\")",
            "return")
        .collect(Collectors.toList());
  }
}
