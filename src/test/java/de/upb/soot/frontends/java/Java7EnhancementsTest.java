package de.upb.soot.frontends.java;

import static org.junit.Assert.assertTrue;

import de.upb.soot.core.SootClass;
import de.upb.soot.signatures.DefaultSignatureFactory;
import de.upb.soot.signatures.JavaClassSignature;
import java.util.Optional;
import org.junit.Before;
import org.junit.Ignore;

/** @author Linghui Luo */
public class Java7EnhancementsTest {
  private WalaClassLoader loader;
  private DefaultSignatureFactory sigFactory;
  private JavaClassSignature declareClassSig;

  @Before
  public void loadClassesWithWala() {
    String srcDir = "src/test/resources/java-target/java7";
    loader = new WalaClassLoader(srcDir, null);
    sigFactory = new DefaultSignatureFactory();
  }

  @Ignore
  public void testBinaryLiterals() {
    declareClassSig = sigFactory.getClassSignature("BinaryLiterals");
    Optional<SootClass> c = loader.getSootClass(declareClassSig);
    assertTrue(c.isPresent());
    SootClass klass = c.get();
    Utils.print(klass, true);
  }

  @Ignore
  public void testCatchMultipleExceptionTypes() {
    declareClassSig = sigFactory.getClassSignature("CatchMultipleExceptionTypes");
    Optional<SootClass> c = loader.getSootClass(declareClassSig);
    assertTrue(c.isPresent());
    SootClass klass = c.get();
    Utils.print(klass, true);
  }

  @Ignore
  public void testStringsInSwitch() {
    declareClassSig = sigFactory.getClassSignature("StringsInSwitch");
    Optional<SootClass> c = loader.getSootClass(declareClassSig);
    assertTrue(c.isPresent());
    SootClass klass = c.get();
    Utils.print(klass, true);
  }

  @Ignore
  public void testTryWithResourcesStatement() {
    declareClassSig = sigFactory.getClassSignature("TryWithResourcesStatement");
    Optional<SootClass> c = loader.getSootClass(declareClassSig);
    assertTrue(c.isPresent());
    SootClass klass = c.get();
    Utils.print(klass, true);
  }

  @Ignore
  public void testUnderscoresInNumericLiterals() {
    declareClassSig = sigFactory.getClassSignature("UnderscoresInNumericLiterals");
    Optional<SootClass> c = loader.getSootClass(declareClassSig);
    assertTrue(c.isPresent());
    SootClass klass = c.get();
    Utils.print(klass, true);
  }

  @Ignore
  public void testTypeInferenceforGenericInstanceCreation() {
    declareClassSig = sigFactory.getClassSignature("TypeInferenceforGenericInstanceCreation");
    Optional<SootClass> c = loader.getSootClass(declareClassSig);
    assertTrue(c.isPresent());
    SootClass klass = c.get();
    Utils.print(klass, true);
  }
}
