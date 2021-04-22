package de.upb.swt.soot.jimple.parser.javatestsuite.java6;

import static org.junit.Assert.assertTrue;

import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.jimple.parser.categories.Java8Test;
import de.upb.swt.soot.jimple.parser.javatestsuite.JimpleTestSuiteBase;
import java.util.Collections;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author Kaustubh Kelkar */
@Category(Java8Test.class)
public class NoModifierClassTest extends JimpleTestSuiteBase {

  @Test
  public void test() {
    SootClass<?> clazz = loadClass(getDeclaredClassSignature());
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
