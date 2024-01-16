package sootup.java.sourcecode.minimaltestsuite.java6;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.model.ClassModifier;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.sourcecode.minimaltestsuite.MinimalSourceTestSuiteBase;

/** @author: Hasitha Rajapakse */
@Category(Java8Test.class)
public class PublicClassTest extends MinimalSourceTestSuiteBase {

  @Test
  public void test() {
    SootClass clazz = loadClass(getDeclaredClassSignature());
    assertEquals(EnumSet.of(ClassModifier.PUBLIC), clazz.getModifiers());

    SootMethod method;
    method = clazz.getMethod(getMethodSignature("private").getSubSignature()).get();
    assertTrue(method.isPrivate());
    assertJimpleStmts(method, expectedBodyStmts());

    method = clazz.getMethod(getMethodSignature("protected").getSubSignature()).get();
    assertTrue(method.isProtected());
    assertJimpleStmts(method, expectedBodyStmts());

    method = clazz.getMethod(getMethodSignature("public").getSubSignature()).get();
    assertTrue(method.isPublic());
    assertJimpleStmts(method, expectedBodyStmts());

    method = clazz.getMethod(getMethodSignature("noModifier").getSubSignature()).get();
    assertTrue(method.getModifiers().isEmpty());
    assertJimpleStmts(method, expectedBodyStmts());
  }

  public MethodSignature getMethodSignature(String modifier) {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), modifier + "Method", "void", Collections.emptyList());
  }

  /**
   *
   *
   * <pre>
   *     public void publicMethod() {
   *
   * }
   * private void privateMethod() {
   *
   * }
   * protected void protectedMethod() {
   *
   * }
   * void noModifierMethod() {
   *
   * }
   * </pre>
   */
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of("r0 := @this: PublicClass", "return").collect(Collectors.toList());
  }
}
