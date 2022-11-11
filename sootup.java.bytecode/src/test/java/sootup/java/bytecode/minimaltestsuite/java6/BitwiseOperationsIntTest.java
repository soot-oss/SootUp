package sootup.java.bytecode.minimaltestsuite.java6;

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
public class BitwiseOperationsIntTest extends MinimalBytecodeTestSuiteBase {

  @Test
  public void test() {

    SootMethod method = loadMethod(getMethodSignature("bitwiseOpAnd"));
    assertJimpleStmts(method, expectedBodyStmtsBitwiseOpAnd());

    method = loadMethod(getMethodSignature("bitwiseOpOr"));
    assertJimpleStmts(method, expectedBodyStmtsBitwiseOpOr());

    method = loadMethod(getMethodSignature("bitwiseOpXor"));
    assertJimpleStmts(method, expectedBodyStmtsBitwiseOpXor());

    method = loadMethod(getMethodSignature("bitwiseOpComplement"));
    assertJimpleStmts(method, expectedBodyStmtsBitwiseOpComplement());

    method = loadMethod(getMethodSignature("bitwiseOpSignedRightShift"));
    assertJimpleStmts(method, expectedBodyStmtsBitwiseOpSignedRightShift());

    method = loadMethod(getMethodSignature("bitwiseOpLeftShift"));
    assertJimpleStmts(method, expectedBodyStmtsBitwiseOpLeftShift());

    method = loadMethod(getMethodSignature("bitwiseOpUnsignedRightShift"));
    assertJimpleStmts(method, expectedBodyStmtsBitwiseOpUnsignedRightShift());
  }

  /**
   *
   *
   * <pre>
   *     public void bitwiseOpAnd(){
   *         int a = 70;
   *         int b = 20;
   *         int c = a&b;
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsBitwiseOpAnd() {
    return Stream.of(
            "l0 := @this: BitwiseOperationsInt", "l1 = 70", "l2 = 20", "l3 = l1 & l2", "return")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   *     public void bitwiseOpOr(){
   *         int a = 70;
   *         int b = 20;
   *         int c = a|b;
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsBitwiseOpOr() {
    return Stream.of(
            "l0 := @this: BitwiseOperationsInt", "l1 = 70", "l2 = 20", "l3 = l1 | l2", "return")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   *     public void bitwiseOpXor(){
   *         int a = 70;
   *         int b = 20;
   *         int c = a^b;
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsBitwiseOpXor() {
    return Stream.of(
            "l0 := @this: BitwiseOperationsInt", "l1 = 70", "l2 = 20", "l3 = l1 ^ l2", "return")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   *     public void bitwiseOpComplement(){
   *         int a = 70;
   *         int b = ~a;
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsBitwiseOpComplement() {
    return Stream.of("l0 := @this: BitwiseOperationsInt", "l1 = 70", "l2 = l1 ^ -1", "return")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   *     public void bitwiseOpSignedRightShift(){
   *         int a = 70;
   *         int b = a >> 5;
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsBitwiseOpSignedRightShift() {
    return Stream.of("l0 := @this: BitwiseOperationsInt", "l1 = 70", "l2 = l1 >> 5", "return")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   *     public void bitwiseOpLeftShift(){
   *         int a = 70;
   *         int b = a << 5;
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsBitwiseOpLeftShift() {
    return Stream.of("l0 := @this: BitwiseOperationsInt", "l1 = 70", "l2 = l1 << 5", "return")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   *     public void bitwiseOpUnsignedRightShift(){
   *         int a = 70;
   *         int b = a >>> 5;
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsBitwiseOpUnsignedRightShift() {
    return Stream.of("l0 := @this: BitwiseOperationsInt", "l1 = 70", "l2 = l1 >>> 5", "return")
        .collect(Collectors.toList());
  }

  public MethodSignature getMethodSignature(String methodName) {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), methodName, "void", Collections.emptyList());
  }
}
