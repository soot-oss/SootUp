package sootup.java.bytecode.frontend.minimaltestsuite.java6;

import java.util.ArrayList;
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
public class TernaryOperatorTest extends MinimalBytecodeTestSuiteBase {
  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "ternaryOperatorMethod", "boolean", Collections.emptyList());
  }

  /**
   *
   *
   * <pre>
   * boolean ternaryOperatorMethod(){
   * return num < 0 ? false : true;
   * }
   *
   * </pre>
   */
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "this := @this: TernaryOperator",
            "$stack1 = this.<TernaryOperator: int num>",
            "if $stack1 >= 0 goto label1",
            "$stack2 = 0",
            "goto label2",
            "label1:",
            "$stack2 = 1",
            "label2:",
            "return $stack2")
        .collect(Collectors.toCollection(ArrayList::new));
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
  }
}
