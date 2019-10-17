/** @author: Hasitha Rajapakse */
package de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.java6;

import categories.Java8Test;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.MinimalTestSuiteBase;
import java.util.Collections;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(Java8Test.class)
public class CharLiteralsTest extends MinimalTestSuiteBase {

  @Test
  public void defaultTest() {
    loadMethod(
        expectedBodyStmts("r0 := @this: CharLiterals", "$i0 = 97", "return"),
        getMethodSignature("charCharacter"));

    loadMethod(
        expectedBodyStmts("r0 := @this: CharLiterals", "$i0 = 37", "return"),
        getMethodSignature("charSymbol"));

    loadMethod(
        expectedBodyStmts("r0 := @this: CharLiterals", "$i0 = 9", "return"),
        getMethodSignature("charBackslashT"));

    loadMethod(
        expectedBodyStmts("r0 := @this: CharLiterals", "$i0 = 92", "return"),
        getMethodSignature("charBackslash"));

    loadMethod(
        expectedBodyStmts("r0 := @this: CharLiterals", "$i0 = 39", "return"),
        getMethodSignature("charSingleQuote"));

    loadMethod(
        expectedBodyStmts("r0 := @this: CharLiterals", "$i0 = 937", "return"),
        getMethodSignature("charUnicode"));

    loadMethod(
        expectedBodyStmts("r0 := @this: CharLiterals", "$i0 = 8482", "return"),
        getMethodSignature("specialChar"));
  }

  public MethodSignature getMethodSignature(String methodName) {
    return identifierFactory.getMethodSignature(
        methodName, getDeclaredClassSignature(), "void", Collections.emptyList());
  }
}
