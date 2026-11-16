package sootup.apk.frontend;

/*-
 * #%L
 * SootUp
 * %%
 * Copyright (C) 2022 - 2026 Kadiray Karakaya, Markus Schmidt, Jonas Klauke, Stefan Schott, Palaniappan Muthuraman, Marcus Hüwe and others
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import org.jf.dexlib2.AccessFlags;
import org.jf.dexlib2.AnnotationVisibility;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.immutable.ImmutableAnnotation;
import org.jf.dexlib2.immutable.ImmutableAnnotationElement;
import org.jf.dexlib2.immutable.ImmutableClassDef;
import org.jf.dexlib2.immutable.ImmutableField;
import org.jf.dexlib2.immutable.ImmutableMethod;
import org.jf.dexlib2.immutable.ImmutableMethodImplementation;
import org.jf.dexlib2.immutable.ImmutableMethodParameter;
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction10x;
import org.jf.dexlib2.immutable.reference.ImmutableFieldReference;
import org.jf.dexlib2.immutable.value.ImmutableAnnotationEncodedValue;
import org.jf.dexlib2.immutable.value.ImmutableArrayEncodedValue;
import org.jf.dexlib2.immutable.value.ImmutableBooleanEncodedValue;
import org.jf.dexlib2.immutable.value.ImmutableEnumEncodedValue;
import org.jf.dexlib2.immutable.value.ImmutableIntEncodedValue;
import org.jf.dexlib2.immutable.value.ImmutableStringEncodedValue;
import org.jf.dexlib2.immutable.value.ImmutableTypeEncodedValue;
import org.jf.dexlib2.writer.io.FileDataStore;
import org.jf.dexlib2.writer.pool.DexPool;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sootup.apk.frontend.main.AndroidVersionInfo;
import sootup.core.IdentifierFactory;
import sootup.core.model.SootField;
import sootup.java.core.AnnotationUsage;
import sootup.java.core.ConstantUtil;
import sootup.java.core.JavaSootClass;
import sootup.java.core.JavaSootField;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.language.JavaJimple;
import sootup.java.core.views.JavaView;

/** Annotations and declared exceptions of classes, fields, methods and parameters of dex code. */
public class DexAnnotationTest {

  private static final String CLASS = "Lexample/Annotated;";
  private static final String MARKER = "Lexample/Marker;";
  private static final String HIDDEN = "Lexample/Hidden;";
  private static final String INNER = "Lexample/Inner;";

  @TempDir static Path tempDir;

  private static JavaView view;
  private static JavaSootClass clazz;

  @BeforeAll
  static void createView() throws IOException {
    Path apk = writeApk(createClass(), createAnnotationClass());
    view =
        new JavaView(
            new ApkAnalysisInputLocation(
                apk,
                new AndroidVersionInfo(apk, ""),
                DexBodyInterceptors.Default.bodyInterceptors()));
    clazz = view.getClass(view.getIdentifierFactory().getClassType("example.Annotated")).get();
  }

  private static ImmutableAnnotation annotation(
      int visibility, String type, ImmutableAnnotationElement... elements) {
    return new ImmutableAnnotation(visibility, type, Set.of(elements));
  }

  private static ImmutableAnnotation throwsIOException() {
    return annotation(
        AnnotationVisibility.SYSTEM,
        "Ldalvik/annotation/Throws;",
        new ImmutableAnnotationElement(
            "value",
            new ImmutableArrayEncodedValue(
                List.of(new ImmutableTypeEncodedValue("Ljava/io/IOException;")))));
  }

