package sootup.java.frontend.minimaltestsuite.java8;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.frontend.minimaltestsuite.MinimalSourceTestSuiteBase;

/** @author Kaustubh Kelkar */
@Tag("Java8")
public class MethodAcceptingLamExprTest extends MinimalSourceTestSuiteBase {

  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "lambdaAsParamMethod", "void", Collections.emptyList());
  }

  @Test
  @Disabled
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
  }
  /** TODO update the expectedBodyStmts when Lambda are supported by Wala */

  /**
   *
   *
   * <pre>
   *     public void lambdaAsParamMethod(){
   * //        Percentage percentageValue = (value -> value/100);
   * //        System.out.println("Percentage : " + percentageValue.calcPercentage(45.0));
   * }
   * </pre>
   */
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of("r0 := @this: MethodAcceptingLamExpr", "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
