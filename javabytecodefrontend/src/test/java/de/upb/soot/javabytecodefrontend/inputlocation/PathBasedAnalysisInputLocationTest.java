package de.upb.soot.javabytecodefrontend.inputlocation;

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

import categories.Java8Test;
import de.upb.soot.frontends.AbstractClassSource;
import de.upb.soot.types.JavaClassType;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author Manuel Benz created on 06.06.18 */
@Category(Java8Test.class)
public class PathBasedAnalysisInputLocationTest extends AbstractAnalysisInputLocationTest {

  @Test(expected = IllegalArgumentException.class)
  public void failsOnFile() {
    // TODO adapt to new testing folder structure
    PathBasedAnalysisInputLocation.createForClassContainer(
        Paths.get("target/test-classes/de/upb/soot/namespaces/PathBasedNamespace.class"));
  }

  @Test
  public void classNotFound() {
    // TODO adapt to new testing folder structure
    Path baseDir = Paths.get("target/test-classes/");
    PathBasedAnalysisInputLocation pathBasedNamespace =
        PathBasedAnalysisInputLocation.createForClassContainer(baseDir);
    final JavaClassType sig =
        getIdentifierFactory().getClassType("NotExisting", "de.upb.soot.namespaces");
    final Optional<? extends AbstractClassSource> classSource =
        pathBasedNamespace.getClassSource(sig);
    Assert.assertFalse(classSource.isPresent());
  }

  @Test
  public void testFolder() {
    // TODO adapt to new testing folder structure
    Path baseDir = Paths.get("target/classes/");
    PathBasedAnalysisInputLocation pathBasedNamespace =
        PathBasedAnalysisInputLocation.createForClassContainer(baseDir);
    final JavaClassType sig =
        getIdentifierFactory()
            .getClassType(
                PathBasedAnalysisInputLocation.class.getSimpleName(),
                PathBasedAnalysisInputLocation.class.getPackage().getName());
    testClassReceival(pathBasedNamespace, sig, CLASSES_IN_JAR);
  }

  @Test
  public void testJar() {
    // TODO adapt to new testing folder structure
    Path jar = Paths.get("target/test-classes/de/upb/soot/namespaces/Soot-4.0-SNAPSHOT.jar");
    PathBasedAnalysisInputLocation pathBasedNamespace =
        PathBasedAnalysisInputLocation.createForClassContainer(jar);
    final JavaClassType sig =
        getIdentifierFactory().getClassType("PathBasedNamespace", "de.upb.soot.namespaces");
    testClassReceival(pathBasedNamespace, sig, CLASSES_IN_JAR);
  }
}
