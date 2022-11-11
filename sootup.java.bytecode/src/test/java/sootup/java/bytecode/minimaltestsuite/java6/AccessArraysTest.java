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
public class AccessArraysTest extends MinimalBytecodeTestSuiteBase {

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
   *         int[] anArrayOfInts = {1, 2, 3};
   *         int val;
   *         for(int item: anArrayOfInts){
   *             val = item;
   *         }
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsIntArrays() {
    return Stream.of(
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
            "return")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   *     public void byteArrays(){
   *         byte[] anArrayOfBytes = {4, 5, 6};
   *         byte val;
   *         for(byte item: anArrayOfBytes){
   *             val = item;
   *         }
   *
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsByteArrays() {
    return Stream.of(
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
            "return")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   *     public void shortArrays(){
   *         short[] anArrayOfShorts = {10, 20, 30};
   *         short val;
   *         for(short item: anArrayOfShorts){
   *             val = item;
   *         }
   *
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsShortArrays() {
    return Stream.of(
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
            "return")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   *     public void longArrays(){
   *         long[] anArrayOfLongs = {547087L, 564645L, 654786L};
   *         long val;
   *         for(long item: anArrayOfLongs){
   *             val = item;
   *         }
   *
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsLongArrays() {
    return Stream.of(
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
            "return")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   *     public void floatArrays(){
   *         float[] anArrayOfFloats = {3.14f, 5.46f, 2.987f, 4.87f};
   *         float val;
   *         for(float item: anArrayOfFloats){
   *             val = item;
   *         }
   *
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsFloatArrays() {
    return Stream.of(
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
            "return")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   *     public void doubleArrays(){
   *         double[] anArrayOfDoubles = {6.765414d, 9.676565646d};
   *         double val;
   *         for(double item: anArrayOfDoubles){
   *             val = item;
   *         }
   *
   *
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsDoubleArrays() {
    return Stream.of(
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
            "return")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   *     public void booleanArrays(){
   *         boolean[] anArrayOfBooleans = {true, false};
   *         boolean val;
   *         for(boolean item: anArrayOfBooleans){
   *             val = item;
   *         }
   *
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsBooleanArrays() {
    return Stream.of(
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
            "return")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   *     public void charArrays(){
   *         char[] anArrayOfChars = {'A', 'b', '&'};
   *         char val;
   *         for(char item: anArrayOfChars){
   *             val = item;
   *         }
   *
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsCharArrays() {
    return Stream.of(
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
            "return")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   *     public void stringArrays(){
   *         String[] anArrayOfStrings = {"Hello World", "Greetings"};
   *         String val;
   *         for(String item: anArrayOfStrings){
   *             val = item;
   *         }
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsStringArrays() {
    return Stream.of(
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
            "return")
        .collect(Collectors.toList());
  }
}
