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
public class AccessArraysTest extends MinimalBytecodeTestSuiteBase {

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature("intArrays"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "l0 := @this: AccessArrays",
            "$stack7 = newarray (int)[3]",
            "$stack7[0] = 1",
            "$stack7[1] = 2",
            "$stack7[2] = 3",
            "l1 = $stack7",
            "l3 = l1",
            "l4 = lengthof l3",
            "l5 = 0",
            "label1:",
            "$stack9 = l5",
            "$stack8 = l4",
            "if $stack9 >= $stack8 goto label2",
            "l6 = l3[l5]",
            "l2 = l6",
            "l5 = l5 + 1",
            "goto label1",
            "label2:",
            "return"));

    method = loadMethod(getMethodSignature("byteArrays"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "l0 := @this: AccessArrays",
            "$stack7 = newarray (byte)[3]",
            "$stack7[0] = 4",
            "$stack7[1] = 5",
            "$stack7[2] = 6",
            "l1 = $stack7",
            "l3 = l1",
            "l4 = lengthof l3",
            "l5 = 0",
            "label1:",
            "$stack9 = l5",
            "$stack8 = l4",
            "if $stack9 >= $stack8 goto label2",
            "l6 = l3[l5]",
            "l2 = l6",
            "l5 = l5 + 1",
            "goto label1",
            "label2:",
            "return"));
    method = loadMethod(getMethodSignature("shortArrays"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "l0 := @this: AccessArrays",
            "$stack7 = newarray (short)[3]",
            "$stack7[0] = 10",
            "$stack7[1] = 20",
            "$stack7[2] = 30",
            "l1 = $stack7",
            "l3 = l1",
            "l4 = lengthof l3",
            "l5 = 0",
            "label1:",
            "$stack9 = l5",
            "$stack8 = l4",
            "if $stack9 >= $stack8 goto label2",
            "l6 = l3[l5]",
            "l2 = l6",
            "l5 = l5 + 1",
            "goto label1",
            "label2:",
            "return"));

    method = loadMethod(getMethodSignature("longArrays"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "l0 := @this: AccessArrays",
            "$stack9 = newarray (long)[3]",
            "$stack9[0] = 547087L",
            "$stack9[1] = 564645L",
            "$stack9[2] = 654786L",
            "l1 = $stack9",
            "l4 = l1",
            "l5 = lengthof l4",
            "l6 = 0",
            "label1:",
            "$stack11 = l6",
            "$stack10 = l5",
            "if $stack11 >= $stack10 goto label2",
            "l7 = l4[l6]",
            "l2 = l7",
            "l6 = l6 + 1",
            "goto label1",
            "label2:",
            "return"));

    method = loadMethod(getMethodSignature("floatArrays"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "l0 := @this: AccessArrays",
            "$stack7 = newarray (float)[4]",
            "$stack7[0] = 3.14F",
            "$stack7[1] = 5.46F",
            "$stack7[2] = 2.987F",
            "$stack7[3] = 4.87F",
            "l1 = $stack7",
            "l3 = l1",
            "l4 = lengthof l3",
            "l5 = 0",
            "label1:",
            "$stack9 = l5",
            "$stack8 = l4",
            "if $stack9 >= $stack8 goto label2",
            "l6 = l3[l5]",
            "l2 = l6",
            "l5 = l5 + 1",
            "goto label1",
            "label2:",
            "return"));

    method = loadMethod(getMethodSignature("doubleArrays"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "l0 := @this: AccessArrays",
            "$stack9 = newarray (double)[2]",
            "$stack9[0] = 6.765414",
            "$stack9[1] = 9.676565646",
            "l1 = $stack9",
            "l4 = l1",
            "l5 = lengthof l4",
            "l6 = 0",
            "label1:",
            "$stack11 = l6",
            "$stack10 = l5",
            "if $stack11 >= $stack10 goto label2",
            "l7 = l4[l6]",
            "l2 = l7",
            "l6 = l6 + 1",
            "goto label1",
            "label2:",
            "return"));
    method = loadMethod(getMethodSignature("booleanArrays"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "l0 := @this: AccessArrays",
            "$stack7 = newarray (boolean)[2]",
            "$stack7[0] = 1",
            "$stack7[1] = 0",
            "l1 = $stack7",
            "l3 = l1",
            "l4 = lengthof l3",
            "l5 = 0",
            "label1:",
            "$stack9 = l5",
            "$stack8 = l4",
            "if $stack9 >= $stack8 goto label2",
            "l6 = l3[l5]",
            "l2 = l6",
            "l5 = l5 + 1",
            "goto label1",
            "label2:",
            "return"));
    method = loadMethod(getMethodSignature("charArrays"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "l0 := @this: AccessArrays",
            "$stack7 = newarray (char)[3]",
            "$stack7[0] = 65",
            "$stack7[1] = 98",
            "$stack7[2] = 38",
            "l1 = $stack7",
            "l3 = l1",
            "l4 = lengthof l3",
            "l5 = 0",
            "label1:",
            "$stack9 = l5",
            "$stack8 = l4",
            "if $stack9 >= $stack8 goto label2",
            "l6 = l3[l5]",
            "l2 = l6",
            "l5 = l5 + 1",
            "goto label1",
            "label2:",
            "return"));

    method = loadMethod(getMethodSignature("stringArrays"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "l0 := @this: AccessArrays",
            "$stack7 = newarray (java.lang.String)[2]",
            "$stack7[0] = \"Hello World\"",
            "$stack7[1] = \"Greetings\"",
            "l1 = $stack7",
            "l3 = l1",
            "l4 = lengthof l3",
            "l5 = 0",
            "label1:",
            "$stack9 = l5",
            "$stack8 = l4",
            "if $stack9 >= $stack8 goto label2",
            "l6 = l3[l5]",
            "l2 = l6",
            "l5 = l5 + 1",
            "goto label1",
            "label2:",
            "return"));
  }

  public MethodSignature getMethodSignature(String methodName) {
    return identifierFactory.getMethodSignature(
        methodName, getDeclaredClassSignature(), "void", Collections.emptyList());
  }
}
