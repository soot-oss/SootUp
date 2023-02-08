package sootup.java.bytecode.minimaltestsuite.java6;

import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.bytecode.minimaltestsuite.MinimalBytecodeTestSuiteBase;

/** @author Kaustubh Kelkar */
@Category(Java8Test.class)
public class StaticMethodTest extends MinimalBytecodeTestSuiteBase {

  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "staticMethod", "void", Collections.emptyList());
  }

  @Test
  public void test() {
    SootMethod method1 = loadMethod(getMethodSignature());
    assertJimpleStmts(method1, expectedBodyStmts());
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
    assertTrue(method.isStatic());
  }

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
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "$stack0 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $stack0.<java.io.PrintStream: void println(java.lang.String)>(\"static method\")",
            "return")
        .collect(Collectors.toList());
  }
}
