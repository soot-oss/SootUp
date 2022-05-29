/** @author: Hasitha Rajapakse */
package de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.java6;

import categories.Java8Test;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.MinimalSourceTestSuiteBase;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(Java8Test.class)
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
            "$r1 = new NamedClassInsideMethod1$MyMathOperation",
            "specialinvoke $r1.<NamedClassInsideMethod1$MyMathOperation: void <init>()>()",
            "interfaceinvoke $r1.<NamedClassInsideMethod$MathOperation: void addition()>()",
            "return")
        .collect(Collectors.toList());
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
  }
}
