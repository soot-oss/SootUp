package de.upb.soot.frontends.java;

import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import de.upb.soot.DefaultIdentifierFactory;
import de.upb.soot.core.SootMethod;
import de.upb.soot.types.JavaClassType;
import java.util.Collections;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author Linghui Luo */
@Category(Java8Test.class)
public class InstanceofInstructionConverstionTest {
  private WalaClassLoader loader;

  private DefaultIdentifierFactory identifierFactory;
  private JavaClassType declareClassSig;

  @Before
  public void loadClassesWithWala() {
    String srcDir = "src/test/resources/selected-java-target/";
    loader = new WalaClassLoader(srcDir, null);
    identifierFactory = DefaultIdentifierFactory.getInstance();
    declareClassSig = identifierFactory.getClassType("InstanceOf");
  }

  @Test
  public void test() {
    Optional<SootMethod> m =
        WalaClassLoaderTestUtils.getSootMethod(
            loader,
            identifierFactory.getMethodSignature(
                "instanceOf",
                declareClassSig,
                "boolean",
                Collections.singletonList("java.lang.Object")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    Utils.print(method, false);
  }
}
