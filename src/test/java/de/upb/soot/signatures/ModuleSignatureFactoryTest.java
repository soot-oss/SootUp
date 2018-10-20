package de.upb.soot.signatures;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import categories.Java9Test;

@Category(Java9Test.class)

public class ModuleSignatureFactoryTest extends SignatureFactoryTest {

  @Test
  public void getPackageSignatureUnnamedModule() {
    ModuleSignatureFactory signatureFactory = new ModuleSignatureFactory();
    PackageSignature packageSignature1 = signatureFactory.getPackageSignature("java.lang");
    assertTrue(packageSignature1 instanceof ModulePackageSignature);
    assertSame(((ModulePackageSignature) packageSignature1).moduleSignature, ModuleSignature.UNNAMED_MODULE);
  }

  // @Test
  // public void dispatchTest() {
  // SignatureFactory signatureFactory = new ModuleSignatureFactory();
  // ClassSignature classSignature1 = signatureFactory.getClassSignature("module-info","","myMod");
  // assertTrue(classSignature1 instanceof ClassSignature);
  // }

  @Test
  public void getPackageSignatureNamedModule() {
    ModuleSignatureFactory signatureFactory = new ModuleSignatureFactory();
    PackageSignature packageSignature1 = signatureFactory.getPackageSignature("java.lang", "myModule");
    assertTrue(packageSignature1 instanceof ModulePackageSignature);
    assertFalse(((ModulePackageSignature) packageSignature1).moduleSignature == ModuleSignature.UNNAMED_MODULE);
  }

  @Test
  public void getModulePackageSignature() {
    ModuleSignatureFactory signatureFactory = new ModuleSignatureFactory();
    ModulePackageSignature packageSignature1 = signatureFactory.getPackageSignature("java.lang", "myModule");
    ModulePackageSignature packageSignature2 = signatureFactory.getPackageSignature("java.lang.invoke", "myModule");
    boolean sameObject = packageSignature1 == packageSignature2;
    assertFalse(sameObject);
  }

  @Test
  public void getModulePackageSignatureSameModule() {
    ModuleSignatureFactory signatureFactory = new ModuleSignatureFactory();
    ModulePackageSignature packageSignature1 = signatureFactory.getPackageSignature("java.lang", "myModule");
    ModulePackageSignature packageSignature2 = signatureFactory.getPackageSignature("java.lang", "myModule");

    boolean samePackage = packageSignature1 == packageSignature2;
    assertTrue(samePackage);

    boolean sameModuleObject = packageSignature1.moduleSignature == packageSignature2.moduleSignature;
    assertTrue(sameModuleObject);
  }

  @Test
  public void getModulePackageSignatureDiffModule() {
    ModuleSignatureFactory signatureFactory = new ModuleSignatureFactory();
    ModulePackageSignature packageSignature1 = signatureFactory.getPackageSignature("java.lang", "myModule1");
    ModulePackageSignature packageSignature2 = signatureFactory.getPackageSignature("java.lang", "myModule2");
    boolean samePackage = packageSignature1 == packageSignature2;
    assertFalse(samePackage);
    boolean sameObject = packageSignature1.moduleSignature == packageSignature2.moduleSignature;
    assertFalse(sameObject);
  }

  @Test(expected = NullPointerException.class)
  public void checkNullModule() {
    ModuleSignatureFactory signatureFactory = new ModuleSignatureFactory();
    ModulePackageSignature packageSignature = signatureFactory.getPackageSignature("myPackage", null);
  }

  @Test(expected = NullPointerException.class)
  public void checkNullModule2() {
    ModuleSignatureFactory signatureFactory = new ModuleSignatureFactory();
    JavaClassSignature classSignature = signatureFactory.getClassSignature("A", "mypackage", null);
  }

  @Test
  public void testModuleInfoSignature() {
    ModuleSignatureFactory signatureFactory = new ModuleSignatureFactory();

    JavaClassSignature classSignature1 = signatureFactory.getClassSignature("module-info");
    assertTrue(classSignature1.isModuleInfo());
  }

  @Test
  public void compModuleSignature() {
    ModuleSignatureFactory signatureFactory = new ModuleSignatureFactory();
    ModuleSignature signature = signatureFactory.getModuleSignature("java.base");
    ModuleSignature signature2 = signatureFactory.getModuleSignature("java.base");
    assertEquals(signature, signature2);
    assertEquals(signature.hashCode(), signature2.hashCode());
    assertEquals(signature.toString(), "java.base");
  }

  @Test
  public void compModuleSignature2() {
    ModuleSignatureFactory signatureFactory = new ModuleSignatureFactory();
    ModuleSignature signature = signatureFactory.getModuleSignature("java.base");
    ModuleSignature signature2 = signatureFactory.getModuleSignature("javafx.base");
    assertNotEquals(signature, signature2);
    assertNotEquals(signature.hashCode(), signature2.hashCode());
    assertEquals(signature2.toString(), "javafx.base");
    assertNotEquals(signature2.toString(), signature.toString());

  }

}
