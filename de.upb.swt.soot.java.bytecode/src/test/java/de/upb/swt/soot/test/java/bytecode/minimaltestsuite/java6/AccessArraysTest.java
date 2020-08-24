package de.upb.swt.soot.test.java.bytecode.minimaltestsuite.java6;

import categories.Java8Test;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.bytecode.minimaltestsuite.MinimalBytecodeTestSuiteBase;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.experimental.categories.Category;

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
        methodName, getDeclaredClassSignature(), "void", Collections.emptyList());
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
   *         byte[][][] byteArray3D = {{{7, 8, 9},{10,11}},{{1, 2, 3},{5, 6}}};;
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
   *         short[][][] shortArray3D = {{{10,20},{40,85}},{{56,59},{95,35}}};
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
   *         long[][][] longArray3D = {{{547087L, 654786L},{547287L, 864645L, 6533786L}},{{34565L,234L},{9851L,63543L}}};
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
   *         float[][][] floatrray3D = {{{3.14f, 5.46f}, {2.987f, 4.87f}},{{65.15f,854.18f},{16.51f,58.14f}}};
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
   *         double[][][] doubleArray3D = {{{6.765414d, 9.676565646d},{45.345435d}},{{3.5656d,68.234234d},{68416.651d,65416.5d}}};
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
   *         boolean[][][] boolArray3D = {{{true, false},{true}},{{false,false},{true}}};
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
   *         char[][][] charArray3D = {{{'A', 'b', '&'},{'c','$'}},{{'2','G'},{'a','%'}}};
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
   *     public void stringArrays() {
   *         String[][][] stringArray3D = {{{"Hello World"}, {"Greetings", "Welcome"}}, {{"Future","Soot"},{"UPB","HNI"}}};
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
