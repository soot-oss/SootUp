package de.upb.soot.signatures;

/*-
 * #%L
 * Soot
 * %%
 * Copyright (C) 2018 Secure Software Engineering Department, University of Paderborn
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import de.upb.soot.namespaces.FileType;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.junit.experimental.categories.Category;

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
  public void eqPackageSignature() {
    SignatureFactory signatureFactory = new DefaultSignatureFactory();
    PackageSignature packageSignature1 = signatureFactory.getPackageSignature("java.lang");
    PackageSignature packageSignature2 = signatureFactory.getPackageSignature("java.lang");
    boolean sameObject = packageSignature1.equals(packageSignature2);
    assertTrue(sameObject);
  }

  @Test
  public void eqPackageSignature2() {
    SignatureFactory signatureFactory = new DefaultSignatureFactory();
    PackageSignature packageSignature1 = signatureFactory.getPackageSignature("java.lang");
    PackageSignature packageSignature2 = signatureFactory.getPackageSignature("java.lang");
    boolean sameObject = packageSignature1.equals(null);
    assertFalse(sameObject);
  }

  @Test
  public void eqPackageSignature3() {
    SignatureFactory signatureFactory = new DefaultSignatureFactory();
    JavaClassType classSignature1 = signatureFactory.getClassType("System", "java.lang");
    JavaClassType classSignature2 = signatureFactory.getClassType("System", "java.lang");
    PackageSignature packageSignature1 = classSignature1.getPackageSignature();
    PackageSignature packageSignature2 = classSignature2.getPackageSignature();
    boolean sameObject = packageSignature1.equals(packageSignature2);
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
    JavaClassType classSignature1 = signatureFactory.getClassType("System", "java.lang");
    JavaClassType classSignature2 = signatureFactory.getClassType("System", "java.lang");
    // Class Signatures are unique but not their package
    boolean sameObject = classSignature1 == classSignature2;
    assertFalse(sameObject);
  }

  @Test
  public void getCompareClassSignature2() {
    SignatureFactory signatureFactory = new DefaultSignatureFactory();
    JavaClassType classSignature1 = signatureFactory.getClassType("System", "java.lang");
    JavaClassType classSignature2 = signatureFactory.getClassType("System", "java.lang");
    // Class Signatures are unique but not their package
    assertEquals(classSignature1, classSignature2);
  }

  @Test
  public void getCompareClassSignature3() {
    SignatureFactory signatureFactory = new DefaultSignatureFactory();
    JavaClassType classSignature1 = signatureFactory.getClassType("System", "java.lang");
    // Class Signatures are unique but not their package
    boolean sameObject = classSignature1.equals(classSignature1);
    assertTrue(sameObject);
  }

  @Test
  public void getCompareClassSignature() {
    SignatureFactory signatureFactory = new DefaultSignatureFactory();
    JavaClassType classSignature1 = signatureFactory.getClassType("System", "java.lang");
    JavaClassType classSignature2 = null;
    // Class Signatures are unique but not their package
    boolean sameObject = classSignature1.equals(classSignature2);
    assertFalse(sameObject);
  }

  @Test
  public void sigToPath() {
    SignatureFactory signatureFactory = new DefaultSignatureFactory();
    JavaClassType classSignature1 = signatureFactory.getClassType("System", "java.lang");
    Path path = classSignature1.toPath(FileType.CLASS);
    // Class Signatures are unique but not their package
    assertEquals(
        path.toString(), "java" + File.separator + "lang" + File.separator + "System.class");
  }

  @Test
  public void sigFromPath() {
    SignatureFactory signatureFactory = new DefaultSignatureFactory();
    Path p = Paths.get("java/lang/System.class");
    JavaClassType classSignature = signatureFactory.fromPath(p);
    assertEquals(classSignature.toString(), "java.lang.System");
  }

  @Test
  public void sigFromPathStartsWithSlash() {
    SignatureFactory signatureFactory = new DefaultSignatureFactory();
    Path p = Paths.get("/java/lang/System.class");
    JavaClassType classSignature = signatureFactory.fromPath(p);
    assertEquals(classSignature.toString(), "java.lang.System");
  }

  @Test
  public void getClassSignatureEmptyPackage() {
    SignatureFactory signatureFactory = new DefaultSignatureFactory();
    JavaClassType classSignature1 = signatureFactory.getClassType("A", "");
    JavaClassType classSignature2 = signatureFactory.getClassType("A");
    // Class Signatures are unique but not their package
    boolean sameObject = classSignature1 == classSignature2;
    assertFalse(sameObject);

    boolean samePackageSignatureObject =
        classSignature1.getPackageSignature() == classSignature2.getPackageSignature();
    assertTrue(samePackageSignatureObject);
    String className = "A";

    assertEquals(classSignature1.toString(), className);
    assertEquals(classSignature2.toString(), className);
  }

  @Test
  public void getClassSignatureFullyQualified() {
    SignatureFactory signatureFactory = new DefaultSignatureFactory();
    JavaClassType classSignature1 = signatureFactory.getClassType("java.lang.System");
    JavaClassType classSignature2 = signatureFactory.getClassType("System", "java.lang");
    // Class Signatures are unique but not their package
    boolean sameObject = classSignature1 == classSignature2;
    assertFalse(sameObject);
  }

  @Test
  public void getInnerClassSignature() {
    SignatureFactory signatureFactory = new DefaultSignatureFactory();
    JavaClassType classSignature1 = signatureFactory.getClassType("java.lang.System$MyClass");
    JavaClassType classSignature2 = signatureFactory.getClassType("System$MyClass", "java.lang");
    // Class Signatures are unique but not their package
    boolean sameObject = classSignature1 == classSignature2;
    assertFalse(sameObject);
    assertEquals(classSignature1, classSignature2);
    assertTrue(classSignature1.isInnerClass());
    assertTrue(classSignature2.isInnerClass());
  }

  @Test
  public void getClassSignaturesPackage() {
    SignatureFactory signatureFactory = new DefaultSignatureFactory();
    JavaClassType classSignature1 = signatureFactory.getClassType("System", "java.lang");
    JavaClassType classSignature2 = signatureFactory.getClassType("System", "java.lang");
    // Class Signatures are unique but not their package
    boolean samePackageSignature =
        classSignature1.getPackageSignature() == classSignature2.getPackageSignature();
    assertTrue(samePackageSignature);

    // but they are equal
    assertEquals(classSignature1, classSignature2);
    assertEquals(classSignature1.hashCode(), classSignature2.hashCode());
  }

  @Test
  public void getMethodSignature() {
    SignatureFactory signatureFactory = new DefaultSignatureFactory();
    JavaClassType declClass = signatureFactory.getClassType("System", "java.lang");
    JavaClassType parameter = signatureFactory.getClassType("java.lang.Class");
    JavaClassType returnType = signatureFactory.getClassType("java.lang.A");

    List<String> parameters = Collections.singletonList("java.lang.Class");

    MethodSignature methodSignature =
        signatureFactory.getMethodSignature("foo", "java.lang.System", "java.lang.A", parameters);
    assertEquals(declClass, methodSignature.getDeclClassSignature());
    assertEquals(returnType, methodSignature.getSignature());
    assertEquals(parameter, methodSignature.getParameterSignatures().get(0));
  }

  @Test
  public void getMethodSignatureString() {
    SignatureFactory signatureFactory = new DefaultSignatureFactory();

    List<String> parameters = Collections.singletonList("java.lang.Class");

    MethodSignature methodSignature =
        signatureFactory.getMethodSignature("foo", "java.lang.System", "java.lang.A", parameters);
    assertEquals(
        "<java.lang.System: java.lang.A foo(java.lang.Class)>", methodSignature.toString());
  }

  @Test
  public void getMethodSignatureString2() {
    SignatureFactory signatureFactory = new DefaultSignatureFactory();

    List<String> parameters = Collections.singletonList("java.lang.Class");

    MethodSignature methodSignature =
        signatureFactory.getMethodSignature("foo", "java.lang.System", "void", parameters);
    assertEquals("<java.lang.System: void foo(java.lang.Class)>", methodSignature.toString());
  }

  @Test
  public void getMethodSignatureString3() {
    SignatureFactory signatureFactory = new DefaultSignatureFactory();

    List<String> parameters = Collections.emptyList();

    MethodSignature methodSignature =
        signatureFactory.getMethodSignature("foo", "java.lang.System", "void", parameters);
    assertEquals("<java.lang.System: void foo()>", methodSignature.toString());
  }

  @Test
  public void getMethodSignatureString4() {
    SignatureFactory signatureFactory = new DefaultSignatureFactory();

    List<String> parameters = Collections.emptyList();
    JavaClassType classSignature = signatureFactory.getClassType("java.lang.System");
    MethodSignature methodSignature =
        signatureFactory.getMethodSignature("foo", classSignature, "void", parameters);
    assertEquals("<java.lang.System: void foo()>", methodSignature.toString());
    assertSame(methodSignature.getDeclClassSignature(), classSignature);
  }

  @Test
  public void getFieldSignature() {
    SignatureFactory signatureFactory = new DefaultSignatureFactory();
    JavaClassType classSignature = signatureFactory.getClassType("java.lang.System");
    FieldSignature fieldSignature =
        signatureFactory.getFieldSignature("foo", classSignature, "int");
    assertEquals("<java.lang.System: int foo>" + "", fieldSignature.toString());
  }

  @Test
  public void compMethodSignature2() {
    SignatureFactory signatureFactory = new DefaultSignatureFactory();

    List<String> parameters = new ArrayList<>();

    MethodSignature methodSignature =
        signatureFactory.getMethodSignature("foo", "java.lang.System", "void", parameters);
    parameters.add("boolean");
    MethodSignature methodSignature2 =
        signatureFactory.getMethodSignature("foo", "java.lang.System", "void", parameters);

    assertNotEquals(methodSignature, methodSignature2);
    assertNotEquals(methodSignature.hashCode(), methodSignature2.hashCode());
  }

  @Test
  public void compMethodSignature1() {
    SignatureFactory signatureFactory = new DefaultSignatureFactory();

    List<String> parameters = Collections.emptyList();

    MethodSignature methodSignature =
        signatureFactory.getMethodSignature("foo", "java.lang.System", "void", parameters);
    MethodSignature methodSignature2 =
        signatureFactory.getMethodSignature("foo", "java.lang.System", "void", parameters);

    assertEquals(methodSignature, methodSignature2);
    assertEquals(methodSignature.hashCode(), methodSignature2.hashCode());
  }

  @Test
  public void getTypeSignature() {
    SignatureFactory signatureFactory = new DefaultSignatureFactory();
    JavaClassType classSignature1 = signatureFactory.getClassType("System", "java.lang");
    Type classSignature2 = signatureFactory.getType("java.lang.System");
    assertEquals(classSignature1, classSignature2);
  }

  @Test
  public void getTypeSignatureTypes() {
    SignatureFactory signatureFactory = new DefaultSignatureFactory();

    Type byteSig = signatureFactory.getType("byte");
    assertSame(byteSig, PrimitiveType.getByteSignature());
    assertSame("byte", byteSig.toString());

    Type shortSig = signatureFactory.getType("SHORT");
    assertSame(shortSig, PrimitiveType.getShort());
    assertSame("short", shortSig.toString());

    Type intSig = signatureFactory.getType("int");
    assertSame(intSig, PrimitiveType.getInt());
    assertSame("int", intSig.toString());

    Type longSig = signatureFactory.getType("loNg");
    assertSame(longSig, PrimitiveType.getLong());
    assertSame("long", longSig.toString());

    Type floatSig = signatureFactory.getType("floAt");
    assertSame(floatSig, PrimitiveType.getFloat());
    assertSame("float", floatSig.toString());

    Type doubleSig = signatureFactory.getType("doUble");
    assertSame(doubleSig, PrimitiveType.getDouble());
    assertSame("double", doubleSig.toString());

    Type charSig = signatureFactory.getType("chaR");
    assertSame(charSig, PrimitiveType.getChar());
    assertSame("char", charSig.toString());

    Type boolSig = signatureFactory.getType("boolean");
    assertSame(boolSig, PrimitiveType.getBoolean());
    assertSame("boolean", boolSig.toString());

    Type nullSig = signatureFactory.getType("nuLl");
    assertSame(nullSig, NullType.getInstance());
    assertSame("null", nullSig.toString());

    Type voidSig = signatureFactory.getType("void");
    assertSame(voidSig, VoidType.getInstance());
    assertSame("void", voidSig.toString());
  }

  @Test(expected = IllegalArgumentException.class)
  public void getTypeSignatureArray() {
    SignatureFactory signatureFactory = new DefaultSignatureFactory();
    Type classSignature2 = signatureFactory.getType("java.lang.System[[]");
  }

  @Test
  public void getTypeSignatureArray2() {
    SignatureFactory signatureFactory = new DefaultSignatureFactory();
    Type base = signatureFactory.getType("int");

    Type classSignature2 = signatureFactory.getType("int[]");
    assertTrue(classSignature2 instanceof ArrayType);
    assertEquals(((ArrayType) classSignature2).getDimension(), 1);
    assertEquals(((ArrayType) classSignature2).getBaseType(), base);
  }

  @Test
  public void getTypeSignatureArray3() {
    SignatureFactory signatureFactory = new DefaultSignatureFactory();
    Type base = signatureFactory.getType("int");

    Type classSignature2 = signatureFactory.getType("int[][][][][]");
    assertTrue(classSignature2 instanceof ArrayType);
    assertEquals(((ArrayType) classSignature2).getDimension(), 5);
    assertEquals(((ArrayType) classSignature2).getBaseType(), base);
  }

  @Test
  public void getTypeSignatureArray4() {
    SignatureFactory signatureFactory = new DefaultSignatureFactory();
    Type base = signatureFactory.getType("java.lang.Fantasy");

    Type classSignature2 = signatureFactory.getType("java.lang.Fantasy[]");
    assertTrue(classSignature2 instanceof ArrayType);
    assertEquals(((ArrayType) classSignature2).getDimension(), 1);
    assertEquals(((ArrayType) classSignature2).getBaseType(), base);
  }

  @Test(expected = NullPointerException.class)
  public void checkNullPackage() {
    SignatureFactory signatureFactory = new DefaultSignatureFactory();
    PackageSignature packageSignature = signatureFactory.getPackageSignature(null);
  }

  @Test(expected = NullPointerException.class)
  public void checkNullPackage2() {
    SignatureFactory signatureFactory = new DefaultSignatureFactory();
    JavaClassType classSignature = signatureFactory.getClassType("A", null);
  }
}
