/** @author: Hasitha Rajapakse */
package de.upb.sse.sootup.test.java.sourcecode.minimaltestsuite.java6;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import de.upb.sse.sootup.core.model.Modifier;
import de.upb.sse.sootup.core.signatures.MethodSignature;
import de.upb.sse.sootup.java.core.JavaSootClass;
import de.upb.sse.sootup.test.java.sourcecode.minimaltestsuite.MinimalSourceTestSuiteBase;
import java.util.*;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author: Hasitha Rajapakse */
@Category(Java8Test.class)
public class NoModifierClassTest extends MinimalSourceTestSuiteBase {

  @Test
  public void test() {
    JavaSootClass clazz = loadClass(getDeclaredClassSignature());
    assertEquals(EnumSet.noneOf(Modifier.class), clazz.getModifiers());

    assertTrue(clazz.getMethod(getMethodSignature("private").getSubSignature()).get().isPrivate());
    assertTrue(
        clazz.getMethod(getMethodSignature("protected").getSubSignature()).get().isProtected());
    assertTrue(clazz.getMethod(getMethodSignature("public").getSubSignature()).get().isPublic());
    assertTrue(
        clazz
            .getMethod(getMethodSignature("noModifier").getSubSignature())
            .get()
            .getModifiers()
            .isEmpty());
  }

  public MethodSignature getMethodSignature(String modifier) {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), modifier + "Method", "void", Collections.emptyList());
  }
}
