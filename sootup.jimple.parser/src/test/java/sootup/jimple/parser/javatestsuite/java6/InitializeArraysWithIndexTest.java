package sootup.jimple.parser.javatestsuite.java6;

import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.jimple.parser.categories.Java8Test;
import sootup.jimple.parser.javatestsuite.JimpleTestSuiteBase;

/** @author Kaustubh Kelkar */
@Category(Java8Test.class)
public class InitializeArraysWithIndexTest extends JimpleTestSuiteBase {

  @Test
  public void test() {

    SootMethod method = loadMethod(getMethodSignature("intArrays"));
    assertJimpleStmts(
        method,
        Stream.of(
                "l0 := @this: InitializeArraysWithIndex",
                "l1 = newarray (int)[3]",
                "l1[0] = 1",
                "l1[1] = 2",
                "l1[2] = 3",
                "return")
            .collect(Collectors.toList()));

    method = loadMethod(getMethodSignature("byteArrays"));
    assertJimpleStmts(
        method,
        Stream.of(
                "l0 := @this: InitializeArraysWithIndex",
                "l1 = newarray (byte)[3]",
                "l1[0] = 4",
                "l1[1] = 5",
                "l1[2] = 6",
                "return")
            .collect(Collectors.toList()));

    method = loadMethod(getMethodSignature("shortArrays"));
    assertJimpleStmts(
        method,
        Stream.of(
                "l0 := @this: InitializeArraysWithIndex",
                "l1 = newarray (short)[3]",
                "l1[0] = 10",
                "l1[1] = 20",
                "l1[2] = 30",
                "return")
            .collect(Collectors.toList()));

    method = loadMethod(getMethodSignature("longArrays"));
    assertJimpleStmts(
        method,
        Stream.of(
                "l0 := @this: InitializeArraysWithIndex",
                "l1 = newarray (long)[3]",
                "l1[0] = 547087L",
                "l1[1] = 564645L",
                "l1[2] = 654786L",
                "return")
            .collect(Collectors.toList()));

    method = loadMethod(getMethodSignature("floatArrays"));

    assertJimpleStmts(
        method,
        Stream.of(
                "l0 := @this: InitializeArraysWithIndex",
                "l1 = newarray (float)[4]",
                "l1[0] = 3.14F",
                "l1[1] = 5.46F",
                "l1[2] = 2.987F",
                "l1[3] = 4.87F",
                "return")
            .collect(Collectors.toList()));

    method = loadMethod(getMethodSignature("doubleArrays"));
    assertJimpleStmts(
        method,
        Stream.of(
                "l0 := @this: InitializeArraysWithIndex",
                "l1 = newarray (double)[2]",
                "l1[0] = 6.765414",
                "l1[1] = 9.676565646",
                "return")
            .collect(Collectors.toList()));

    method = loadMethod(getMethodSignature("booleanArrays"));
    assertJimpleStmts(
        method,
        Stream.of(
                "l0 := @this: InitializeArraysWithIndex",
                "l1 = newarray (boolean)[2]",
                "l1[0] = 1",
                "l1[1] = 0",
                "return")
            .collect(Collectors.toList()));

    method = loadMethod(getMethodSignature("charArrays"));
    assertJimpleStmts(
        method,
        Stream.of(
                "l0 := @this: InitializeArraysWithIndex",
                "l1 = newarray (char)[3]",
                "l1[0] = 65",
                "l1[1] = 98",
                "l1[2] = 38",
                "return")
            .collect(Collectors.toList()));

    method = loadMethod(getMethodSignature("stringArrays"));
    assertJimpleStmts(
        method,
        Stream.of(
                "l0 := @this: InitializeArraysWithIndex",
                "l1 = newarray (java.lang.String)[2]",
                "l1[0] = \"Hello World\"",
                "l1[1] = \"Greetings\"",
                "return")
            .collect(Collectors.toList()));
  }

  public MethodSignature getMethodSignature(String methodName) {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), methodName, "void", Collections.emptyList());
  }
}
