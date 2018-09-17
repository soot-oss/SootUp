package de.upb.soot.signatures;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import categories.Java8Test;

@Category(Java8Test.class)

public class SignatureFactoryTest {

  @Test
  public void getSamePackageSignature() {
    SignatureFactory signatureFactory = new DefaultSignatureFactory();
    PackageSignature packageSignature1 = signatureFactory.getPackageSignature("java.lang");
    PackageSignature packageSignature2 = signatureFactory.getPackageSignature("java.lang");
    boolean sameObject = packageSignature1 == packageSignature2;
    assertTrue(sameObject);
  }

  @Test
  public void getDiffPackageSignature() {
    SignatureFactory signatureFactory = new DefaultSignatureFactory();
    PackageSignature packageSignature1 = signatureFactory.getPackageSignature("java.lang");
    PackageSignature packageSignature2 = signatureFactory.getPackageSignature("java.lang.invoke");
    boolean sameObject = packageSignature1 == packageSignature2;
    assertFalse(sameObject);
  }

  @Test
  public void getClassSignature() {
    SignatureFactory signatureFactory = new DefaultSignatureFactory();
    ClassSignature classSignature1 = signatureFactory.getClassSignature("System", "java.lang");
    ClassSignature classSignature2 = signatureFactory.getClassSignature("System", "java.lang");
    // Class Signatures are unique but not their package
    boolean sameObject = classSignature1 == classSignature2;
    assertFalse(sameObject);
  }

  @Test
  public void getClassSignatureEmptyPackage() {
    SignatureFactory signatureFactory = new DefaultSignatureFactory();
    ClassSignature classSignature1 = signatureFactory.getClassSignature("A", "");
    ClassSignature classSignature2 = signatureFactory.getClassSignature("A");
    // Class Signatures are unique but not their package
    boolean sameObject = classSignature1 == classSignature2;
    assertFalse(sameObject);

    boolean samePackageSignatureObject = classSignature1.packageSignature == classSignature2.packageSignature;
    assertTrue(samePackageSignatureObject);
    String className = "A";

    assertEquals(classSignature1.toString(), className);
    assertEquals(classSignature2.toString(), className);
  }

  @Test
  public void getClassSignatureFullyQualified() {
    SignatureFactory signatureFactory = new DefaultSignatureFactory();
    ClassSignature classSignature1 = signatureFactory.getClassSignature("java.lang.System");
    ClassSignature classSignature2 = signatureFactory.getClassSignature("System", "java.lang");
    // Class Signatures are unique but not their package
    boolean sameObject = classSignature1 == classSignature2;
    assertFalse(sameObject);
  }

  @Test
  public void getClassSignaturesPackage() {
    SignatureFactory signatureFactory = new DefaultSignatureFactory();
    ClassSignature classSignature1 = signatureFactory.getClassSignature("System", "java.lang");
    ClassSignature classSignature2 = signatureFactory.getClassSignature("System", "java.lang");
    // Class Signatures are unique but not their package
    boolean samePackageSignature = classSignature1.packageSignature == classSignature2.packageSignature;
    assertTrue(samePackageSignature);

    // but they are equal
    assertEquals(classSignature1, classSignature2);
    assertEquals(classSignature1.hashCode(), classSignature2.hashCode());
  }

  @Test
  public void getMethodSignature() {
    SignatureFactory signatureFactory = new DefaultSignatureFactory();
    ClassSignature declClass = signatureFactory.getClassSignature("System", "java.lang");
    ClassSignature parameter = signatureFactory.getClassSignature("java.lang.Class");
    ClassSignature returnType = signatureFactory.getClassSignature("java.lang.A");

    List<String> parameters = Collections.singletonList("java.lang.Class");

    MethodSignature methodSignature
        = signatureFactory.getMethodSignature("foo", "java.lang.System", "java.lang.A", parameters);
    assertEquals(declClass, methodSignature.declClassSignature);
    assertEquals(returnType, methodSignature.returnTypeSignature);
    assertEquals(parameter, methodSignature.parameterSignatures.get(0));
  }

  @Test
  public void getMethodSignatureString() {
    SignatureFactory signatureFactory = new DefaultSignatureFactory();

    List<String> parameters = Collections.singletonList("java.lang.Class");

    MethodSignature methodSignature
        = signatureFactory.getMethodSignature("foo", "java.lang.System", "java.lang.A", parameters);
    assertEquals("<java.lang.System:java.lang.A foo(java.lang.Class)>", methodSignature.toString());
  }

