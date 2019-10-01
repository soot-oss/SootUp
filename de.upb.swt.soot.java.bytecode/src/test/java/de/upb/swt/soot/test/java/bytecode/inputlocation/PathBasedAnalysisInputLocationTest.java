package de.upb.swt.soot.test.java.bytecode.inputlocation;

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
import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import de.upb.swt.soot.core.Project;
import de.upb.swt.soot.core.frontend.AbstractClassSource;
import de.upb.swt.soot.core.types.JavaClassType;
import de.upb.swt.soot.core.views.JavaView;
import de.upb.swt.soot.core.views.View;
import de.upb.swt.soot.java.bytecode.inputlocation.PathBasedAnalysisInputLocation;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author Manuel Benz created on 06.06.18 */
@Category(Java8Test.class)
public class PathBasedAnalysisInputLocationTest extends AbstractAnalysisInputLocationTest {

  Path jar = Paths.get("../shared-test-resources/Soot-4.0-SNAPSHOT.jar");

  @Test
  public void testJar() {
    PathBasedAnalysisInputLocation pathBasedNamespace =
        PathBasedAnalysisInputLocation.createForClassContainer(jar);
    System.err.println(jar.toFile().getAbsolutePath());
    final JavaClassType sig =
        getIdentifierFactory().getClassType("PathBasedNamespace", "de.upb.soot.namespaces");
    testClassReceival(pathBasedNamespace, sig, CLASSES_IN_JAR);
  }

  void runtimeContains(View view, String classname, String packageName) {
    final JavaClassType sig = getIdentifierFactory().getClassType(classname, packageName);
    assertTrue(sig + " is not found in rt.jar", view.getClass(sig).isPresent());
  }

  @Test
  public void testRuntimeJar() {
    PathBasedAnalysisInputLocation pathBasedNamespace =
        PathBasedAnalysisInputLocation.createForClassContainer(
            Paths.get(System.getProperty("java.home") + "/lib/rt.jar"));

    final Collection<? extends AbstractClassSource> classSources =
        pathBasedNamespace.getClassSources(getIdentifierFactory());

    View v = new JavaView(new Project(pathBasedNamespace));

    // test some standard jre classes
    runtimeContains(v, "Object", "java.lang");
    runtimeContains(v, "List", "java.util");
    runtimeContains(v, "Map", "java.util");
    runtimeContains(v, "ArrayList", "java.util");
    runtimeContains(v, "HashMap", "java.util");
    runtimeContains(v, "Collection", "java.util");
    runtimeContains(v, "Comparator", "java.util");
  }
}
