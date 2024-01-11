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
public class MethodAcceptingVarTest extends MinimalBytecodeTestSuiteBase {

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
            "l0 := @this: MethodAcceptingVar",
            "l1 := @parameter0: short",
            "$stack2 = l1 + 1",
            "l1 = (short) $stack2",
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
            "l0 := @this: MethodAcceptingVar",
            "l1 := @parameter0: byte",
            "$stack2 = l1 + 1",
            "l1 = (byte) $stack2",
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
            "l0 := @this: MethodAcceptingVar", "l1 := @parameter0: char", "l1 = 97", "return")
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
            "l0 := @this: MethodAcceptingVar", "l1 := @parameter0: int", "l1 = l1 + 1", "return")
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
            "l0 := @this: MethodAcceptingVar",
            "l1 := @parameter0: long",
            "l1 = 123456777L",
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
            "l0 := @this: MethodAcceptingVar", "l1 := @parameter0: float", "l1 = 7.77F", "return")
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
            "l0 := @this: MethodAcceptingVar",
            "l1 := @parameter0: double",
            "l1 = 1.787777777",
            "return")
        .collect(Collectors.toList());
  }
}
