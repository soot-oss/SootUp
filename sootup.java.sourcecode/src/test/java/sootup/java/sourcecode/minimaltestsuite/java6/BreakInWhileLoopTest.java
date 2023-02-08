/** @author: Hasitha Rajapakse */
package sootup.java.sourcecode.minimaltestsuite.java6;

import categories.Java8Test;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.sourcecode.minimaltestsuite.MinimalSourceTestSuiteBase;

@Category(Java8Test.class)
public class BreakInWhileLoopTest extends MinimalSourceTestSuiteBase {

  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "breakInWhileLoop", "void", Collections.emptyList());
  }

  /**  <pre>
   * public void breakInWhileLoop() {
   * int num = 10;
   * int i = 5;
   * while (num > 0) {
   * num--;
   * if (num == i) {
   * break;
   * }
   * }
   * }
   *
   * <pre>*/
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: BreakInWhileLoop",
            "$i0 = 10",
            "$i1 = 5",
            "label1:",
            "$z0 = $i0 > 0",
            "if $z0 == 0 goto label3",
            "$i2 = $i0",
            "$i3 = $i0 - 1",
            "$i0 = $i3",
            "$z1 = $i0 == $i1",
            "if $z1 == 0 goto label2",
            "goto label3",
            "label2:",
            "goto label1",
            "label3:",
            "return")
        .collect(Collectors.toList());
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
  }
}
