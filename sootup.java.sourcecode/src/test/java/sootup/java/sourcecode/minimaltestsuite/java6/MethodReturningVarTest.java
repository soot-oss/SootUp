package sootup.java.sourcecode.minimaltestsuite.java6;

import categories.Java8Test;
import java.util.*;
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
public class MethodReturningVarTest extends MinimalSourceTestSuiteBase {

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
    return Stream.of("r0 := @this: MethodReturningVar", "$i0 = 10", "return $i0")
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
    return Stream.of("r0 := @this: MethodReturningVar", "$i0 = 0", "return $i0")
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
    return Stream.of("r0 := @this: MethodReturningVar", "$i0 = 97", "return $i0")
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
    return Stream.of("r0 := @this: MethodReturningVar", "$i0 = 512", "return $i0")
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
    return Stream.of("r0 := @this: MethodReturningVar", "$i0 = 123456789", "return $i0")
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
    return Stream.of("r0 := @this: MethodReturningVar", "$f0 = 3.14F", "return $f0")
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
    return Stream.of("r0 := @this: MethodReturningVar", "$d0 = 1.96969654", "return $d0")
        .collect(Collectors.toList());
  }
}
