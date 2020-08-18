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
public class AnonymousClassInsideMethodTest extends MinimalSourceTestSuiteBase {
  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "anonymousClassInsideMethod", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  /**  <pre>    public void anonymousClassInsideMethod() {
   *
   * MathOperation myMathOperation = new MathOperation() {
   * int i = 0;
   *
   * @Override
   * public void addition() {
   * i++;
   * }
   * };
   *
   * myMathOperation.addition();
   *
   * }
   * }
   * <pre>*/
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: AnonymousClassInsideMethod",
            "$r1 = new AnonymousClassInsideMethod$1",
            "specialinvoke $r1.<AnonymousClassInsideMethod$1: void <init>()>()",
            "interfaceinvoke $r1.<AnonymousClassInsideMethod$MathOperation: void addition()>()",
            "return")
        .collect(Collectors.toList());
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
  }
}
