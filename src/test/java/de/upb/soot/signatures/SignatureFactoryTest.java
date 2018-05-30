package de.upb.soot.signatures;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SignatureFactoryTest {

  @Test
  public void getSamePackageSignature() {
    SignatureFactory signatureFactory = new SignatureFactory();
    PackageSignature packageSignature1 = signatureFactory.getPackageSignature("java.lang");
    PackageSignature packageSignature2 = signatureFactory.getPackageSignature("java.lang");
    boolean sameObject = packageSignature1 == packageSignature2;
    assertTrue(sameObject);
  }

  @Test
  public void getDiffPackageSignature() {
    SignatureFactory signatureFactory = new SignatureFactory();
    PackageSignature packageSignature1 = signatureFactory.getPackageSignature("java.lang");
    PackageSignature packageSignature2 = signatureFactory.getPackageSignature("java.lang.invoke");
    boolean sameObject = packageSignature1 == packageSignature2;
    assertFalse(sameObject);
  }

  @Test
  public void getClassSignature() {
    SignatureFactory signatureFactory = new SignatureFactory();
    ClassSignature classSignature1 = signatureFactory.getClassSignature("System", "java.lang");
    ClassSignature classSignature2 = signatureFactory.getClassSignature("System", "java.lang");
    // Class Signatures are unique but not their package
    boolean sameObject = classSignature1 == classSignature2;
    assertFalse(sameObject);

  }
  @Test
  public void getClassSignaturesPackage() {
    SignatureFactory signatureFactory = new SignatureFactory();
    ClassSignature classSignature1 = signatureFactory.getClassSignature("System", "java.lang");
    ClassSignature classSignature2 = signatureFactory.getClassSignature("System", "java.lang");
    // Class Signatures are unique but not their package
    boolean samePackageSignature =
            classSignature1.packageSignature == classSignature2.packageSignature;
    assertTrue(samePackageSignature);

    //but they are equal
    assertTrue(classSignature1.equals(classSignature2));
    assertTrue(classSignature1.hashCode() == classSignature2.hashCode());

  }

  @Test
  public void getMethodSignature() {
    SignatureFactory signatureFactory = new SignatureFactory();
  }
}
