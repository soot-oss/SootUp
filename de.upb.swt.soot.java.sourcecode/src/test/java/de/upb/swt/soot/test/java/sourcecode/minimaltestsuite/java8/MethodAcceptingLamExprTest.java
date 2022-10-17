package de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.java8;

import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.MinimalSourceTestSuiteBase;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Ignore;

/** @author Kaustubh Kelkar */
public class MethodAcceptingLamExprTest extends MinimalSourceTestSuiteBase {

  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "lambdaAsParamMethod", "void", Collections.emptyList());
  }

  @org.junit.Test
  @Ignore
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
