/** @author: Hasitha Rajapakse */
package de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.java6;

import static de.upb.swt.soot.core.util.Utils.filterJimple;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import categories.Java8Test;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.MinimalSourceTestSuiteBase;
import java.util.*;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(Java8Test.class)
public class VariableDeclarationTest extends MinimalSourceTestSuiteBase {

  @Test
  public void test() {

    SootMethod method = loadMethod(getMethodSignature("shortVariable"));
    assertJimpleStmts(
        method, expectedBodyStmts("r0 := @this: VariableDeclaration", "$i0 = 10", "return"));

    method = loadMethod(getMethodSignature("byteVariable"));
    assertJimpleStmts(
        method, expectedBodyStmts("r0 := @this: VariableDeclaration", "$i0 = 0", "return"));

    method = loadMethod(getMethodSignature("charVariable"));
    assertJimpleStmts(
        method, expectedBodyStmts("r0 := @this: VariableDeclaration", "$i0 = 97", "return"));

    method = loadMethod(getMethodSignature("intVariable"));
    assertJimpleStmts(
        method, expectedBodyStmts("r0 := @this: VariableDeclaration", "$i0 = 512", "return"));

    method = loadMethod(getMethodSignature("longVariable"));
    assertJimpleStmts(
        method, expectedBodyStmts("r0 := @this: VariableDeclaration", "$i0 = 123456789", "return"));

    method = loadMethod(getMethodSignature("floatVariable"));
    assertJimpleStmts(
        method, expectedBodyStmts("r0 := @this: VariableDeclaration", "$f0 = 3.14F", "return"));

    method = loadMethod(getMethodSignature("doubleVariable"));
    assertJimpleStmts(
        method,
        expectedBodyStmts("r0 := @this: VariableDeclaration", "$d0 = 1.96969654", "return"));
  }

  @Ignore
  public void classTypeDefWithoutAssignment() {
    // TODO: [ms] fix: Type of Local $r1 is should be (java.lang.)String
    SootMethod method = loadMethod(getMethodSignature("classTypeDefWithoutAssignment"));
    Body body = method.getBody();
    assertNotNull(body);

    List<String> actualStmts = filterJimple(body.toString());
    assertEquals(
        expectedBodyStmts(
            "java.lang.String $r1",
            "VariableDeclaration r0",
            "r0 := @this: VariableDeclaration",
            "$r1 = null",
            "return"),
        actualStmts);
  }

  public MethodSignature getMethodSignature(String methodName) {
    return identifierFactory.getMethodSignature(
        methodName, getDeclaredClassSignature(), "void", Collections.emptyList());
  }
}
