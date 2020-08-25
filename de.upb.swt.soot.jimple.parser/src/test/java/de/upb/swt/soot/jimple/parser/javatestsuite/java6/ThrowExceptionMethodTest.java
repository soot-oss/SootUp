package de.upb.swt.soot.jimple.parser.javatestsuite.java6;

import static org.junit.Assert.assertTrue;

import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.jimple.parser.javatestsuite.JimpleTestSuiteBase;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Ignore;

/** @author Kaustubh Kelkar */
public class ThrowExceptionMethodTest extends JimpleTestSuiteBase {

  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "divideByZero", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  public MethodSignature getThrowCustomExceptionSignature() {
    return identifierFactory.getMethodSignature(
        "throwCustomException", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  @org.junit.Test
  public void test() {
    SootMethod method1 = loadMethod(getMethodSignature());
    assertJimpleStmts(method1, expectedBodyStmts());
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
    assertTrue(
        method.getExceptionSignatures().stream()
            .anyMatch(classType -> classType.getClassName().equals("ArithmeticException")));
    /** TODO can not detect the custom exception a */
  }

  @Ignore
  public void ignoreTest() {
    SootMethod method = loadMethod(getThrowCustomExceptionSignature());
    assertJimpleStmts(method, expectedBodyStmts1());
    assertTrue(
        method.getExceptionSignatures().stream()
            .anyMatch(classType -> classType.getClassName().equals("CustomException")));
  }

  public List<String> expectedBodyStmts() {
    return Stream.of("r0 := @this: ThrowExceptionMethod", "$i0 = 8 / 0", "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }

  public MethodSignature getMethodSignature1() {
    return identifierFactory.getMethodSignature(
        "throwCustomException", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  public List<String> expectedBodyStmts1() {
    return Stream.of(
            "r0 := @this: ThrowExceptionMethod",
            "$r1 = new CustomException",
            "specialinvoke $r1.<CustomException: void <init>()>()",
            "throw $r1",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