  @Test
  public void getMethodSignatureString2() {
    SignatureFactory signatureFactory = new DefaultSignatureFactory();

    List<String> parameters = Collections.singletonList("java.lang.Class");

    MethodSignature methodSignature = signatureFactory.getMethodSignature("foo", "java.lang.System", "void", parameters);
    assertEquals("<java.lang.System:void foo(java.lang.Class)>", methodSignature.toString());
  }

  @Test
  public void getMethodSignatureString3() {
    SignatureFactory signatureFactory = new DefaultSignatureFactory();

    List<String> parameters = Collections.emptyList();

    MethodSignature methodSignature = signatureFactory.getMethodSignature("foo", "java.lang.System", "void", parameters);
    assertEquals("<java.lang.System:void foo()>", methodSignature.toString());
  }

  @Test
  public void getMethodSignatureString4() {
    SignatureFactory signatureFactory = new DefaultSignatureFactory();

    List<String> parameters = Collections.emptyList();
    ClassSignature classSignature = signatureFactory.getClassSignature("java.lang.System");
    MethodSignature methodSignature = signatureFactory.getMethodSignature("foo", classSignature, "void", parameters);
    assertEquals("<java.lang.System:void foo()>", methodSignature.toString());
    assertSame(methodSignature.declClassSignature, classSignature);
  }

  @Test
  public void getTypeSignature() {
    SignatureFactory signatureFactory = new DefaultSignatureFactory();
    ClassSignature classSignature1 = signatureFactory.getClassSignature("System", "java.lang");
    TypeSignature classSignature2 = signatureFactory.getTypeSignature("java.lang.System");
    assertEquals(classSignature1, classSignature2);
  }

  @Test
  public void getTypeSignatureTypes() {
    SignatureFactory signatureFactory = new DefaultSignatureFactory();

    TypeSignature byteSig = signatureFactory.getTypeSignature("byte");
    assertSame(byteSig, PrimitiveTypeSignature.BYTE_TYPE_SIGNATURE);

    TypeSignature shortSig = signatureFactory.getTypeSignature("SHORT");
    assertSame(shortSig, PrimitiveTypeSignature.SHORT_TYPE_SIGNATURE);

    TypeSignature intSig = signatureFactory.getTypeSignature("int");
    assertSame(intSig, PrimitiveTypeSignature.INT_TYPE_SIGNATURE);

    TypeSignature longSig = signatureFactory.getTypeSignature("loNg");
    assertSame(longSig, PrimitiveTypeSignature.LONG_TYPE_SIGNATURE);

    TypeSignature floatSig = signatureFactory.getTypeSignature("floAt");
    assertSame(floatSig, PrimitiveTypeSignature.FLOAT_TYPE_SIGNATURE);

    TypeSignature doubleSig = signatureFactory.getTypeSignature("doUble");
    assertSame(doubleSig, PrimitiveTypeSignature.DOUBLE_TYPE_SIGNATURE);

    TypeSignature charSig = signatureFactory.getTypeSignature("chaR");
    assertSame(charSig, PrimitiveTypeSignature.CHAR_TYPE_SIGNATURE);

    TypeSignature boolSig = signatureFactory.getTypeSignature("boolean");
    assertSame(boolSig, PrimitiveTypeSignature.BOOLEAN_TYPE_SIGNATURE);

    TypeSignature nullSig = signatureFactory.getTypeSignature("nuLl");
    assertSame(nullSig, NullTypeSignature.NULL_TYPE_SIGNATURE);

    TypeSignature voidSig = signatureFactory.getTypeSignature("void");
    assertSame(voidSig, VoidTypeSignature.VOID_TYPE_SIGNATURE);
  }

  @Test(expected = NullPointerException.class)
  public void checkNullPackage() {
    SignatureFactory signatureFactory = new DefaultSignatureFactory();
    PackageSignature packageSignature = signatureFactory.getPackageSignature(null);
  }

  @Test(expected = NullPointerException.class)
  public void checkNullPackage2() {
    SignatureFactory signatureFactory = new DefaultSignatureFactory();
    ClassSignature classSignature = signatureFactory.getClassSignature("A", null);
  }
}
