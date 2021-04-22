package de.upb.swt.soot.test.java.bytecode.minimaltestsuite.java6;

import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.java.core.JavaSootClass;
import de.upb.swt.soot.test.java.bytecode.minimaltestsuite.MinimalBytecodeTestSuiteBase;
import java.util.Collections;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author Kaustubh Kelkar */
@Category(Java8Test.class)
public class NoModifierClassTest extends MinimalBytecodeTestSuiteBase {

  @Test
  public void test() {
    JavaSootClass clazz = loadClass(getDeclaredClassSignature());
    // TODO SYNCHRONIZED modifier  does not work
    // assertEquals(EnumSet.noneOf(Modifier.class), clazz.getModifiers());
    assertTrue(clazz.getMethod(getMethodSignature("private")).get().isPrivate());
    assertTrue(clazz.getMethod(getMethodSignature("protected")).get().isProtected());
    assertTrue(clazz.getMethod(getMethodSignature("public")).get().isPublic());
    assertTrue(clazz.getMethod(getMethodSignature("noModifier")).get().getModifiers().isEmpty());
  }

  public MethodSignature getMethodSignature(String modifier) {
    return identifierFactory.getMethodSignature(
        modifier + "Method", getDeclaredClassSignature(), "void", Collections.emptyList());
  }
}
