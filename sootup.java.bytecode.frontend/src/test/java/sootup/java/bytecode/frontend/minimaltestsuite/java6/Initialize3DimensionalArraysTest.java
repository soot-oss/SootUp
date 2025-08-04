package sootup.java.bytecode.frontend.minimaltestsuite.java6;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.bytecode.frontend.minimaltestsuite.MinimalBytecodeTestSuiteBase;

/**
 * @author Kaustubh Kelkar
 */
public class Initialize3DimensionalArraysTest extends MinimalBytecodeTestSuiteBase {

  @Test
  public void test() {

    SootMethod method = loadMethod(getMethodSignature("intArrays"));
    assertJimpleStmts(method, expectedBodyStmtsIntArrays());

    method = loadMethod(getMethodSignature("byteArrays"));
    assertJimpleStmts(method, expectedBodyStmtsByteArrays());

    method = loadMethod(getMethodSignature("shortArrays"));
    assertJimpleStmts(method, expectedBodyStmtsShortArrays());

    method = loadMethod(getMethodSignature("longArrays"));
    assertJimpleStmts(method, expectedBodyStmtsLongArrays());

    method = loadMethod(getMethodSignature("floatArrays"));
    assertJimpleStmts(method, expectedBodyStmtsFloatArrays());

    method = loadMethod(getMethodSignature("doubleArrays"));
    assertJimpleStmts(method, expectedBodyStmtsDoubleArrays());

    method = loadMethod(getMethodSignature("booleanArrays"));
    assertJimpleStmts(method, expectedBodyStmtsBooleanArrays());

    method = loadMethod(getMethodSignature("charArrays"));
    assertJimpleStmts(method, expectedBodyStmtsCharArrays());

    method = loadMethod(getMethodSignature("stringArrays"));
    assertJimpleStmts(method, expectedBodyStmtsStringArrays());
  }

