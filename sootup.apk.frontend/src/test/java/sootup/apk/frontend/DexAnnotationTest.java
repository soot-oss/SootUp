package sootup.apk.frontend;

/*-
 * #%L
 * SootUp
 * %%
 * Copyright (C) 2022 - 2024 Kadiray Karakaya, Markus Schmidt, Jonas Klauke, Stefan Schott, Palaniappan Muthuraman, Marcus Hüwe and others
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

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.jf.dexlib2.AccessFlags;
import org.jf.dexlib2.AnnotationVisibility;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.immutable.ImmutableAnnotation;
import org.jf.dexlib2.immutable.ImmutableAnnotationElement;
import org.jf.dexlib2.immutable.ImmutableClassDef;
import org.jf.dexlib2.immutable.ImmutableDexFile;
import org.jf.dexlib2.immutable.reference.ImmutableFieldReference;
import org.jf.dexlib2.immutable.value.ImmutableArrayEncodedValue;
import org.jf.dexlib2.immutable.value.ImmutableEnumEncodedValue;
import org.jf.dexlib2.immutable.value.ImmutableIntEncodedValue;
import org.jf.dexlib2.immutable.value.ImmutableStringEncodedValue;
import org.jf.dexlib2.immutable.value.ImmutableTypeEncodedValue;
import org.jf.dexlib2.writer.pool.DexPool;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sootup.apk.frontend.main.AndroidVersionInfo;
import sootup.java.core.AnnotationUsage;
import sootup.java.core.views.JavaView;

public class DexAnnotationTest {

  @TempDir static Path tempDir;

  @Test
  public void annotationValuesAreConverted() throws IOException {
    ImmutableAnnotation foo =
        new ImmutableAnnotation(
            AnnotationVisibility.RUNTIME,
            "Ldex/Foo;",
            Set.of(
                new ImmutableAnnotationElement("s", new ImmutableStringEncodedValue("bar")),
                new ImmutableAnnotationElement("n", new ImmutableIntEncodedValue(3)),
                new ImmutableAnnotationElement(
                    "c", new ImmutableTypeEncodedValue("Ljava/lang/String;")),
                new ImmutableAnnotationElement(
                    "e",
                    new ImmutableEnumEncodedValue(
                        new ImmutableFieldReference("Ldex/Color;", "RED", "Ldex/Color;"))),
                new ImmutableAnnotationElement(
                    "a",
                    new ImmutableArrayEncodedValue(
                        List.of(
                            new ImmutableIntEncodedValue(1), new ImmutableIntEncodedValue(2))))));
    ImmutableAnnotation bar =
        new ImmutableAnnotation(
            AnnotationVisibility.BUILD,
            "Ldex/Bar;",
            Set.of(new ImmutableAnnotationElement("s", new ImmutableStringEncodedValue("other"))));
    ImmutableAnnotation system =
        new ImmutableAnnotation(
            AnnotationVisibility.SYSTEM,
            "Ldalvik/annotation/Signature;",
            Set.of(
                new ImmutableAnnotationElement(
                    "value",
                    new ImmutableArrayEncodedValue(
                        List.of(new ImmutableStringEncodedValue("Ljava/util/List<*>;"))))));

    Map<String, AnnotationUsage> usages = annotationsOf(Set.of(foo, bar, system));

    assertEquals(Set.of("dex.Foo", "dex.Bar"), usages.keySet());
    assertEquals(
        "{a=[1, 2], c=class \"Ljava/lang/String;\", e=<dex.Color: dex.Color RED>, n=3, s=\"bar\"}",
        sorted(usages.get("dex.Foo").getValues()));
    assertEquals("{s=\"other\"}", sorted(usages.get("dex.Bar").getValues()));
  }

  private static String sorted(Map<String, Object> values) {
    return new java.util.TreeMap<>(values).toString();
  }

  private static Map<String, AnnotationUsage> annotationsOf(Set<ImmutableAnnotation> annotations)
      throws IOException {
    ImmutableClassDef classDef =
        new ImmutableClassDef(
            "Ldex/Annotated;",
            AccessFlags.PUBLIC.getValue(),
            "Ljava/lang/Object;",
            null,
            null,
            annotations,
            null,
            null);
    Path dex = tempDir.resolve("Annotated.dex");
    DexPool.writeTo(dex.toString(), new ImmutableDexFile(Opcodes.forApi(15), List.of(classDef)));

    JavaView view =
        new JavaView(
            List.of(
                new ApkAnalysisInputLocation(
                    dex,
                    new AndroidVersionInfo(dex, ""),
                    DexBodyInterceptors.Default.bodyInterceptors())));
    Iterable<AnnotationUsage> usages =
        view.getClass(view.getIdentifierFactory().getClassType("dex.Annotated"))
            .get()
            .getAnnotations();
    return StreamSupport.stream(usages.spliterator(), false)
        .collect(Collectors.toMap(usage -> usage.getAnnotation().getFullyQualifiedName(), u -> u));
  }
}
