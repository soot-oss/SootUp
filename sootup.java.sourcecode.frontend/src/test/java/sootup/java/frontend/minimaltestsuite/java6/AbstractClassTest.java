/** @author: Hasitha Rajapakse */
package sootup.java.frontend.minimaltestsuite.java6;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.frontend.minimaltestsuite.MinimalSourceTestSuiteBase;

@Tag("Java8")
public class AbstractClassTest extends MinimalSourceTestSuiteBase {

  @Test
  public void test() {
    SootClass clazz = loadClass(getDeclaredClassSignature());
    // The SuperClass is the abstract one
    System.out.println(clazz.getSuperclass());
    SootClass superClazz = loadClass(clazz.getSuperclass().get());
    assertTrue(superClazz.isAbstract());
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
  }

  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "abstractClass", "void", Collections.emptyList());
  }

  /**
   *
   *
   * <pre>
   *     public void abstractClass(){
   *         A obj = new AbstractClass();
   *         obj.a();
   *     }
   * </pre>
   */
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: AbstractClass",
            "r1 = new AbstractClass",
            "specialinvoke r1.<AbstractClass: void <init>()>()",
            "virtualinvoke r1.<A: void a()>()",
            "return")
        .collect(Collectors.toList());
  }
}
