/** @author: Hasitha Rajapakse */
package de.upb.sse.sootup.test.java.sourcecode.minimaltestsuite.java6;

import categories.Java8Test;
import de.upb.sse.sootup.core.model.SootMethod;
import de.upb.sse.sootup.core.signatures.MethodSignature;
import de.upb.sse.sootup.test.java.sourcecode.minimaltestsuite.MinimalSourceTestSuiteBase;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(Java8Test.class)
public class DoWhileLoopTest extends MinimalSourceTestSuiteBase {
  // TODO extends MinimalTestSuiteBase
  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "doWhileLoop", "void", Collections.emptyList());
  }

  /**  <pre>
   * public void doWhileLoop() {
   * int num = 10;
   * int i = 0;
   * do {
   * i++;
   * } while (num > i);
   * }
   *
   * <pre>*/
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: DoWhileLoop",
            "$i0 = 10",
            "$i1 = 0",
            "label1:",
            "$i2 = $i1",
            "$i3 = $i1 + 1",
            "$i1 = $i3",
            "$z0 = $i0 > $i1",
            "if $z0 != 0 goto label1",
            "return")
        .collect(Collectors.toList());
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
  }
}
