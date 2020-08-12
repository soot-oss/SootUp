package de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.java6;

import categories.Java8Test;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.MinimalSourceTestSuiteBase;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * @author Hasitha Rajapakse
 * @author Kaustubh Kelkar
 */
@Category(Java8Test.class)
public class BitwiseOperationsIntTest extends MinimalSourceTestSuiteBase {

  @Test
  public void test() {

    SootMethod method = loadMethod(getMethodSignature("bitwiseOpAnd"));
    assertJimpleStmts(method, expectedBodyStmtsBitwiseOpAnd());

    method = loadMethod(getMethodSignature("bitwiseOpOr"));
    assertJimpleStmts(method, expectedBodyStmtsBitwiseOpOr());

    method = loadMethod(getMethodSignature("bitwiseOpXor"));
    assertJimpleStmts(method, expectedBodyStmtsBitwiseOpXor());

    method = loadMethod(getMethodSignature("bitwiseOpComplement"));
    assertJimpleStmts(method, expectedBodyStmtsBitwiseOpComplement());

    method = loadMethod(getMethodSignature("bitwiseOpSignedRightShift"));
    assertJimpleStmts(method, expectedBodyStmtsBitwiseOpSignedRightShift());

    method = loadMethod(getMethodSignature("bitwiseOpLeftShift"));
    assertJimpleStmts(method, expectedBodyStmtsBitwiseOpLeftShift());

    method = loadMethod(getMethodSignature("bitwiseOpUnsignedRightShift"));
    assertJimpleStmts(method, expectedBodyStmtsBitwiseOpUnsignedRightShift());
  }

  public MethodSignature getMethodSignature(String methodName) {
    return identifierFactory.getMethodSignature(
        methodName, getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  public List<String> expectedBodyStmtsBitwiseOpAnd() {
    return Stream.of(
            "r0 := @this: BitwiseOperationsInt",
            "$i0 = 70",
            "$i1 = 20",
            "$i2 = $i0 & $i1",
            "return")
        .collect(Collectors.toList());
  }

  public List<String> expectedBodyStmtsBitwiseOpOr() {
    return Stream.of(
            "r0 := @this: BitwiseOperationsInt",
            "$i0 = 70",
            "$i1 = 20",
            "$i2 = $i0 | $i1",
            "return")
        .collect(Collectors.toList());
  }

  public List<String> expectedBodyStmtsBitwiseOpXor() {
    return Stream.of(
            "r0 := @this: BitwiseOperationsInt",
            "$i0 = 70",
            "$i1 = 20",
            "$i2 = $i0 ^ $i1",
            "return")
        .collect(Collectors.toList());
  }

  public List<String> expectedBodyStmtsBitwiseOpComplement() {
    return Stream.of("r0 := @this: BitwiseOperationsInt", "$i0 = 70", "$i1 = neg $i0", "return")
        .collect(Collectors.toList());
  }

  public List<String> expectedBodyStmtsBitwiseOpSignedRightShift() {
    return Stream.of("r0 := @this: BitwiseOperationsInt", "$i0 = 70", "$i1 = $i0 >> 5", "return")
        .collect(Collectors.toList());
  }

  public List<String> expectedBodyStmtsBitwiseOpLeftShift() {
    return Stream.of("r0 := @this: BitwiseOperationsInt", "$i0 = 70", "$i1 = $i0 << 5", "return")
        .collect(Collectors.toList());
  }

  public List<String> expectedBodyStmtsBitwiseOpUnsignedRightShift() {
    return Stream.of("r0 := @this: BitwiseOperationsInt", "$i0 = 70", "$i1 = $i0 >>> 5", "return")
        .collect(Collectors.toList());
  }
}
