/** @author: Hasitha Rajapakse */
package sootup.java.sourcecode.minimaltestsuite.java6;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import java.util.*;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.model.ClassModifier;
import sootup.core.signatures.MethodSignature;
import sootup.java.core.JavaSootClass;
import sootup.java.sourcecode.minimaltestsuite.MinimalSourceTestSuiteBase;

/** @author: Hasitha Rajapakse */
@Category(Java8Test.class)
public class NoModifierClassTest extends MinimalSourceTestSuiteBase {

  @Test
  public void test() {
    JavaSootClass clazz = loadClass(getDeclaredClassSignature());
    assertEquals(EnumSet.noneOf(ClassModifier.class), clazz.getModifiers());

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
