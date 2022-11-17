package sootup.java.bytecode.minimaltestsuite.java6;

import categories.Java8Test;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.bytecode.minimaltestsuite.MinimalBytecodeTestSuiteBase;

/** @author Kaustubh Kelkar */
@Category(Java8Test.class)
public class InitializeMultidimensionalArraysTest extends MinimalBytecodeTestSuiteBase {

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
   *         int[][] anArrayOfInts = {{1, 2, 3},{5, 6},{7, 8, 9}};
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsIntArrays() {
    return Stream.of(
            "l0 := @this: InitializeMultidimensionalArrays",
            "$stack2 = newarray (int[])[3]",
            "$stack3 = newarray (int)[3]",
            "$stack3[0] = 1",
            "$stack3[1] = 2",
            "$stack3[2] = 3",
            "$stack2[0] = $stack3",
            "$stack4 = newarray (int)[2]",
            "$stack4[0] = 5",
            "$stack4[1] = 6",
            "$stack2[1] = $stack4",
            "$stack5 = newarray (int)[3]",
            "$stack5[0] = 7",
            "$stack5[1] = 8",
            "$stack5[2] = 9",
            "$stack2[2] = $stack5",
            "l1 = $stack2",
            "return")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   * public void byteArrays(){
   *         byte[][] anArrayOfBytes = {{4, 5},{2}};
   *
   *     }</pre>
   */
  public List<String> expectedBodyStmtsByteArrays() {
    return Stream.of(
            "l0 := @this: InitializeMultidimensionalArrays",
            "$stack2 = newarray (byte[])[2]",
            "$stack3 = newarray (byte)[2]",
            "$stack3[0] = 4",
            "$stack3[1] = 5",
            "$stack2[0] = $stack3",
            "$stack4 = newarray (byte)[1]",
            "$stack4[0] = 2",
            "$stack2[1] = $stack4",
            "l1 = $stack2",
            "return")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>public void shortArrays(){
   *         short[][] anArrayOfShorts = {{10, 20, 30},{40}};
   *
   *     }</pre>
   */
  public List<String> expectedBodyStmtsShortArrays() {
    return Stream.of(
            "l0 := @this: InitializeMultidimensionalArrays",
            "$stack2 = newarray (short[])[2]",
            "$stack3 = newarray (short)[3]",
            "$stack3[0] = 10",
            "$stack3[1] = 20",
            "$stack3[2] = 30",
            "$stack2[0] = $stack3",
            "$stack4 = newarray (short)[1]",
            "$stack4[0] = 40",
            "$stack2[1] = $stack4",
            "l1 = $stack2",
            "return")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>public void longArrays(){
   *         long[][] anArrayOfLongs = {{547087L, 654786L},{547287L, 864645L, 6533786L},{34565L,234L}};
   *
   *     }</pre>
   */
  public List<String> expectedBodyStmtsLongArrays() {
    return Stream.of(
            "l0 := @this: InitializeMultidimensionalArrays",
            "$stack2 = newarray (long[])[3]",
            "$stack3 = newarray (long)[2]",
            "$stack3[0] = 547087L",
            "$stack3[1] = 654786L",
            "$stack2[0] = $stack3",
            "$stack4 = newarray (long)[3]",
            "$stack4[0] = 547287L",
            "$stack4[1] = 864645L",
            "$stack4[2] = 6533786L",
            "$stack2[1] = $stack4",
            "$stack5 = newarray (long)[2]",
            "$stack5[0] = 34565L",
            "$stack5[1] = 234L",
            "$stack2[2] = $stack5",
            "l1 = $stack2",
            "return")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   *     public void floatArrays(){
   *         float[][] anArrayOfFloats = {{3.14f, 5.46f}, {2.987f, 4.87f}};
   *
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsFloatArrays() {
    return Stream.of(
            "l0 := @this: InitializeMultidimensionalArrays",
            "$stack2 = newarray (float[])[2]",
            "$stack3 = newarray (float)[2]",
            "$stack3[0] = 3.14F",
            "$stack3[1] = 5.46F",
            "$stack2[0] = $stack3",
            "$stack4 = newarray (float)[2]",
            "$stack4[0] = 2.987F",
            "$stack4[1] = 4.87F",
            "$stack2[1] = $stack4",
            "l1 = $stack2",
            "return")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   *     public void doubleArrays(){
   *         double[][] anArrayOfDoubles = {{6.765414d, 9.676565646d},{45.345435d},{3.5656d,68.234234d}};
   *
   *
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsDoubleArrays() {
    return Stream.of(
            "l0 := @this: InitializeMultidimensionalArrays",
            "$stack2 = newarray (double[])[3]",
            "$stack3 = newarray (double)[2]",
            "$stack3[0] = 6.765414",
            "$stack3[1] = 9.676565646",
            "$stack2[0] = $stack3",
            "$stack4 = newarray (double)[1]",
            "$stack4[0] = 45.345435",
            "$stack2[1] = $stack4",
            "$stack5 = newarray (double)[2]",
            "$stack5[0] = 3.5656",
            "$stack5[1] = 68.234234",
            "$stack2[2] = $stack5",
            "l1 = $stack2",
            "return")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   * public void booleanArrays(){
   *         boolean[][] anArrayOfBooleans = {{true, false},{true}};
   *
   *     }</pre>
   */
  public List<String> expectedBodyStmtsBooleanArrays() {
    return Stream.of(
            "l0 := @this: InitializeMultidimensionalArrays",
            "$stack2 = newarray (boolean[])[2]",
            "$stack3 = newarray (boolean)[2]",
            "$stack3[0] = 1",
            "$stack3[1] = 0",
            "$stack2[0] = $stack3",
            "$stack4 = newarray (boolean)[1]",
            "$stack4[0] = 1",
            "$stack2[1] = $stack4",
            "l1 = $stack2",
            "return")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   *     public void charArrays(){
   *         char[][] anArrayOfChars = {{'A', 'b', '&'},{'c','$'},{'2','G'}};
   *
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsCharArrays() {
    return Stream.of(
            "l0 := @this: InitializeMultidimensionalArrays",
            "$stack2 = newarray (char[])[3]",
            "$stack3 = newarray (char)[3]",
            "$stack3[0] = 65",
            "$stack3[1] = 98",
            "$stack3[2] = 38",
            "$stack2[0] = $stack3",
            "$stack4 = newarray (char)[2]",
            "$stack4[0] = 99",
            "$stack4[1] = 36",
            "$stack2[1] = $stack4",
            "$stack5 = newarray (char)[2]",
            "$stack5[0] = 50",
            "$stack5[1] = 71",
            "$stack2[2] = $stack5",
            "l1 = $stack2",
            "return")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   *     public void stringArrays(){
   *         String[][] anArrayOfStrings = {{"Hello World"}, {"Greetings", "Welcome"}};
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsStringArrays() {
    return Stream.of(
            "l0 := @this: InitializeMultidimensionalArrays",
            "$stack2 = newarray (java.lang.String[])[2]",
            "$stack3 = newarray (java.lang.String)[1]",
            "$stack3[0] = \"Hello World\"",
            "$stack2[0] = $stack3",
            "$stack4 = newarray (java.lang.String)[2]",
            "$stack4[0] = \"Greetings\"",
            "$stack4[1] = \"Welcome\"",
            "$stack2[1] = $stack4",
            "l1 = $stack2",
            "return")
        .collect(Collectors.toList());
  }
}
