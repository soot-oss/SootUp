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
public class NamedClassInsideMethodTest extends MinimalBytecodeTestSuiteBase {
  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "namedClassInsideMethod", "void", Collections.emptyList());
  }

  /**  <pre>
   * public void namedClassInsideMethod() {
   *
   * class MyMathOperation implements MathOperation {
   * int i = 0;
   * public void addition() {
   * i++;
   * }
   * }
   *
   * MathOperation myMathOperation = new MyMathOperation();
   * myMathOperation.addition();
   *
   * }
   *
   * <pre>*/
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "this := @this: NamedClassInsideMethod",
            "$stack2 = new NamedClassInsideMethod$1MyMathOperation",
            "specialinvoke $stack2.<NamedClassInsideMethod$1MyMathOperation: void <init>(NamedClassInsideMethod)>(this)",
            "l1 = $stack2",
            "interfaceinvoke l1.<NamedClassInsideMethod$MathOperation: void addition()>()",
            "return")
        .collect(Collectors.toList());
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
  }
}
