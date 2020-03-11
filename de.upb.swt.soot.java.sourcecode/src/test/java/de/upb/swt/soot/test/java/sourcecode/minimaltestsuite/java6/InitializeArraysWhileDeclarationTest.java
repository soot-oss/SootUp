/** @author: Hasitha Rajapakse */
package de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.java6;

import categories.Java8Test;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.MinimalSourceTestSuiteBase;
import java.util.*;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(Java8Test.class)
public class InitializeArraysWhileDeclarationTest extends MinimalSourceTestSuiteBase {

  @Test
  public void defaultTest() {
    SootMethod method = loadMethod(getMethodSignature("intArrays"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "r0 := @this: InitializeArraysWhileDeclaration",
            "$r1 = newarray (int[])[3]",
            "$r1[0] = 1",
            "$r1[1] = 2",
            "$r1[2] = 3",
            "return"));

    method = loadMethod(getMethodSignature("byteArrays"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "r0 := @this: InitializeArraysWhileDeclaration",
            "$r1 = newarray (byte[])[3]",
            "$r1[0] = 4",
            "$r1[1] = 5",
            "$r1[2] = 6",
            "return"));

    method = loadMethod(getMethodSignature("shortArrays"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "r0 := @this: InitializeArraysWhileDeclaration",
            "$r1 = newarray (short[])[3]",
            "$r1[0] = 10",
            "$r1[1] = 20",
            "$r1[2] = 30",
            "return"));

    method = loadMethod(getMethodSignature("longArrays"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "r0 := @this: InitializeArraysWhileDeclaration",
            "$r1 = newarray (long[])[3]",
            "$r1[0] = 547087L",
            "$r1[1] = 564645L",
            "$r1[2] = 654786L",
            "return"));

    method = loadMethod(getMethodSignature("floatArrays"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "r0 := @this: InitializeArraysWhileDeclaration",
            "$r1 = newarray (float[])[4]",
            "$r1[0] = 3.14F",
            "$r1[1] = 5.46F",
            "$r1[2] = 2.987F",
            "$r1[3] = 4.87F",
            "return"));

    method = loadMethod(getMethodSignature("doubleArrays"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "r0 := @this: InitializeArraysWhileDeclaration",
            "$r1 = newarray (double[])[2]",
            "$r1[0] = 6.765414",
            "$r1[1] = 9.676565646",
            "return"));

    method = loadMethod(getMethodSignature("booleanArrays"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "r0 := @this: InitializeArraysWhileDeclaration",
            "$r1 = newarray (boolean[])[2]",
            "$r1[0] = 1",
            "$r1[1] = 0",
            "return"));

    method = loadMethod(getMethodSignature("charArrays"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "r0 := @this: InitializeArraysWhileDeclaration",
            "$r1 = newarray (char[])[3]",
            "$r1[0] = 65",
            "$r1[1] = 98",
            "$r1[2] = 38",
            "return"));

    method = loadMethod(getMethodSignature("stringArrays"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "r0 := @this: InitializeArraysWhileDeclaration",
            "$r1 = newarray (java.lang.String[])[2]",
            "$r1[0] = \"Hello World\"",
            "$r1[1] = \"Greetings\"",
            "return"));
  }

  public MethodSignature getMethodSignature(String methodName) {
    return identifierFactory.getMethodSignature(
        methodName, getDeclaredClassSignature(), "void", Collections.emptyList());
  }
}
