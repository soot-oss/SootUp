package de.upb.sse.sootup.test.java.bytecode.minimaltestsuite.java8;

import categories.Java8Test;
import de.upb.sse.sootup.core.model.SootClass;
import de.upb.sse.sootup.core.model.SootMethod;
import de.upb.sse.sootup.java.core.JavaIdentifierFactory;
import de.upb.sse.sootup.test.java.bytecode.minimaltestsuite.MinimalBytecodeTestSuiteBase;
import java.util.Collections;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author Bastian Haverkamp */
@Category(Java8Test.class)
public class CossiInputTest extends MinimalBytecodeTestSuiteBase {

  // FIXME: [bh] convert() in AsmMethodSource does not terminate
  // hint: only CossiInput$CossiInputBuilder.build(...) is broken
  @Test
  @Ignore("FIXME")
  public void test() {
    // only care if it terminates here..

    SootClass<?> clazz = loadClass(getDeclaredClassSignature());
    clazz.getMethods().forEach(SootMethod::getBody);

    SootClass<?> innerClazz =
        loadClass(JavaIdentifierFactory.getInstance().getClassType("CossiInput$CossiInputBuilder"));

    innerClazz.getMethod("build", Collections.emptyList()).get().getBody();
  }
}
