package sootup.java.bytecode.frontend.inputlocation;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import categories.TestCategories;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.core.model.SourceType;
import sootup.core.types.ClassType;
import sootup.java.core.JavaModuleIdentifierFactory;
import sootup.java.core.ModuleInfoAnalysisInputLocation;
import sootup.java.core.signatures.ModuleSignature;
import sootup.java.core.types.ModuleJavaClassType;
import sootup.java.core.views.JavaModuleView;
import sootup.java.core.views.JavaView;

@Tag(TestCategories.JAVA_9_CATEGORY)
public class ModuleMultiReleaseJarAnalysisInputLocationTest extends AnalysisInputLocationTest {
  final Path mmrj = Paths.get("../shared-test-resources/multi-release-jar-modular/mrjar.jar");

  @Test
  @Disabled("// FIXME")
  public void modularMultiReleaseJar() {

    // TODO: test & create multiple types of input
    // - [x] no module-info.class in root; module-info.class in version
    // - [ ] no module-info.class in root; no module-info.class in version
    // - [ ] module-info.class in root; no module-info.class in version
    // - [ ] no module-info.class in root; no module-info.class in version -> non modular

    assertTrue(MultiReleaseJarAnalysisInputLocation.isMultiReleaseJar(mmrj));

    final ClassType utilityNoModule =
        getIdentifierFactory().getClassType("de.upb.swt.multirelease.Utility");

    final ModuleJavaClassType utilityModule =
        JavaModuleIdentifierFactory.getInstance()
            .getClassType("de.upb.swt.multirelease/de.upb.swt.multirelease.Utility");

    final ClassType classType2 =
        getIdentifierFactory().getClassType("de.upb.swt.multirelease.Main");

    ModuleInfoAnalysisInputLocation moduleMultiReleaseJarAnalysisInputLocation8 =
        new ModuleMultiReleaseJarAnalysisInputLocation(mmrj, SourceType.Application, 8);

    final JavaView view_8 =
        new JavaModuleView(
            Collections.emptyList(),
            Collections.singletonList(moduleMultiReleaseJarAnalysisInputLocation8));

    ModuleMultiReleaseJarAnalysisInputLocation moduleMultiReleaseJarAnalysisInputLocation9 =
        new ModuleMultiReleaseJarAnalysisInputLocation(mmrj, SourceType.Application, 9);
    final JavaModuleView view_9 =
        new JavaModuleView(
            Collections.emptyList(),
            Collections.singletonList(moduleMultiReleaseJarAnalysisInputLocation9));
    ModuleSignature moduleSignature =
        JavaModuleIdentifierFactory.getModuleSignature("de.upb.swt.multirelease");

    assertEquals(Collections.singleton(moduleSignature), view_9.getNamedModules());

    assertTrue(view_9.getModuleInfo(moduleSignature).isPresent());

    assertEquals(1, view_9.getModuleClasses(moduleSignature).size());

    assertEquals(
        "de.upb.swt.multirelease.Utility",
        view_9.getModuleClasses(moduleSignature).stream()
            .findAny()
            .get()
            .getType()
            .getFullyQualifiedName());

    // for java 9
    assertEquals(
        "/META-INF/versions/9/de/upb/swt/multirelease/Utility.class",
        view_9.getClass(utilityModule).get().getClassSource().getSourcePath().toString());
    // different class will be returned if no module is specified
    assertEquals(
        "/de/upb/swt/multirelease/Utility.class",
        view_9.getClass(utilityNoModule).get().getClassSource().getSourcePath().toString());
    assertEquals(
        "/de/upb/swt/multirelease/Main.class",
        view_9.getClass(classType2).get().getClassSource().getSourcePath().toString());
    // assert that method is correctly resolved to base
    assertTrue(
        view_9
            .getClass(utilityModule)
            .get()
            .getMethod(
                getIdentifierFactory()
                    .getMethodSubSignature(
                        "printVersion",
                        getIdentifierFactory().getType("void"),
                        Collections.emptyList()))
            .get()
            .getBody()
            .toString()
            .contains("java 9"));

    // for java 8
    assertEquals(
        "/de/upb/swt/multirelease/Utility.class",
        view_8.getClass(utilityNoModule).get().getClassSource().getSourcePath().toString());
    assertFalse(view_8.getClass(utilityModule).isPresent());
    assertEquals(
        "/de/upb/swt/multirelease/Main.class",
        view_8.getClass(classType2).get().getClassSource().getSourcePath().toString());
    // assert that method is correctly resolved to base
    assertTrue(
        view_8
            .getClass(utilityNoModule)
            .get()
            .getMethod(
                getIdentifierFactory()
                    .getMethodSubSignature(
                        "printVersion",
                        getIdentifierFactory().getType("void"),
                        Collections.emptyList()))
            .get()
            .getBody()
            .toString()
            .contains("java 8"));

    assertTrue(
        view_8
            .getClass(utilityNoModule)
            .get()
            .getMethod(
                getIdentifierFactory()
                    .getMethodSubSignature(
                        "printVersion",
                        getIdentifierFactory().getType("void"),
                        Collections.emptyList()))
            .get()
            .getBody()
            .toString()
            .contains("java 8"));

    // TODO: test what happens if we put a *non* ModuleClassType into a
    // ModuleMultiReleaseAnalysisInoutLocation

  }
}
