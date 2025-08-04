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
public class MultiInterfaceImplClassTest extends MinimalBytecodeTestSuiteBase {
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "interfaceMethod", "void", Collections.emptyList());
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
    SootClass clazz = loadClass(getDeclaredClassSignature());
    assertTrue(
        clazz.getInterfaces().stream()
            .anyMatch(
                javaClassType -> {
                  return javaClassType.getClassName().equals("InterfaceImplDummy");
                }));
  }

  /**
   *
   *
   * <pre>
   * public void interfaceMethod(){
   * System.out.print("Method from InterfaceImpl is implemented");
   * System.out.println("Variable from InterfaceImpl is "+a);
   * };
   *
   * </pre>
   */
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "this := @this: MultiInterfaceImplClass",
            "$stack1 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $stack1.<java.io.PrintStream: void print(java.lang.String)>(\"Method from InterfaceImpl is implemented\")",
            "$stack2 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $stack2.<java.io.PrintStream: void println(java.lang.String)>(\"Variable from InterfaceImpl is 10\")",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
