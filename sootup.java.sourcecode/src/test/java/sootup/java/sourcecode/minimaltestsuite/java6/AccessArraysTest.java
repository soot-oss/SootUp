/** @author: Hasitha Rajapakse */
package sootup.java.sourcecode.minimaltestsuite.java6;

import categories.Java8Test;
import java.util.*;
import org.junit.Ignore;
import org.junit.experimental.categories.Category;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.sourcecode.minimaltestsuite.MinimalSourceTestSuiteBase;

@Category(Java8Test.class)
public class AccessArraysTest extends MinimalSourceTestSuiteBase {

  @Ignore
  public void test() {

    // FIXME [ms] see InstructionConverter.convertUnaryOpInstruction(...)
    // TODO split into multiple test cases

    SootMethod method = loadMethod(getMethodSignature("intArrays"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "r0 := @this: AccessArrays",
            "$r1 = newarray (int[])[3]",
            "$r1[0] = 1",
            "$r1[1] = 2",
            "$r1[2] = 3",
            "$i0 = 0",
            "$r2 = $r1",
            "$i1 = 0",
            "label1:",
            "$i2 = lengthof $r2",
            "$z0 = $i1 < $i2",
            "if $z0 == 0 goto label2",
            "$r3 = $r2[$i1]",
            "$i0 = $r3",
            "$i3 = $i1",
            "$i4 = $i1 + 1",
            "$i1 = $i4",
            "goto label1",
            "label2:",
            "return"));

    method = loadMethod(getMethodSignature("byteArrays"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "r0 := @this: AccessArrays",
            "$r1 = newarray (byte[])[3]",
            "$r1[0] = 4",
            "$r1[1] = 5",
            "$r1[2] = 6",
            "$i0 = 0",
            "$r2 = $r1",
            "$i1 = 0",
            "label1:",
            "$i2 = lengthof $r2",
            "$z0 = $i1 < $i2",
            "if $z0 == 0 goto label2",
            "$r3 = $r2[$i1]",
            "$i0 = $r3",
            "$i3 = $i1",
            "$i4 = $i1 + 1",
            "$i1 = $i4",
            "goto label1",
            "label2:",
            "return"));
    method = loadMethod(getMethodSignature("shortArrays"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "r0 := @this: AccessArrays",
            "$r1 = newarray (short[])[3]",
            "$r1[0] = 10",
            "$r1[1] = 20",
            "$r1[2] = 30",
            "$i0 = 0",
            "$r2 = $r1",
            "$i1 = 0",
            "label1:",
            "$i2 = lengthof $r2",
            "$z0 = $i1 < $i2",
            "if $z0 == 0 goto label2",
            "$r3 = $r2[$i1]",
            "$i0 = $r3",
            "$i3 = $i1",
            "$i4 = $i1 + 1",
            "$i1 = $i4",
            "goto label1",
            "label2:",
            "return"));

    method = loadMethod(getMethodSignature("longArrays"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "r0 := @this: AccessArrays",
            "$r1 = newarray (long[])[3]",
            "$r1[0] = 547087L",
            "$r1[1] = 564645L",
            "$r1[2] = 654786L",
            "$i0 = 0",
            "$r2 = $r1",
            "$i1 = 0",
            "label1:",
            "$i2 = lengthof $r2",
            "$z0 = $i1 < $i2",
            "if $z0 == 0 goto label2",
            "$r3 = $r2[$i1]",
            "$i0 = $r3",
            "$i3 = $i1",
            "$i4 = $i1 + 1",
            "$i1 = $i4",
            "goto label1",
            "label2:",
            "return"));

    method = loadMethod(getMethodSignature("floatArrays"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "r0 := @this: AccessArrays",
            "$r1 = newarray (float[])[4]",
            "$r1[0] = 3.14F",
            "$r1[1] = 5.46F",
            "$r1[2] = 2.987F",
            "$r1[3] = 4.87F",
            "$d0 = 0.0",
            "$r2 = $r1",
            "$i0 = 0",
            "label1:",
            "$i1 = lengthof $r2",
            "$z0 = $i0 < $i1",
            "if $z0 == 0 goto label2",
            "$r3 = $r2[$i0]",
            "$d0 = $r3",
            "$i2 = $i0",
            "$i3 = $i0 + 1",
            "$i0 = $i3",
            "goto label1",
            "label2:",
            "return"));

    method = loadMethod(getMethodSignature("doubleArrays"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "r0 := @this: AccessArrays",
            "$r1 = newarray (double[])[2]",
            "$r1[0] = 6.765414",
            "$r1[1] = 9.676565646",
            "$d0 = 0.0",
            "$r2 = $r1",
            "$i0 = 0",
            "label1:",
            "$i1 = lengthof $r2",
            "$z0 = $i0 < $i1",
            "if $z0 == 0 goto label2",
            "$r3 = $r2[$i0]",
            "$d0 = $r3",
            "$i2 = $i0",
            "$i3 = $i0 + 1",
            "$i0 = $i3",
            "goto label1",
            "label2:",
            "return"));

    method = loadMethod(getMethodSignature("booleanArrays"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "r0 := @this: AccessArrays",
            "$r1 = newarray (boolean[])[2]",
            "$r1[0] = 1",
            "$r1[1] = 0",
            "$l2 = null", // TODO:[ms] CHECK! should this be null? --> "boolean val;"
            "$r3 = $r1",
            "$i0 = 0",
            "label1:",
            "$i1 = lengthof $r3",
            "$z0 = $i0 < $i1",
            "if $z0 == 0 goto label2",
            "$r4 = $r3[$i0]",
            "$r2 = $r4",
            "$i2 = $i0",
            "$i3 = $i0 + 1",
            "$i0 = $i3",
            "goto label1",
            "label2:",
            "return"));

    method = loadMethod(getMethodSignature("charArrays"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "r0 := @this: AccessArrays",
            "$r1 = newarray (char[])[3]",
            "$r1[0] = 65",
            "$r1[1] = 98",
            "$r1[2] = 38",
            "$i0 = 0",
            "$r2 = $r1",
            "$i1 = 0",
            "label1:",
            "$i2 = lengthof $r2",
            "$z0 = $i1 < $i2",
            "if $z0 == 0 goto label2",
            "$r3 = $r2[$i1]",
            "$i0 = $r3",
            "$i3 = $i1",
            "$i4 = $i1 + 1",
            "$i1 = $i4",
            "goto label1",
            "label2:",
            "return"));

    method = loadMethod(getMethodSignature("stringArrays"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "r0 := @this: AccessArrays",
            "$r1 = newarray (java.lang.String[])[2]",
            "$r1[0] = \"Hello World\"",
            "$r1[1] = \"Greetings\"",
            "$r2 = null",
            "$r3 = $r1",
            "$i0 = 0",
            "label1:",
            "$i1 = lengthof $r3",
            "$z0 = $i0 < $i1",
            "if $z0 == 0 goto label2",
            "$r4 = $r3[$i0]",
            "$r2 = $r4",
            "$i2 = $i0",
            "$i3 = $i0 + 1",
            "$i0 = $i3",
            "goto label1",
            "label2:",
            "return"));
  }

  public MethodSignature getMethodSignature(String methodName) {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), methodName, "void", Collections.emptyList());
  }
}
