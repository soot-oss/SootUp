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
public class LabelledLoopBreakTest extends MinimalSourceTestSuiteBase {

  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "labelledLoopBreak", "void", Collections.emptyList());
  }

  /**  <pre>
   * public void labelledLoopBreak() {
   * start:
   * for (int i = 0; i < 5; i++) {
   * for (int j = 0; j < 5; j++) {
   * if (i == 1) {
   * break start;
   * }
   * }
   * }
   * }
   *
   * <pre>*/
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: LabelledLoopBreak",
            "$i0 = 0",
            "label1:",
            "$z0 = $i0 < 5",
            "if $z0 == 0 goto label5",
            "$i1 = 0",
            "label2:",
            "$z1 = $i1 < 5",
            "if $z1 == 0 goto label4",
            "$z2 = $i0 == 1",
            "if $z2 == 0 goto label3",
            "goto label5",
            "label3:",
            "$i2 = $i1",
            "$i3 = $i1 + 1",
            "$i1 = $i3",
            "goto label2",
            "label4:",
            "$i4 = $i0",
            "$i5 = $i0 + 1",
            "$i0 = $i5",
            "goto label1",
            "label5:",
            "return")
        .collect(Collectors.toList());
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
  }
}
