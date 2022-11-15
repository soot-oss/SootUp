package sootup.java.sourcecode.minimaltestsuite.java6;

import categories.Java8Test;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.sourcecode.minimaltestsuite.MinimalSourceTestSuiteBase;

/**
 * @author Hasitha Rajapakse
 * @author Kaustubh Kelkar
 */
@Category(Java8Test.class)
public class AssertStatementTest extends MinimalSourceTestSuiteBase {
  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "assertStatement", "void", Collections.emptyList());
  }

  public MethodSignature getMethodSignatureExtend() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "assertStatementExtend", "void", Collections.emptyList());
  }

  public MethodSignature getMethodSignatureExtend2() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "assertStatementExtend2", "void", Collections.emptyList());
  }

  /**
   *
   *
   * <pre>
   *     public void assertStatement() {
   *         assert "" != null;
   *     }
   * </pre>
   */
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: AssertStatement",
            "$z0 = \"\" != null",
            "$z1 = <AssertStatement: boolean $assertionsDisabled>",
            "if $z1 == 1 goto label1",
            "if $z0 == 1 goto label1",
            "$r1 = new java.lang.AssertionError",
            "specialinvoke $r1.<java.lang.AssertionError: void <init>()>()",
            "throw $r1",
            "label1:",
            "nop",
            "return")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   *     public void assertStatementExtend() {
   *         assert "" != null;
   *         int x = 4;
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsExtend() {
    return Stream.of(
            "r0 := @this: AssertStatement",
            "$z0 = \"\" != null",
            "$z1 = <AssertStatement: boolean $assertionsDisabled>",
            "if $z1 == 1 goto label1",
            "if $z0 == 1 goto label1",
            "$r1 = new java.lang.AssertionError",
            "specialinvoke $r1.<java.lang.AssertionError: void <init>()>()",
            "throw $r1",
            "label1:",
            "nop",
            "$i0 = 4",
            "return")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   *     public void assertStatementExtend2() {
   *         assert "first" != null;
   *         int x = 1;
   *         assert "second" != null;
   *         x = 2;
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsExtend2() {
    return Stream.of(
            "r0 := @this: AssertStatement",
            "$z0 = \"first\" != null",
            "$z1 = <AssertStatement: boolean $assertionsDisabled>",
            "if $z1 == 1 goto label1",
            "if $z0 == 1 goto label1",
            "$r1 = new java.lang.AssertionError",
            "specialinvoke $r1.<java.lang.AssertionError: void <init>()>()",
            "throw $r1",
            "label1:",
            "nop",
            "$i0 = 1",
            "$z2 = \"second\" != null",
            "$z3 = <AssertStatement: boolean $assertionsDisabled>",
            "if $z3 == 1 goto label2",
            "if $z2 == 1 goto label2",
            "$r2 = new java.lang.AssertionError",
            "specialinvoke $r2.<java.lang.AssertionError: void <init>()>()",
            "throw $r2",
            "label2:",
            "nop",
            "$i0 = 2",
            "return")
        .collect(Collectors.toList());
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
    method = loadMethod(getMethodSignatureExtend());
    assertJimpleStmts(method, expectedBodyStmtsExtend());
    method = loadMethod(getMethodSignatureExtend2());
    assertJimpleStmts(method, expectedBodyStmtsExtend2());
  }
}
