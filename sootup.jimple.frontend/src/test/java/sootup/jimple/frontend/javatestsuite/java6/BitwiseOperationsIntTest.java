package sootup.jimple.frontend.javatestsuite.java6;

import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.jimple.frontend.javatestsuite.JimpleTestSuiteBase;

/**
 * @author Kaustubh Kelkar
 */
public class BitwiseOperationsIntTest extends JimpleTestSuiteBase {

  @Test
  public void test() {

    SootMethod method = loadMethod(getMethodSignature("bitwiseOpAnd"));
    assertJimpleStmts(
        method,
        Stream.of(
                "l0 := @this: BitwiseOperationsInt", "l1 = 70", "l2 = 20", "l3 = l1 & l2", "return")
            .collect(Collectors.toList()));

    method = loadMethod(getMethodSignature("bitwiseOpOr"));
    assertJimpleStmts(
        method,
        Stream.of(
                "l0 := @this: BitwiseOperationsInt", "l1 = 70", "l2 = 20", "l3 = l1 | l2", "return")
            .collect(Collectors.toList()));

    method = loadMethod(getMethodSignature("bitwiseOpXor"));
    assertJimpleStmts(
        method,
        Stream.of(
                "l0 := @this: BitwiseOperationsInt", "l1 = 70", "l2 = 20", "l3 = l1 ^ l2", "return")
            .collect(Collectors.toList()));

    method = loadMethod(getMethodSignature("bitwiseOpComplement"));
    assertJimpleStmts(
        method,
        Stream.of("l0 := @this: BitwiseOperationsInt", "l1 = 70", "l2 = l1 ^ -1", "return")
            .collect(Collectors.toList()));

    method = loadMethod(getMethodSignature("bitwiseOpSignedRightShift"));
    assertJimpleStmts(
        method,
        Stream.of("l0 := @this: BitwiseOperationsInt", "l1 = 70", "l2 = l1 >> 5", "return")
            .collect(Collectors.toList()));

    method = loadMethod(getMethodSignature("bitwiseOpLeftShift"));
    assertJimpleStmts(
        method,
        Stream.of("l0 := @this: BitwiseOperationsInt", "l1 = 70", "l2 = l1 << 5", "return")
            .collect(Collectors.toList()));

    method = loadMethod(getMethodSignature("bitwiseOpUnsignedRightShift"));
    assertJimpleStmts(
        method,
        Stream.of("l0 := @this: BitwiseOperationsInt", "l1 = 70", "l2 = l1 >>> 5", "return")
            .collect(Collectors.toList()));
  }

  public MethodSignature getMethodSignature(String methodName) {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), methodName, "void", Collections.emptyList());
  }
}
