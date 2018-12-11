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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ModuleSignatureFactoryTest extends SignatureFactoryTest {

  @Test
  public void getPackageSignatureUnnamedModule() {
    ModuleSignatureFactory signatureFactory = new ModuleSignatureFactory();
    PackageSignature packageSignature1 = signatureFactory.getPackageSignature("java.lang");
    assertTrue(packageSignature1 instanceof ModulePackageSignature);
    assertSame(((ModulePackageSignature) packageSignature1).moduleSignature, ModuleSignature.UNNAMED_MODULE);
  }

  @Test
  public void getPackageSignatureNamedModule() {
    ModuleSignatureFactory signatureFactory = new ModuleSignatureFactory();
    PackageSignature packageSignature1 = signatureFactory.getPackageSignature("java.lang", "myModule");
    assertTrue(packageSignature1 instanceof ModulePackageSignature);
    assertFalse(((ModulePackageSignature) packageSignature1).moduleSignature == ModuleSignature.UNNAMED_MODULE);
  }

  @Test
  public void getModulePackageSignature() {
    ModuleSignatureFactory signatureFactory = new ModuleSignatureFactory();
    ModulePackageSignature packageSignature1
        = signatureFactory.getPackageSignature("java.lang", "myModule");
    ModulePackageSignature packageSignature2
        = signatureFactory.getPackageSignature("java.lang.invoke", "myModule");
    boolean sameObject = packageSignature1 == packageSignature2;
    assertFalse(sameObject);
  }

  @Test
  public void getModulePackageSignatureSameModule() {
    ModuleSignatureFactory signatureFactory = new ModuleSignatureFactory();
    ModulePackageSignature packageSignature1
        = signatureFactory.getPackageSignature("java.lang", "myModule");
    ModulePackageSignature packageSignature2
        = signatureFactory.getPackageSignature("java.lang", "myModule");

    boolean samePackage = packageSignature1 == packageSignature2;
    assertTrue(samePackage);

    boolean sameModuleObject = packageSignature1.moduleSignature == packageSignature2.moduleSignature;
    assertTrue(sameModuleObject);
  }

  @Test
  public void getModulePackageSignatureDiffModule() {
    ModuleSignatureFactory signatureFactory = new ModuleSignatureFactory();
    ModulePackageSignature packageSignature1
        = signatureFactory.getPackageSignature("java.lang", "myModule1");
    ModulePackageSignature packageSignature2
        = signatureFactory.getPackageSignature("java.lang", "myModule2");
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
    ClassSignature classSignature = signatureFactory.getClassSignature("A", "mypackage", null);
  }
}
