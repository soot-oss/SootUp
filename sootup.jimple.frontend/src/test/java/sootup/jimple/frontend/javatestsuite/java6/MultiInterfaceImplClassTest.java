package sootup.jimple.frontend.javatestsuite.java6;

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
import sootup.jimple.frontend.javatestsuite.JimpleTestSuiteBase;

/**
 * @author Kaustubh Kelkar
 */
public class MultiInterfaceImplClassTest extends JimpleTestSuiteBase {
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

  public List<String> expectedBodyStmts() {
    return Stream.of(
            "l0 := @this: MultiInterfaceImplClass",
            "$stack1 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $stack1.<java.io.PrintStream: void print(java.lang.String)>(\"Method from InterfaceImpl is implemented\")",
            "$stack2 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $stack2.<java.io.PrintStream: void println(java.lang.String)>(\"Variable from InterfaceImpl is 10\")",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
