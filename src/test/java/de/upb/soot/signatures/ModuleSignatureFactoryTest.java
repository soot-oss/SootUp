package de.upb.soot.signatures;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import categories.Java9Test;

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
    assertNotSame(((ModulePackageSignature) packageSignature1).moduleSignature, ModuleSignature.UNNAMED_MODULE);
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
    ModuleSignature signature = ModuleSignatureFactory.getModuleSignature("java.base");
    ModuleSignature signature2 = ModuleSignatureFactory.getModuleSignature("java.base");
    assertEquals(signature, signature2);
    assertEquals(signature.hashCode(), signature2.hashCode());
    assertEquals(signature.toString(), "java.base");
  }

  @Test
  public void compModuleSignature2() {
    ModuleSignatureFactory signatureFactory = new ModuleSignatureFactory();
    ModuleSignature signature = ModuleSignatureFactory.getModuleSignature("java.base");
    ModuleSignature signature2 = ModuleSignatureFactory.getModuleSignature("javafx.base");
    assertNotEquals(signature, signature2);
    assertNotEquals(signature.hashCode(), signature2.hashCode());
    assertEquals(signature2.toString(), "javafx.base");
    assertNotEquals(signature2.toString(), signature.toString());

  }

}
