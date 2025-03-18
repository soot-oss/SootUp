package sootup.java.bytecode.frontend.minimaltestsuite.java6;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.bytecode.frontend.minimaltestsuite.MinimalBytecodeTestSuiteBase;

/**
 * @author Kaustubh Kelkar
 */
public class BreakInWhileLoopTest extends MinimalBytecodeTestSuiteBase {

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
            "this := @this: BreakInWhileLoop",
            "l1 = 10",
            "l2 = 5",
            "label1:",
            "if l1 <= 0 goto label2",
            "l1 = l1 + -1",
            "if l1 != l2 goto label1",
            "goto label2",
            "label2:",
            "return")
        .collect(Collectors.toList());
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
  }
}