  private static ImmutableClassDef createClass() {
    ImmutableAnnotation marker =
        annotation(
            AnnotationVisibility.RUNTIME,
            MARKER,
            new ImmutableAnnotationElement("i", new ImmutableIntEncodedValue(7)),
            new ImmutableAnnotationElement("s", new ImmutableStringEncodedValue("x")),
            new ImmutableAnnotationElement(
                "c", new ImmutableTypeEncodedValue("Ljava/lang/String;")),
            new ImmutableAnnotationElement(
                "e",
                new ImmutableEnumEncodedValue(
                    new ImmutableFieldReference("Lexample/Color;", "RED", "Lexample/Color;"))),
            new ImmutableAnnotationElement(
                "a",
                new ImmutableArrayEncodedValue(
                    List.of(new ImmutableIntEncodedValue(1), new ImmutableIntEncodedValue(2)))),
            new ImmutableAnnotationElement(
                "n",
                new ImmutableAnnotationEncodedValue(
                    INNER,
                    Set.of(
                        new ImmutableAnnotationElement(
                            "v", ImmutableBooleanEncodedValue.TRUE_VALUE)))));
    ImmutableAnnotation hidden = annotation(AnnotationVisibility.BUILD, HIDDEN);
    ImmutableAnnotation signature =
        annotation(
            AnnotationVisibility.SYSTEM,
            "Ldalvik/annotation/Signature;",
            new ImmutableAnnotationElement(
                "value",
                new ImmutableArrayEncodedValue(
                    List.of(new ImmutableStringEncodedValue("Ljava/lang/Object;")))));

    ImmutableField field =
        new ImmutableField(
            CLASS, "f", "I", AccessFlags.PUBLIC.getValue(), null, Set.of(marker), null);

    ImmutableMethod abstractMethod =
        new ImmutableMethod(
            CLASS,
            "abstractMethod",
            List.of(new ImmutableMethodParameter("I", Set.of(hidden), null)),
            "V",
            AccessFlags.PUBLIC.getValue() | AccessFlags.ABSTRACT.getValue(),
            Set.of(marker, throwsIOException()),
            null,
            null);

    ImmutableMethod concreteMethod =
        new ImmutableMethod(
            CLASS,
            "concreteMethod",
            List.of(
                new ImmutableMethodParameter("Ljava/lang/String;", Set.of(), null),
                new ImmutableMethodParameter("I", Set.of(marker, hidden), null)),
            "V",
            AccessFlags.PUBLIC.getValue(),
            Set.of(hidden, throwsIOException()),
            null,
            new ImmutableMethodImplementation(
                3, List.of(new ImmutableInstruction10x(Opcode.RETURN_VOID)), null, null));

    return new ImmutableClassDef(
        CLASS,
        AccessFlags.PUBLIC.getValue() | AccessFlags.ABSTRACT.getValue(),
        "Ljava/lang/Object;",
        null,
        null,
        Set.of(marker, hidden, signature),
        List.of(field),
        List.of(abstractMethod, concreteMethod));
  }

  /**
   * The annotation Marker, whose methods i, s, a and n have default values; dex keeps them in the
   * AnnotationDefault system annotation of the class.
   */
  private static ImmutableClassDef createAnnotationClass() {
    int flags = AccessFlags.PUBLIC.getValue() | AccessFlags.ABSTRACT.getValue();
    List<ImmutableMethod> methods = new ArrayList<>();
    for (String[] method :
        List.of(
            new String[] {"i", "I"},
            new String[] {"s", "Ljava/lang/String;"},
            new String[] {"a", "[I"},
            new String[] {"n", INNER},
            new String[] {"noDefault", "I"})) {
      methods.add(
          new ImmutableMethod(
              MARKER, method[0], List.of(), method[1], flags, Set.of(), null, null));
    }
    ImmutableAnnotation defaults =
        annotation(
            AnnotationVisibility.SYSTEM,
            "Ldalvik/annotation/AnnotationDefault;",
            new ImmutableAnnotationElement(
                "value",
                new ImmutableAnnotationEncodedValue(
                    MARKER,
                    Set.of(
                        new ImmutableAnnotationElement("i", new ImmutableIntEncodedValue(7)),
                        new ImmutableAnnotationElement("s", new ImmutableStringEncodedValue("x")),
                        new ImmutableAnnotationElement(
                            "a",
                            new ImmutableArrayEncodedValue(
                                List.of(
                                    new ImmutableIntEncodedValue(1),
                                    new ImmutableIntEncodedValue(2)))),
                        new ImmutableAnnotationElement(
                            "n",
                            new ImmutableAnnotationEncodedValue(
                                INNER,
                                Set.of(
                                    new ImmutableAnnotationElement(
                                        "v", ImmutableBooleanEncodedValue.TRUE_VALUE))))))));
    return new ImmutableClassDef(
        MARKER,
        flags | AccessFlags.INTERFACE.getValue() | AccessFlags.ANNOTATION.getValue(),
        "Ljava/lang/Object;",
        List.of("Ljava/lang/annotation/Annotation;"),
        null,
        Set.of(defaults),
        List.of(),
        methods);
  }

