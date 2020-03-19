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
public class CharLiteralsTest extends MinimalBytecodeTestSuiteBase {

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature("charCharacter"));
    assertJimpleStmts(method, expectedBodyStmts("l0 := @this: CharLiterals", "l1 = 97", "return"));

    method = loadMethod(getMethodSignature("charSymbol"));
    assertJimpleStmts(method, expectedBodyStmts("l0 := @this: CharLiterals", "l1 = 37", "return"));

    method = loadMethod(getMethodSignature("charBackslashT"));
    assertJimpleStmts(method, expectedBodyStmts("l0 := @this: CharLiterals", "l1 = 9", "return"));

    method = loadMethod(getMethodSignature("charBackslash"));
    assertJimpleStmts(method, expectedBodyStmts("l0 := @this: CharLiterals", "l1 = 92", "return"));

    method = loadMethod(getMethodSignature("charSingleQuote"));
    assertJimpleStmts(method, expectedBodyStmts("l0 := @this: CharLiterals", "l1 = 39", "return"));

    method = loadMethod(getMethodSignature("charUnicode"));
    assertJimpleStmts(method, expectedBodyStmts("l0 := @this: CharLiterals", "l1 = 937", "return"));

    method = loadMethod(getMethodSignature("specialChar"));
    assertJimpleStmts(
        method, expectedBodyStmts("l0 := @this: CharLiterals", "l1 = 8482", "return"));
  }

  public MethodSignature getMethodSignature(String methodName) {
    return identifierFactory.getMethodSignature(
        methodName, getDeclaredClassSignature(), "void", Collections.emptyList());
  }
}
