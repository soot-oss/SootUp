package de.upb.swt.soot.test.java.bytecode.minimaltestsuite.java6;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.bytecode.minimaltestsuite.MinimalBytecodeTestSuiteBase;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author Kaustubh Kelkar */
@Category(Java8Test.class)
public class MethodOverloadingTest extends MinimalBytecodeTestSuiteBase {
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
    assertTrue(sootClass.getMethod(getMethodSignature()).isPresent());
    assertTrue(sootClass.getMethod(getMethodSignatureSingleParam()).isPresent());
    assertTrue(sootClass.getMethod(getMethodSignatureInit()).isPresent());
    assertEquals(3, sootClass.getMethods().size());
  }

  /**  <pre>
   * int calculate(int a, int b){
   * return a+b;
   *
   * }
   * int calculate(int a){
   * return a+a;
   *
   * }
   *
   * <pre>*/
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "l0 := @this: MethodOverloading",
            "l1 := @parameter0: int",
            "l2 := @parameter1: int",
            "$stack3 = l1 + l2",
            "return $stack3")
        .collect(Collectors.toCollection(ArrayList::new));
  }

  public List<String> expectedBodyStmts1() {
    return Stream.of(
            "l0 := @this: MethodOverloading",
            "l1 := @parameter0: int",
            "$stack2 = l1 + l1",
            "return $stack2")
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
