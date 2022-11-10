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
public class EvaluationOrderWithParenthesesTest extends MinimalSourceTestSuiteBase {

  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(),
        "evaluationOrderWithParentheses",
        "void",
        Collections.emptyList());
  }

  /**
   *
   *
   * <pre>
   *     public void evaluationOrderWithParentheses(){
   * int b = ((1 + 2) * 3);
   * }
   * </pre>
   */
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: EvaluationOrderWithParentheses", "$i0 = 1 + 2", "$i1 = $i0 * 3", "return")
        .collect(Collectors.toList());
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
  }
}
