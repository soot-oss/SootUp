package sootup.java.core.model;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2026 the SootUp contributors
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

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import org.junit.jupiter.api.Test;
import sootup.core.inputlocation.EagerInputLocation;
import sootup.core.jimple.basic.NoPositionInformation;
import sootup.core.model.ClassModifier;
import sootup.core.model.SourceType;
import sootup.core.types.VoidType;
import sootup.java.core.JavaClassFileMetadata;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.JavaInnerClassInfo;
import sootup.java.core.OverridingJavaClassSource;

class JavaClassFileMetadataTest {
  private final JavaIdentifierFactory factory = new JavaIdentifierFactory();

  @Test
  void oldConstructorsAndBuildersDefaultToAbsentMetadata() {
    var type = factory.getClassType("Example");
    var input = new EagerInputLocation();
    var path = Path.of("Example.class");
    var source =
        new OverridingJavaClassSource(
            input,
            path,
            type,
            null,
            Collections.emptySet(),
            null,
            Collections.emptySet(),
            Collections.emptySet(),
            NoPositionInformation.getInstance(),
            EnumSet.of(ClassModifier.PUBLIC),
            Collections.emptyList(),
            Collections.emptyList(),
            Collections.emptyList());
    var built =
        OverridingJavaClassSource.OverridingJavaClassSourceBuilder.builder()
            .withAnalysisInputLocation(input)
            .withSourcePath(path)
            .withClassType(type)
            .withPosition(NoPositionInformation.getInstance())
            .withAnnotation(Collections.emptyList())
            .withMethodAnnotation(Collections.emptyList())
            .withFieldAnnotation(Collections.emptyList())
            .build();
    for (var candidate : List.of(source, built)) {
      assertSame(JavaClassFileMetadata.empty(), candidate.getClassFileMetadata());
      var clazz = candidate.buildClass(SourceType.Application);
      assertTrue(clazz.getSourceFile().isEmpty());
      assertTrue(clazz.getSourceDebugExtension().isEmpty());
      assertTrue(clazz.getEnclosingClass().isEmpty());
      assertTrue(clazz.getEnclosingMethod().isEmpty());
      assertTrue(clazz.getInnerClasses().isEmpty());
    }
  }

  @Test
  void innerClassesAreAnImmutableSnapshotOfTheInputList() {
    var entry =
        new JavaInnerClassInfo(
            factory.getClassType("Outer$Inner"), factory.getClassType("Outer"), "Inner", 9);
    var entries = new ArrayList<>(List.of(entry));
    var metadata = new JavaClassFileMetadata("Outer.java", null, null, null, entries);
    entries.clear();
    assertEquals(List.of(entry), metadata.getInnerClasses());
    assertThrows(UnsupportedOperationException.class, () -> metadata.getInnerClasses().clear());
  }

  @Test
  void enclosingMethodMustBelongToTheRecordedClass() {
    var owner = factory.getClassType("Outer");
    var method = factory.getMethodSignature(owner, "make", VoidType.getInstance(), List.of());
    assertThrows(
        IllegalArgumentException.class,
        () -> new JavaClassFileMetadata(null, null, null, method, List.of()));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new JavaClassFileMetadata(
                null, null, factory.getClassType("Other"), method, List.of()));
    var metadata = new JavaClassFileMetadata(null, null, owner, method, List.of());
    assertEquals(owner, metadata.getEnclosingClass().orElseThrow());
    assertEquals(method, metadata.getEnclosingMethod().orElseThrow());
  }
}
