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
public class ContinueInWhileLoopTest extends MinimalSourceTestSuiteBase {

  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "continueInWhileLoop", "void", Collections.emptyList());
  }

  /**  <pre>
   * public void continueInWhileLoop(){
   * int num = 0;
   * while (num < 10) {
   * if (num == 5) {
   * num++;
   * continue;
   * }
   * num++;
   * }
   * }
   *
   * <pre>*/
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: ContinueInWhileLoop",
            "$i0 = 0",
            "label1:",
            "$z0 = $i0 < 10",
            "if $z0 == 0 goto label4",
            "$z1 = $i0 == 5",
            "if $z1 == 0 goto label2",
            "$i1 = $i0",
            "$i2 = $i0 + 1",
            "$i0 = $i2",
            "goto label3",
            "label2:",
            "$i3 = $i0",
            "$i4 = $i0 + 1",
            "$i0 = $i4",
            "label3:",
            "goto label1",
            "label4:",
            "return")
        .collect(Collectors.toList());
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
  }
}
