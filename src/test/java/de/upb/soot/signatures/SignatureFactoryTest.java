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
import de.upb.soot.DefaultFactories;
import de.upb.soot.namespaces.FileType;
import de.upb.soot.types.ArrayType;
import de.upb.soot.types.DefaultTypeFactory;
import de.upb.soot.types.JavaClassType;
import de.upb.soot.types.NullType;
import de.upb.soot.types.PrimitiveType;
import de.upb.soot.types.Type;
import de.upb.soot.types.VoidType;
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
    DefaultFactories factories = DefaultFactories.create();
    DefaultSignatureFactory signatureFactory = factories.getSignatureFactory();
    PackageSignature packageSignature1 = signatureFactory.getPackageSignature("java.lang");
    PackageSignature packageSignature2 = signatureFactory.getPackageSignature("java.lang");
    boolean sameObject = packageSignature1 == packageSignature2;
    assertTrue(sameObject);
  }

  @Test
  public void eqPackageSignature() {
    DefaultFactories factories = DefaultFactories.create();
    DefaultSignatureFactory signatureFactory = factories.getSignatureFactory();
    PackageSignature packageSignature1 = signatureFactory.getPackageSignature("java.lang");
    PackageSignature packageSignature2 = signatureFactory.getPackageSignature("java.lang");
    boolean sameObject = packageSignature1.equals(packageSignature2);
    assertTrue(sameObject);
  }

  @Test
  public void eqPackageSignature2() {
    DefaultFactories factories = DefaultFactories.create();
    DefaultSignatureFactory signatureFactory = factories.getSignatureFactory();
    PackageSignature packageSignature1 = signatureFactory.getPackageSignature("java.lang");
    PackageSignature packageSignature2 = signatureFactory.getPackageSignature("java.lang");
    boolean sameObject = packageSignature1.equals(null);
    assertFalse(sameObject);
  }

  @Test
  public void eqPackageSignature3() {
    DefaultFactories factories = DefaultFactories.create();
    DefaultTypeFactory typeFactory = factories.getTypeFactory();
    JavaClassType classSignature1 = typeFactory.getClassType("System", "java.lang");
    JavaClassType classSignature2 = typeFactory.getClassType("System", "java.lang");
    PackageSignature packageSignature1 = classSignature1.getPackageSignature();
    PackageSignature packageSignature2 = classSignature2.getPackageSignature();
    boolean sameObject = packageSignature1.equals(packageSignature2);
    assertTrue(sameObject);
  }

  @Test
  public void getDiffPackageSignature() {
    DefaultFactories factories = DefaultFactories.create();
    DefaultSignatureFactory signatureFactory = factories.getSignatureFactory();
    PackageSignature packageSignature1 = signatureFactory.getPackageSignature("java.lang");
    PackageSignature packageSignature2 = signatureFactory.getPackageSignature("java.lang.invoke");
    boolean sameObject = packageSignature1 == packageSignature2;
    assertFalse(sameObject);
  }

  @Test
  public void getClassSignature() {
    DefaultFactories factories = DefaultFactories.create();
    DefaultTypeFactory typeFactory = factories.getTypeFactory();
    JavaClassType classSignature1 = typeFactory.getClassType("System", "java.lang");
    JavaClassType classSignature2 = typeFactory.getClassType("System", "java.lang");
    // Class Signatures are unique but not their package
    boolean sameObject = classSignature1 == classSignature2;
    assertFalse(sameObject);
  }

  @Test
  public void getCompareClassSignature2() {
    DefaultFactories factories = DefaultFactories.create();
    DefaultTypeFactory typeFactory = factories.getTypeFactory();
    JavaClassType classSignature1 = typeFactory.getClassType("System", "java.lang");
    JavaClassType classSignature2 = typeFactory.getClassType("System", "java.lang");
    // Class Signatures are unique but not their package
    assertEquals(classSignature1, classSignature2);
  }

  @Test
  public void getCompareClassSignature3() {
    DefaultFactories factories = DefaultFactories.create();
    DefaultTypeFactory typeFactory = factories.getTypeFactory();
    JavaClassType classSignature1 = typeFactory.getClassType("System", "java.lang");
    // Class Signatures are unique but not their package
    boolean sameObject = classSignature1.equals(classSignature1);
    assertTrue(sameObject);
  }

  @Test
  public void getCompareClassSignature() {
    DefaultFactories factories = DefaultFactories.create();
    DefaultTypeFactory typeFactory = factories.getTypeFactory();
    JavaClassType classSignature1 = typeFactory.getClassType("System", "java.lang");
    JavaClassType classSignature2 = null;
    // Class Signatures are unique but not their package
    boolean sameObject = classSignature1.equals(classSignature2);
    assertFalse(sameObject);
  }

  @Test
  public void sigToPath() {
    DefaultFactories factories = DefaultFactories.create();
    DefaultTypeFactory typeFactory = factories.getTypeFactory();
    JavaClassType classSignature1 = typeFactory.getClassType("System", "java.lang");
    Path path = classSignature1.toPath(FileType.CLASS);
    // Class Signatures are unique but not their package
    assertEquals(
        path.toString(), "java" + File.separator + "lang" + File.separator + "System.class");
  }

  @Test
  public void sigFromPath() {
    DefaultFactories factories = DefaultFactories.create();
    DefaultTypeFactory typeFactory = factories.getTypeFactory();
    Path p = Paths.get("java/lang/System.class");
    JavaClassType classSignature = typeFactory.fromPath(p);
    assertEquals(classSignature.toString(), "java.lang.System");
  }

  @Test
  public void sigFromPathStartsWithSlash() {
    DefaultFactories factories = DefaultFactories.create();
    DefaultTypeFactory typeFactory = factories.getTypeFactory();
    Path p = Paths.get("/java/lang/System.class");
    JavaClassType classSignature = typeFactory.fromPath(p);
    assertEquals(classSignature.toString(), "java.lang.System");
  }

  @Test
  public void getClassSignatureEmptyPackage() {
    DefaultFactories factories = DefaultFactories.create();
    DefaultTypeFactory typeFactory = factories.getTypeFactory();
    JavaClassType classSignature1 = typeFactory.getClassType("A", "");
    JavaClassType classSignature2 = typeFactory.getClassType("A");
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
    DefaultFactories factories = DefaultFactories.create();
    DefaultTypeFactory typeFactory = factories.getTypeFactory();
    JavaClassType classSignature1 = typeFactory.getClassType("java.lang.System");
    JavaClassType classSignature2 = typeFactory.getClassType("System", "java.lang");
    // Class Signatures are unique but not their package
    boolean sameObject = classSignature1 == classSignature2;
    assertFalse(sameObject);
  }

  @Test
  public void getInnerClassSignature() {
    DefaultFactories factories = DefaultFactories.create();
    DefaultTypeFactory typeFactory = factories.getTypeFactory();
    JavaClassType classSignature1 = typeFactory.getClassType("java.lang.System$MyClass");
    JavaClassType classSignature2 = typeFactory.getClassType("System$MyClass", "java.lang");
    // Class Signatures are unique but not their package
    boolean sameObject = classSignature1 == classSignature2;
    assertFalse(sameObject);
    assertEquals(classSignature1, classSignature2);
    assertTrue(classSignature1.isInnerClass());
    assertTrue(classSignature2.isInnerClass());
  }

  @Test
  public void getClassSignaturesPackage() {
    DefaultFactories factories = DefaultFactories.create();
    DefaultTypeFactory typeFactory = factories.getTypeFactory();
    JavaClassType classSignature1 = typeFactory.getClassType("System", "java.lang");
    JavaClassType classSignature2 = typeFactory.getClassType("System", "java.lang");
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
    DefaultFactories factories = DefaultFactories.create();
    DefaultSignatureFactory signatureFactory = factories.getSignatureFactory();
    DefaultTypeFactory typeFactory = factories.getTypeFactory();
    JavaClassType declClass = typeFactory.getClassType("System", "java.lang");
    JavaClassType parameter = typeFactory.getClassType("java.lang.Class");
    JavaClassType returnType = typeFactory.getClassType("java.lang.A");

    List<String> parameters = Collections.singletonList("java.lang.Class");

    MethodSignature methodSignature =
        signatureFactory.getMethodSignature("foo", "java.lang.System", "java.lang.A", parameters);
    assertEquals(declClass, methodSignature.getDeclClassSignature());
    assertEquals(returnType, methodSignature.getSignature());
    assertEquals(parameter, methodSignature.getParameterSignatures().get(0));
  }

  @Test
  public void getMethodSignatureString() {
    DefaultFactories factories = DefaultFactories.create();
    DefaultSignatureFactory signatureFactory = factories.getSignatureFactory();
    DefaultTypeFactory typeFactory = factories.getTypeFactory();

    List<String> parameters = Collections.singletonList("java.lang.Class");

    MethodSignature methodSignature =
        signatureFactory.getMethodSignature("foo", "java.lang.System", "java.lang.A", parameters);
    assertEquals(
        "<java.lang.System: java.lang.A foo(java.lang.Class)>", methodSignature.toString());
  }

  @Test
  public void getMethodSignatureString2() {
    DefaultFactories factories = DefaultFactories.create();
    DefaultSignatureFactory signatureFactory = factories.getSignatureFactory();
    DefaultTypeFactory typeFactory = factories.getTypeFactory();

    List<String> parameters = Collections.singletonList("java.lang.Class");

    MethodSignature methodSignature =
        signatureFactory.getMethodSignature("foo", "java.lang.System", "void", parameters);
    assertEquals("<java.lang.System: void foo(java.lang.Class)>", methodSignature.toString());
  }

  @Test
  public void getMethodSignatureString3() {
    DefaultFactories factories = DefaultFactories.create();
    DefaultSignatureFactory signatureFactory = factories.getSignatureFactory();

    List<String> parameters = Collections.emptyList();

    MethodSignature methodSignature =
        signatureFactory.getMethodSignature("foo", "java.lang.System", "void", parameters);
    assertEquals("<java.lang.System: void foo()>", methodSignature.toString());
  }

  @Test
  public void getMethodSignatureString4() {
    DefaultFactories factories = DefaultFactories.create();
    DefaultSignatureFactory signatureFactory = factories.getSignatureFactory();
    DefaultTypeFactory typeFactory = factories.getTypeFactory();

    List<String> parameters = Collections.emptyList();
    JavaClassType classSignature = typeFactory.getClassType("java.lang.System");
    MethodSignature methodSignature =
        signatureFactory.getMethodSignature("foo", classSignature, "void", parameters);
    assertEquals("<java.lang.System: void foo()>", methodSignature.toString());
    assertSame(methodSignature.getDeclClassSignature(), classSignature);
  }

  @Test
  public void getFieldSignature() {
    DefaultFactories factories = DefaultFactories.create();
    DefaultSignatureFactory signatureFactory = factories.getSignatureFactory();
    DefaultTypeFactory typeFactory = factories.getTypeFactory();
    JavaClassType classSignature = typeFactory.getClassType("java.lang.System");
    FieldSignature fieldSignature =
        signatureFactory.getFieldSignature("foo", classSignature, "int");
    assertEquals("<java.lang.System: int foo>" + "", fieldSignature.toString());
  }

  @Test
  public void compMethodSignature2() {
    DefaultFactories factories = DefaultFactories.create();
    DefaultSignatureFactory signatureFactory = factories.getSignatureFactory();
    DefaultTypeFactory typeFactory = factories.getTypeFactory();

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
    DefaultFactories factories = DefaultFactories.create();
    DefaultSignatureFactory signatureFactory = factories.getSignatureFactory();
    DefaultTypeFactory typeFactory = factories.getTypeFactory();

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
    DefaultFactories factories = DefaultFactories.create();
    DefaultTypeFactory typeFactory = factories.getTypeFactory();
    JavaClassType classSignature1 = typeFactory.getClassType("System", "java.lang");
    Type classSignature2 = typeFactory.getType("java.lang.System");
    assertEquals(classSignature1, classSignature2);
  }

  @Test
  public void getTypeSignatureTypes() {
    DefaultFactories factories = DefaultFactories.create();
    DefaultTypeFactory typeFactory = factories.getTypeFactory();

    Type byteSig = typeFactory.getType("byte");
    assertSame(byteSig, PrimitiveType.getByteSignature());
    assertSame("byte", byteSig.toString());

    Type shortSig = typeFactory.getType("SHORT");
    assertSame(shortSig, PrimitiveType.getShort());
    assertSame("short", shortSig.toString());

    Type intSig = typeFactory.getType("int");
    assertSame(intSig, PrimitiveType.getInt());
    assertSame("int", intSig.toString());

    Type longSig = typeFactory.getType("loNg");
    assertSame(longSig, PrimitiveType.getLong());
    assertSame("long", longSig.toString());

    Type floatSig = typeFactory.getType("floAt");
    assertSame(floatSig, PrimitiveType.getFloat());
    assertSame("float", floatSig.toString());

    Type doubleSig = typeFactory.getType("doUble");
    assertSame(doubleSig, PrimitiveType.getDouble());
    assertSame("double", doubleSig.toString());

    Type charSig = typeFactory.getType("chaR");
    assertSame(charSig, PrimitiveType.getChar());
    assertSame("char", charSig.toString());

    Type boolSig = typeFactory.getType("boolean");
    assertSame(boolSig, PrimitiveType.getBoolean());
    assertSame("boolean", boolSig.toString());

    Type nullSig = typeFactory.getType("nuLl");
    assertSame(nullSig, NullType.getInstance());
    assertSame("null", nullSig.toString());

    Type voidSig = typeFactory.getType("void");
    assertSame(voidSig, VoidType.getInstance());
    assertSame("void", voidSig.toString());
  }

  @Test(expected = IllegalArgumentException.class)
  public void getTypeSignatureArray() {
    DefaultFactories factories = DefaultFactories.create();
    DefaultTypeFactory typeFactory = factories.getTypeFactory();
    Type classSignature2 = typeFactory.getType("java.lang.System[[]");
  }

  @Test
  public void getTypeSignatureArray2() {
    DefaultFactories factories = DefaultFactories.create();
    DefaultTypeFactory typeFactory = factories.getTypeFactory();
    Type base = typeFactory.getType("int");

    Type classSignature2 = typeFactory.getType("int[]");
    assertTrue(classSignature2 instanceof ArrayType);
    assertEquals(((ArrayType) classSignature2).getDimension(), 1);
    assertEquals(((ArrayType) classSignature2).getBaseType(), base);
  }

  @Test
  public void getTypeSignatureArray3() {
    DefaultFactories factories = DefaultFactories.create();
    DefaultTypeFactory typeFactory = factories.getTypeFactory();
    Type base = typeFactory.getType("int");

    Type classSignature2 = typeFactory.getType("int[][][][][]");
    assertTrue(classSignature2 instanceof ArrayType);
    assertEquals(((ArrayType) classSignature2).getDimension(), 5);
    assertEquals(((ArrayType) classSignature2).getBaseType(), base);
  }

  @Test
  public void getTypeSignatureArray4() {
    DefaultFactories factories = DefaultFactories.create();
    DefaultTypeFactory typeFactory = factories.getTypeFactory();
    Type base = typeFactory.getType("java.lang.Fantasy");

    Type classSignature2 = typeFactory.getType("java.lang.Fantasy[]");
    assertTrue(classSignature2 instanceof ArrayType);
    assertEquals(((ArrayType) classSignature2).getDimension(), 1);
    assertEquals(((ArrayType) classSignature2).getBaseType(), base);
  }

  @Test(expected = NullPointerException.class)
  public void checkNullPackage() {
    DefaultFactories factories = DefaultFactories.create();
    DefaultSignatureFactory signatureFactory = factories.getSignatureFactory();
    DefaultTypeFactory typeFactory = factories.getTypeFactory();
    PackageSignature packageSignature = signatureFactory.getPackageSignature(null);
  }

  @Test(expected = NullPointerException.class)
  public void checkNullPackage2() {
    DefaultFactories factories = DefaultFactories.create();
    DefaultTypeFactory typeFactory = factories.getTypeFactory();
    JavaClassType classSignature = typeFactory.getClassType("A", null);
  }
}
