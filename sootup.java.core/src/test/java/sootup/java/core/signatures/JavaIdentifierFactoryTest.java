package sootup.java.core.signatures;

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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.IdentifierFactory;
import sootup.core.signatures.FieldSignature;
import sootup.core.signatures.MethodSignature;
import sootup.core.signatures.PackageName;
import sootup.core.types.*;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.JavaModuleIdentifierFactory;
import sootup.java.core.types.JavaClassType;

@Category(Java8Test.class)
public class JavaIdentifierFactoryTest {

  @Test
  public void getSamePackageSignature() {
    IdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();
    PackageName packageName1 = identifierFactory.getPackageName("java.lang");
    PackageName packageName2 = identifierFactory.getPackageName("java.lang");
    assertSame(packageName1, packageName2);
  }

  @Test
  public void eqPackageSignature() {
    IdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();
    PackageName packageName1 = identifierFactory.getPackageName("java.lang");
    PackageName packageName2 = identifierFactory.getPackageName("java.lang");
    assertEquals(packageName1, packageName2);
  }

  @Test
  public void eqPackageSignatureViaClassType() {
    JavaIdentifierFactory typeFactory = JavaIdentifierFactory.getInstance();
    JavaClassType classSignature1 = typeFactory.getClassType("System", "java.lang");
    JavaClassType classSignature2 = typeFactory.getClassType("System", "java.lang");
    PackageName packageName1 = classSignature1.getPackageName();
    PackageName packageName2 = classSignature2.getPackageName();
    assertEquals(packageName1, packageName2);
  }

  @Test
  public void getDiffPackageSignature() {
    IdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();
    PackageName packageName1 = identifierFactory.getPackageName("java.lang");
    PackageName packageName2 = identifierFactory.getPackageName("java.lang.invoke");
    assertNotEquals(packageName1, packageName2);
  }

  @Test
  public void getClassSignature() {
    JavaIdentifierFactory typeFactory = JavaIdentifierFactory.getInstance();
    ClassType classSignature1 = typeFactory.getClassType("System", "java.lang");
    ClassType classSignature2 = typeFactory.getClassType("System", "java.lang");
    // Class Signatures are unique but not their package
    assertSame(classSignature1, classSignature2);
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
    assertEquals(classSignature1, classSignature1);
  }

  @Test
  public void getCompareClassSignature() {
    JavaIdentifierFactory typeFactory = JavaIdentifierFactory.getInstance();
    ClassType classSignature1 = typeFactory.getClassType("System", "java.lang");
    ClassType classSignature2 = null;
    // Class Signatures are unique but not their package
    assertNotEquals(classSignature1, classSignature2);
  }

  @Test
  public void sigFromPath() {
    JavaIdentifierFactory typeFactory = JavaIdentifierFactory.getInstance();
    Path rootDirectory = Paths.get("");
    Path p = Paths.get("java/lang/System.class");
    ClassType classSignature = typeFactory.fromPath(rootDirectory, p);
    assertEquals(classSignature.toString(), "java.lang.System");
  }

  @Test
  public void sigFromPathStartsWithSlash() {
    JavaIdentifierFactory typeFactory = JavaIdentifierFactory.getInstance();
    Path rootDirectory = Paths.get("/");
    Path p = Paths.get("/java/lang/System.class");
    ClassType classSignature = typeFactory.fromPath(rootDirectory, p);
    assertEquals(classSignature.toString(), "java.lang.System");
  }

  @Test
  public void getClassSignatureEmptyPackage() {
    JavaIdentifierFactory typeFactory = JavaIdentifierFactory.getInstance();
    JavaClassType classSignature1 = typeFactory.getClassType("A", "");
    JavaClassType classSignature2 = typeFactory.getClassType("A");
    // Class Signatures are unique but not their package
    assertSame(classSignature1, classSignature2);

    assertSame(classSignature1.getPackageName(), classSignature2.getPackageName());

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
    assertSame(classSignature1, classSignature2);
  }

