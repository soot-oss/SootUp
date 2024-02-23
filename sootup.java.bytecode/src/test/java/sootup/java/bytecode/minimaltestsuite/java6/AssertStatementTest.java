package sootup.java.bytecode.minimaltestsuite.java6;

import categories.TestCategories;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.bytecode.minimaltestsuite.MinimalBytecodeTestSuiteBase;

/** @author Kaustubh Kelkar */
@Tag(TestCategories.JAVA_8_CATEGORY)
public class AssertStatementTest extends MinimalBytecodeTestSuiteBase {
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
            "l0 := @this: AssertStatement",
            "$stack1 = <AssertStatement: boolean $assertionsDisabled>",
            "if $stack1 != 0 goto label1",
            "if \"\" != null goto label1",
            "$stack2 = new java.lang.AssertionError",
            "specialinvoke $stack2.<java.lang.AssertionError: void <init>()>()",
            "throw $stack2",
            "label1:",
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
            "l0 := @this: AssertStatement",
            "$stack2 = <AssertStatement: boolean $assertionsDisabled>",
            "if $stack2 != 0 goto label1",
            "if \"\" != null goto label1",
            "$stack3 = new java.lang.AssertionError",
            "specialinvoke $stack3.<java.lang.AssertionError: void <init>()>()",
            "throw $stack3",
            "label1:",
            "l1 = 4",
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
            "l0 := @this: AssertStatement",
            "$stack2 = <AssertStatement: boolean $assertionsDisabled>",
            "if $stack2 != 0 goto label1",
            "if \"first\" != null goto label1",
            "$stack5 = new java.lang.AssertionError",
            "specialinvoke $stack5.<java.lang.AssertionError: void <init>()>()",
            "throw $stack5",
            "label1:",
            "l1 = 1",
            "$stack3 = <AssertStatement: boolean $assertionsDisabled>",
            "if $stack3 != 0 goto label2",
            "if \"second\" != null goto label2",
            "$stack4 = new java.lang.AssertionError",
            "specialinvoke $stack4.<java.lang.AssertionError: void <init>()>()",
            "throw $stack4",
            "label2:",
            "l1 = 2",
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
