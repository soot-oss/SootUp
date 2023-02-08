package sootup.java.sourcecode.minimaltestsuite.java8;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Ignore;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.sourcecode.minimaltestsuite.MinimalSourceTestSuiteBase;

public class MethodReferenceTest extends MinimalSourceTestSuiteBase {

  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "methodRefMethod", "void", Collections.emptyList());
  }

  /** TODO Update the source code when WALA supports lambda expression */
  @Ignore
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
  }

  /**
   *
   *
   * <pre>
   *     public void methodRefMethod(){
   * System.out.println("Instance Method");
   * MethodReference obj1 = new MethodReference();
   *
   * }
   * </pre>
   */
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: MethodReference",
            "$r1 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r1.<java.io.PrintStream: void println(java.lang.String)>(\"Instance Method\")",
            "$r2 = new MethodReference",
            "specialinvoke $r2.<MethodReference: void <init>()>()",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
