package de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.java6;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.MinimalSourceTestSuiteBase;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;

/** @author Kaustubh Kelkar */
public class MethodOverloadingTest extends MinimalSourceTestSuiteBase {
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
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());

    method = loadMethod(getMethodSignatureSingleParam());
    assertJimpleStmts(method, expectedBodyStmts1());

    SootClass sootClass = loadClass(getDeclaredClassSignature());
    assertTrue(sootClass.getMethod(getMethodSignature().getSubSignature()).isPresent());
    assertTrue(sootClass.getMethod(getMethodSignatureSingleParam().getSubSignature()).isPresent());
    assertTrue(sootClass.getMethod(getMethodSignatureInit().getSubSignature()).isPresent());
    assertEquals(3, sootClass.getMethods().size());
  }

  /**
   *
   *
   * <pre>
   *     int calculate(int a, int b){
   *         return a+b;
   *
   *     }
   * </pre>
   */
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: MethodOverloading",
            "$i0 := @parameter0: int",
            "$i1 := @parameter1: int",
            "$i2 = $i0 + $i1",
            "return $i2")
        .collect(Collectors.toCollection(ArrayList::new));
  }

  /**
   *
   *
   * <pre>
   * int calculate(int a){
   *         return a+a;
   *
   *     }
   *     </pre>
   */
  public List<String> expectedBodyStmts1() {
    return Stream.of(
            "r0 := @this: MethodOverloading",
            "$i0 := @parameter0: int",
            "$i1 = $i0 + $i0",
            "return $i1")
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
