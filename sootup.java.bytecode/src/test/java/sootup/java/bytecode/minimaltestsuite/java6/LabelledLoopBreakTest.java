package sootup.java.bytecode.minimaltestsuite.java6;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import categories.TestCategories;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.bytecode.minimaltestsuite.MinimalBytecodeTestSuiteBase;

/** @author Kaustubh Kelkar */
@Tag(TestCategories.JAVA_8_CATEGORY)
public class LabelledLoopBreakTest extends MinimalBytecodeTestSuiteBase {

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
            "l0 := @this: LabelledLoopBreak",
            "l1 = 0",
            "label1:",
            "if l1 >= 5 goto label5",
            "l2 = 0",
            "label2:",
            "if l2 >= 5 goto label4",
            "if l1 != 1 goto label3",
            "goto label5",
            "label3:",
            "l2 = l2 + 1",
            "goto label2",
            "label4:",
            "l1 = l1 + 1",
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
