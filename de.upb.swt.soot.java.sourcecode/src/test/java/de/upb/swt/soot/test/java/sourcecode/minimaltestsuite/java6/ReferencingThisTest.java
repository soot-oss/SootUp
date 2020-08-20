package de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.java6;

import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.MinimalSourceTestSuiteBase;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;

public class ReferencingThisTest extends MinimalSourceTestSuiteBase {

  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "thisMethod", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  /**
   *
   *
   * <pre>
   *     ReferencingThis(){
   * this(10,20);
   * System.out.println("this() to invoke current class constructor");
   * }
   * ReferencingThis getObject(){
   * System.out.println("'this' keyword to return the current class instance");
   * return this;
   * }
   * void show(){
   * System.out.println("'this' keyword as method parameter");
   * thisDisplay(this);
   * }
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
