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
public class VariableDeclarationTest extends MinimalBytecodeTestSuiteBase {

  @Test
  public void test() {

    SootMethod method = loadMethod(getMethodSignature("shortVariable"));

    assertJimpleStmts(
        method, expectedBodyStmts("l0 := @this: VariableDeclaration", "l1 = 10", "return"));

    method = loadMethod(getMethodSignature("byteVariable"));

    assertJimpleStmts(
        method, expectedBodyStmts("l0 := @this: VariableDeclaration", "l1 = 0", "return"));

    method = loadMethod(getMethodSignature("charVariable"));

    assertJimpleStmts(
        method, expectedBodyStmts("l0 := @this: VariableDeclaration", "l1 = 97", "return"));

    method = loadMethod(getMethodSignature("intVariable"));
    assertJimpleStmts(
        method, expectedBodyStmts("l0 := @this: VariableDeclaration", "l1 = 512", "return"));

    method = loadMethod(getMethodSignature("longVariable"));
    assertJimpleStmts(
        method, expectedBodyStmts("l0 := @this: VariableDeclaration", "l1 = 123456789L", "return"));

    method = loadMethod(getMethodSignature("floatVariable"));
    assertJimpleStmts(
        method, expectedBodyStmts("l0 := @this: VariableDeclaration", "l1 = 3.14F", "return"));

    method = loadMethod(getMethodSignature("doubleVariable"));
    assertJimpleStmts(
        method, expectedBodyStmts("l0 := @this: VariableDeclaration", "l1 = 1.96969654", "return"));
  }

  public MethodSignature getMethodSignature(String methodName) {
    return identifierFactory.getMethodSignature(
        methodName, getDeclaredClassSignature(), "void", Collections.emptyList());
  }
}
