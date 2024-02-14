/** @author: Hasitha Rajapakse */
package sootup.java.sourcecode.minimaltestsuite.java6;


import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.sourcecode.minimaltestsuite.MinimalSourceTestSuiteBase;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("Java8")
public class FinalMethodTest extends MinimalSourceTestSuiteBase {

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
    assertTrue(method.isFinal());
  }

  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "finalMethod", "void", Collections.emptyList());
  }

  /**
   *
   *
   * <pre>
   *     final void finalMethod(){
   * System.out.println("final method");
   * }
   * </pre>
   */
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: FinalMethod",
            "r1 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke r1.<java.io.PrintStream: void println(java.lang.String)>(\"final method\")",
            "return")
        .collect(Collectors.toList());
  }
}
