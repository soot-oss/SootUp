/** @author: Hasitha Rajapakse */
package sootup.java.sourcecode.minimaltestsuite.java6;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static sootup.core.util.Utils.filterJimple;

import categories.Java8Test;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.model.Body;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.sourcecode.minimaltestsuite.MinimalSourceTestSuiteBase;

@Category(Java8Test.class)
public class VariableDeclarationTest extends MinimalSourceTestSuiteBase {

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

  @Ignore
  public void classTypeDefWithoutAssignment() {
    // TODO: [ms] fix: Type of Local $r1 is should be (java.lang.)String
    // TODO [kk]: Actual   :[unknown $u0, VariableDeclaration r0, r0 := @this: VariableDeclaration,
    // $u0 = null, return]
    SootMethod method = loadMethod(getMethodSignature("classTypeDefWithoutAssignment"));
    Body body = method.getBody();
    assertNotNull(body);

    List<String> actualStmts = filterJimple(body.toString());
    assertEquals(
        expectedBodyStmts(
            "java.lang.String $r1",
            "VariableDeclaration r0",
            "r0 := @this: VariableDeclaration",
            "$r1 = null",
            "return"),
        actualStmts);
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
    return Stream.of("r0 := @this: VariableDeclaration", "$i0 = 10", "return")
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
    return Stream.of("r0 := @this: VariableDeclaration", "$i0 = 0", "return")
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
    return Stream.of("r0 := @this: VariableDeclaration", "$i0 = 97", "return")
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
    return Stream.of("r0 := @this: VariableDeclaration", "$i0 = 512", "return")
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
    return Stream.of("r0 := @this: VariableDeclaration", "$i0 = 123456789", "return")
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
    return Stream.of("r0 := @this: VariableDeclaration", "$f0 = 3.14F", "return")
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
    return Stream.of("r0 := @this: VariableDeclaration", "$d0 = 1.96969654", "return")
        .collect(Collectors.toList());
  }
}
