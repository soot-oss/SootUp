package sootup.jimple.frontend.javatestsuite.java6;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.jimple.frontend.javatestsuite.JimpleTestSuiteBase;

/**
 * @author Kaustubh Kelkar
 */
public class AnonymousClassInsideMethodTest extends JimpleTestSuiteBase {

  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "anonymousClassInsideMethod", "void", Collections.emptyList());
  }

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
