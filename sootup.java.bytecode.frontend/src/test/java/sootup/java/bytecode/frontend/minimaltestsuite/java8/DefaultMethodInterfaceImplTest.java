package sootup.java.bytecode.frontend.minimaltestsuite.java8;

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
public class DefaultMethodInterfaceImplTest extends MinimalBytecodeTestSuiteBase {

  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "interfaceMethod", "void", Collections.emptyList());
  }

  public MethodSignature getDefaultMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "defaultInterfaceMethod", "void", Collections.emptyList());
  }

  @Test
  public void test() {
    SootMethod method1 = loadMethod(getMethodSignature());
    assertJimpleStmts(method1, expectedBodyStmts());
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
    method = loadMethod(getDefaultMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts1());
    SootClass clazz = loadClass(getDeclaredClassSignature());
    assertTrue(
        clazz.getInterfaces().stream()
            .anyMatch(
                javaClassType -> {
                  return javaClassType.getClassName().equalsIgnoreCase("DefaultMethodInterface");
                }));
  }

  /**
   *
   *
   * <pre>
   * public void interfaceMethod(){
   * System.out.println("Method interfaceMethod() is implemented");
   * }
   * public void defaultInterfaceMethod(){
   *
   * //Add this line after default methods are supported
   *
   * //DefaultMethodInterface.super.defaultInterfaceMethod();
   *
   * System.out.println("Method defaultInterfaceMethod() is implemented");
   * };
   *
   * </pre>
   */
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "this := @this: DefaultMethodInterfaceImpl",
            "$stack1 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $stack1.<java.io.PrintStream: void println(java.lang.String)>(\"Method interfaceMethod() is implemented\")",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }

  public List<String> expectedBodyStmts1() {
    return Stream.of(
            "this := @this: DefaultMethodInterfaceImpl",
            "specialinvoke this.<DefaultMethodInterface: void defaultInterfaceMethod()>()",
            "$stack1 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $stack1.<java.io.PrintStream: void println(java.lang.String)>(\"Method defaultInterfaceMethod() is implemented\")",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
