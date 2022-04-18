package de.upb.swt.soot.test;

import categories.SlowTest;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import java.util.Collections;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author Bastian Haverkamp */
@Category(SlowTest.class)
public class CossiInputTest extends MinimalBytecodeTestSuiteBase {

  @Test
  public void test() {
    // only care if it terminates here..

    SootClass<?> clazz = loadClass(getDeclaredClassSignature());
    clazz.getMethods().forEach(SootMethod::getBody);

    SootClass<?> innerClazz =
        loadClass(JavaIdentifierFactory.getInstance().getClassType("CossiInput$CossiInputBuilder"));

    innerClazz.getMethod("build", Collections.emptyList()).get().getBody();
  }
}
