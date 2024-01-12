package sootup.java.bytecode.inputlocation;

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

import static org.junit.Assert.assertFalse;

import categories.Java8Test;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.model.SourceType;
import sootup.core.types.ClassType;
import sootup.java.core.JavaModuleIdentifierFactory;
import sootup.java.core.language.JavaLanguage;
import sootup.java.core.signatures.ModuleSignature;
import sootup.java.core.types.ModuleJavaClassType;
import sootup.java.core.views.JavaModuleView;
import sootup.java.core.views.JavaView;

@Category(Java8Test.class)
public class MultiReleaseJarAnalysisInputLocationTest extends AnalysisInputLocationTest {

  @Test
  public void multiReleaseJar() {
    final ClassType classType =
        getIdentifierFactory().getClassType("de.upb.swt.multirelease.Utility");
    final ClassType classType2 =
        getIdentifierFactory().getClassType("de.upb.swt.multirelease.Main");

    final JavaView view_min =
        new JavaView(
            new MultiReleaseJarAnalysisInputLocation(
                mrj, SourceType.Application, new JavaLanguage(1)));
    final JavaView view_8 =
        new JavaView(
            new MultiReleaseJarAnalysisInputLocation(
                mrj, SourceType.Application, new JavaLanguage(8)));
    final JavaView view_9 =
        new JavaView(
            new MultiReleaseJarAnalysisInputLocation(
                mrj, SourceType.Application, new JavaLanguage(9)));
    final JavaView view_10 =
        new JavaView(
            new MultiReleaseJarAnalysisInputLocation(
                mrj, SourceType.Application, new JavaLanguage(10)));
    final JavaView view_max =
        new JavaView(
            new MultiReleaseJarAnalysisInputLocation(
                mrj, SourceType.Application, new JavaLanguage(Integer.MAX_VALUE)));

    // for java10
    Assert.assertEquals(
        "/META-INF/versions/9/de/upb/swt/multirelease/Utility.class",
        view_10.getClass(classType).get().getClassSource().getSourcePath().toString());
    Assert.assertEquals(
        "/de/upb/swt/multirelease/Main.class",
        view_10.getClass(classType2).get().getClassSource().getSourcePath().toString());

    // assert that method is correctly resolved
    Assert.assertTrue(
        view_10
            .getClass(classType)
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

    // for java 9
    Assert.assertEquals(
        "/META-INF/versions/9/de/upb/swt/multirelease/Utility.class",
        view_9.getClass(classType).get().getClassSource().getSourcePath().toString());
    Assert.assertEquals(
        "/de/upb/swt/multirelease/Main.class",
        view_9.getClass(classType2).get().getClassSource().getSourcePath().toString());

    // for java 8
    Assert.assertEquals(
        "/de/upb/swt/multirelease/Utility.class",
        view_8.getClass(classType).get().getClassSource().getSourcePath().toString());
    Assert.assertEquals(
        "/de/upb/swt/multirelease/Main.class",
        view_8.getClass(classType2).get().getClassSource().getSourcePath().toString());
    // assert that method is correctly resolved to base
    Assert.assertTrue(
        view_8
            .getClass(classType)
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

    // for max int
    Assert.assertEquals(
        "/META-INF/versions/9/de/upb/swt/multirelease/Utility.class",
        view_max.getClass(classType).get().getClassSource().getSourcePath().toString());
    Assert.assertEquals(
        "/de/upb/swt/multirelease/Main.class",
        view_max.getClass(classType2).get().getClassSource().getSourcePath().toString());

    // for min int
    Assert.assertEquals(
        "/de/upb/swt/multirelease/Utility.class",
        view_min.getClass(classType).get().getClassSource().getSourcePath().toString());
    Assert.assertEquals(
        "/de/upb/swt/multirelease/Main.class",
        view_min.getClass(classType2).get().getClassSource().getSourcePath().toString());
  }

  @Test
  public void modularMultiReleaseJar() {
    final ClassType utilityNoModule =
        getIdentifierFactory().getClassType("de.upb.swt.multirelease.Utility");

    final ModuleJavaClassType utilityModule =
        JavaModuleIdentifierFactory.getInstance()
            .getClassType("de.upb.swt.multirelease/de.upb.swt.multirelease.Utility");

    final ClassType classType2 =
        getIdentifierFactory().getClassType("de.upb.swt.multirelease.Main");

    MultiReleaseJarAnalysisInputLocation moduleMultiReleaseJarAnalysisInputLocation8 =
        new MultiReleaseJarAnalysisInputLocation(mmrj, SourceType.Application, new JavaLanguage(8));
    final JavaView view_8 =
        new JavaView(Collections.singletonList(moduleMultiReleaseJarAnalysisInputLocation8));

    MultiReleaseJarAnalysisInputLocation moduleMultiReleaseJarAnalysisInputLocation9 =
        new MultiReleaseJarAnalysisInputLocation(mmrj, SourceType.Application, new JavaLanguage(9));
    final JavaModuleView view_9 =
        new JavaModuleView(
            Collections.emptyList(),
            Collections.singletonList(moduleMultiReleaseJarAnalysisInputLocation9));

    ModuleSignature moduleSignature =
        JavaModuleIdentifierFactory.getModuleSignature("de.upb.swt.multirelease");

    Assert.assertEquals(Collections.singleton(moduleSignature), view_9.getNamedModules());

    Assert.assertTrue(view_9.getModuleInfo(moduleSignature).isPresent());

    Assert.assertEquals(1, view_9.getModuleClasses(moduleSignature).size());

    Assert.assertEquals(
        "de.upb.swt.multirelease.Utility",
        view_9.getModuleClasses(moduleSignature).stream()
            .findAny()
            .get()
            .getType()
            .getFullyQualifiedName());

    // for java 9
    Assert.assertEquals(
        "/META-INF/versions/9/de/upb/swt/multirelease/Utility.class",
        view_9.getClass(utilityModule).get().getClassSource().getSourcePath().toString());
    // different class will be returned if no module is specified
    Assert.assertEquals(
        "/de/upb/swt/multirelease/Utility.class",
        view_9.getClass(utilityNoModule).get().getClassSource().getSourcePath().toString());
    Assert.assertEquals(
        "/de/upb/swt/multirelease/Main.class",
        view_9.getClass(classType2).get().getClassSource().getSourcePath().toString());
    // assert that method is correctly resolved to base
    Assert.assertTrue(
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
    Assert.assertEquals(
        "/de/upb/swt/multirelease/Utility.class",
        view_8.getClass(utilityNoModule).get().getClassSource().getSourcePath().toString());
    assertFalse(view_8.getClass(utilityModule).isPresent());
    Assert.assertEquals(
        "/de/upb/swt/multirelease/Main.class",
        view_8.getClass(classType2).get().getClassSource().getSourcePath().toString());
    // assert that method is correctly resolved to base
    Assert.assertTrue(
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
  }
}
