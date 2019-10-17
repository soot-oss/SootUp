/** @author: Hasitha Rajapakse */
package de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.java6;

import categories.Java8Test;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.MinimalTestSuiteBase;
import java.util.*;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(Java8Test.class)
public class VariableDeclarationTest extends MinimalTestSuiteBase {

  @Test
  public void defaultTest() {

    loadMethod(
        expectedBodyStmts("r0 := @this: VariableDeclaration", "$i0 = 10", "return"),
        getMethodSignature("shortVariable"));

    loadMethod(
        expectedBodyStmts("r0 := @this: VariableDeclaration", "$i0 = 0", "return"),
        getMethodSignature("byteVariable"));

    loadMethod(
        expectedBodyStmts("r0 := @this: VariableDeclaration", "$i0 = 97", "return"),
        getMethodSignature("charVariable"));

    loadMethod(
        expectedBodyStmts("r0 := @this: VariableDeclaration", "$i0 = 512", "return"),
        getMethodSignature("intVariable"));

    loadMethod(
        expectedBodyStmts("r0 := @this: VariableDeclaration", "$i0 = 123456789", "return"),
        getMethodSignature("longVariable"));

    loadMethod(
        expectedBodyStmts("r0 := @this: VariableDeclaration", "$f0 = 3.14F", "return"),
        getMethodSignature("floatVariable"));

    loadMethod(
        expectedBodyStmts("r0 := @this: VariableDeclaration", "$d0 = 1.96969654", "return"),
        getMethodSignature("doubleVariable"));
  }

  public MethodSignature getMethodSignature(String methodName) {
    return identifierFactory.getMethodSignature(
        methodName, getDeclaredClassSignature(), "void", Collections.emptyList());
  }
}
