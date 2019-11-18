package de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.java6;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.MinimalTestSuiteBase;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;

/** @author Kaustubh Kelkar */
public class MethodOverloadingTest extends MinimalTestSuiteBase {
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "calculate", getDeclaredClassSignature(), "int", Arrays.asList("int", "int"));
  }
  /** @returns the method signature needed for second method in testCase */
  public MethodSignature getMethodSignatureSingleParam() {
    return identifierFactory.getMethodSignature(
        "calculate", getDeclaredClassSignature(), "int", Collections.singletonList("int"));
  }

  public MethodSignature getMethodSignatureInit() {
    return identifierFactory.getMethodSignature(
        "<init>", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  @Test
  @Override
  public void defaultTest() {
    loadMethod(expectedBodyStmts(), getMethodSignature());
    loadMethod(expectedBodyStmts1(), getMethodSignatureSingleParam());

    SootClass sootClass = loadClass(getDeclaredClassSignature());
    assertTrue(sootClass.getMethod(getMethodSignature()).isPresent());
    assertTrue(sootClass.getMethod(getMethodSignatureSingleParam()).isPresent());
    assertTrue(sootClass.getMethod(getMethodSignatureInit()).isPresent());
    assertEquals(3, sootClass.getMethods().size());
  }

  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: MethodOverloading",
            "a := @parameter0: int",
            "b := @parameter1: int",
            "$i0 = a + b",
            "return $i0")
        .collect(Collectors.toCollection(ArrayList::new));
  }

  public List<String> expectedBodyStmts1() {
    return Stream.of(
            "r0 := @this: MethodOverloading", "a := @parameter0: int", "$i0 = a + a", "return $i0")
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
