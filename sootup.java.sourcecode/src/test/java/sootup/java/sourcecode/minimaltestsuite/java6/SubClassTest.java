package sootup.java.sourcecode.minimaltestsuite.java6;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.sourcecode.minimaltestsuite.MinimalSourceTestSuiteBase;

/** @author Kaustubh Kelkar */
public class SubClassTest extends MinimalSourceTestSuiteBase {
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "subclassMethod", "void", Collections.emptyList());
  }

  /** @returns the method signature needed for second method in testCase */
  public MethodSignature getMethodSignature1() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "superclassMethod", "void", Collections.emptyList());
  }

  /**
   *
   *
   * <pre>
   *     public void subclassMethod() {
   *         aa=10;
   *         bb=20;
   *         cc=30;
   *         dd=40;
   *     }
   * </pre>
   */
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: SubClass",
            "r0.<SubClass: int aa> = 10",
            "r0.<SubClass: int bb> = 20",
            "r0.<SubClass: int cc> = 30",
            "r0.<SubClass: int dd> = 40",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }

  /**
   *
   *
   * <pre>
   *     public void superclassMethod(){
   *         super.superclassMethod();
   *         a=100;
   *         b=200;
   *         c=300;
   *    }
   * </pre>
   */
  public List<String> expectedBodyStmts1() {
    return Stream.of(
            "r0 := @this: SubClass",
            "specialinvoke r0.<SuperClass: void superclassMethod()>()",
            "r0.<SuperClass: int a> = 100",
            "r0.<SuperClass: int b> = 200",
            "r0.<SuperClass: int c> = 300",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
    method = loadMethod(getMethodSignature1());
    assertJimpleStmts(method, expectedBodyStmts1());
    SootClass<?> sootClass = loadClass(getDeclaredClassSignature());
    assertTrue(sootClass.getSuperclass().get().getClassName().equals("SuperClass"));
  }
}
