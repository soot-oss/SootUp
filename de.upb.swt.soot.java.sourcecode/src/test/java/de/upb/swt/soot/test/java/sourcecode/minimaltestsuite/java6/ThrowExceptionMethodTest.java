package de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.java6;

import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.MinimalTestSuiteBase;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** @author Kaustubh Kelkar */
public class ThrowExceptionMethodTest extends MinimalTestSuiteBase {

  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "divideByZero", getDeclaredClassSignature(), "void", Collections.emptyList());
  } // How to check for exceptions in method signature

  @Override
  public void defaultTest() {
    super.defaultTest();
    loadMethod(expectedBodyStmts1(), getMethodSignature1());
    loadMethod(expectedBodyStmts2(), getMethodSignature2());
    /** TODO Checking methods' signatures for exceptions */
  }

  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of("r0 := @this: ThrowExceptionMethod", "$i0 = 8 / 0", "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }

  public MethodSignature getMethodSignature1() {
    return identifierFactory.getMethodSignature(
        "divideThrowsException", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  public List<String> expectedBodyStmts1() {
    return Stream.of("r0 := @this: ThrowExceptionMethod", "$i0 = 8 / 0", "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }

  public MethodSignature getMethodSignature2() {
    return identifierFactory.getMethodSignature(
        "divideThrowException", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  public List<String> expectedBodyStmts2() {
    return Stream.of(
            "r0 := @this: ThrowExceptionMethod",
            "$r1 = new java.lang.ArithmeticException",
            "specialinvoke $r1.<java.lang.ArithmeticException: void <init>()>()",
            "throw $r1",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
