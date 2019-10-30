package de.upb.swt.soot.test.core.signatures;

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

import static org.junit.Assert.*;

import categories.Java8Test;
import de.upb.swt.soot.core.IdentifierFactory;
import de.upb.swt.soot.core.inputlocation.FileType;
import de.upb.swt.soot.core.signatures.FieldSignature;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.signatures.PackageName;
import de.upb.swt.soot.core.types.*;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import de.upb.swt.soot.java.core.types.JavaClassType;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(Java8Test.class)
public class IdentifierFactoryTest {

  @Test
  public void getSamePackageSignature() {
    IdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();
    PackageName packageName1 = identifierFactory.getPackageName("java.lang");
    PackageName packageName2 = identifierFactory.getPackageName("java.lang");
    boolean sameObject = packageName1 == packageName2;
    assertTrue(sameObject);
  }

  @Test
  public void eqPackageSignature() {
    IdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();
    PackageName packageName1 = identifierFactory.getPackageName("java.lang");
    PackageName packageName2 = identifierFactory.getPackageName("java.lang");
    boolean sameObject = packageName1.equals(packageName2);
    assertTrue(sameObject);
  }

  @Test
  public void eqPackageSignature2() {
    IdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();
    PackageName packageName1 = identifierFactory.getPackageName("java.lang");
    PackageName packageName2 = identifierFactory.getPackageName("java.lang");
    boolean sameObject = packageName1.equals(null);
    assertFalse(sameObject);
  }

  @Test
  public void eqPackageSignature3() {
    JavaIdentifierFactory typeFactory = JavaIdentifierFactory.getInstance();
    JavaClassType classSignature1 = typeFactory.getClassType("System", "java.lang");
    JavaClassType classSignature2 = typeFactory.getClassType("System", "java.lang");
    PackageName packageName1 = classSignature1.getPackageName();
    PackageName packageName2 = classSignature2.getPackageName();
    boolean sameObject = packageName1.equals(packageName2);
    assertTrue(sameObject);
  }

  @Test
  public void getDiffPackageSignature() {
    IdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();
    PackageName packageName1 = identifierFactory.getPackageName("java.lang");
    PackageName packageName2 = identifierFactory.getPackageName("java.lang.invoke");
    boolean sameObject = packageName1 == packageName2;
    assertFalse(sameObject);
  }

  @Test
  public void getClassSignature() {
    JavaIdentifierFactory typeFactory = JavaIdentifierFactory.getInstance();
    ClassType classSignature1 = typeFactory.getClassType("System", "java.lang");
    ClassType classSignature2 = typeFactory.getClassType("System", "java.lang");
    // Class Signatures are unique but not their package
    boolean sameObject = classSignature1 == classSignature2;
    assertFalse(sameObject);
  }

  @Test
  public void getCompareClassSignature2() {
    JavaIdentifierFactory typeFactory = JavaIdentifierFactory.getInstance();
    ClassType classSignature1 = typeFactory.getClassType("System", "java.lang");
    ClassType classSignature2 = typeFactory.getClassType("System", "java.lang");
    // Class Signatures are unique but not their package
    assertEquals(classSignature1, classSignature2);
  }

  @Test
  public void getCompareClassSignature3() {
    JavaIdentifierFactory typeFactory = JavaIdentifierFactory.getInstance();
    ClassType classSignature1 = typeFactory.getClassType("System", "java.lang");
    // Class Signatures are unique but not their package
    boolean sameObject = classSignature1.equals(classSignature1);
    assertTrue(sameObject);
  }

  @Test
  public void getCompareClassSignature() {
    JavaIdentifierFactory typeFactory = JavaIdentifierFactory.getInstance();
    ClassType classSignature1 = typeFactory.getClassType("System", "java.lang");
    ClassType classSignature2 = null;
    // Class Signatures are unique but not their package
    boolean sameObject = classSignature1.equals(classSignature2);
    assertFalse(sameObject);
  }

  @Test
  public void sigToPath() {
    JavaIdentifierFactory typeFactory = JavaIdentifierFactory.getInstance();
    JavaClassType classSignature1 = typeFactory.getClassType("System", "java.lang");
    Path path = classSignature1.toPath(FileType.CLASS);
    // Class Signatures are unique but not their package
    assertEquals(
        path.toString(), "java" + File.separator + "lang" + File.separator + "System.class");
  }

  @Test
  public void sigFromPath() {
    JavaIdentifierFactory typeFactory = JavaIdentifierFactory.getInstance();
    Path p = Paths.get("java/lang/System.class");
    ClassType classSignature = typeFactory.fromPath(p);
    assertEquals(classSignature.toString(), "java.lang.System");
  }

