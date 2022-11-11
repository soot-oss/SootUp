package sootup.java.sourcecode.minimaltestsuite.java8;

import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Ignore;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.sourcecode.minimaltestsuite.MinimalSourceTestSuiteBase;

public class StaticMethodInterfaceImplTest extends MinimalSourceTestSuiteBase {

  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(),
        "methodStaticMethodInterfaceImpl",
        "void",
        Collections.emptyList());
  }

  private MethodSignature getStaticMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "initStatic", "void", Collections.emptyList());
  }

  /**
   *
   *
   * <pre>
   * public void display(){
   * System.out.println("Inside display - StaticmethodInterfaceImpl");
   * }
   * </pre>
   */
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: StaticMethodInterfaceImpl",
            "$r1 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r1.<java.io.PrintStream: void println(java.lang.String)>(\"Inside display - StaticmethodInterfaceImpl\")",
            "return")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   *     static public void initStatic(){
   * System.out.println("Inside initStatic - StaticmethodInterface");
   * }
   * </pre>
   */
  public List<String> expectedBodyStmts1() {
    return Stream.of(
            "$r0 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r0.<java.io.PrintStream: void println(java.lang.String)>(\"Inside initStatic - StaticmethodInterfaceImpl\")",
            "return")
        .collect(Collectors.toList());
  }

  // TODO: enable test when TypeMethodReference is Supported by Wala/SourceCodeFrontend
  @Ignore
  public void test() {
    assertJimpleStmts(loadMethod(getStaticMethodSignature()), expectedBodyStmts1());

    SootMethod staticMethod = loadMethod(getStaticMethodSignature());
    assertJimpleStmts(staticMethod, expectedBodyStmts1());
    assertTrue(staticMethod.isStatic() && staticMethod.getName().equals("initStatic"));

    SootClass<?> sootClass = loadClass(getDeclaredClassSignature());
    assertTrue(
        sootClass.getInterfaces().stream()
            .anyMatch(
                javaClassType -> javaClassType.getClassName().equals("StaticMethodInterface")));
  }
}
