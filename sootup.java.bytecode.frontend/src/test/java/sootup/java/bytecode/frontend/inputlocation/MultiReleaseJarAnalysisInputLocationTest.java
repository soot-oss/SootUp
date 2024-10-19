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
import static org.junit.jupiter.api.Assertions.assertTrue;

import categories.TestCategories;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.core.model.SourceType;
import sootup.core.signatures.MethodSubSignature;
import sootup.core.types.ClassType;
import sootup.java.core.views.JavaView;

@Tag(TestCategories.JAVA_8_CATEGORY)
public class MultiReleaseJarAnalysisInputLocationTest extends AnalysisInputLocationTest {

  final Path mrj = Paths.get("../shared-test-resources/multi-release-jar/mrjar.jar");
  JavaView view_min;
  JavaView view_8;
  JavaView view_9;
  JavaView view_10;
  JavaView view_max;
  ClassType classType;
  ClassType classType2;

  @Test
  public void multiReleaseJar() {

    view_min =
        new JavaView(new MultiReleaseJarAnalysisInputLocation(mrj, SourceType.Application, 1));
    view_8 = new JavaView(new MultiReleaseJarAnalysisInputLocation(mrj, SourceType.Application, 8));
    view_9 = new JavaView(new MultiReleaseJarAnalysisInputLocation(mrj, SourceType.Application, 9));
    view_10 =
        new JavaView(new MultiReleaseJarAnalysisInputLocation(mrj, SourceType.Application, 10));
    view_max =
        new JavaView(
            new MultiReleaseJarAnalysisInputLocation(
                mrj, SourceType.Application, Integer.MAX_VALUE));

    classType = getIdentifierFactory().getClassType("de.upb.sse.multirelease.Utility");
    classType2 = getIdentifierFactory().getClassType("de.upb.sse.multirelease.Main");

    assertTrue(MultiReleaseJarAnalysisInputLocation.isMultiReleaseJar(mrj));

    // for java 8
    assertEquals(
        "/de/upb/sse/multirelease/Utility.class",
        view_8.getClass(classType).get().getClassSource().getSourcePath().toString());
    assertEquals(
        "/de/upb/sse/multirelease/Main.class",
        view_8.getClass(classType2).get().getClassSource().getSourcePath().toString());
    // assert that method is correctly resolved to base
    MethodSubSignature printBodyMethodSubSig =
        getIdentifierFactory()
            .getMethodSubSignature(
                "printVersion", getIdentifierFactory().getType("void"), Collections.emptyList());
    assertTrue(
        view_8
            .getClass(classType)
            .get()
            .getMethod(printBodyMethodSubSig)
            .get()
            .getBody()
            .toString()
            .contains("java 8"));

    // for java 9
    assertEquals(
        "/META-INF/versions/9/de/upb/sse/multirelease/Utility.class",
        view_9.getClass(classType).get().getClassSource().getSourcePath().toString());
    assertEquals(
        "/de/upb/sse/multirelease/Main.class",
        view_9.getClass(classType2).get().getClassSource().getSourcePath().toString());

    // for java10
    assertEquals(
        "/META-INF/versions/10/de/upb/sse/multirelease/Utility.class",
        view_10.getClass(classType).get().getClassSource().getSourcePath().toString());
    assertEquals(
        "/de/upb/sse/multirelease/Main.class",
        view_10.getClass(classType2).get().getClassSource().getSourcePath().toString());

    // assert that method is correctly resolved
    assertTrue(
        view_10
            .getClass(classType)
            .get()
            .getMethod(printBodyMethodSubSig)
            .get()
            .getBody()
            .toString()
            .contains("java 10"));

    // for min int
    assertEquals(
        "/de/upb/sse/multirelease/Utility.class",
        view_min.getClass(classType).get().getClassSource().getSourcePath().toString());
    assertEquals(
        "/de/upb/sse/multirelease/Main.class",
        view_min.getClass(classType2).get().getClassSource().getSourcePath().toString());

    // for max int
    assertEquals(
        "/META-INF/versions/10/de/upb/sse/multirelease/Utility.class",
        view_max.getClass(classType).get().getClassSource().getSourcePath().toString());
    assertEquals(
        "/de/upb/sse/multirelease/Main.class",
        view_max.getClass(classType2).get().getClassSource().getSourcePath().toString());

    // getClasses
    List<String> collectedClassesWPrintBody9 =
        view_9
            .getClasses()
            .map(c -> c.getMethod(printBodyMethodSubSig))
            .filter(Optional::isPresent)
            .map(m -> m.get().getBody().toString())
            .collect(Collectors.toList());
    assertEquals(1, collectedClassesWPrintBody9.size());
    assertTrue(collectedClassesWPrintBody9.get(0).contains("java 9"));

    List<String> collectedClassesWPrintBody10 =
        view_10
            .getClasses()
            .map(c -> c.getMethod(printBodyMethodSubSig))
            .filter(Optional::isPresent)
            .map(m -> m.get().getBody().toString())
            .collect(Collectors.toList());
    assertEquals(1, collectedClassesWPrintBody10.size());
    assertTrue(collectedClassesWPrintBody10.get(0).contains("java 10"));
  }

  @Test
  public void testVersions() {
    List<Integer> languageVersions = MultiReleaseJarAnalysisInputLocation.getLanguageVersions(mrj);
    assertTrue(languageVersions.contains(9));
    assertTrue(languageVersions.contains(10));
  }
}
