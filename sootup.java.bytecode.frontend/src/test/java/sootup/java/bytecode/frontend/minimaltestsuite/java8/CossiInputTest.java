package sootup.java.bytecode.frontend.minimaltestsuite.java8;

import categories.TestCategories;
import java.util.Collections;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.java.bytecode.frontend.minimaltestsuite.MinimalBytecodeTestSuiteBase;
import sootup.java.core.JavaIdentifierFactory;

/** @author Bastian Haverkamp */
@Tag(TestCategories.JAVA_8_CATEGORY)
public class CossiInputTest extends MinimalBytecodeTestSuiteBase {

  // FIXME: [bh] convert() in AsmMethodSource does not terminate
  // hint: only CossiInput$CossiInputBuilder.build(...) is broken
  @Test
  @Disabled("FIXME")
  public void test() {
    // only care if it terminates here..

    SootClass clazz = loadClass(getDeclaredClassSignature());
    clazz.getMethods().forEach(SootMethod::getBody);

    SootClass innerClazz =
        loadClass(JavaIdentifierFactory.getInstance().getClassType("CossiInput$CossiInputBuilder"));

    innerClazz.getMethod("build", Collections.emptyList()).get().getBody();
  }
}
