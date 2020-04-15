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
public class CharLiteralsTest extends JimpleTestSuiteBase {

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature("charCharacter"));
    assertJimpleStmts(method, expectedBodyStmts("r0 := @this: CharLiterals", "$i0 = 97", "return"));

    method = loadMethod(getMethodSignature("charSymbol"));
    assertJimpleStmts(method, expectedBodyStmts("r0 := @this: CharLiterals", "$i0 = 37", "return"));

    method = loadMethod(getMethodSignature("charBackslashT"));
    assertJimpleStmts(method, expectedBodyStmts("r0 := @this: CharLiterals", "$i0 = 9", "return"));

    method = loadMethod(getMethodSignature("charBackslash"));
    assertJimpleStmts(method, expectedBodyStmts("r0 := @this: CharLiterals", "$i0 = 92", "return"));

    method = loadMethod(getMethodSignature("charSingleQuote"));
    assertJimpleStmts(method, expectedBodyStmts("r0 := @this: CharLiterals", "$i0 = 39", "return"));

    method = loadMethod(getMethodSignature("charUnicode"));
    assertJimpleStmts(
        method, expectedBodyStmts("r0 := @this: CharLiterals", "$i0 = 937", "return"));

    method = loadMethod(getMethodSignature("specialChar"));
    assertJimpleStmts(
        method, expectedBodyStmts("r0 := @this: CharLiterals", "$i0 = 8482", "return"));
  }

  public MethodSignature getMethodSignature(String methodName) {
    return identifierFactory.getMethodSignature(
        methodName, getDeclaredClassSignature(), "void", Collections.emptyList());
  }
}
