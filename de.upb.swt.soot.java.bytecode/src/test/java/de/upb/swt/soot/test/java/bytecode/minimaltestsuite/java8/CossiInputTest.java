package de.upb.swt.soot.test.java.bytecode.minimaltestsuite.java8;

import categories.Java8Test;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import de.upb.swt.soot.test.java.bytecode.minimaltestsuite.MinimalBytecodeTestSuiteBase;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author Bastian Haverkamp */
@Category(Java8Test.class)
public class CossiInputTest extends MinimalBytecodeTestSuiteBase {

  // FIXME: [bh] results in a stack underrun in AsmMethodSource.convertMethodInsn
  // hint: only CossiInput$CossiInputBuilder.build(...) is broken
  @Test
  @Ignore("FIXME")
  public void test() {
    // only care if it terminates here..

    SootClass<?> clazz = loadClass(getDeclaredClassSignature());
    clazz.getMethods().forEach(SootMethod::getBody);

    SootClass<?> innerClazz =
        loadClass(JavaIdentifierFactory.getInstance().getClassType("CossiInput$CossiInputBuilder"));
    innerClazz.getMethods().forEach(SootMethod::getBody);
  }
}
