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
public class InitializeArraysWithIndexTest extends MinimalBytecodeTestSuiteBase {

  @Test
  public void test() {

    SootMethod method = loadMethod(getMethodSignature("intArrays"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "l0 := @this: InitializeArraysWithIndex",
            "l1 = newarray (int)[3]",
            "l1[0] = 1",
            "l1[1] = 2",
            "l1[2] = 3",
            "return"));

    method = loadMethod(getMethodSignature("byteArrays"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "l0 := @this: InitializeArraysWithIndex",
            "l1 = newarray (byte)[3]",
            "l1[0] = 4",
            "l1[1] = 5",
            "l1[2] = 6",
            "return"));

    method = loadMethod(getMethodSignature("shortArrays"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "l0 := @this: InitializeArraysWithIndex",
            "l1 = newarray (short)[3]",
            "l1[0] = 10",
            "l1[1] = 20",
            "l1[2] = 30",
            "return"));

    method = loadMethod(getMethodSignature("longArrays"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "l0 := @this: InitializeArraysWithIndex",
            "l1 = newarray (long)[3]",
            "l1[0] = 547087L",
            "l1[1] = 564645L",
            "l1[2] = 654786L",
            "return"));

    method = loadMethod(getMethodSignature("floatArrays"));

    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "l0 := @this: InitializeArraysWithIndex",
            "l1 = newarray (float)[4]",
            "l1[0] = 3.14F",
            "l1[1] = 5.46F",
            "l1[2] = 2.987F",
            "l1[3] = 4.87F",
            "return"));

    method = loadMethod(getMethodSignature("doubleArrays"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "l0 := @this: InitializeArraysWithIndex",
            "l1 = newarray (double)[2]",
            "l1[0] = 6.765414",
            "l1[1] = 9.676565646",
            "return"));

    method = loadMethod(getMethodSignature("booleanArrays"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "l0 := @this: InitializeArraysWithIndex",
            "l1 = newarray (boolean)[2]",
            "l1[0] = 1",
            "l1[1] = 0",
            "return"));

    method = loadMethod(getMethodSignature("charArrays"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "l0 := @this: InitializeArraysWithIndex",
            "l1 = newarray (char)[3]",
            "l1[0] = 65",
            "l1[1] = 98",
            "l1[2] = 38",
            "return"));

    method = loadMethod(getMethodSignature("stringArrays"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "l0 := @this: InitializeArraysWithIndex",
            "l1 = newarray (java.lang.String)[2]",
            "l1[0] = \"Hello World\"",
            "l1[1] = \"Greetings\"",
            "return"));
  }

  public MethodSignature getMethodSignature(String methodName) {
    return identifierFactory.getMethodSignature(
        methodName, getDeclaredClassSignature(), "void", Collections.emptyList());
  }
}
