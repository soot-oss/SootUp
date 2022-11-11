package sootup.java.sourcecode.minimaltestsuite.java6;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.sourcecode.minimaltestsuite.MinimalSourceTestSuiteBase;

/** @author Kaustubh Kelkar */
public class InstanceOfCheckTest extends MinimalSourceTestSuiteBase {
  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "instanceOfCheckMethod", "void", Collections.emptyList());
  }

  @org.junit.Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
    SootClass<?> sootClass = loadClass(getDeclaredClassSignature());
    assertTrue(sootClass.getSuperclass().get().getClassName().equals("InstanceOfCheckSuper"));
  }

  /**
   *
   *
   * <pre>
   *     public void instanceOfCheckMethod(){
   * InstanceOfCheck obj= new InstanceOfCheck();
   * System.out.println(obj instanceof InstanceOfCheckSuper);
   * }
   * </pre>
   */
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: InstanceOfCheck",
            "$r1 = new InstanceOfCheck",
            "specialinvoke $r1.<InstanceOfCheck: void <init>()>()",
            "$r2 = <java.lang.System: java.io.PrintStream out>",
            "$z0 = $r1 instanceof InstanceOfCheckSuper",
            "virtualinvoke $r2.<java.io.PrintStream: void println(boolean)>($z0)",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
