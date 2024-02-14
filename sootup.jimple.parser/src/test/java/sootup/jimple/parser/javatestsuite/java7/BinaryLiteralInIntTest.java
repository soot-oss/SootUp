package sootup.jimple.parser.javatestsuite.java7;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.jimple.parser.javatestsuite.JimpleTestSuiteBase;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** @author Kaustubh Kelkar */
@Tag("Java8")
public class BinaryLiteralInIntTest extends JimpleTestSuiteBase {
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "binaryLiteralInInt", "void", Collections.emptyList());
  }

  public List<String> expectedBodyStmts() {
    return Stream.of(
            "l0 := @this: BinaryLiteralInInt", "l1 = -1589272251", "l2 = 5", "l3 = 5", "return")
        .collect(Collectors.toList());
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
  }
}
