package sootup.java.bytecode.minimaltestsuite.java6;

import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.bytecode.minimaltestsuite.MinimalBytecodeTestSuiteBase;

/** @author Kaustubh Kelkar */
@Category(Java8Test.class)
public class AbstractClassTest extends MinimalBytecodeTestSuiteBase {

  @Test
  public void test() {
    SootClass clazz = loadClass(getDeclaredClassSignature());
    // The SuperClass is the abstract one
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
            "l0 := @this: AbstractClass",
            "$stack2 = new AbstractClass",
            "specialinvoke $stack2.<AbstractClass: void <init>()>()",
            "l1 = $stack2",
            "virtualinvoke l1.<A: void a()>()",
            "return")
        .collect(Collectors.toList());
  }
}