  @Test
  public void sigFromPathStartsWithSlash() {
    JavaIdentifierFactory typeFactory = JavaIdentifierFactory.getInstance();
    Path p = Paths.get("/java/lang/System.class");
    ClassType classSignature = typeFactory.fromPath(p);
    assertEquals(classSignature.toString(), "java.lang.System");
  }

  @Test
  public void getClassSignatureEmptyPackage() {
    JavaIdentifierFactory typeFactory = JavaIdentifierFactory.getInstance();
    JavaClassType classSignature1 = typeFactory.getClassType("A", "");
    JavaClassType classSignature2 = typeFactory.getClassType("A");
    // Class Signatures are unique but not their package
    boolean sameObject = classSignature1 == classSignature2;
    assertFalse(sameObject);

    boolean samePackageSignatureObject =
        classSignature1.getPackageName() == classSignature2.getPackageName();
    assertTrue(samePackageSignatureObject);
    String className = "A";

    assertEquals(classSignature1.toString(), className);
    assertEquals(classSignature2.toString(), className);
  }

  @Test
  public void getClassSignatureFullyQualified() {
    JavaIdentifierFactory typeFactory = JavaIdentifierFactory.getInstance();
    ClassType classSignature1 = typeFactory.getClassType("java.lang.System");
    ClassType classSignature2 = typeFactory.getClassType("System", "java.lang");
    // Class Signatures are unique but not their package
    boolean sameObject = classSignature1 == classSignature2;
    assertFalse(sameObject);
  }

  @Test
  public void getInnerClassSignature() {
    JavaIdentifierFactory typeFactory = JavaIdentifierFactory.getInstance();
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
    JavaIdentifierFactory typeFactory = JavaIdentifierFactory.getInstance();
    JavaClassType classSignature1 = typeFactory.getClassType("System", "java.lang");
    JavaClassType classSignature2 = typeFactory.getClassType("System", "java.lang");
    // Class Signatures are unique but not their package
    boolean samePackageSignature =
        classSignature1.getPackageName() == classSignature2.getPackageName();
    assertTrue(samePackageSignature);

    // but they are equal
    assertEquals(classSignature1, classSignature2);
    assertEquals(classSignature1.hashCode(), classSignature2.hashCode());
  }

  @Test
  public void getMethodSignature() {
    IdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();
    JavaIdentifierFactory typeFactory = JavaIdentifierFactory.getInstance();
    ClassType declClass = typeFactory.getClassType("System", "java.lang");
    ClassType parameter = typeFactory.getClassType("java.lang.Class");
    ClassType returnType = typeFactory.getClassType("java.lang.A");

    List<String> parameters = Collections.singletonList("java.lang.Class");

    MethodSignature methodSignature =
        identifierFactory.getMethodSignature("foo", "java.lang.System", "java.lang.A", parameters);
    assertEquals(declClass, methodSignature.getDeclClassType());
    assertEquals(returnType, methodSignature.getType());
    assertEquals(parameter, methodSignature.getParameterSignatures().get(0));
  }

  @Test
  public void getMethodSignatureString() {
    IdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();
    JavaIdentifierFactory typeFactory = JavaIdentifierFactory.getInstance();

    List<String> parameters = Collections.singletonList("java.lang.Class");

    MethodSignature methodSignature =
        identifierFactory.getMethodSignature("foo", "java.lang.System", "java.lang.A", parameters);
    assertEquals(
        "<java.lang.System: java.lang.A foo(java.lang.Class)>", methodSignature.toString());
  }

  @Test
  public void getMethodSignatureString2() {
    IdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();
    JavaIdentifierFactory typeFactory = JavaIdentifierFactory.getInstance();

    List<String> parameters = Collections.singletonList("java.lang.Class");

    MethodSignature methodSignature =
        identifierFactory.getMethodSignature("foo", "java.lang.System", "void", parameters);
    assertEquals("<java.lang.System: void foo(java.lang.Class)>", methodSignature.toString());
  }

  @Test
  public void getMethodSignatureString3() {
    IdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();

    List<String> parameters = Collections.emptyList();

    MethodSignature methodSignature =
        identifierFactory.getMethodSignature("foo", "java.lang.System", "void", parameters);
    assertEquals("<java.lang.System: void foo()>", methodSignature.toString());
  }

  @Test
  public void getMethodSignatureString4() {
    IdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();
    JavaIdentifierFactory typeFactory = JavaIdentifierFactory.getInstance();

    List<String> parameters = Collections.emptyList();
    ClassType classSignature = typeFactory.getClassType("java.lang.System");
    MethodSignature methodSignature =
        identifierFactory.getMethodSignature("foo", classSignature, "void", parameters);
    assertEquals("<java.lang.System: void foo()>", methodSignature.toString());
    assertSame(methodSignature.getDeclClassType(), classSignature);
  }

