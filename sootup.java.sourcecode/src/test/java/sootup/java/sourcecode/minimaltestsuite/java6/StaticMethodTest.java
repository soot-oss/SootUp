/*Author Kaustubh Kelkar*/

package sootup.java.sourcecode.minimaltestsuite.java6;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.sourcecode.minimaltestsuite.MinimalSourceTestSuiteBase;

/** @author Kaustubh Kelkar */
@Tag("Java8")
public class StaticMethodTest extends MinimalSourceTestSuiteBase {

  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "staticMethod", "void", Collections.emptyList());
  }

  @Test
  public void test() {
    SootMethod method1 = loadMethod(getMethodSignature());
    assertJimpleStmts(method1, expectedBodyStmts());
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
    assertTrue(method.isStatic());
  }

  /**
   *
   *
   * <pre>
   *     static void staticMethod(){
   * System.out.println("static method");
   * }
   * </pre>
   */
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke r0.<java.io.PrintStream: void println(java.lang.String)>(\"static method\")",
            "return")
        .collect(Collectors.toList());
  }
}
