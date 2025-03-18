package sootup.java.bytecode.frontend.minimaltestsuite.java6;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import org.junit.jupiter.api.Test;
import sootup.core.signatures.MethodSignature;
import sootup.java.bytecode.frontend.minimaltestsuite.MinimalBytecodeTestSuiteBase;
import sootup.java.core.JavaSootClass;

/**
 * @author Kaustubh Kelkar
 */
public class NoModifierClassTest extends MinimalBytecodeTestSuiteBase {

  @Test
  public void test() {
    JavaSootClass clazz = loadClass(getDeclaredClassSignature());
    // TODO SYNCHRONIZED modifier  does not work
    // assertEquals(EnumSet.noneOf(Modifier.class), clazz.getModifiers());
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
