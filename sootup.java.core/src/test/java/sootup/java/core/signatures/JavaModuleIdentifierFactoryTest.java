package sootup.java.core.signatures;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import sootup.core.signatures.FieldSignature;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.JavaModuleIdentifierFactory;
import sootup.java.core.types.ModuleJavaClassType;

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

public class JavaModuleIdentifierFactoryTest extends JavaIdentifierFactoryTest {

  @Test
  public void getPackageSignatureUnnamedModule() {
    JavaModuleIdentifierFactory identifierFactory = new JavaModuleIdentifierFactory();
    ModulePackageName packageName1 = identifierFactory.getPackageName("java.lang");
    assertSame(packageName1.getModuleSignature(), ModuleSignature.UNNAMED_MODULE);
  }

  @Test
  public void getPackageSignatureNamedModule() {
    JavaModuleIdentifierFactory identifierFactory = new JavaModuleIdentifierFactory();
    ModulePackageName packageName1 = identifierFactory.getPackageName("java.lang", "myModule");
    assertNotSame(packageName1.getModuleSignature(), ModuleSignature.UNNAMED_MODULE);
    assertEquals(packageName1.getModuleSignature().toString(), "myModule");
  }

  @Test
  public void getModulePackageSignature() {
    JavaModuleIdentifierFactory identifierFactory = new JavaModuleIdentifierFactory();
    ModulePackageName packageSignature1 = identifierFactory.getPackageName("java.lang", "myModule");
    ModulePackageName packageSignature2 =
        identifierFactory.getPackageName("java.lang.invoke", "myModule");
    assertNotSame(packageSignature1, packageSignature2);
  }

  @Test
  public void getModulePackageSignatureSameModule() {
    JavaModuleIdentifierFactory identifierFactory = new JavaModuleIdentifierFactory();
    ModulePackageName packageSignature1 = identifierFactory.getPackageName("java.lang", "myModule");
    ModulePackageName packageSignature2 = identifierFactory.getPackageName("java.lang", "myModule");

    assertSame(packageSignature1, packageSignature2);
    assertSame(packageSignature1.getModuleSignature(), packageSignature2.getModuleSignature());
  }

  @Test
  public void getModulePackageSignatureDiffModule() {
    JavaModuleIdentifierFactory identifierFactory = new JavaModuleIdentifierFactory();
    ModulePackageName packageSignature1 =
        identifierFactory.getPackageName("java.lang", "myModule1");
    ModulePackageName packageSignature2 =
        identifierFactory.getPackageName("java.lang", "myModule2");

    assertNotSame(packageSignature1, packageSignature2);
    assertNotSame(packageSignature1.getModuleSignature(), packageSignature2.getModuleSignature());

    assertSame(packageSignature1.getName(), packageSignature2.getName());
    assertNotEquals(packageSignature1, packageSignature2);
  }

  @Test
  public void getSameClassTypeSameModule() {
    JavaModuleIdentifierFactory identifierFactory = new JavaModuleIdentifierFactory();
    ModuleJavaClassType classType1 =
        identifierFactory.getClassType("Strawberry", "fruit.red", "food.fruit");
    ModuleJavaClassType classType2 =
        identifierFactory.getClassType("Strawberry", "fruit.red", "food.fruit");
    assertSame(classType1, classType2);
  }

  @Test
  public void getDiffClassTypeDiffModule() {
    JavaModuleIdentifierFactory identifierFactory = new JavaModuleIdentifierFactory();
    ModuleJavaClassType classType1 =
        identifierFactory.getClassType("Strawberry", "fruit.red", "food.fruit");
    ModuleJavaClassType classType2 =
        identifierFactory.getClassType("Strawberry", "fruit.red", "food.other");
    assertNotSame(classType1, classType2);
    assertNotEquals(classType1, classType2);
  }

  @Test
  public void testModuleInfoSignature() {
    JavaModuleIdentifierFactory typeFactory = new JavaModuleIdentifierFactory();
    assertThrows(IllegalArgumentException.class, () -> typeFactory.getClassType("module-info"));
  }

  @Test
  public void compModuleSignature() {
    ModuleSignature signature = JavaModuleIdentifierFactory.getModuleSignature("java.base");
    ModuleSignature signature2 = JavaModuleIdentifierFactory.getModuleSignature("java.base");
    assertEquals(signature, signature2);
    assertEquals(signature.hashCode(), signature2.hashCode());
    assertEquals(signature.toString(), "java.base");
  }

  @Test
  public void compModuleSignature2() {
    ModuleSignature signature = JavaModuleIdentifierFactory.getModuleSignature("java.base");
    ModuleSignature signature2 = JavaModuleIdentifierFactory.getModuleSignature("javafx.base");
    assertNotEquals(signature, signature2);
    assertNotEquals(signature.hashCode(), signature2.hashCode());
    assertEquals(signature2.toString(), "javafx.base");
    assertNotEquals(signature2.toString(), signature.toString());
  }

  @Test
  public void parseMethodnFieldSig() {
    String methodSignatureString = "<java.base/java.lang.String: boolean startsWith(String)>";
    MethodSignature methodSignature =
        new JavaModuleIdentifierFactory().parseMethodSignature(methodSignatureString);
    assertEquals(methodSignatureString, methodSignature.toString());

    String fieldsSigStr = "<java.base/java.lang.String: char[] value>";
    FieldSignature fieldSignature =
        new JavaModuleIdentifierFactory().parseFieldSignature(fieldsSigStr);
    assertEquals(fieldsSigStr, fieldSignature.toString());
  }

