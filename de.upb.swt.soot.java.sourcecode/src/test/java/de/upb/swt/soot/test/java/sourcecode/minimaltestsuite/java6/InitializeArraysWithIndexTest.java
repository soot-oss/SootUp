/** @author: Hasitha Rajapakse */
package de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.java6;

import categories.Java8Test;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.MinimalTestSuiteBase;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(Java8Test.class)
public class InitializeArraysWithIndexTest extends MinimalTestSuiteBase {

  @Test
  public void defaultTest() {
    loadMethod(
        expectedBodyStmts(
            Stream.of(
                    "r0 := @this: InitializeArraysWithIndex",
                    "$r1 = newarray (int[])[3]",
                    "$r1[0] = 1",
                    "$r1[1] = 2",
                    "$r1[2] = 3",
                    "return")
                .collect(Collectors.toList())),
        getMethodSignature("intArrays"));

    loadMethod(
        expectedBodyStmts(
            Stream.of(
                    "r0 := @this: InitializeArraysWithIndex",
                    "$r1 = newarray (byte[])[3]",
                    "$r1[0] = 4",
                    "$r1[1] = 5",
                    "$r1[2] = 6",
                    "return")
                .collect(Collectors.toList())),
        getMethodSignature("byteArrays"));

    loadMethod(
        expectedBodyStmts(
            Stream.of(
                    "r0 := @this: InitializeArraysWithIndex",
                    "$r1 = newarray (short[])[3]",
                    "$r1[0] = 10",
                    "$r1[1] = 20",
                    "$r1[2] = 30",
                    "return")
                .collect(Collectors.toList())),
        getMethodSignature("shortArrays"));

    loadMethod(
        expectedBodyStmts(
            Stream.of(
                    "r0 := @this: InitializeArraysWithIndex",
                    "$r1 = newarray (long[])[3]",
                    "$r1[0] = 547087L",
                    "$r1[1] = 564645L",
                    "$r1[2] = 654786L",
                    "return")
                .collect(Collectors.toList())),
        getMethodSignature("longArrays"));

    loadMethod(
        expectedBodyStmts(
            Stream.of(
                    "r0 := @this: InitializeArraysWithIndex",
                    "$r1 = newarray (float[])[4]",
                    "$r1[0] = 3.14F",
                    "$r1[1] = 5.46F",
                    "$r1[2] = 2.987F",
                    "$r1[3] = 4.87F",
                    "return")
                .collect(Collectors.toList())),
        getMethodSignature("floatArrays"));

    loadMethod(
        expectedBodyStmts(
            Stream.of(
                    "r0 := @this: InitializeArraysWithIndex",
                    "$r1 = newarray (double[])[2]",
                    "$r1[0] = 6.765414",
                    "$r1[1] = 9.676565646",
                    "return")
                .collect(Collectors.toList())),
        getMethodSignature("doubleArrays"));

    loadMethod(
        expectedBodyStmts(
            Stream.of(
                    "r0 := @this: InitializeArraysWithIndex",
                    "$r1 = newarray (boolean[])[2]",
                    "$r1[0] = 1",
                    "$r1[1] = 0",
                    "return")
                .collect(Collectors.toList())),
        getMethodSignature("booleanArrays"));

    loadMethod(
        expectedBodyStmts(
            Stream.of(
                    "r0 := @this: InitializeArraysWithIndex",
                    "$r1 = newarray (char[])[3]",
                    "$r1[0] = 65",
                    "$r1[1] = 98",
                    "$r1[2] = 38",
                    "return")
                .collect(Collectors.toList())),
        getMethodSignature("charArrays"));

    loadMethod(
        expectedBodyStmts(
            Stream.of(
                    "r0 := @this: InitializeArraysWithIndex",
                    "$r1 = newarray (java.lang.String[])[2]",
                    "$r1[0] = \"Hello World\"",
                    "$r1[1] = \"Greetings\"",
                    "return")
                .collect(Collectors.toList())),
        getMethodSignature("stringArrays"));
  }

  public MethodSignature getMethodSignature(String methodName) {
    return identifierFactory.getMethodSignature(
        methodName, getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  public List<String> expectedBodyStmts(List<String> jimpleLines) {
    return jimpleLines;
  }
}
