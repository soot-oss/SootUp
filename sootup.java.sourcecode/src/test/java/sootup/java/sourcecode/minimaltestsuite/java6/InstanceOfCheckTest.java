package sootup.java.sourcecode.minimaltestsuite.java6;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.sourcecode.minimaltestsuite.MinimalSourceTestSuiteBase;

/** @author Kaustubh Kelkar */
@Tag("Java8")
public class InstanceOfCheckTest extends MinimalSourceTestSuiteBase {
  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "instanceOfCheckMethod", "void", Collections.emptyList());
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
    SootClass sootClass = loadClass(getDeclaredClassSignature());
    assertEquals("InstanceOfCheckSuper", sootClass.getSuperclass().get().getClassName());
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
            "r1 = new InstanceOfCheck",
            "specialinvoke r1.<InstanceOfCheck: void <init>()>()",
            "r2 = <java.lang.System: java.io.PrintStream out>",
            "z0 = r1 instanceof InstanceOfCheckSuper",
            "virtualinvoke r2.<java.io.PrintStream: void println(boolean)>(z0)",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
