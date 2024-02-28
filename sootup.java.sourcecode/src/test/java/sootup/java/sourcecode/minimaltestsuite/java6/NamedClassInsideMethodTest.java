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

@Tag("Java8")
public class NamedClassInsideMethodTest extends MinimalSourceTestSuiteBase {
  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "namedClassInsideMethod", "void", Collections.emptyList());
  }

  /**  <pre>
   * public void namedClassInsideMethod() {
   *
   * class MyMathOperation implements MathOperation {
   * int i = 0;
   * public void addition() {
   * i++;
   * }
   * }
   *
   * MathOperation myMathOperation = new MyMathOperation();
   * myMathOperation.addition();
   *
   * }
   *
   * <pre>*/
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: NamedClassInsideMethod",
            "r1 = new NamedClassInsideMethod1$MyMathOperation",
            "specialinvoke r1.<NamedClassInsideMethod1$MyMathOperation: void <init>()>()",
            "interfaceinvoke r1.<NamedClassInsideMethod$MathOperation: void addition()>()",
            "return")
        .collect(Collectors.toList());
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
  }
}
