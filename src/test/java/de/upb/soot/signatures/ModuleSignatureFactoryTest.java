package de.upb.soot.signatures;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ModuleSignatureFactoryTest extends SignatureFactoryTest {

  @Test
  public void getPackageSignatureUnnamedModule() {
    ModuleSignatureFactory signatureFactory = new ModuleSignatureFactory();
    PackageSignature packageSignature1 = signatureFactory.getPackageSignature("java.lang");
    assertTrue(packageSignature1 instanceof ModulePackageSignature);
    assertTrue(((ModulePackageSignature) packageSignature1).moduleSignature == ModuleSignature.UNNAMED_MODULE_SIGNATURE);
  }

  @Test
  public void getPackageSignatureNamedModule() {
    ModuleSignatureFactory signatureFactory = new ModuleSignatureFactory();
    PackageSignature packageSignature1 = signatureFactory.getPackageSignature("java.lang", "myModule");
    assertTrue(packageSignature1 instanceof ModulePackageSignature);
    assertFalse(((ModulePackageSignature) packageSignature1).moduleSignature == ModuleSignature.UNNAMED_MODULE_SIGNATURE);
  }

  @Test
  public void getModulePackageSignature() {
    ModuleSignatureFactory signatureFactory = new ModuleSignatureFactory();
    ModulePackageSignature packageSignature1
        = (ModulePackageSignature) signatureFactory.getPackageSignature("java.lang", "myModule");
    ModulePackageSignature packageSignature2
        = (ModulePackageSignature) signatureFactory.getPackageSignature("java.lang.invoke", "myModule");
    boolean sameObject = packageSignature1 == packageSignature2;
    assertFalse(sameObject);
  }

  @Test
  public void getModulePackageSignatureSameModule() {
    ModuleSignatureFactory signatureFactory = new ModuleSignatureFactory();
    ModulePackageSignature packageSignature1
        = (ModulePackageSignature) signatureFactory.getPackageSignature("java.lang", "myModule");
    ModulePackageSignature packageSignature2
        = (ModulePackageSignature) signatureFactory.getPackageSignature("java.lang", "myModule");
    boolean samePackage = packageSignature1 == packageSignature2;
    assertTrue(samePackage);

    boolean sameModuleObject = packageSignature1.moduleSignature == packageSignature2.moduleSignature;
    assertTrue(sameModuleObject);
  }

  @Test
  public void getModulePackageSignatureDiffModule() {
    ModuleSignatureFactory signatureFactory = new ModuleSignatureFactory();
    ModulePackageSignature packageSignature1
        = (ModulePackageSignature) signatureFactory.getPackageSignature("java.lang", "myModule1");
    ModulePackageSignature packageSignature2
        = (ModulePackageSignature) signatureFactory.getPackageSignature("java.lang", "myModule2");
    boolean samePackage = packageSignature1 == packageSignature2;
    assertFalse(samePackage);
    boolean sameObject = packageSignature1.moduleSignature == packageSignature2.moduleSignature;
    assertFalse(sameObject);
  }

}
