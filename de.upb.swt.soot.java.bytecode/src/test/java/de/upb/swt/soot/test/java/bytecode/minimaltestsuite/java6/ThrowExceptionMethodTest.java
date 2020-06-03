package de.upb.swt.soot.test.java.bytecode.minimaltestsuite.java6;

import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.bytecode.minimaltestsuite.MinimalBytecodeTestSuiteBase;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Ignore;
import org.junit.experimental.categories.Category;

/** @author Kaustubh Kelkar */
@Category(Java8Test.class)
public class ThrowExceptionMethodTest extends MinimalBytecodeTestSuiteBase {

  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "divideByZero", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of("l0 := @this: ThrowExceptionMethod", "l1 = 8 / 0", "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }

  public MethodSignature getMethodSignature1() {
    return identifierFactory.getMethodSignature(
        "throwCustomException", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  public List<String> expectedBodyStmts1() {
    return Stream.of(
            "l0 := @this: ThrowExceptionMethod",
            "label1:",
            "$stack2 = new CustomException",
            "specialinvoke $stack2.<CustomException: void <init>(java.lang.String)>(\"Custom Exception\")",
            "throw $stack2",
            "label2:",
            "$stack3 := @caughtexception",
            "l1 = $stack3",
            "$stack4 = <java.lang.System: java.io.PrintStream out>",
            "$stack5 = virtualinvoke l1.<CustomException: java.lang.String getMessage()>()",
            "virtualinvoke $stack4.<java.io.PrintStream: void println(java.lang.String)>($stack5)",
            "return",
            "catch CustomException from label1 to label2 with label2")
        .collect(Collectors.toCollection(ArrayList::new));
  }

  @org.junit.Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
    method = loadMethod(getMethodSignature1());
    assertJimpleStmts(method, expectedBodyStmts1());
    System.out.println(method.getBody().getStmts().toString());
    assertTrue(
        method.getExceptionSignatures().stream()
            .anyMatch(classType -> classType.getClassName().equals("CustomException")));
  }

  @Ignore
  public void IgnoreTEst() {
    SootMethod method = loadMethod(getMethodSignature());
    method = loadMethod(getMethodSignature1());
    assertJimpleStmts(method, expectedBodyStmts1());
    assertTrue(
        method.getExceptionSignatures().stream()
            .anyMatch(classType -> classType.getClassName().equals("CustomException")));
  }
}
