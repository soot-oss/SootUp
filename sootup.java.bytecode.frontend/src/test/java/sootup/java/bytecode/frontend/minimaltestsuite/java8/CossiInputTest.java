package sootup.java.bytecode.frontend.minimaltestsuite.java8;

import java.util.Collections;
import org.junit.jupiter.api.Test;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.java.bytecode.frontend.minimaltestsuite.MinimalBytecodeTestSuiteBase;

/**
 * @author Bastian Haverkamp
 */
public class CossiInputTest extends MinimalBytecodeTestSuiteBase {

  @Test
  public void test() {
    SootClass clazz = loadClass(getDeclaredClassSignature());
    clazz.getMethods().forEach(SootMethod::getBody);

    SootClass innerClazz =
        loadClass(identifierFactory.getClassType("CossiInput$CossiInputBuilder"));

    innerClazz.getMethod("build", Collections.emptyList()).get().getBody();
  }
}