  @Test
  public void getFieldSignature() {
    IdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();
    JavaIdentifierFactory typeFactory = JavaIdentifierFactory.getInstance();
    ClassType classSignature = typeFactory.getClassType("java.lang.System");
    FieldSignature fieldSignature =
        identifierFactory.getFieldSignature("foo", classSignature, "int");
    assertEquals("<java.lang.System: int foo>" + "", fieldSignature.toString());
  }

  @Test
  public void compMethodSignature2() {
    IdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();
    JavaIdentifierFactory typeFactory = JavaIdentifierFactory.getInstance();

    List<String> parameters = new ArrayList<>();

    MethodSignature methodSignature =
        identifierFactory.getMethodSignature("foo", "java.lang.System", "void", parameters);
    parameters.add("boolean");
    MethodSignature methodSignature2 =
        identifierFactory.getMethodSignature("foo", "java.lang.System", "void", parameters);

    assertNotEquals(methodSignature, methodSignature2);
    assertNotEquals(methodSignature.hashCode(), methodSignature2.hashCode());
  }

  @Test
  public void compMethodSignature1() {
    IdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();
    JavaIdentifierFactory typeFactory = JavaIdentifierFactory.getInstance();

    List<String> parameters = Collections.emptyList();

    MethodSignature methodSignature =
        identifierFactory.getMethodSignature("foo", "java.lang.System", "void", parameters);
    MethodSignature methodSignature2 =
        identifierFactory.getMethodSignature("foo", "java.lang.System", "void", parameters);

    assertEquals(methodSignature, methodSignature2);
    assertEquals(methodSignature.hashCode(), methodSignature2.hashCode());
  }

  @Test
  public void getTypeSignature() {
    JavaIdentifierFactory typeFactory = JavaIdentifierFactory.getInstance();
    ClassType classSignature1 = typeFactory.getClassType("System", "java.lang");
    Type classSignature2 = typeFactory.getType("java.lang.System");
    assertEquals(classSignature1, classSignature2);
  }

  @Test
  public void getTypeSignatureTypes() {
    JavaIdentifierFactory typeFactory = JavaIdentifierFactory.getInstance();

    Type byteSig = typeFactory.getType("byte");
    assertSame(byteSig, PrimitiveType.getByte());
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
    JavaIdentifierFactory typeFactory = JavaIdentifierFactory.getInstance();
    Type classSignature2 = typeFactory.getType("java.lang.System[[]");
  }

  @Test
  public void getTypeSignatureArray2() {
    JavaIdentifierFactory typeFactory = JavaIdentifierFactory.getInstance();
    Type base = typeFactory.getType("int");

    Type classSignature2 = typeFactory.getType("int[]");
    assertTrue(classSignature2 instanceof ArrayType);
    assertEquals(((ArrayType) classSignature2).getDimension(), 1);
    assertEquals(((ArrayType) classSignature2).getBaseType(), base);
  }

  @Test
  public void getTypeSignatureArray3() {
    JavaIdentifierFactory typeFactory = JavaIdentifierFactory.getInstance();
    Type base = typeFactory.getType("int");

    Type classSignature2 = typeFactory.getType("int[][][][][]");
    assertTrue(classSignature2 instanceof ArrayType);
    assertEquals(((ArrayType) classSignature2).getDimension(), 5);
    assertEquals(((ArrayType) classSignature2).getBaseType(), base);
  }

  @Test
  public void getTypeSignatureArray4() {
    JavaIdentifierFactory typeFactory = JavaIdentifierFactory.getInstance();
    Type base = typeFactory.getType("java.lang.Fantasy");

    Type classSignature2 = typeFactory.getType("java.lang.Fantasy[]");
    assertTrue(classSignature2 instanceof ArrayType);
    assertEquals(((ArrayType) classSignature2).getDimension(), 1);
    assertEquals(((ArrayType) classSignature2).getBaseType(), base);
  }

  @Test(expected = NullPointerException.class)
  public void checkNullPackage() {
    IdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();
    JavaIdentifierFactory typeFactory = JavaIdentifierFactory.getInstance();
    PackageName packageName = identifierFactory.getPackageName(null);
  }

  @Test(expected = NullPointerException.class)
  public void checkNullPackage2() {
    JavaIdentifierFactory typeFactory = JavaIdentifierFactory.getInstance();
    ClassType classSignature = typeFactory.getClassType("A", null);
  }
}
