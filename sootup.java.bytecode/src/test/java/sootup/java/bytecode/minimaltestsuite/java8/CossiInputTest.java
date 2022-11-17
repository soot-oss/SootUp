package sootup.java.bytecode.minimaltestsuite.java8;

import categories.Java8Test;
import java.util.Collections;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.java.bytecode.minimaltestsuite.MinimalBytecodeTestSuiteBase;
import sootup.java.core.JavaIdentifierFactory;

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
