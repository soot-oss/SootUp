package sootup.java.bytecode.minimaltestsuite.java6;

import categories.Java8Test;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.bytecode.minimaltestsuite.MinimalBytecodeTestSuiteBase;

/** @author Kaustubh Kelkar */
@Category(Java8Test.class)
public class ContinueInWhileLoopTest extends MinimalBytecodeTestSuiteBase {

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
            "l0 := @this: ContinueInWhileLoop",
            "l1 = 0",
            "label1:",
            "$stack3 = l1",
            "$stack2 = 10",
            "if $stack3 >= $stack2 goto label3",
            "if l1 != 5 goto label2",
            "l1 = l1 + 1",
            "goto label1",
            "label2:",
            "l1 = l1 + 1",
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