  /** Writes the classes into a dex file inside an APK that reuses the manifest of a test APK. */
  private static Path writeApk(ImmutableClassDef... classDefs) throws IOException {
    Path dex = tempDir.resolve("classes.dex");
    DexPool pool = new DexPool(Opcodes.getDefault());
    for (ImmutableClassDef classDef : classDefs) {
      pool.internClass(classDef);
    }
    pool.writeTo(new FileDataStore(dex.toFile()));

    Path apk = tempDir.resolve("Annotated.apk");
    try (ZipFile template = new ZipFile("src/test/resources/SharedZero.apk");
        ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(apk))) {
      out.putNextEntry(new ZipEntry("AndroidManifest.xml"));
      try (InputStream manifest =
          template.getInputStream(template.getEntry("AndroidManifest.xml"))) {
        manifest.transferTo(out);
      }
      out.closeEntry();
      out.putNextEntry(new ZipEntry("classes.dex"));
      Files.copy(dex, (OutputStream) out);
      out.closeEntry();
    }
    return apk;
  }

  private static AnnotationUsage expectedMarker() {
    IdentifierFactory factory = view.getIdentifierFactory();
    Map<String, Object> inner = new LinkedHashMap<>();
    inner.put("v", ConstantUtil.fromObject(true, factory));
    Map<String, Object> values = new LinkedHashMap<>();
    values.put("i", ConstantUtil.fromObject(7, factory));
    values.put("s", ConstantUtil.fromObject("x", factory));
    values.put("c", JavaJimple.newClassConstant("Ljava/lang/String;", factory));
    values.put("e", ConstantUtil.fromObject(new String[] {"example.Color", "RED"}, factory));
    values.put(
        "a", List.of(ConstantUtil.fromObject(1, factory), ConstantUtil.fromObject(2, factory)));
    values.put("n", new AnnotationUsage(factory.getClassType("example.Inner"), inner, true));
    return new AnnotationUsage(factory.getClassType("example.Marker"), values, true);
  }

  private static AnnotationUsage expectedHidden() {
    return new AnnotationUsage(
        view.getIdentifierFactory().getClassType("example.Hidden"), Map.of(), false);
  }

  private static List<AnnotationUsage> list(Iterable<AnnotationUsage> annotations) {
    List<AnnotationUsage> list = new ArrayList<>();
    annotations.forEach(list::add);
    return list;
  }

  private static JavaSootMethod method(String name) {
    return clazz.getMethods().stream()
        .filter(m -> m.getName().equals(name))
        .findFirst()
        .orElseThrow();
  }

  @Test
  void classAnnotationsSkipSystemAnnotations() {
    List<AnnotationUsage> annotations = list(clazz.getAnnotations());
    assertEquals(2, annotations.size(), annotations.toString());
    assertTrue(annotations.contains(expectedMarker()), annotations.toString());
    assertTrue(annotations.contains(expectedHidden()), annotations.toString());
  }

  @Test
  void fieldAnnotations() {
    SootField field = clazz.getField("f").orElseThrow();
    assertEquals(List.of(expectedMarker()), list(((JavaSootField) field).getAnnotations()));
  }

  @Test
  void methodAnnotationsAndThrownExceptions() {
    for (String name : List.of("abstractMethod", "concreteMethod")) {
      JavaSootMethod method = method(name);
      assertEquals(
          List.of(view.getIdentifierFactory().getClassType("java.io.IOException")),
          method.getExceptionSignatures(),
          name);
      assertFalse(
          list(method.getAnnotations()).stream()
              .anyMatch(a -> a.getAnnotation().getPackageName().getName().startsWith("dalvik")),
          name);
    }
    assertEquals(List.of(expectedMarker()), list(method("abstractMethod").getAnnotations()));
    assertEquals(List.of(expectedHidden()), list(method("concreteMethod").getAnnotations()));
  }

  @Test
  void parameterAnnotations() {
    JavaSootMethod abstractMethod = method("abstractMethod");
    assertEquals(List.of(expectedHidden()), abstractMethod.getParameterAnnotations(0));
    assertEquals(List.of(), abstractMethod.getParameterAnnotations(1));

    JavaSootMethod concreteMethod = method("concreteMethod");
    assertEquals(List.of(), concreteMethod.getParameterAnnotations(0));
    List<AnnotationUsage> second = concreteMethod.getParameterAnnotations(1);
    assertEquals(2, second.size(), second.toString());
    assertTrue(second.containsAll(List.of(expectedMarker(), expectedHidden())));
    assertEquals(
        List.of(expectedMarker()), concreteMethod.getParameterAnnotations(1, "RuntimeVisible"));
    assertEquals(
        List.of(expectedHidden()), concreteMethod.getParameterAnnotations(1, "RuntimeInvisible"));
  }

  @Test
  void bodyOfAnnotatedMethodResolves() {
    assertTrue(method("concreteMethod").hasBody());
    assertFalse(method("concreteMethod").getBody().getStmts().isEmpty());
  }

  @Test
  void annotationsDoNotShareValues() {
    // a former implementation reused one value map for all annotations of an element
    assertTrue(
        list(clazz.getAnnotations()).stream()
            .filter(a -> a.getAnnotation().getClassName().equals("Hidden"))
            .allMatch(a -> a.getValues().isEmpty()));
  }

  @Test
  void annotationDefaultValues() {
    IdentifierFactory factory = view.getIdentifierFactory();
    JavaSootClass marker = view.getClass(factory.getClassType("example.Marker")).orElseThrow();
    Map<String, Object> expected = new LinkedHashMap<>();
    expected.put("i", ConstantUtil.fromObject(7, factory));
    expected.put("s", ConstantUtil.fromObject("x", factory));
    expected.put(
        "a", List.of(ConstantUtil.fromObject(1, factory), ConstantUtil.fromObject(2, factory)));
    expected.put(
        "n",
        new AnnotationUsage(
            factory.getClassType("example.Inner"),
            Map.of("v", ConstantUtil.fromObject(true, factory)),
            true));

    assertEquals(expected, marker.getAnnotationDefaultValues());
    assertTrue(marker.getMethodsByName("noDefault").iterator().next().getDefaultValue().isEmpty());
  }
}
