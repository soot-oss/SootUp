package de.upb.soot.frontends.java;

import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import de.upb.soot.DefaultFactories;
import de.upb.soot.core.SootClass;
import de.upb.soot.types.DefaultTypeFactory;
import de.upb.soot.types.JavaClassType;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author Linghui Luo */
@Category(Java8Test.class)
public class Java7EnhancementsTest {
  private WalaClassLoader loader;
  private DefaultTypeFactory typeFactory;
  private JavaClassType declareClassSig;

  @Before
  public void loadClassesWithWala() {
    String srcDir = "src/test/resources/java-target/java7";
    loader = new WalaClassLoader(srcDir, null);
    DefaultFactories factories = DefaultFactories.create();
    typeFactory = factories.getTypeFactory();
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
