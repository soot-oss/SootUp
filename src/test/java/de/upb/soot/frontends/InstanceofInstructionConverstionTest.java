package de.upb.soot.frontends;

import static org.junit.Assert.assertTrue;

import de.upb.soot.core.SootMethod;
import de.upb.soot.frontends.java.WalaClassLoader;
import de.upb.soot.signatures.DefaultSignatureFactory;
import de.upb.soot.signatures.JavaClassSignature;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import categories.Java8Test;

/**
 *
 * @author Linghui Luo
 *
 */
@Category(Java8Test.class)
public class InstanceofInstructionConverstionTest {
  private WalaClassLoader loader;
  private DefaultSignatureFactory sigFactory;
  private JavaClassSignature declareClassSig;

  @Before
  public void loadClassesWithWala() {
    String srcDir = "src/test/resources/selected-java-target/";
    loader = new WalaClassLoader(srcDir, null);
    sigFactory = new DefaultSignatureFactory();
    declareClassSig = sigFactory.getClassSignature("InstanceOf");
  }

  @Test
  public void test() {
    Optional<SootMethod> m = loader
        .getSootMethod(
            sigFactory.getMethodSignature("instanceOf", declareClassSig, "boolean", Collections.singletonList("java.lang.Object")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    Utils.print(method, false);
  }
}