  public MethodSignature getMethodSignature(String methodName) {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), methodName, "void", Collections.emptyList());
  }

  /**
   *
   *
   * <pre>
   *     public void intArrays(){
   *         int[][][] intArray3D = {{{1, 2, 3},{5, 6}},{{7, 8, 9},{10,11}}};
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsIntArrays() {
    return Stream.of(
            "this := @this: Initialize3DimensionalArrays",
            "$stack5 = newarray (int[][])[2]",
            "$stack3 = newarray (int[])[2]",
            "$stack2 = newarray (int)[3]",
            "$stack2[0] = 1",
            "$stack2[1] = 2",
            "$stack2[2] = 3",
            "$stack3[0] = $stack2",
            "$stack4 = newarray (int)[2]",
            "$stack4[0] = 5",
            "$stack4[1] = 6",
            "$stack3[1] = $stack4",
            "$stack5[0] = $stack3",
            "$stack7 = newarray (int[])[2]",
            "$stack6 = newarray (int)[3]",
            "$stack6[0] = 7",
            "$stack6[1] = 8",
            "$stack6[2] = 9",
            "$stack7[0] = $stack6",
            "$stack8 = newarray (int)[2]",
            "$stack8[0] = 10",
            "$stack8[1] = 11",
            "$stack7[1] = $stack8",
            "$stack5[1] = $stack7",
            "l1 = $stack5",
            "return")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   *     public void byteArrays(){
   *         byte[][][] byteArray3D = {{{7, 8, 9},{10,11}},{{1, 2, 3},{5, 6}}};;
   *
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsByteArrays() {
    return Stream.of(
            "this := @this: Initialize3DimensionalArrays",
            "$stack5 = newarray (byte[][])[2]",
            "$stack3 = newarray (byte[])[2]",
            "$stack2 = newarray (byte)[3]",
            "$stack2[0] = 7",
            "$stack2[1] = 8",
            "$stack2[2] = 9",
            "$stack3[0] = $stack2",
            "$stack4 = newarray (byte)[2]",
            "$stack4[0] = 10",
            "$stack4[1] = 11",
            "$stack3[1] = $stack4",
            "$stack5[0] = $stack3",
            "$stack7 = newarray (byte[])[2]",
            "$stack6 = newarray (byte)[3]",
            "$stack6[0] = 1",
            "$stack6[1] = 2",
            "$stack6[2] = 3",
            "$stack7[0] = $stack6",
            "$stack8 = newarray (byte)[2]",
            "$stack8[0] = 5",
            "$stack8[1] = 6",
            "$stack7[1] = $stack8",
            "$stack5[1] = $stack7",
            "l1 = $stack5",
            "return")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   *     public void shortArrays(){
   *         short[][][] shortArray3D = {{{10,20},{40,85}},{{56,59},{95,35}}};
   *
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsShortArrays() {
    return Stream.of(
            "this := @this: Initialize3DimensionalArrays",
            "$stack5 = newarray (short[][])[2]",
            "$stack3 = newarray (short[])[2]",
            "$stack2 = newarray (short)[2]",
            "$stack2[0] = 10",
            "$stack2[1] = 20",
            "$stack3[0] = $stack2",
            "$stack4 = newarray (short)[2]",
            "$stack4[0] = 40",
            "$stack4[1] = 85",
            "$stack3[1] = $stack4",
            "$stack5[0] = $stack3",
            "$stack7 = newarray (short[])[2]",
            "$stack6 = newarray (short)[2]",
            "$stack6[0] = 56",
            "$stack6[1] = 59",
            "$stack7[0] = $stack6",
            "$stack8 = newarray (short)[2]",
            "$stack8[0] = 95",
            "$stack8[1] = 35",
            "$stack7[1] = $stack8",
            "$stack5[1] = $stack7",
            "l1 = $stack5",
            "return")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   *     public void longArrays(){
   *         long[][][] longArray3D = {{{547087L, 654786L},{547287L, 864645L, 6533786L}},{{34565L,234L},{9851L,63543L}}};
   *
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsLongArrays() {
    return Stream.of(
            "this := @this: Initialize3DimensionalArrays",
            "$stack5 = newarray (long[][])[2]",
            "$stack3 = newarray (long[])[2]",
            "$stack2 = newarray (long)[2]",
            "$stack2[0] = 547087L",
            "$stack2[1] = 654786L",
            "$stack3[0] = $stack2",
            "$stack4 = newarray (long)[3]",
            "$stack4[0] = 547287L",
            "$stack4[1] = 864645L",
            "$stack4[2] = 6533786L",
            "$stack3[1] = $stack4",
            "$stack5[0] = $stack3",
            "$stack7 = newarray (long[])[2]",
            "$stack6 = newarray (long)[2]",
            "$stack6[0] = 34565L",
            "$stack6[1] = 234L",
            "$stack7[0] = $stack6",
            "$stack8 = newarray (long)[2]",
            "$stack8[0] = 9851L",
            "$stack8[1] = 63543L",
            "$stack7[1] = $stack8",
            "$stack5[1] = $stack7",
            "l1 = $stack5",
            "return")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   *     public void floatArrays(){
   *         float[][][] floatrray3D = {{{3.14f, 5.46f}, {2.987f, 4.87f}},{{65.15f,854.18f},{16.51f,58.14f}}};
   *
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsFloatArrays() {
    return Stream.of(
            "this := @this: Initialize3DimensionalArrays",
            "$stack5 = newarray (float[][])[2]",
            "$stack3 = newarray (float[])[2]",
            "$stack2 = newarray (float)[2]",
            "$stack2[0] = 3.14F",
            "$stack2[1] = 5.46F",
            "$stack3[0] = $stack2",
            "$stack4 = newarray (float)[2]",
            "$stack4[0] = 2.987F",
            "$stack4[1] = 4.87F",
            "$stack3[1] = $stack4",
            "$stack5[0] = $stack3",
            "$stack7 = newarray (float[])[2]",
            "$stack6 = newarray (float)[2]",
            "$stack6[0] = 65.15F",
            "$stack6[1] = 854.18F",
            "$stack7[0] = $stack6",
            "$stack8 = newarray (float)[2]",
            "$stack8[0] = 16.51F",
            "$stack8[1] = 58.14F",
            "$stack7[1] = $stack8",
            "$stack5[1] = $stack7",
            "l1 = $stack5",
            "return")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   *     public void doubleArrays(){
   *         double[][][] doubleArray3D = {{{6.765414d, 9.676565646d},{45.345435d}},{{3.5656d,68.234234d},{68416.651d,65416.5d}}};
   *
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsDoubleArrays() {
    return Stream.of(
            "this := @this: Initialize3DimensionalArrays",
            "$stack5 = newarray (double[][])[2]",
            "$stack3 = newarray (double[])[2]",
            "$stack2 = newarray (double)[2]",
            "$stack2[0] = 6.765414",
            "$stack2[1] = 9.676565646",
            "$stack3[0] = $stack2",
            "$stack4 = newarray (double)[1]",
            "$stack4[0] = 45.345435",
            "$stack3[1] = $stack4",
            "$stack5[0] = $stack3",
            "$stack7 = newarray (double[])[2]",
            "$stack6 = newarray (double)[2]",
            "$stack6[0] = 3.5656",
            "$stack6[1] = 68.234234",
            "$stack7[0] = $stack6",
            "$stack8 = newarray (double)[2]",
            "$stack8[0] = 68416.651",
            "$stack8[1] = 65416.5",
            "$stack7[1] = $stack8",
            "$stack5[1] = $stack7",
            "l1 = $stack5",
            "return")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   *     public void booleanArrays(){
   *         boolean[][][] boolArray3D = {{{true, false},{true}},{{false,false},{true}}};
   *
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsBooleanArrays() {
    return Stream.of(
            "this := @this: Initialize3DimensionalArrays",
            "$stack5 = newarray (boolean[][])[2]",
            "$stack3 = newarray (boolean[])[2]",
            "$stack2 = newarray (boolean)[2]",
            "$stack2[0] = 1",
            "$stack2[1] = 0",
            "$stack3[0] = $stack2",
            "$stack4 = newarray (boolean)[1]",
            "$stack4[0] = 1",
            "$stack3[1] = $stack4",
            "$stack5[0] = $stack3",
            "$stack7 = newarray (boolean[])[2]",
            "$stack6 = newarray (boolean)[2]",
            "$stack6[0] = 0",
            "$stack6[1] = 0",
            "$stack7[0] = $stack6",
            "$stack8 = newarray (boolean)[1]",
            "$stack8[0] = 1",
            "$stack7[1] = $stack8",
            "$stack5[1] = $stack7",
            "l1 = $stack5",
            "return")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   *     public void charArrays(){
   *         char[][][] charArray3D = {{{'A', 'b', '&'},{'c',''}},{{'2','G'},{'a','%'}}};
   *
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsCharArrays() {
    return Stream.of(
            "this := @this: Initialize3DimensionalArrays",
            "$stack5 = newarray (char[][])[2]",
            "$stack3 = newarray (char[])[2]",
            "$stack2 = newarray (char)[3]",
            "$stack2[0] = 65",
            "$stack2[1] = 98",
            "$stack2[2] = 38",
            "$stack3[0] = $stack2",
            "$stack4 = newarray (char)[2]",
            "$stack4[0] = 99",
            "$stack4[1] = 36",
            "$stack3[1] = $stack4",
            "$stack5[0] = $stack3",
            "$stack7 = newarray (char[])[2]",
            "$stack6 = newarray (char)[2]",
            "$stack6[0] = 50",
            "$stack6[1] = 71",
            "$stack7[0] = $stack6",
            "$stack8 = newarray (char)[2]",
            "$stack8[0] = 97",
            "$stack8[1] = 37",
            "$stack7[1] = $stack8",
            "$stack5[1] = $stack7",
            "l1 = $stack5",
            "return")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   *     public void stringArrays() {
   *         String[][][] stringArray3D = {{{"Hello World"}, {"Greetings", "Welcome"}}, {{"Future","Soot"},{"UPB","HNI"}}};
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsStringArrays() {
    return Stream.of(
            "this := @this: Initialize3DimensionalArrays",
            "$stack5 = newarray (java.lang.String[][])[2]",
            "$stack3 = newarray (java.lang.String[])[2]",
            "$stack2 = newarray (java.lang.String)[1]",
            "$stack2[0] = \"Hello World\"",
            "$stack3[0] = $stack2",
            "$stack4 = newarray (java.lang.String)[2]",
            "$stack4[0] = \"Greetings\"",
            "$stack4[1] = \"Welcome\"",
            "$stack3[1] = $stack4",
            "$stack5[0] = $stack3",
            "$stack7 = newarray (java.lang.String[])[2]",
            "$stack6 = newarray (java.lang.String)[2]",
            "$stack6[0] = \"Future\"",
            "$stack6[1] = \"Soot\"",
            "$stack7[0] = $stack6",
            "$stack8 = newarray (java.lang.String)[2]",
            "$stack8[0] = \"UPB\"",
            "$stack8[1] = \"HNI\"",
            "$stack7[1] = $stack8",
            "$stack5[1] = $stack7",
            "l1 = $stack5",
            "return")
        .collect(Collectors.toList());
  }
}
