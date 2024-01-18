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
public class MethodReturningVarTest extends MinimalBytecodeTestSuiteBase {

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature("short"));
    assertJimpleStmts(method, expectedBodyStmtsShort());

    method = loadMethod(getMethodSignature("byte"));
    assertJimpleStmts(method, expectedBodyStmtsByte());

    method = loadMethod(getMethodSignature("char"));
    assertJimpleStmts(method, expectedBodyStmtsChar());

    method = loadMethod(getMethodSignature("int"));
    assertJimpleStmts(method, expectedBodyStmtsInt());

    method = loadMethod(getMethodSignature("long"));
    assertJimpleStmts(method, expectedBodyStmtsLong());

    method = loadMethod(getMethodSignature("float"));
    assertJimpleStmts(method, expectedBodyStmtsFloat());

    method = loadMethod(getMethodSignature("double"));
    assertJimpleStmts(method, expectedBodyStmtsDouble());
  }

  public MethodSignature getMethodSignature(String datatype) {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), datatype + "Variable", datatype, Collections.emptyList());
  }

  /**
   *
   *
   * <pre>
   * public short shortVariable() {
   *         short a = 10;
   *         return a;
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsShort() {
    return Stream.of("l0 := @this: MethodReturningVar", "l1 = 10", "return l1")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   * public byte byteVariable() {
   *         byte b = 0;
   *         return b;
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsByte() {
    return Stream.of("l0 := @this: MethodReturningVar", "l1 = 0", "return l1")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   * public char charVariable() {
   *         char c = 'a';
   *         return c;
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsChar() {
    return Stream.of("l0 := @this: MethodReturningVar", "l1 = 97", "return l1")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   * public int intVariable() {
   *         int d = 512;
   *         return d;
   *     }
   *     </pre>
   */
  public List<String> expectedBodyStmtsInt() {
    return Stream.of("l0 := @this: MethodReturningVar", "l1 = 512", "return l1")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   * public long longVariable() {
   *         long e = 123456789;
   *         return e;
   *
   *     }
   *     </pre>
   */
  public List<String> expectedBodyStmtsLong() {
    return Stream.of("l0 := @this: MethodReturningVar", "l1 = 123456789L", "return l1")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   *     public float floatVariable() {
   *         float f = 3.14f;
   *         return f;
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsFloat() {
    return Stream.of("l0 := @this: MethodReturningVar", "l1 = 3.14F", "return l1")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   *     public double doubleVariable() {
   *         double g = 1.96969654d;
   *         return g;
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsDouble() {
    return Stream.of("l0 := @this: MethodReturningVar", "l1 = 1.96969654", "return l1")
        .collect(Collectors.toList());
  }
}
