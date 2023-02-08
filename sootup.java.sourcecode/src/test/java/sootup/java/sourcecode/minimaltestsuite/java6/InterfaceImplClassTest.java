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
public class InterfaceImplClassTest extends MinimalSourceTestSuiteBase {
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "interfaceMethod", "void", Collections.emptyList());
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
    SootClass<?> clazz = loadClass(getDeclaredClassSignature());
    assertTrue(
        clazz.getInterfaces().stream()
            .anyMatch(
                javaClassType -> {
                  return javaClassType.getClassName().equalsIgnoreCase("InterfaceImpl");
                }));
  }

  /**
   *
   *
   * <pre>
   *     public void interfaceMethod(){
   * System.out.print("Method from InterfaceImpl is implemented");
   * System.out.println("Variable from InterfaceImpl is "+a);
   * };
   * </pre>
   */
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: InterfaceImplClass",
            "$r1 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r1.<java.io.PrintStream: void print(java.lang.String)>(\"Method from InterfaceImpl is implemented\")",
            "$r2 = <java.lang.System: java.io.PrintStream out>",
            "$r4 = new java.lang.StringBuilder",
            "specialinvoke $r4.<java.lang.StringBuilder: void <init>(java.lang.String)>(\"Variable from InterfaceImpl is \")",
            "$r5 = virtualinvoke $r4.<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>(10)",
            "$r3 = virtualinvoke $r5.<java.lang.StringBuilder: java.lang.StringBuilder toString()>()",
            "virtualinvoke $r2.<java.io.PrintStream: void println(java.lang.String)>($r3)",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
