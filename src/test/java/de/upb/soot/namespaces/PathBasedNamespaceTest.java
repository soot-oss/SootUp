package de.upb.soot.namespaces;

/*-
 * #%L
 * Soot
 * %%
 * Copyright (C) 06.06.2018 Manuel Benz
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

import de.upb.soot.namespaces.classprovider.ClassSource;
import de.upb.soot.signatures.JavaClassSignature;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import categories.Java8Test;

/** @author Manuel Benz created on 06.06.18 */
@Category(Java8Test.class)
public class PathBasedNamespaceTest extends AbstractNamespaceTest {

  @Test(expected = IllegalArgumentException.class)
  public void failsOnFile() {
    // TODO adapt to new testing folder structure
    PathBasedNamespace
        .createForClassContainer(Paths.get("target/test-classes/de/upb/soot/namespaces/PathBasedNamespaceTest.class"));
  }

  @Test
  public void classNotFound() {
    // TODO adapt to new testing folder structure
    Path baseDir = Paths.get("target/test-classes/");
    PathBasedNamespace pathBasedNamespace = PathBasedNamespace.createForClassContainer(baseDir);
    final JavaClassSignature sig = getSignatureFactory().getClassSignature("NotExisting", "de.upb.soot.namespaces");
    final Optional<ClassSource> classSource = pathBasedNamespace.getClassSource(sig);
    Assert.assertFalse(classSource.isPresent());
  }

  @Test
  public void testFolder() {
    // TODO adapt to new testing folder structure
    Path baseDir = Paths.get("target/classes/");
    PathBasedNamespace pathBasedNamespace = PathBasedNamespace.createForClassContainer(baseDir);
    final JavaClassSignature sig = getSignatureFactory().getClassSignature("PathBasedNamespace", "de.upb.soot.namespaces");
    testClassReceival(pathBasedNamespace, sig, CLASSES_IN_JAR);
  }

  @Test
  public void testJar() {
    // TODO adapt to new testing folder structure
    Path jar = Paths.get("target/test-classes/de/upb/soot/namespaces/Soot-4.0-SNAPSHOT.jar");
    PathBasedNamespace pathBasedNamespace = PathBasedNamespace.createForClassContainer(jar);
    final JavaClassSignature sig = getSignatureFactory().getClassSignature("PathBasedNamespace", "de.upb.soot.namespaces");
    testClassReceival(pathBasedNamespace, sig, CLASSES_IN_JAR);
  }
}
