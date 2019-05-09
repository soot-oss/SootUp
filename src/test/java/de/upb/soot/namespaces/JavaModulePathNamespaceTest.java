package de.upb.soot.namespaces;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import categories.Java9Test;
import de.upb.soot.frontends.ClassSource;
import de.upb.soot.signatures.IdentifierFactory;
import de.upb.soot.types.JavaClassType;
import de.upb.soot.types.ModuleIdentifierFactory;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.powermock.reflect.Whitebox;

@Category(Java9Test.class)
public class JavaModulePathNamespaceTest extends AbstractNamespaceTest {

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
    final JavaModulePathNamespace javaClassPathNamespace =
        new JavaModulePathNamespace(
            "target/test-classes/de/upb/soot/namespaces/modules", getClassProvider());
    final JavaClassType sig = getIdentifierFactory().getClassType("module-info", "", "fancyMod");
    Optional<ClassSource> classSource = javaClassPathNamespace.getClassSource(sig);
    assertTrue(classSource.isPresent());
  }

  @Test
  public void singleDir2() {
    final JavaModulePathNamespace javaClassPathNamespace =
        new JavaModulePathNamespace(
            "target/test-classes/de/upb/soot/namespaces/modules", getClassProvider());
    final JavaClassType sig = getIdentifierFactory().getClassType("module-info", "", "fancyMod");
    // TODO: check for a better minClassFoundNumber
    // also all JDK classes are loaded
    testClassReceival(javaClassPathNamespace, sig, 50);
  }

  @Test
  public void singleJar() {
    final JavaModulePathNamespace javaClassPathNamespace =
        new JavaModulePathNamespace(
            "target/test-classes/de/upb/soot/namespaces/modules/de.upb.mod.jar",
            getClassProvider());
    final JavaClassType sig = getIdentifierFactory().getClassType("module-info", "", "de.upb.mod");
    Optional<ClassSource> classSource = javaClassPathNamespace.getClassSource(sig);
    assertTrue(classSource.isPresent());
  }

  @Test
  public void testTypeWrapper() throws Exception {
    final JavaModulePathNamespace javaClassPathNamespace =
        new JavaModulePathNamespace(
            "target/test-classes/de/upb/soot/namespaces/modules/de.upb.mod.jar",
            getClassProvider());
    Class<?> signatureClass =
        Whitebox.getInnerClassType(JavaModulePathNamespace.class, "TypeFactoryWrapper");
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
