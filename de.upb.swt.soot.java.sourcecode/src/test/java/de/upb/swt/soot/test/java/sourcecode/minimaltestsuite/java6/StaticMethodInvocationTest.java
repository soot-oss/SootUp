package de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.java6;

import categories.Java8Test;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.MinimalSourceTestSuiteBase;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author: Hasitha Rajapakse * */
@Category(Java8Test.class)
public class StaticMethodInvocationTest extends MinimalSourceTestSuiteBase {
  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "staticMethodInvocation", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  /**  <pre>    static void staticMethod(){
   * System.out.println("static method");
   * }
   * <pre>*/
  /**  <pre>    public static void staticmethod(){
   * String str = "Hello World";
   * }
   * public void staticMethodInvocation(){
   * StaticMethodInvocation.staticmethod();
   * }
   * <pre>*/
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: StaticMethodInvocation",
            "staticinvoke <StaticMethodInvocation: void staticmethod()>()",
            "return")
        .collect(Collectors.toList());
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
  }
}
