package sootup.java.sourcecode.minimaltestsuite.java6;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.sourcecode.minimaltestsuite.MinimalSourceTestSuiteBase;

public class DeclareFloatTest extends MinimalSourceTestSuiteBase {
  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "declareFloatMethod", "void", Collections.emptyList());
  }

  /**
   *
   *
   * <pre>
   *     void declareFloatMethod(){
   * System.out.println(f1);
   * System.out.println(f2);
   * }
   * </pre>
   */
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: DeclareFloat",
            "$r1 = <java.lang.System: java.io.PrintStream out>",
            "$f0 = r0.<DeclareFloat: float f1>",
            "virtualinvoke $r1.<java.io.PrintStream: void println(float)>($f0)",
            "$r2 = <java.lang.System: java.io.PrintStream out>",
            "$f1 = r0.<DeclareFloat: float f2>",
            "virtualinvoke $r2.<java.io.PrintStream: void println(float)>($f1)",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
  }
}
