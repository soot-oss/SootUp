/** @author: Hasitha Rajapakse */
package de.upb.swt.soot.javatestsuite.java6;

import de.upb.swt.soot.categories.Java8Test;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.javatestsuite.JimpleTestSuiteBase;
import java.util.Collections;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(Java8Test.class)
public class BitwiseOperationsIntTest extends JimpleTestSuiteBase {

  @Test
  public void test() {

    SootMethod method = loadMethod(getMethodSignature("bitwiseOpAnd"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "r0 := @this: BitwiseOperationsInt",
            "$i0 = 70",
            "$i1 = 20",
            "$i2 = $i0 & $i1",
            "return"));

    method = loadMethod(getMethodSignature("bitwiseOpOr"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "r0 := @this: BitwiseOperationsInt",
            "$i0 = 70",
            "$i1 = 20",
            "$i2 = $i0 | $i1",
            "return"));

    method = loadMethod(getMethodSignature("bitwiseOpXor"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "r0 := @this: BitwiseOperationsInt",
            "$i0 = 70",
            "$i1 = 20",
            "$i2 = $i0 ^ $i1",
            "return"));

    method = loadMethod(getMethodSignature("bitwiseOpComplement"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "r0 := @this: BitwiseOperationsInt", "$i0 = 70", "$i1 = neg $i0", "return"));

    method = loadMethod(getMethodSignature("bitwiseOpSignedRightShift"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "r0 := @this: BitwiseOperationsInt", "$i0 = 70", "$i1 = $i0 >> 5", "return"));

    method = loadMethod(getMethodSignature("bitwiseOpLeftShift"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "r0 := @this: BitwiseOperationsInt", "$i0 = 70", "$i1 = $i0 << 5", "return"));

    method = loadMethod(getMethodSignature("bitwiseOpUnsignedRightShift"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "r0 := @this: BitwiseOperationsInt", "$i0 = 70", "$i1 = $i0 >>> 5", "return"));
  }

  public MethodSignature getMethodSignature(String methodName) {
    return identifierFactory.getMethodSignature(
        methodName, getDeclaredClassSignature(), "void", Collections.emptyList());
  }
}
