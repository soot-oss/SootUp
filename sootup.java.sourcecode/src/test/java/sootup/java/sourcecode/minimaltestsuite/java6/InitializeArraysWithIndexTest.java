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
public class InitializeArraysWithIndexTest extends MinimalSourceTestSuiteBase {

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
   *    public void intArrays(){
   *         int[] anArrayOfInts = new int[3];
   *         anArrayOfInts[0] = 1;
   *         anArrayOfInts[1] = 2;
   *         anArrayOfInts[2] = 3;
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsIntArrays() {
    return Stream.of(
            "r0 := @this: InitializeArraysWithIndex",
            "$r1 = newarray (int)[3]",
            "$r1[0] = 1",
            "$r1[1] = 2",
            "$r1[2] = 3",
            "return")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   *     public void byteArrays(){
   *         byte[] anArrayOfBytes = new byte[3];
   *         anArrayOfBytes[0] = 4;
   *         anArrayOfBytes[1] = 5;
   *         anArrayOfBytes[2] = 6;
   *
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsByteArrays() {
    return Stream.of(
            "r0 := @this: InitializeArraysWithIndex",
            "$r1 = newarray (byte)[3]",
            "$r1[0] = 4",
            "$r1[1] = 5",
            "$r1[2] = 6",
            "return")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   * public void shortArrays(){
   *         short[] anArrayOfShorts = new short[3];
   *         anArrayOfShorts[0] = 10;
   *         anArrayOfShorts[1] = 20;
   *         anArrayOfShorts[2] = 30;
   *
   *     }</pre>
   */
  public List<String> expectedBodyStmtsShortArrays() {
    return Stream.of(
            "r0 := @this: InitializeArraysWithIndex",
            "$r1 = newarray (short)[3]",
            "$r1[0] = 10",
            "$r1[1] = 20",
            "$r1[2] = 30",
            "return")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   *     public void longArrays(){
   *         long[] anArrayOfLongs = new long[3];
   *         anArrayOfLongs[0] = 547087L;
   *         anArrayOfLongs[1] = 564645L;
   *         anArrayOfLongs[2] = 654786L;
   *
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsLongArrays() {
    return Stream.of(
            "r0 := @this: InitializeArraysWithIndex",
            "$r1 = newarray (long)[3]",
            "$r1[0] = 547087L",
            "$r1[1] = 564645L",
            "$r1[2] = 654786L",
            "return")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   *     public void floatArrays(){
   *         float[] anArrayOfFloats = new float[4];
   *         anArrayOfFloats[0] = 3.14f;
   *         anArrayOfFloats[1] = 5.46f;
   *         anArrayOfFloats[2] = 2.987f;
   *         anArrayOfFloats[3] = 4.87f;
   *
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsFloatArrays() {
    return Stream.of(
            "r0 := @this: InitializeArraysWithIndex",
            "$r1 = newarray (float)[4]",
            "$r1[0] = 3.14F",
            "$r1[1] = 5.46F",
            "$r1[2] = 2.987F",
            "$r1[3] = 4.87F",
            "return")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   *     public void doubleArrays(){
   *         double[] anArrayOfDoubles = new double[2];
   *         anArrayOfDoubles[0] = 6.765414d;
   *         anArrayOfDoubles[1] = 9.676565646d;
   *
   *
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsDoubleArrays() {
    return Stream.of(
            "r0 := @this: InitializeArraysWithIndex",
            "$r1 = newarray (double)[2]",
            "$r1[0] = 6.765414",
            "$r1[1] = 9.676565646",
            "return")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   *     public void booleanArrays(){
   *         boolean[] anArrayOfBooleans = new boolean[2];
   *         anArrayOfBooleans[0] = true;
   *         anArrayOfBooleans[1] = false;
   *
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsBooleanArrays() {
    return Stream.of(
            "r0 := @this: InitializeArraysWithIndex",
            "$r1 = newarray (boolean)[2]",
            "$r1[0] = 1",
            "$r1[1] = 0",
            "return")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   *     public void charArrays(){
   *         char[] anArrayOfChars = new char[3];
   *         anArrayOfChars[0] = 'A';
   *         anArrayOfChars[1] = 'b';
   *         anArrayOfChars[2] = '&';
   *
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsCharArrays() {
    return Stream.of(
            "r0 := @this: InitializeArraysWithIndex",
            "$r1 = newarray (char)[3]",
            "$r1[0] = 65",
            "$r1[1] = 98",
            "$r1[2] = 38",
            "return")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   *     public void stringArrays(){
   *         String[] anArrayOfStrings = new String[2];
   *         anArrayOfStrings[0] = "Hello World";
   *         anArrayOfStrings[1] = "Greetings";
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsStringArrays() {
    return Stream.of(
            "r0 := @this: InitializeArraysWithIndex",
            "$r1 = newarray (java.lang.String)[2]",
            "$r1[0] = \"Hello World\"",
            "$r1[1] = \"Greetings\"",
            "return")
        .collect(Collectors.toList());
  }
}
