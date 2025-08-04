package sootup.java.bytecode.frontend.minimaltestsuite.java6;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.bytecode.frontend.minimaltestsuite.MinimalBytecodeTestSuiteBase;

/**
 * @author Kaustubh Kelkar
 */
public class SubClassTest extends MinimalBytecodeTestSuiteBase {
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "subclassMethod", "void", Collections.emptyList());
  }

  /**
   * @returns the method signature needed for second method in testCase
   */
  public MethodSignature getMethodSignature1() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "superclassMethod", "void", Collections.emptyList());
  }

  @Test
  public void testSuperClassStmts() {
    SootMethod m = loadMethod(getMethodSignature1());
    assertJimpleStmts(m, expectedBodyStmts1());
    SootClass sootClass = loadClass(getDeclaredClassSignature());
    assertTrue(sootClass.getSuperclass().get().getClassName().equals("SuperClass"));
  }

  /**
   *
   *
   * <pre>
   * public void subclassMethod() {
   * aa=10;
   * bb=20;
   * cc=30;
   * dd=40;
   * }
   * public void superclassMethod(){
   * super.superclassMethod();
   * a=100;
   * b=200;
   * c=300;
   * }
   *
   * </pre>
   */
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "this := @this: SubClass",
            "this.<SubClass: int aa> = 10",
            "this.<SubClass: int bb> = 20",
            "this.<SubClass: int cc> = 30",
            "this.<SubClass: int dd> = 40",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }

  public List<String> expectedBodyStmts1() {
    return Stream.of(
            "this := @this: SubClass",
            "specialinvoke this.<SuperClass: void superclassMethod()>()",
            "this.<SubClass: int a> = 100",
            "this.<SubClass: int b> = 200",
            "this.<SubClass: int c> = 300",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
  }
}
