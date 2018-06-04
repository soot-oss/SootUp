package de.upb.soot.signatures;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;

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
  public void getClassSignatureFQN() {
    SignatureFactory signatureFactory = new SignatureFactory();
    ClassSignature classSignature1 = signatureFactory.getClassSignature("java.lang.System");
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

    // but they are equal
    assertTrue(classSignature1.equals(classSignature2));
    assertTrue(classSignature1.hashCode() == classSignature2.hashCode());
  }

  @Test
  public void getMethodSignature() {
    SignatureFactory signatureFactory = new SignatureFactory();
    ClassSignature declClass = signatureFactory.getClassSignature("System", "java.lang");
    ClassSignature parameter = signatureFactory.getClassSignature("java.lang.Class");
    ClassSignature returnType = signatureFactory.getClassSignature("java.lang.A");

    List<String> parameters = Collections.singletonList("java.lang.Class");

    MethodSignature methodSignature =
        signatureFactory.getMethodSignature("foo", "java.lang.System", parameters, "java.lang.A");
    assertTrue(declClass.equals(methodSignature.declClassSignature));
    assertTrue(returnType.equals(methodSignature.returnTypeSignature));
    assertTrue(parameter.equals(methodSignature.parameterSignatures.get(0)));
  }

  @Test
  public void getTypeSignature() {
    SignatureFactory signatureFactory = new SignatureFactory();
    ClassSignature classSignature1 = signatureFactory.getClassSignature("System", "java.lang");
    TypeSignature classSignature2 = signatureFactory.getTypeSignature("java.lang.System");
    assertTrue(classSignature1.equals(classSignature2));
  }

  @Test
  public void getTypeSignatureTypes() {
    SignatureFactory signatureFactory = new SignatureFactory();

    TypeSignature byteSig = signatureFactory.getTypeSignature("byte");
    assertTrue(byteSig == PrimitiveTypeSignature.BYTE_TYPE_SIGNATURE);

    TypeSignature shortSig = signatureFactory.getTypeSignature("SHORT");
    assertTrue(shortSig == PrimitiveTypeSignature.SHORT_TYPE_SIGNATURE);

    TypeSignature intSig = signatureFactory.getTypeSignature("int");
    assertTrue(intSig == PrimitiveTypeSignature.INT_TYPE_SIGNATURE);

    TypeSignature longSig = signatureFactory.getTypeSignature("loNg");
    assertTrue(longSig == PrimitiveTypeSignature.LONG_TYPE_SIGNATURE);

    TypeSignature floatSig = signatureFactory.getTypeSignature("floAt");
    assertTrue(floatSig == PrimitiveTypeSignature.FLOAT_TYPE_SIGNATURE);

    TypeSignature doubleSig = signatureFactory.getTypeSignature("doUble");
    assertTrue(doubleSig == PrimitiveTypeSignature.DOUBLE_TYPE_SIGNATURE);

    TypeSignature charSig = signatureFactory.getTypeSignature("chaR");
    assertTrue(charSig == PrimitiveTypeSignature.CHAR_TYPE_SIGNATURE);

    TypeSignature boolSig = signatureFactory.getTypeSignature("boolean");
    assertTrue(boolSig == PrimitiveTypeSignature.BOOLEAN_TYPE_SIGNATURE);

    TypeSignature nullSig = signatureFactory.getTypeSignature("nuLl");
    assertTrue(nullSig == NullTypeSignature.NULL_TYPE_SIGNATURE);

    TypeSignature voidSig = signatureFactory.getTypeSignature("void");
    assertTrue(voidSig == VoidTypeSignature.VOID_TYPE_SIGNATURE);
  }
}
