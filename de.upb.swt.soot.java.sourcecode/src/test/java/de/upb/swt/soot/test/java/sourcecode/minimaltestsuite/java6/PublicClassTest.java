package de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.java6;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import de.upb.swt.soot.core.model.Modifier;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.MinimalTestSuiteBase;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author: Hasitha Rajapakse */
@Category(Java8Test.class)
public class PublicClassTest extends MinimalTestSuiteBase {

  @Test
  public void defaultTest() {
    SootClass clazz = loadClass(getDeclaredClassSignature());
    assertEquals(EnumSet.of(Modifier.PUBLIC), clazz.getModifiers());

    SootMethod method;
    method = clazz.getMethod(getMethodSignature("private")).get();
    assertTrue(method.isPrivate());
    assertJimpleStmts(method, expectedBodyStmts());

    method = clazz.getMethod(getMethodSignature("protected")).get();
    assertTrue(method.isProtected());
    assertJimpleStmts(method, expectedBodyStmts());

    method = clazz.getMethod(getMethodSignature("public")).get();
    assertTrue(method.isPublic());
    assertJimpleStmts(method, expectedBodyStmts());

    method = clazz.getMethod(getMethodSignature("noModifier")).get();
    assertEquals(method.getModifiers().toString(), "[]");
    assertJimpleStmts(method, expectedBodyStmts());
  }

  public MethodSignature getMethodSignature(String modifier) {
    return identifierFactory.getMethodSignature(
        modifier + "Method", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of("r0 := @this: PublicClass", "return").collect(Collectors.toList());
  }
}
