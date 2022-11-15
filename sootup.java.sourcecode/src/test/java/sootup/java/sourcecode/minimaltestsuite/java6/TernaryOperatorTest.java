package sootup.java.sourcecode.minimaltestsuite.java6;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.sourcecode.minimaltestsuite.MinimalSourceTestSuiteBase;

/** @author Kaustubh Kelkar */
public class TernaryOperatorTest extends MinimalSourceTestSuiteBase {
  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "ternaryOperatorMethod", "boolean", Collections.emptyList());
  }

  /**
   *
   *
   * <pre>
   *     boolean ternaryOperatorMethod(){
   * return num < 0 ? false : true;
   * }
   * </pre>
   */
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: TernaryOperator",
            "$i0 = r0.<TernaryOperator: int num>",
            "$z0 = $i0 < 0",
            "if $z0 == 0 goto label1",
            "$z1 = 0",
            "goto label2",
            "label1:",
            "$z1 = 1",
            "label2:",
            "return $z1")
        .collect(Collectors.toCollection(ArrayList::new));
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
  }
}
