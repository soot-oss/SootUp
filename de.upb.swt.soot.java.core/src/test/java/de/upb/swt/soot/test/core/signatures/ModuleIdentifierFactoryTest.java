package de.upb.swt.soot.test.core.signatures;

import static org.junit.Assert.*;

import categories.Java9Test;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.java.core.ModuleIdentifierFactory;
import de.upb.swt.soot.java.core.signatures.ModulePackageName;
import de.upb.swt.soot.java.core.signatures.ModuleSignature;
import de.upb.swt.soot.java.core.types.JavaClassType;
import org.junit.Test;
import org.junit.experimental.categories.Category;

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
public class ModuleIdentifierFactoryTest extends IdentifierFactoryTest {

  @Test
  public void getPackageSignatureUnnamedModule() {
    ModuleIdentifierFactory identifierFactory = ModuleIdentifierFactory.getInstance();
    ModulePackageName packageName1 = identifierFactory.getPackageName("java.lang");
    assertTrue(packageName1 instanceof ModulePackageName);
    assertSame(packageName1.getModuleSignature(), ModuleSignature.UNNAMED_MODULE);
  }

  // @Test
  // public void dispatchTest() {
  // IdentifierFactory identifierFactory = new ModuleIdentifierFactory();
  // ClassSignature classSignature1 = identifierFactory.getClassSignature("module-info","","myMod");
  // assertTrue(classSignature1 instanceof ClassSignature);
  // }

  @Test
  public void getPackageSignatureNamedModule() {
    ModuleIdentifierFactory identifierFactory = ModuleIdentifierFactory.getInstance();
    ModulePackageName packageName1 = identifierFactory.getPackageSignature("java.lang", "myModule");
    assertTrue(packageName1 instanceof ModulePackageName);
    assertNotSame(packageName1.getModuleSignature(), ModuleSignature.UNNAMED_MODULE);
  }

  @Test
  public void getModulePackageSignature() {
    ModuleIdentifierFactory identifierFactory = ModuleIdentifierFactory.getInstance();
    ModulePackageName packageSignature1 =
        identifierFactory.getPackageSignature("java.lang", "myModule");
    ModulePackageName packageSignature2 =
        identifierFactory.getPackageSignature("java.lang.invoke", "myModule");
    boolean sameObject = packageSignature1 == packageSignature2;
    assertFalse(sameObject);
  }

  @Test
  public void getModulePackageSignatureSameModule() {
    ModuleIdentifierFactory identifierFactory = ModuleIdentifierFactory.getInstance();
    ModulePackageName packageSignature1 =
        identifierFactory.getPackageSignature("java.lang", "myModule");
    ModulePackageName packageSignature2 =
        identifierFactory.getPackageSignature("java.lang", "myModule");

    boolean samePackage = packageSignature1 == packageSignature2;
    assertTrue(samePackage);

    boolean sameModuleObject =
        packageSignature1.getModuleSignature() == packageSignature2.getModuleSignature();
    assertTrue(sameModuleObject);
  }

  @Test
  public void getModulePackageSignatureDiffModule() {
    ModuleIdentifierFactory identifierFactory = ModuleIdentifierFactory.getInstance();
    ModulePackageName packageSignature1 =
        identifierFactory.getPackageSignature("java.lang", "myModule1");
    ModulePackageName packageSignature2 =
        identifierFactory.getPackageSignature("java.lang", "myModule2");
    boolean samePackage = packageSignature1 == packageSignature2;
    assertFalse(samePackage);
    boolean sameObject =
        packageSignature1.getModuleSignature() == packageSignature2.getModuleSignature();
    assertFalse(sameObject);
  }

  @Test(expected = NullPointerException.class)
  public void checkNullModule() {
    ModuleIdentifierFactory identifierFactory = ModuleIdentifierFactory.getInstance();
    ModulePackageName packageSignature = identifierFactory.getPackageSignature("myPackage", null);
  }

  @Test(expected = NullPointerException.class)
  public void checkNullModule2() {
    ModuleIdentifierFactory typeFactory = ModuleIdentifierFactory.getInstance();
    ClassType classSignature = typeFactory.getClassType("A", "mypackage", null);
  }

  @Test
  public void testModuleInfoSignature() {
    ModuleIdentifierFactory typeFactory = ModuleIdentifierFactory.getInstance();
    JavaClassType classSignature1 = typeFactory.getClassType("module-info");
    assertTrue(classSignature1.isModuleInfo());
  }

  @Test
  public void compModuleSignature() {
    ModuleSignature signature = ModuleIdentifierFactory.getModuleSignature("java.base");
    ModuleSignature signature2 = ModuleIdentifierFactory.getModuleSignature("java.base");
    assertEquals(signature, signature2);
    assertEquals(signature.hashCode(), signature2.hashCode());
    assertEquals(signature.toString(), "java.base");
  }

  @Test
  public void compModuleSignature2() {
    ModuleSignature signature = ModuleIdentifierFactory.getModuleSignature("java.base");
    ModuleSignature signature2 = ModuleIdentifierFactory.getModuleSignature("javafx.base");
    assertNotEquals(signature, signature2);
    assertNotEquals(signature.hashCode(), signature2.hashCode());
    assertEquals(signature2.toString(), "javafx.base");
    assertNotEquals(signature2.toString(), signature.toString());
  }
}
