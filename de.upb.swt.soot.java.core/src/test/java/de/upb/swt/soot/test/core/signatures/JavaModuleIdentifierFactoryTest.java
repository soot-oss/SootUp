package de.upb.swt.soot.test.core.signatures;

import static org.junit.Assert.*;

import categories.Java9Test;
import de.upb.swt.soot.java.core.JavaModuleIdentifierFactory;
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
public class JavaModuleIdentifierFactoryTest extends IdentifierFactoryTest {

  @Test
  public void getPackageSignatureUnnamedModule() {
    JavaModuleIdentifierFactory identifierFactory = JavaModuleIdentifierFactory.getInstance();
    ModulePackageName packageName1 = identifierFactory.getPackageName("java.lang");
    assertTrue(packageName1 instanceof ModulePackageName);
    assertSame(packageName1.getModuleSignature(), ModuleSignature.UNNAMED_MODULE);
  }

  @Test
  public void getPackageSignatureNamedModule() {
    JavaModuleIdentifierFactory identifierFactory = JavaModuleIdentifierFactory.getInstance();
    ModulePackageName packageName1 = identifierFactory.getPackageSignature("java.lang", "myModule");
    assertTrue(packageName1 instanceof ModulePackageName);
    assertNotSame(packageName1.getModuleSignature(), ModuleSignature.UNNAMED_MODULE);
    assertEquals(packageName1.getModuleSignature().toString(), "myModule");
  }

  @Test
  public void getModulePackageSignature() {
    JavaModuleIdentifierFactory identifierFactory = JavaModuleIdentifierFactory.getInstance();
    ModulePackageName packageSignature1 =
        identifierFactory.getPackageSignature("java.lang", "myModule");
    ModulePackageName packageSignature2 =
        identifierFactory.getPackageSignature("java.lang.invoke", "myModule");
    assertNotSame(packageSignature1, packageSignature2);
  }

  @Test
  public void getModulePackageSignatureSameModule() {
    JavaModuleIdentifierFactory identifierFactory = JavaModuleIdentifierFactory.getInstance();
    ModulePackageName packageSignature1 =
        identifierFactory.getPackageSignature("java.lang", "myModule");
    ModulePackageName packageSignature2 =
        identifierFactory.getPackageSignature("java.lang", "myModule");

    assertSame(packageSignature1, packageSignature2);
    assertSame(packageSignature1.getModuleSignature(), packageSignature2.getModuleSignature());
  }

  @Test
  public void getModulePackageSignatureDiffModule() {
    JavaModuleIdentifierFactory identifierFactory = JavaModuleIdentifierFactory.getInstance();
    ModulePackageName packageSignature1 =
        identifierFactory.getPackageSignature("java.lang", "myModule1");
    ModulePackageName packageSignature2 =
        identifierFactory.getPackageSignature("java.lang", "myModule2");

    assertNotSame(packageSignature1, packageSignature2);
    assertNotSame(packageSignature1.getModuleSignature(), packageSignature2.getModuleSignature());

    assertSame(packageSignature1.getPackageName(), packageSignature2.getPackageName());
    assertNotEquals(packageSignature1, packageSignature2);
  }

  @Test
  public void testModuleInfoSignature() {
    JavaModuleIdentifierFactory typeFactory = JavaModuleIdentifierFactory.getInstance();
    JavaClassType classSignature1 = typeFactory.getClassType("module-info");
    assertTrue(classSignature1.isModuleInfo());
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
}
