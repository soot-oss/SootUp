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
public class StaticVariableTest extends MinimalSourceTestSuiteBase {

  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "staticVariable", "void", Collections.emptyList());
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
    SootClass<?> clazz = loadClass(getDeclaredClassSignature());
    assertTrue(
        clazz.getFields().stream()
            .anyMatch(
                element -> {
                  return element.getName().equals("num") && element.isStatic();
                }));
  }

  /**
   *
   *
   * <pre>
   *     public static void staticVariable(){
   * System.out.println(num);
   * }
   * </pre>
   */
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "$r0 = <java.lang.System: java.io.PrintStream out>",
            "$i0 = <StaticVariable: int num>",
            "virtualinvoke $r0.<java.io.PrintStream: void println(int)>($i0)",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
