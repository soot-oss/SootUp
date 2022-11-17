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
public class MethodAcceptingVarTest extends MinimalSourceTestSuiteBase {

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
        getDeclaredClassSignature(),
        datatype + "Variable",
        "void",
        Collections.singletonList(datatype));
  }

  /**
   *
   *
   * <pre>
   *    public void shortVariable(short a) {
   *        a++;
   * }
   * </pre>
   */
  public List<String> expectedBodyStmtsShort() {
    return Stream.of(
            "r0 := @this: MethodAcceptingVar",
            "$s0 := @parameter0: short",
            "$s1 = $s0",
            "$s2 = $s0 + 1",
            "$s0 = $s2",
            "return")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   * public void byteVariable(byte b) {
   *     b++;
   * }
   * </pre>
   */
  public List<String> expectedBodyStmtsByte() {
    return Stream.of(
            "r0 := @this: MethodAcceptingVar",
            "$b0 := @parameter0: byte",
            "$b1 = $b0",
            "$b2 = $b0 + 1",
            "$b0 = $b2",
            "return")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   * public void charVariable(char c) {
   *    c = 'a';
   * }
   * </pre>
   */
  public List<String> expectedBodyStmtsChar() {
    return Stream.of(
            "r0 := @this: MethodAcceptingVar", "$c0 := @parameter0: char", "$c0 = 97", "return")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   * public void intVariable(int d) {
   *    d++;
   * }
   * </pre>
   */
  public List<String> expectedBodyStmtsInt() {
    return Stream.of(
            "r0 := @this: MethodAcceptingVar",
            "$i0 := @parameter0: int",
            "$i1 = $i0",
            "$i2 = $i0 + 1",
            "$i0 = $i2",
            "return")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   * public void longVariable(long e) {
   *     e = 123456777;
   * }
   * </pre>
   */
  public List<String> expectedBodyStmtsLong() {
    return Stream.of(
            "r0 := @this: MethodAcceptingVar",
            "$l0 := @parameter0: long",
            "$l0 = 123456777",
            "return")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   * public void floatVariable(float f) {
   *         f = 7.77f;
   * }
   * </pre>
   */
  public List<String> expectedBodyStmtsFloat() {
    return Stream.of(
            "r0 := @this: MethodAcceptingVar", "$f0 := @parameter0: float", "$f0 = 7.77F", "return")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   * public void doubleVariable(double g) {
   *     g = 1.787777777;
   * }
   * </pre>
   */
  public List<String> expectedBodyStmtsDouble() {
    return Stream.of(
            "r0 := @this: MethodAcceptingVar",
            "$d0 := @parameter0: double",
            "$d0 = 1.787777777",
            "return")
        .collect(Collectors.toList());
  }
}
