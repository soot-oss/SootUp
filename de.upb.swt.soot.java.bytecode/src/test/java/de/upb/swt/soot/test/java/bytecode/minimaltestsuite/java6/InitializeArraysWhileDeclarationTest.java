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
public class InitializeArraysWhileDeclarationTest extends MinimalBytecodeTestSuiteBase {

  @Test
  public void defaultTest() {
    SootMethod method = loadMethod(getMethodSignature("intArrays"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "l0 := @this: InitializeArraysWhileDeclaration",
            "$stack2 = newarray (int)[3]",
            "$stack2[0] = 1",
            "$stack2[1] = 2",
            "$stack2[2] = 3",
            "l1 = $stack2",
            "return"));

    method = loadMethod(getMethodSignature("byteArrays"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "l0 := @this: InitializeArraysWhileDeclaration",
            "$stack2 = newarray (byte)[3]",
            "$stack2[0] = 4",
            "$stack2[1] = 5",
            "$stack2[2] = 6",
            "l1 = $stack2",
            "return"));

    method = loadMethod(getMethodSignature("shortArrays"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "l0 := @this: InitializeArraysWhileDeclaration",
            "$stack2 = newarray (short)[3]",
            "$stack2[0] = 10",
            "$stack2[1] = 20",
            "$stack2[2] = 30",
            "l1 = $stack2",
            "return"));

    method = loadMethod(getMethodSignature("longArrays"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "l0 := @this: InitializeArraysWhileDeclaration",
            "$stack2 = newarray (long)[3]",
            "$stack2[0] = 547087L",
            "$stack2[1] = 564645L",
            "$stack2[2] = 654786L",
            "l1 = $stack2",
            "return"));

    method = loadMethod(getMethodSignature("floatArrays"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "l0 := @this: InitializeArraysWhileDeclaration",
            "$stack2 = newarray (float)[4]",
            "$stack2[0] = 3.14F",
            "$stack2[1] = 5.46F",
            "$stack2[2] = 2.987F",
            "$stack2[3] = 4.87F",
            "l1 = $stack2",
            "return"));

    method = loadMethod(getMethodSignature("doubleArrays"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "l0 := @this: InitializeArraysWhileDeclaration",
            "$stack2 = newarray (double)[2]",
            "$stack2[0] = 6.765414",
            "$stack2[1] = 9.676565646",
            "l1 = $stack2",
            "return"));

    method = loadMethod(getMethodSignature("booleanArrays"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "l0 := @this: InitializeArraysWhileDeclaration",
            "$stack2 = newarray (boolean)[2]",
            "$stack2[0] = 1",
            "$stack2[1] = 0",
            "l1 = $stack2",
            "return"));

    method = loadMethod(getMethodSignature("charArrays"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "l0 := @this: InitializeArraysWhileDeclaration",
            "$stack2 = newarray (char)[3]",
            "$stack2[0] = 65",
            "$stack2[1] = 98",
            "$stack2[2] = 38",
            "l1 = $stack2",
            "return"));

    method = loadMethod(getMethodSignature("stringArrays"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "l0 := @this: InitializeArraysWhileDeclaration",
            "$stack2 = newarray (java.lang.String)[2]",
            "$stack2[0] = \"Hello World\"",
            "$stack2[1] = \"Greetings\"",
            "l1 = $stack2",
            "return"));
  }

  public MethodSignature getMethodSignature(String methodName) {
    return identifierFactory.getMethodSignature(
        methodName, getDeclaredClassSignature(), "void", Collections.emptyList());
  }
}
