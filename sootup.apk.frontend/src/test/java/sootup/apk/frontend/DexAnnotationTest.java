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
import org.jf.dexlib2.immutable.ImmutableField;
import org.jf.dexlib2.immutable.ImmutableMethod;
import org.jf.dexlib2.immutable.reference.ImmutableFieldReference;
import org.jf.dexlib2.immutable.value.ImmutableArrayEncodedValue;
import org.jf.dexlib2.immutable.value.ImmutableEnumEncodedValue;
import org.jf.dexlib2.immutable.value.ImmutableIntEncodedValue;
import org.jf.dexlib2.immutable.value.ImmutableStringEncodedValue;
import org.jf.dexlib2.immutable.value.ImmutableTypeEncodedValue;
import org.jf.dexlib2.writer.pool.DexPool;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sootup.apk.frontend.Util.DexUtil;
import sootup.apk.frontend.main.AndroidVersionInfo;
import sootup.core.types.ClassType;
import sootup.java.core.AnnotationUsage;
import sootup.java.core.JavaSootClass;
import sootup.java.core.JavaSootMethod;
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

  /** Methods keep their throws clause and annotations, fields their annotations. */
  @Test
  public void methodAndFieldMetadataIsConverted() throws IOException {
    ImmutableAnnotation throwsIo =
        new ImmutableAnnotation(
            AnnotationVisibility.SYSTEM,
            "Ldalvik/annotation/Throws;",
            Set.of(
                new ImmutableAnnotationElement(
                    "value",
                    new ImmutableArrayEncodedValue(
                        List.of(new ImmutableTypeEncodedValue("Ljava/io/IOException;"))))));
    ImmutableAnnotation jsInterface =
        new ImmutableAnnotation(
            AnnotationVisibility.RUNTIME, "Landroid/webkit/JavascriptInterface;", Set.of());
    ImmutableMethod method =
        new ImmutableMethod(
            "Ldex/Members;",
            "read",
            null,
            "V",
            AccessFlags.PUBLIC.getValue() | AccessFlags.ABSTRACT.getValue(),
            Set.of(throwsIo, jsInterface),
            null,
            null);
    ImmutableField field =
        new ImmutableField(
            "Ldex/Members;",
            "name",
            "Ljava/lang/String;",
            AccessFlags.PUBLIC.getValue(),
            null,
            Set.of(new ImmutableAnnotation(AnnotationVisibility.RUNTIME, "Ldex/Marked;", Set.of())),
            null);
    JavaSootClass clazz =
        load(
            new ImmutableClassDef(
                "Ldex/Members;",
                AccessFlags.PUBLIC.getValue() | AccessFlags.ABSTRACT.getValue(),
                "Ljava/lang/Object;",
                null,
                null,
                null,
                List.of(field),
                List.of(method)));

    JavaSootMethod read = clazz.getMethodsByName("read").iterator().next();
    assertEquals(
        List.of("java.io.IOException"),
        read.getExceptionSignatures().stream()
            .map(ClassType::getFullyQualifiedName)
            .collect(Collectors.toList()));
    assertEquals(
        Set.of("android.webkit.JavascriptInterface"), annotationNames(read.getAnnotations()));
    assertEquals(
        Set.of("dex.Marked"), annotationNames(clazz.getField("name").get().getAnnotations()));
  }

  private static Set<String> annotationNames(Iterable<AnnotationUsage> usages) {
    return StreamSupport.stream(usages.spliterator(), false)
        .map(usage -> usage.getAnnotation().getFullyQualifiedName())
        .collect(Collectors.toSet());
  }

  private static String sorted(Map<String, Object> values) {
    return new java.util.TreeMap<>(values).toString();
  }

  private static Map<String, AnnotationUsage> annotationsOf(Set<ImmutableAnnotation> annotations)
      throws IOException {
    JavaSootClass clazz =
        load(
            new ImmutableClassDef(
                "Ldex/Annotated;",
                AccessFlags.PUBLIC.getValue(),
                "Ljava/lang/Object;",
                null,
                null,
                annotations,
                null,
                null));
    return StreamSupport.stream(clazz.getAnnotations().spliterator(), false)
        .collect(Collectors.toMap(usage -> usage.getAnnotation().getFullyQualifiedName(), u -> u));
  }

  private static JavaSootClass load(ImmutableClassDef classDef) throws IOException {
    String name = DexUtil.toQualifiedName(classDef.getType());
    Path dex = tempDir.resolve(name + ".dex");
    DexPool.writeTo(dex.toString(), new ImmutableDexFile(Opcodes.forApi(15), List.of(classDef)));
    JavaView view =
        new JavaView(
            List.of(
                new ApkAnalysisInputLocation(
                    dex,
                    new AndroidVersionInfo(dex, ""),
                    DexBodyInterceptors.Default.bodyInterceptors())));
    return view.getClass(view.getIdentifierFactory().getClassType(name)).get();
  }
}