  @Test
  public void wrapper_test() {

    ModuleSignature baseSig = JavaModuleIdentifierFactory.getModuleSignature("java.base");
    JavaModuleIdentifierFactory wrapper = new JavaModuleIdentifierFactory().forModule(baseSig);
    assertEquals(
        "java.base/fruit.red.Strawberry", wrapper.getClassType("fruit.red.Strawberry").toString());
    assertEquals(
        "food.fruit/fruit.red.Apple",
        wrapper.getClassType("food.fruit/fruit.red.Apple").toString());

    {
      // not the "real" sig from java.lang.String
      String methodSignatureString = "<java.base/java.lang.String: boolean startsWith(String)>";
      MethodSignature methodSignature = wrapper.parseMethodSignature(methodSignatureString);
      assertEquals(
          "<java.base/java.lang.String: boolean startsWith(java.base/String)>",
          methodSignature.toString());
    }

    {
      // w/O moduleSig
      String methodSignatureString = "<java.lang.String: boolean startsWith(java.lang.String)>";
      MethodSignature methodSignature = wrapper.parseMethodSignature(methodSignatureString);
      assertEquals(
          "<java.base/java.lang.String: boolean startsWith(java.base/java.lang.String)>",
          methodSignature.toString());
    }

    {
      // full
      String methodSignatureString =
          "<java.base/java.lang.String: boolean startsWith(java.base/java.lang.String)>";
      MethodSignature methodSignature = wrapper.parseMethodSignature(methodSignatureString);
      assertEquals(
          "<java.base/java.lang.String: boolean startsWith(java.base/java.lang.String)>",
          methodSignature.toString());
    }

    {
      // unnamed module w package
      String methodSignatureString =
          "<java.base/java.lang.String: boolean startsWith(/java.lang.String)>";
      MethodSignature methodSignature = wrapper.parseMethodSignature(methodSignatureString);
      assertEquals(
          "<java.base/java.lang.String: boolean startsWith(java.lang.String)>",
          methodSignature.toString());
    }

    {
      // unnamed module w/o packag
      String methodSignatureString = "<java.base/java.lang.String: boolean startsWith(/String)>";
      MethodSignature methodSignature = wrapper.parseMethodSignature(methodSignatureString);
      assertEquals(
          "<java.base/java.lang.String: boolean startsWith(String)>", methodSignature.toString());
    }
  }

  @Test
  public void isMainMethod() {
    JavaModuleIdentifierFactory identifierFactory = new JavaModuleIdentifierFactory();
    MethodSignature mainMethodSig =
        identifierFactory.parseMethodSignature(
            "<modmain/pkgmain.Main: void main(java.lang.String[])>");
    assertTrue(identifierFactory.isMainSubSignature(mainMethodSig.getSubSignature()));
    // After Java9, we could have the following
    MethodSignature mainMethodSigJ9 =
        identifierFactory.parseMethodSignature(
            "<modmain/pkgmain.Main: void main(java.base/java.lang.String[])>");
    assertTrue(identifierFactory.isMainSubSignature(mainMethodSigJ9.getSubSignature()));
  }

  @Test
  public void modulePackageNamesWithAmbiguousSplitAreDistinct() {
    // module "a" / package "b.c" and module "a.b" / package "c" would collide under a cache keyed
    // on moduleName + "." + packageName.
    JavaModuleIdentifierFactory identifierFactory = new JavaModuleIdentifierFactory();
    ModulePackageName first = identifierFactory.getPackageName("b.c", "a");
    ModulePackageName second = identifierFactory.getPackageName("c", "a.b");
    assertNotSame(first, second);
    assertNotEquals(first, second);
    assertEquals("b.c", first.getName());
    assertEquals("c", second.getName());
    assertEquals("a", first.getModuleSignature().getModuleName());
    assertEquals("a.b", second.getModuleSignature().getModuleName());
  }

  @Test
  public void classTypeIsSharedAcrossModuleFactoryInstances() {
    // every per-module wrapper used to keep its own caches, so the same class requested through
    // two of them came back as two instances
    ModuleSignature module = JavaModuleIdentifierFactory.getModuleSignature("myModule");
    ModuleJavaClassType viaWrapper =
        new JavaModuleIdentifierFactory().forModule(module).getClassType("some.Klass");
    ModuleJavaClassType viaBaseFactory =
        new JavaModuleIdentifierFactory().getClassType("Klass", "some", module);
    assertSame(viaWrapper, viaBaseFactory);

    ModuleJavaClassType viaOtherWrapper =
        new JavaModuleIdentifierFactory()
            .forModule("otherModule")
            .getClassType("myModule/some.Klass");
    assertSame(viaWrapper, viaOtherWrapper);
  }

  @Test
  public void moduleClassTypeIsNotConflatedWithPlainClassType() {
    ClassType plain = new JavaIdentifierFactory().getClassType("some.Klass");
    ModuleJavaClassType moduleScoped = new JavaModuleIdentifierFactory().getClassType("some.Klass");
    assertNotSame(plain, moduleScoped);
    assertNotEquals(plain, moduleScoped);
  }

  @Test
  public void moduleSignatureIsHashConsed() {
    assertSame(
        JavaModuleIdentifierFactory.getModuleSignature("myModule"),
        JavaModuleIdentifierFactory.getModuleSignature("myModule"));
    assertSame(ModuleSignature.UNNAMED_MODULE, JavaModuleIdentifierFactory.getModuleSignature(""));
  }
}
