package de.upb.swt.soot.test.java.bytecode.inputlocation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import categories.Java9Test;
import de.upb.swt.soot.core.IdentifierFactory;
import de.upb.swt.soot.core.frontend.AbstractClassSource;
import de.upb.swt.soot.java.bytecode.inputlocation.JavaModulePathAnalysisInputLocation;
import de.upb.swt.soot.java.core.ModuleIdentifierFactory;
import de.upb.swt.soot.java.core.types.JavaClassType;
import java.util.Optional;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.powermock.reflect.Whitebox;

@Category(Java9Test.class)
public class JavaModulePathNamespaceTest extends AnalysisInputLocationTest {

  private ModuleIdentifierFactory identifierFactory;

  @Before
  @Override
  public void setUp() {
    super.setUp();

    identifierFactory = ModuleIdentifierFactory.getInstance();
  }

  @Override
  protected ModuleIdentifierFactory getIdentifierFactory() {
    return identifierFactory;
  }

  @Test
  public void singleDir() {
    final JavaModulePathAnalysisInputLocation javaClassPathNamespace =
        new JavaModulePathAnalysisInputLocation(
            "../shared-test-resources/java9-target/de/upb/soot/namespaces/modules");
    final JavaClassType sig = getIdentifierFactory().getClassType("module-info", "", "fancyMod");
    Optional<? extends AbstractClassSource> classSource =
        javaClassPathNamespace.getClassSource(sig);
    assertTrue(classSource.isPresent());
  }

  @Test
  @Ignore // does not work before adapting module loading in soot
  public void singleDir2() {
    final JavaModulePathAnalysisInputLocation javaClassPathNamespace =
        new JavaModulePathAnalysisInputLocation(
            "../shared-test-resources/java9-target/de/upb/soot/namespaces/modules");
    final JavaClassType sig = getIdentifierFactory().getClassType("module-info", "", "fancyMod");
    // TODO: check for a better minClassFoundNumber
    // also all JDK classes are loaded
    testClassReceival(javaClassPathNamespace, sig, 50);
  }

  @Test
  public void singleJar() {
    final JavaModulePathAnalysisInputLocation javaClassPathNamespace =
        new JavaModulePathAnalysisInputLocation(
            "../shared-test-resources/java9-target/de/upb/soot/namespaces/modules/de.upb.mod.jar");
    final JavaClassType sig = getIdentifierFactory().getClassType("module-info", "", "de.upb.mod");
    Optional<? extends AbstractClassSource> classSource =
        javaClassPathNamespace.getClassSource(sig);
    assertTrue(classSource.isPresent());
  }

  @Test
  public void testTypeWrapper() throws Exception {
    final JavaModulePathAnalysisInputLocation javaClassPathNamespace =
        new JavaModulePathAnalysisInputLocation(
            "../shared-test-resources/java9-target/de/upb/soot/namespaces/modules/de.upb.mod.jar");
    Class<?> signatureClass =
        Whitebox.getInnerClassType(
            JavaModulePathAnalysisInputLocation.class, "IdentifierFactoryWrapper");
    // Constructor constructor = Whitebox.getConstructor(signatureClass, IdentifierFactory.class,
    // String.class);
    Object typeFactoryWrapper =
        Whitebox.invokeConstructor(
            signatureClass,
            new Class[] {IdentifierFactory.class, String.class},
            new Object[] {getIdentifierFactory(), "myJava.mod"});
    Object res1 = Whitebox.invokeMethod(typeFactoryWrapper, "getClassType", "java.lang.System");
    assertEquals(res1, getIdentifierFactory().getClassType("java.lang.System"));

    res1 = Whitebox.invokeMethod(typeFactoryWrapper, "getClassType", "java.lang", "System");
    assertEquals(res1, getIdentifierFactory().getClassType("java.lang", "System"));

    res1 = Whitebox.invokeMethod(typeFactoryWrapper, "getType", "int");
    assertEquals(res1, getIdentifierFactory().getType("int"));

    res1 = Whitebox.invokeMethod(typeFactoryWrapper, "getType", "int");
    assertEquals(res1, getIdentifierFactory().getType("int"));
  }
}
