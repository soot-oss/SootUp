package de.upb.swt.soot.test.java.bytecode.minimaltestsuite.java6;

import categories.Java8Test;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.bytecode.minimaltestsuite.MinimalBytecodeTestSuiteBase;
import java.util.Collections;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author Kaustubh Kelkar */
@Category(Java8Test.class)
public class BitwiseOperationsIntTest extends MinimalBytecodeTestSuiteBase {

  @Test
  @Override
  public void defaultTest() {

    SootMethod method = loadMethod(getMethodSignature("bitwiseOpAnd"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "l0 := @this: BitwiseOperationsInt", "l1 = 70", "l2 = 20", "l3 = l1 & l2", "return"));

    method = loadMethod(getMethodSignature("bitwiseOpOr"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "l0 := @this: BitwiseOperationsInt", "l1 = 70", "l2 = 20", "l3 = l1 | l2", "return"));

    method = loadMethod(getMethodSignature("bitwiseOpXor"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "l0 := @this: BitwiseOperationsInt", "l1 = 70", "l2 = 20", "l3 = l1 ^ l2", "return"));

    method = loadMethod(getMethodSignature("bitwiseOpComplement"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "l0 := @this: BitwiseOperationsInt", "l1 = 70", "l2 = l1 ^ -1", "return"));

    method = loadMethod(getMethodSignature("bitwiseOpSignedRightShift"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "l0 := @this: BitwiseOperationsInt", "l1 = 70", "l2 = l1 >> 5", "return"));

    method = loadMethod(getMethodSignature("bitwiseOpLeftShift"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "l0 := @this: BitwiseOperationsInt", "l1 = 70", "l2 = l1 << 5", "return"));

    method = loadMethod(getMethodSignature("bitwiseOpUnsignedRightShift"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "l0 := @this: BitwiseOperationsInt", "l1 = 70", "l2 = l1 >>> 5", "return"));
  }

  public MethodSignature getMethodSignature(String methodName) {
    return identifierFactory.getMethodSignature(
        methodName, getDeclaredClassSignature(), "void", Collections.emptyList());
  }
}