  @Test
  public void getInnerClassSignature() {
    JavaIdentifierFactory typeFactory = JavaIdentifierFactory.getInstance();
    JavaClassType classSignature1 = typeFactory.getClassType("java.lang.System$MyClass");
    JavaClassType classSignature2 = typeFactory.getClassType("System$MyClass", "java.lang");
    // Class Signatures are unique but not their package
    assertNotSame(classSignature1, classSignature2);
    assertEquals(classSignature1, classSignature2);
  }

  @Test
  public void getClassSignaturesPackage() {
    JavaIdentifierFactory typeFactory = JavaIdentifierFactory.getInstance();
    JavaClassType classSignature1 = typeFactory.getClassType("System", "java.lang");
    JavaClassType classSignature2 = typeFactory.getClassType("System", "java.lang");
    // Class Signatures are unique but not their package
    assertSame(classSignature1.getPackageName(), classSignature2.getPackageName());

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
    assertEquals(parameter, methodSignature.getParameterTypes().get(0));
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

    List<String> parameters = Collections.emptyList();
    ClassType classSignature = identifierFactory.getClassType("java.lang.System");
    MethodSignature methodSignature =
        identifierFactory.getMethodSignature(classSignature, "foo", "void", parameters);
    assertEquals("<java.lang.System: void foo()>", methodSignature.toString());
    assertSame(methodSignature.getDeclClassType(), classSignature);
  }

  @Test
  public void getFieldSignature() {
    IdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();
    ClassType classSignature = identifierFactory.getClassType("java.lang.System");
    FieldSignature fieldSignature =
        identifierFactory.getFieldSignature("foo", classSignature, "int");
    assertEquals("<java.lang.System: int foo>" + "", fieldSignature.toString());
  }

  @Test
  public void compMethodSignature2() {
    IdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();

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
    assertEquals("byte", byteSig.toString());
    assertFalse(byteSig instanceof ClassType);

    Type ByteSig = typeFactory.getType("Byte");
    assertEquals("Byte", ByteSig.toString());
    assertNotEquals(ByteSig, PrimitiveType.getByte());
    assertTrue(ByteSig instanceof ClassType);

    Type shortSig = typeFactory.getType("short");
    assertSame(shortSig, PrimitiveType.getShort());
    assertEquals("short", shortSig.toString());

    Type intSig = typeFactory.getType("int");
    assertSame(intSig, PrimitiveType.getInt());
    assertEquals("int", intSig.toString());

    Type IntSig = typeFactory.getType("Int");
    assertNotEquals(IntSig, PrimitiveType.getInt());
    assertEquals("Int", IntSig.toString());
    assertTrue(IntSig instanceof ClassType);

    Type longSig = typeFactory.getType("long");
    assertSame(longSig, PrimitiveType.getLong());
    assertEquals("long", longSig.toString());

    Type floatSig = typeFactory.getType("float");
    assertSame(floatSig, PrimitiveType.getFloat());
    assertEquals("float", floatSig.toString());

    Type doubleSig = typeFactory.getType("double");
    assertSame(doubleSig, PrimitiveType.getDouble());
    assertEquals("double", doubleSig.toString());

    Type charSig = typeFactory.getType("char");
    assertSame(charSig, PrimitiveType.getChar());
    assertEquals("char", charSig.toString());

    Type boolSig = typeFactory.getType("boolean");
    assertSame(boolSig, PrimitiveType.getBoolean());
    assertEquals("boolean", boolSig.toString());

    Type BoolSig = typeFactory.getType("Boolean");
    assertEquals("Boolean", BoolSig.toString());
    assertNotEquals(BoolSig, PrimitiveType.getBoolean());
    assertTrue(BoolSig instanceof ClassType);

    Type nullSig = typeFactory.getType("null");
    assertSame(nullSig, NullType.getInstance());
    assertEquals("null", nullSig.toString());

    Type voidSig = typeFactory.getType("void");
    assertSame(voidSig, VoidType.getInstance());
    assertEquals("void", voidSig.toString());
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

  @Test(expected = IllegalArgumentException.class)
  public void testParse() {
    // not ok!
    String fieldsSigStr = "<java.base/java.lang.String: [] value>";
    FieldSignature fieldSignature =
        JavaModuleIdentifierFactory.getInstance().parseFieldSignature(fieldsSigStr);
    assertEquals(fieldsSigStr, fieldSignature.toString());
  }
}
