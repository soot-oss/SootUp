package sootup.java.bytecode.minimaltestsuite.java8;

import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.bytecode.minimaltestsuite.MinimalBytecodeTestSuiteBase;

/** @author Kaustubh Kelkar */
@Category(Java8Test.class)
public class StaticMethodInterfaceImplTest extends MinimalBytecodeTestSuiteBase {

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

  @Test
  public void test() {
    SootMethod method = loadMethod(getStaticMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts1());
    SootMethod staticMethod = loadMethod(getStaticMethodSignature());
    assertJimpleStmts(staticMethod, expectedBodyStmts1());
    assertTrue(staticMethod.isStatic() && staticMethod.getName().equals("initStatic"));
    SootClass<?> sootClass = loadClass(getDeclaredClassSignature());
    assertTrue(
        sootClass.getInterfaces().stream()
            .anyMatch(
                javaClassType -> {
                  return javaClassType.getClassName().equals("StaticMethodInterface");
                }));
  }

  /**
   *
   *
   * <pre>
   *
   * </pre>
   */
  /**
   *
   *
   * <pre>
   * static void staticMethod(){
   * System.out.println("static method");
   * }
   *
   * </pre>
   */
  /**
   *
   *
   * <pre>
   * static public void initStatic(){
   * System.out.println("Inside initStatic - StaticmethodInterface");
   * }
   *
   * </pre>
   */
  /**
   *
   *
   * <pre>
   * static public void initStatic(){
   * System.out.println("Inside initStatic - StaticmethodInterfaceImpl");
   * }
   * public void display(){
   * System.out.println("Inside display - StaticmethodInterfaceImpl");
   * }
   *
   * </pre>
   */
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "$stack0 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $stack0.<java.io.PrintStream: void println(java.lang.String)>(\"Inside initStatic - StaticmethodInterfaceImpl\")",
            "return")
        .collect(Collectors.toList());
  }

  public List<String> expectedBodyStmts1() {
    return Stream.of(
            "$stack0 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $stack0.<java.io.PrintStream: void println(java.lang.String)>(\"Inside initStatic - StaticmethodInterfaceImpl\")",
            "return")
        .collect(Collectors.toList());
  }
}
