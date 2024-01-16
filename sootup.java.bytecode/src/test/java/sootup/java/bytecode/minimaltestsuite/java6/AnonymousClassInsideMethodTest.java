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
public class AnonymousClassInsideMethodTest extends MinimalBytecodeTestSuiteBase {
  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "anonymousClassInsideMethod", "void", Collections.emptyList());
  }

  /**
   *
   *
   * <pre>
   * public void anonymousClassInsideMethod() {
   *
   * MathOperation myMathOperation = new MathOperation() {
   * int i = 0;
   *
   * @Override
   * public void addition() {
   * i++;
   * }
   * };
   *
   * myMathOperation.addition();
   *
   * }
   * }
   *
   * </pre>
   */
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "l0 := @this: AnonymousClassInsideMethod",
            "$stack2 = new AnonymousClassInsideMethod$1",
            "specialinvoke $stack2.<AnonymousClassInsideMethod$1: void <init>(AnonymousClassInsideMethod)>(l0)",
            "l1 = $stack2",
            "interfaceinvoke l1.<AnonymousClassInsideMethod$MathOperation: void addition()>()",
            "return")
        .collect(Collectors.toList());
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
  }
}
