package de.upb.swt.soot.java.bytecode.minimaltestsuite.java6;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import de.upb.swt.soot.core.model.Modifier;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.java.bytecode.minimaltestsuite.MinimalBytecodeTestSuiteBase;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author Kaustubh Kelkar */
@Category(Java8Test.class)
public class PublicClassTest extends MinimalBytecodeTestSuiteBase {

  @Test
  public void test() {
    SootClass<?> clazz = loadClass(getDeclaredClassSignature());
    assertEquals(EnumSet.of(Modifier.PUBLIC, Modifier.SYNCHRONIZED), clazz.getModifiers());

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
   * public void publicMethod() {
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
   *
   * </pre>
   */
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of("l0 := @this: PublicClass", "return").collect(Collectors.toList());
  }
}