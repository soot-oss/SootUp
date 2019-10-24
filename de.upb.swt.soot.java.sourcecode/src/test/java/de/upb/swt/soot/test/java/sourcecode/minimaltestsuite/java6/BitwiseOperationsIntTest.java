/** @author: Hasitha Rajapakse */
package de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.java6;

import categories.Java8Test;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.MinimalTestSuiteBase;
import java.util.Collections;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(Java8Test.class)
public class BitwiseOperationsIntTest extends MinimalTestSuiteBase {

  @Test
  public void defaultTest() {

    loadMethod(
        expectedBodyStmts(
            "r0 := @this: BitwiseOperationsInt",
            "$i0 = 70",
            "$i1 = 20",
            "$i2 = $i0 & $i1",
            "return"),
        getMethodSignature("bitwiseOpAnd"));

    loadMethod(
        expectedBodyStmts(
            "r0 := @this: BitwiseOperationsInt",
            "$i0 = 70",
            "$i1 = 20",
            "$i2 = $i0 | $i1",
            "return"),
        getMethodSignature("bitwiseOpOr"));

    loadMethod(
        expectedBodyStmts(
            "r0 := @this: BitwiseOperationsInt",
            "$i0 = 70",
            "$i1 = 20",
            "$i2 = $i0 ^ $i1",
            "return"),
        getMethodSignature("bitwiseOpXor"));

    loadMethod(
        expectedBodyStmts(
            "r0 := @this: BitwiseOperationsInt", "$i0 = 70", "$i1 = neg $i0", "return"),
        getMethodSignature("bitwiseOpComplement"));

    loadMethod(
        expectedBodyStmts(
            "r0 := @this: BitwiseOperationsInt", "$i0 = 70", "$i1 = $i0 >> 5", "return"),
        getMethodSignature("bitwiseOpSignedRightShift"));

    loadMethod(
        expectedBodyStmts(
            "r0 := @this: BitwiseOperationsInt", "$i0 = 70", "$i1 = $i0 << 5", "return"),
        getMethodSignature("bitwiseOpLeftShift"));

    loadMethod(
        expectedBodyStmts(
            "r0 := @this: BitwiseOperationsInt", "$i0 = 70", "$i1 = $i0 >>> 5", "return"),
        getMethodSignature("bitwiseOpUnsignedRightShift"));
  }

  public MethodSignature getMethodSignature(String methodName) {
    return identifierFactory.getMethodSignature(
        methodName, getDeclaredClassSignature(), "void", Collections.emptyList());
  }
}
