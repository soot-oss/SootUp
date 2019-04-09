package de.upb.soot.namespaces;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import categories.Java9Test;
import de.upb.soot.ModuleFactories;
import de.upb.soot.frontends.ClassSource;
import de.upb.soot.signatures.ModuleSignatureFactory;
import de.upb.soot.signatures.SignatureFactory;
import de.upb.soot.types.JavaClassType;
import de.upb.soot.types.ModuleTypeFactory;
import java.util.Collections;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.powermock.reflect.Whitebox;

@Category(Java9Test.class)
public class JavaModulePathNamespaceTest extends AbstractNamespaceTest {

  private ModuleSignatureFactory signatureFactory;
  private ModuleTypeFactory typeFactory;

  @Before
  @Override
  public void setUp() {
    super.setUp();

    ModuleFactories factories = ModuleFactories.create();
    signatureFactory = factories.getSignatureFactory();
    typeFactory = factories.getTypeFactory();
  }

  @Override
  protected ModuleSignatureFactory getSignatureFactory() {
    return signatureFactory;
  }

  @Override
  public ModuleTypeFactory getTypeFactory() {
    return typeFactory;
  }

  @Test
  public void singleDir() {
    final JavaModulePathNamespace javaClassPathNamespace =
        new JavaModulePathNamespace(
            "target/test-classes/de/upb/soot/namespaces/modules", getClassProvider());
    final JavaClassType sig = getTypeFactory().getClassType("module-info", "", "fancyMod");
    Optional<ClassSource> classSource = javaClassPathNamespace.getClassSource(sig);
    assertTrue(classSource.isPresent());
  }

  @Test
  public void singleDir2() {
    final JavaModulePathNamespace javaClassPathNamespace =
        new JavaModulePathNamespace(
            "target/test-classes/de/upb/soot/namespaces/modules", getClassProvider());
    final JavaClassType sig = getTypeFactory().getClassType("module-info", "", "fancyMod");
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
    final JavaClassType sig = getTypeFactory().getClassType("module-info", "", "de.upb.mod");
    Optional<ClassSource> classSource = javaClassPathNamespace.getClassSource(sig);
    assertTrue(classSource.isPresent());
  }

  @Test
  public void testSignatureWrapper() throws Exception {
    ModuleSignatureFactory signatureFactory = getSignatureFactory();
    final JavaModulePathNamespace javaClassPathNamespace =
        new JavaModulePathNamespace(
            "target/test-classes/de/upb/soot/namespaces/modules/de.upb.mod.jar",
            getClassProvider());
    Class signatureCLass =
        Whitebox.getInnerClassType(JavaModulePathNamespace.class, "SignatureFactoryWrapper");
    // Constructor constructor = Whitebox.getConstructor(signatureCLass, SignatureFactory.class,
    // String.class);
    Object signatureFacotryWrapper =
        Whitebox.invokeConstructor(
            signatureCLass,
            new Class[] {JavaModulePathNamespace.class, SignatureFactory.class, String.class},
            new Object[] {javaClassPathNamespace, signatureFactory, "myJava.mod"});
    Object res1 =
        Whitebox.invokeMethod(signatureFacotryWrapper, "getClassSignature", "java.lang.System");
    assertEquals(res1, getTypeFactory().getClassType("java.lang.System"));

    res1 =
        Whitebox.invokeMethod(signatureFacotryWrapper, "getClassSignature", "java.lang", "System");
    assertEquals(res1, getTypeFactory().getClassType("java.lang", "System"));

    res1 = Whitebox.invokeMethod(signatureFacotryWrapper, "getTypeSignature", "int");
    assertEquals(res1, getTypeFactory().getType("int"));

    res1 = Whitebox.invokeMethod(signatureFacotryWrapper, "getTypeSignature", "int");
    assertEquals(res1, getTypeFactory().getType("int"));

    res1 =
        Whitebox.invokeMethod(
            signatureFacotryWrapper,
            "getMethodSignature",
            "metho1",
            "java.lang.System",
            "void",
            Collections.emptyList());
    assertEquals(
        res1,
        signatureFactory.getMethodSignature(
            "metho1", "java.lang.System", "void", Collections.emptyList()));

    res1 =
        Whitebox.invokeMethod(
            signatureFacotryWrapper,
            "getMethodSignature",
            "metho1",
            getTypeFactory().getClassType("java.lang.System"),
            "void",
            Collections.emptyList());
    assertEquals(
        res1,
        signatureFactory.getMethodSignature(
            "metho1",
            getTypeFactory().getClassType("java.lang.System"),
            "void",
            Collections.emptyList()));

    res1 = Whitebox.invokeMethod(signatureFacotryWrapper, "getPackageSignature", "java.lang");
    assertEquals(res1, signatureFactory.getPackageSignature("java.lang"));
  }
}
