package de.upb.sse.sootup.test.java.sourcecode.frontend;

import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import de.upb.sse.sootup.core.model.SootClass;
import de.upb.sse.sootup.core.util.Utils;
import de.upb.sse.sootup.java.core.JavaIdentifierFactory;
import de.upb.sse.sootup.java.core.types.JavaClassType;
import de.upb.sse.sootup.java.sourcecode.frontend.WalaJavaClassProvider;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author Linghui Luo */
@Category(Java8Test.class)
public class Java7EnhancementsTest {
  private WalaJavaClassProvider loader;
  private JavaIdentifierFactory typeFactory;
  private JavaClassType declareClassSig;

  @Before
  public void loadClassesWithWala() {
    String srcDir = "../shared-test-resources/java-target/java7";
    loader = new WalaJavaClassProvider(srcDir);
    typeFactory = JavaIdentifierFactory.getInstance();
  }

  @Test
  public void testBinaryLiterals() {
    declareClassSig = typeFactory.getClassType("BinaryLiterals");
    Optional<SootClass> c = loader.getSootClass(declareClassSig);
    assertTrue(c.isPresent());
    SootClass klass = c.get();
    // TODO. replace the next line with assertions.
    Utils.outputJimple(klass, false);
  }

  @Test
  public void testCatchMultipleExceptionTypes() {
    declareClassSig = typeFactory.getClassType("CatchMultipleExceptionTypes");
    Optional<SootClass> c = loader.getSootClass(declareClassSig);
    assertTrue(c.isPresent());
    SootClass klass = c.get();
    // TODO. replace the next line with assertions.
    Utils.outputJimple(klass, false);
  }

  @Test
  public void testStringsInSwitch() {
    declareClassSig = typeFactory.getClassType("StringsInSwitch");
    Optional<SootClass> c = loader.getSootClass(declareClassSig);
    assertTrue(c.isPresent());
    SootClass klass = c.get();
    // TODO. replace the next line with assertions.
    Utils.outputJimple(klass, false);
  }

  @Test
  public void testTryWithResourcesStatement() {
    declareClassSig = typeFactory.getClassType("TryWithResourcesStatement");
    Optional<SootClass> c = loader.getSootClass(declareClassSig);
    assertTrue(c.isPresent());
    SootClass klass = c.get();
    // TODO. replace the next line with assertions.
    Utils.outputJimple(klass, false);
  }

  @Test
  public void testUnderscoresInNumericLiterals() {
    declareClassSig = typeFactory.getClassType("UnderscoresInNumericLiterals");
    Optional<SootClass> c = loader.getSootClass(declareClassSig);
    assertTrue(c.isPresent());
    SootClass klass = c.get();
    // TODO. replace the next line with assertions.
    Utils.outputJimple(klass, false);
  }

  @Test
  public void testTypeInferenceforGenericInstanceCreation() {
    declareClassSig = typeFactory.getClassType("TypeInferenceforGenericInstanceCreation");
    Optional<SootClass> c = loader.getSootClass(declareClassSig);
    assertTrue(c.isPresent());
    SootClass klass = c.get();
    // TODO. replace the next line with assertions.
    Utils.outputJimple(klass, false);
  }
}
