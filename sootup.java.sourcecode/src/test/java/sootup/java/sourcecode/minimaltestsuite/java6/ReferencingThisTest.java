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

public class ReferencingThisTest extends MinimalSourceTestSuiteBase {

  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "thisMethod", "void", Collections.emptyList());
  }

  /**
   *
   *
   * <pre>
   * void thisMethod(){
   * System.out.println(" this keyword as an argument in the constructor call");
   * ReferencingThis obj= new ReferencingThis(this.a, this.b);
   * obj.show();
   * }
   * </pre>
   */
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: ReferencingThis",
            "$r1 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r1.<java.io.PrintStream: void println(java.lang.String)>(\" this keyword as an argument in the constructor call\")",
            "$r2 = new ReferencingThis",
            "$i0 = r0.<ReferencingThis: int a>",
            "$i1 = r0.<ReferencingThis: int b>",
            "specialinvoke $r2.<ReferencingThis: void <init>(int,int)>($i0, $i1)",
            "virtualinvoke $r2.<ReferencingThis: void show()>()",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
  }
}
