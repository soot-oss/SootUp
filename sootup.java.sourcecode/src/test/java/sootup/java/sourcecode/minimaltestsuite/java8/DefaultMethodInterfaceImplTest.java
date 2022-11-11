package sootup.java.sourcecode.minimaltestsuite.java8;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Ignore;
import sootup.core.model.SootClass;
import sootup.core.signatures.MethodSignature;
import sootup.java.sourcecode.minimaltestsuite.MinimalSourceTestSuiteBase;

/** @author Kaustubh Kelkar */
public class DefaultMethodInterfaceImplTest extends MinimalSourceTestSuiteBase {

  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "interfaceMethod", "void", Collections.emptyList());
  }

  public MethodSignature getDefaultMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "defaultInterfaceMethod", "void", Collections.emptyList());
  }

  /**
   *
   *
   * <pre>
   *
   * public void interfaceMethod(){
   * System.out.println("Method interfaceMethod() is implemented");
   * }
   *
   * </pre>
   */
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: DefaultMethodInterfaceImpl",
            "$r1 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r1.<java.io.PrintStream: void println(java.lang.String)>(\"Method interfaceMethod() is implemented\")",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }

  /**
   *
   *
   * <pre>
   * public void defaultInterfaceMethod(){
   *
   * //Add this line after default methods are supported
   *
   * //DefaultMethodInterface.super.defaultInterfaceMethod();
   *
   * System.out.println("Method defaultInterfaceMethod() is implemented");
   * };
   * </pre>
   */
  public List<String> expectedBodyStmts1() {
    return Stream.of(
            "r0 := @this: DefaultMethodInterfaceImpl",
            "$r1 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r1.<java.io.PrintStream: void println(java.lang.String)>(\"Method defaultInterfaceMethod() is implemented\")",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }

  /** TODO Update the source code once default methods in WALA are supported */
  @Ignore
  public void test() {

    assertJimpleStmts(loadMethod(getMethodSignature()), expectedBodyStmts());
    assertJimpleStmts(loadMethod(getDefaultMethodSignature()), expectedBodyStmts1());

    SootClass<?> clazz = loadClass(getDeclaredClassSignature());
    assertTrue(
        clazz.getInterfaces().stream()
            .anyMatch(
                javaClassType ->
                    javaClassType.getClassName().equalsIgnoreCase("DefaultMethodInterface")));
  }
}
