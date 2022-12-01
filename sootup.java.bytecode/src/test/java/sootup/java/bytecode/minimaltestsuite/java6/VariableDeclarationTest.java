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
public class VariableDeclarationTest extends MinimalBytecodeTestSuiteBase {

  @Test
  public void test() {

    SootMethod method = loadMethod(getMethodSignature("shortVariable"));
    assertJimpleStmts(method, expectedBodyStmtsShortVariable());

    method = loadMethod(getMethodSignature("byteVariable"));
    assertJimpleStmts(method, expectedBodyStmtsByteVariable());

    method = loadMethod(getMethodSignature("charVariable"));
    assertJimpleStmts(method, expectedBodyStmtsCharVariable());

    method = loadMethod(getMethodSignature("intVariable"));
    assertJimpleStmts(method, expectedBodyStmtsIntVariable());

    method = loadMethod(getMethodSignature("longVariable"));
    assertJimpleStmts(method, expectedBodyStmtsLongVariable());

    method = loadMethod(getMethodSignature("floatVariable"));
    assertJimpleStmts(method, expectedBodyStmtsFloatVariable());

    method = loadMethod(getMethodSignature("doubleVariable"));
    assertJimpleStmts(method, expectedBodyStmtsDoubleVariable());
  }

  public MethodSignature getMethodSignature(String methodName) {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), methodName, "void", Collections.emptyList());
  }

  /**
   *
   *
   * <pre>
   *     public void shortVariable() {
   *         short a = 10;
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsShortVariable() {
    return Stream.of("l0 := @this: VariableDeclaration", "l1 = 10", "return")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   *     public void byteVariable() {
   *         byte b = 0;
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsByteVariable() {
    return Stream.of("l0 := @this: VariableDeclaration", "l1 = 0", "return")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   *     public void charVariable() {
   *         char c = 'a';
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsCharVariable() {
    return Stream.of("l0 := @this: VariableDeclaration", "l1 = 97", "return")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   *     public void intVariable() {
   *         int d = 512;
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsIntVariable() {
    return Stream.of("l0 := @this: VariableDeclaration", "l1 = 512", "return")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   *     public void longVariable() {
   *         long e = 123456789;
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsLongVariable() {
    return Stream.of("l0 := @this: VariableDeclaration", "l1 = 123456789L", "return")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   *     public void floatVariable() {
   *         float f = 3.14f;
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsFloatVariable() {
    return Stream.of("l0 := @this: VariableDeclaration", "l1 = 3.14F", "return")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   *     public void doubleVariable() {
   *         double g = 1.96969654d;
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsDoubleVariable() {
    return Stream.of("l0 := @this: VariableDeclaration", "l1 = 1.96969654", "return")
        .collect(Collectors.toList());
  }
}
