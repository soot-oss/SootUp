package sootup.jimple.frontend.javatestsuite.java6;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import org.junit.jupiter.api.Test;
import sootup.core.model.SootClass;
import sootup.core.signatures.MethodSignature;
import sootup.jimple.frontend.javatestsuite.JimpleTestSuiteBase;

/**
 * @author Kaustubh Kelkar
 */
public class NoModifierClassTest extends JimpleTestSuiteBase {

  @Test
  public void test() {
    SootClass clazz = loadClass(getDeclaredClassSignature());
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
