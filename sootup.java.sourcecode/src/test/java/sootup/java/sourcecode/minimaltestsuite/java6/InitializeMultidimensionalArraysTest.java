package sootup.java.sourcecode.minimaltestsuite.java6;

import categories.Java8Test;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.sourcecode.minimaltestsuite.MinimalSourceTestSuiteBase;

/**
 * @author Hasitha Rajapakse
 * @author Kaustubh Kelkar
 */
@Category(Java8Test.class)
public class InitializeMultidimensionalArraysTest extends MinimalSourceTestSuiteBase {

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
            "r0 := @this: InitializeMultidimensionalArrays",
            "$r1 = newarray (int[])[3]",
            "$r2 = newarray (int)[3]",
            "$r2[0] = 1",
            "$r2[1] = 2",
            "$r2[2] = 3",
            "$r1[0] = $r2",
            "$r3 = newarray (int)[2]",
            "$r3[0] = 5",
            "$r3[1] = 6",
            "$r1[1] = $r3",
            "$r4 = newarray (int)[3]",
            "$r4[0] = 7",
            "$r4[1] = 8",
            "$r4[2] = 9",
            "$r1[2] = $r4",
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
            "r0 := @this: InitializeMultidimensionalArrays",
            "$r1 = newarray (byte[])[2]",
            "$r2 = newarray (byte)[2]",
            "$r2[0] = 4",
            "$r2[1] = 5",
            "$r1[0] = $r2",
            "$r3 = newarray (byte)[1]",
            "$r3[0] = 2",
            "$r1[1] = $r3",
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
            "r0 := @this: InitializeMultidimensionalArrays",
            "$r1 = newarray (short[])[2]",
            "$r2 = newarray (short)[3]",
            "$r2[0] = 10",
            "$r2[1] = 20",
            "$r2[2] = 30",
            "$r1[0] = $r2",
            "$r3 = newarray (short)[1]",
            "$r3[0] = 40",
            "$r1[1] = $r3",
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
            "r0 := @this: InitializeMultidimensionalArrays",
            "$r1 = newarray (long[])[3]",
            "$r2 = newarray (long)[2]",
            "$r2[0] = 547087L",
            "$r2[1] = 654786L",
            "$r1[0] = $r2",
            "$r3 = newarray (long)[3]",
            "$r3[0] = 547287L",
            "$r3[1] = 864645L",
            "$r3[2] = 6533786L",
            "$r1[1] = $r3",
            "$r4 = newarray (long)[2]",
            "$r4[0] = 34565L",
            "$r4[1] = 234L",
            "$r1[2] = $r4",
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
            "r0 := @this: InitializeMultidimensionalArrays",
            "$r1 = newarray (float[])[2]",
            "$r2 = newarray (float)[2]",
            "$r2[0] = 3.14F",
            "$r2[1] = 5.46F",
            "$r1[0] = $r2",
            "$r3 = newarray (float)[2]",
            "$r3[0] = 2.987F",
            "$r3[1] = 4.87F",
            "$r1[1] = $r3",
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
            "r0 := @this: InitializeMultidimensionalArrays",
            "$r1 = newarray (double[])[3]",
            "$r2 = newarray (double)[2]",
            "$r2[0] = 6.765414",
            "$r2[1] = 9.676565646",
            "$r1[0] = $r2",
            "$r3 = newarray (double)[1]",
            "$r3[0] = 45.345435",
            "$r1[1] = $r3",
            "$r4 = newarray (double)[2]",
            "$r4[0] = 3.5656",
            "$r4[1] = 68.234234",
            "$r1[2] = $r4",
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
            "r0 := @this: InitializeMultidimensionalArrays",
            "$r1 = newarray (boolean[])[2]",
            "$r2 = newarray (boolean)[2]",
            "$r2[0] = 1",
            "$r2[1] = 0",
            "$r1[0] = $r2",
            "$r3 = newarray (boolean)[1]",
            "$r3[0] = 1",
            "$r1[1] = $r3",
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
            "r0 := @this: InitializeMultidimensionalArrays",
            "$r1 = newarray (char[])[3]",
            "$r2 = newarray (char)[3]",
            "$r2[0] = 65",
            "$r2[1] = 98",
            "$r2[2] = 38",
            "$r1[0] = $r2",
            "$r3 = newarray (char)[2]",
            "$r3[0] = 99",
            "$r3[1] = 36",
            "$r1[1] = $r3",
            "$r4 = newarray (char)[2]",
            "$r4[0] = 50",
            "$r4[1] = 71",
            "$r1[2] = $r4",
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
            "r0 := @this: InitializeMultidimensionalArrays",
            "$r1 = newarray (java.lang.String[])[2]",
            "$r2 = newarray (java.lang.String)[1]",
            "$r2[0] = \"Hello World\"",
            "$r1[0] = $r2",
            "$r3 = newarray (java.lang.String)[2]",
            "$r3[0] = \"Greetings\"",
            "$r3[1] = \"Welcome\"",
            "$r1[1] = $r3",
            "return")
        .collect(Collectors.toList());
  }
}
