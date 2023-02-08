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
public class InitializeArraysWhileDeclarationTest extends MinimalBytecodeTestSuiteBase {

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
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsIntArrays() {
    return Stream.of(
            "l0 := @this: InitializeArraysWhileDeclaration",
            "$stack2 = newarray (int)[3]",
            "$stack2[0] = 1",
            "$stack2[1] = 2",
            "$stack2[2] = 3",
            "l1 = $stack2",
            "return")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   *     public void byteArrays(){
   *         byte[] anArrayOfBytes = {4, 5, 6};
   *
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsByteArrays() {
    return Stream.of(
            "l0 := @this: InitializeArraysWhileDeclaration",
            "$stack2 = newarray (byte)[3]",
            "$stack2[0] = 4",
            "$stack2[1] = 5",
            "$stack2[2] = 6",
            "l1 = $stack2",
            "return")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   *     public void shortArrays(){
   *         short[] anArrayOfShorts = {10, 20, 30};
   *
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsShortArrays() {
    return Stream.of(
            "l0 := @this: InitializeArraysWhileDeclaration",
            "$stack2 = newarray (short)[3]",
            "$stack2[0] = 10",
            "$stack2[1] = 20",
            "$stack2[2] = 30",
            "l1 = $stack2",
            "return")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   *     public void longArrays(){
   *         long[] anArrayOfLongs = {547087L, 564645L, 654786L};
   *
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsLongArrays() {
    return Stream.of(
            "l0 := @this: InitializeArraysWhileDeclaration",
            "$stack2 = newarray (long)[3]",
            "$stack2[0] = 547087L",
            "$stack2[1] = 564645L",
            "$stack2[2] = 654786L",
            "l1 = $stack2",
            "return")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   *     public void floatArrays(){
   *         float[] anArrayOfFloats = {3.14f, 5.46f, 2.987f, 4.87f};
   *
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsFloatArrays() {
    return Stream.of(
            "l0 := @this: InitializeArraysWhileDeclaration",
            "$stack2 = newarray (float)[4]",
            "$stack2[0] = 3.14F",
            "$stack2[1] = 5.46F",
            "$stack2[2] = 2.987F",
            "$stack2[3] = 4.87F",
            "l1 = $stack2",
            "return")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   *     public void doubleArrays(){
   *         double[] anArrayOfDoubles = {6.765414d, 9.676565646d};
   *
   *
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsDoubleArrays() {
    return Stream.of(
            "l0 := @this: InitializeArraysWhileDeclaration",
            "$stack2 = newarray (double)[2]",
            "$stack2[0] = 6.765414",
            "$stack2[1] = 9.676565646",
            "l1 = $stack2",
            "return")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   *     public void booleanArrays(){
   *         boolean[] anArrayOfBooleans = {true, false};
   *
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsBooleanArrays() {
    return Stream.of(
            "l0 := @this: InitializeArraysWhileDeclaration",
            "$stack2 = newarray (boolean)[2]",
            "$stack2[0] = 1",
            "$stack2[1] = 0",
            "l1 = $stack2",
            "return")
        .collect(Collectors.toList());
  }

  /**
   * public void charArrays(){ char[] anArrayOfChars = {'A', 'b', '&'};
   *
   * <p>}
   */
  public List<String> expectedBodyStmtsCharArrays() {
    return Stream.of(
            "l0 := @this: InitializeArraysWhileDeclaration",
            "$stack2 = newarray (char)[3]",
            "$stack2[0] = 65",
            "$stack2[1] = 98",
            "$stack2[2] = 38",
            "l1 = $stack2",
            "return")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   *     public void stringArrays(){
   *         String[] anArrayOfStrings = {"Hello World", "Greetings"};
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsStringArrays() {
    return Stream.of(
            "l0 := @this: InitializeArraysWhileDeclaration",
            "$stack2 = newarray (java.lang.String)[2]",
            "$stack2[0] = \"Hello World\"",
            "$stack2[1] = \"Greetings\"",
            "l1 = $stack2",
            "return")
        .collect(Collectors.toList());
  }
}
