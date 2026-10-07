package sootup.java.bytecode.frontend;

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

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import sootup.core.jimple.basic.NoPositionInformation;
import sootup.core.model.ClassModifier;
import sootup.core.model.FieldModifier;
import sootup.core.model.MethodModifier;
import sootup.core.model.SourceType;
import sootup.core.types.PrimitiveType;
import sootup.core.types.VoidType;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.JavaInnerClassInfo;
import sootup.java.core.JavaSootClass;
import sootup.java.core.OverridingJavaClassSource;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;

class ClassNestingAndSourceMetadataTest {
  private static final String OWNER = "fixtures/Outer";
  private static final String SMAP =
      "SMAP\r\nOriginal.kt\r\nKotlin\r\n*S Kotlin\r\n*F\r\n+ 1 Original.kt\r\n"
          + "src/\u03bb/Original.kt\r\n*L\r\n1#1,3:100\r\n*E\r\n";
  private final JavaIdentifierFactory factory = new JavaIdentifierFactory();
  @TempDir Path directory;

  @Test
  void readsSourceFileAndSmapWithoutRemappingStatementLines() throws Exception {
    ClassWriter writer = writer("fixtures/Subject", Opcodes.ACC_PUBLIC);
    writer.visitSource("Generated.java", SMAP);
    MethodVisitor method =
        writer.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "run", "()V", null, null);
    method.visitCode();
    Label line = new Label();
    method.visitLabel(line);
    method.visitLineNumber(101, line);
    method.visitInsn(Opcodes.RETURN);
    method.visitMaxs(0, 0);
    method.visitEnd();
    JavaSootClass clazz = load(writer);
    assertEquals("Generated.java", clazz.getSourceFile().orElseThrow());
    assertEquals(SMAP, clazz.getSourceDebugExtension().orElseThrow());
    assertEquals("Subject.class", clazz.getClassSource().getSourcePath().getFileName().toString());
    var body = clazz.getMethodsByName("run").iterator().next().getBody();
    assertFalse(body.getStmts().isEmpty());
    body.getStmts()
        .forEach(
            stmt -> assertEquals(101, stmt.getPositionInfo().getStmtPosition().getFirstLine()));
  }

  @Test
  void preservesEmptyUnicodeAndLongSourceDebugExtensionsWithoutSourceFile() throws Exception {
    List<String> extensions = List.of("", "debug\u0000\u03bb", "x".repeat(70000));
    for (int index = 0; index < extensions.size(); index++) {
      ClassWriter writer = writer("fixtures/Debug" + index, Opcodes.ACC_PUBLIC);
      writer.visitSource(null, extensions.get(index));
      JavaSootClass clazz = load(writer);
      assertTrue(clazz.getSourceFile().isEmpty());
      assertEquals(extensions.get(index), clazz.getSourceDebugExtension().orElseThrow());
    }
  }

  @Test
  void keepsInnerClassesInClassfileOrderWithNamesAndRawAccessFlags() throws Exception {
    ClassWriter writer = writer(OWNER, Opcodes.ACC_PUBLIC);
    int nestedFlags = Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL;
    writer.visitInnerClass(OWNER + "$Z", OWNER, "Z", nestedFlags);
    writer.visitInnerClass(
        "fixtures/Elsewhere$Member", "fixtures/Elsewhere", "Member", Opcodes.ACC_PROTECTED);
    writer.visitInnerClass(OWNER + "$1", null, null, 0);
    writer.visitInnerClass(OWNER + "$2Local", null, "Local", Opcodes.ACC_FINAL);
    JavaSootClass clazz = load(writer);
    List<JavaInnerClassInfo> entries = clazz.getInnerClasses();
    assertEquals(
        List.of(
            "fixtures.Outer$Z",
            "fixtures.Elsewhere$Member",
            "fixtures.Outer$1",
            "fixtures.Outer$2Local"),
        entries.stream().map(entry -> entry.getInnerClass().toString()).toList());
    assertEquals(type(OWNER), entries.get(0).getOuterClass().orElseThrow());
    assertEquals("Z", entries.get(0).getInnerName().orElseThrow());
    assertEquals(nestedFlags, entries.get(0).getAccessFlags());
    assertEquals(type("fixtures/Elsewhere"), entries.get(1).getOuterClass().orElseThrow());
    assertEquals(Opcodes.ACC_PROTECTED, entries.get(1).getAccessFlags());
    assertTrue(entries.get(2).getInnerName().isEmpty());
    assertTrue(entries.get(2).getOuterClass().isEmpty());
    assertEquals("Local", entries.get(3).getInnerName().orElseThrow());
    assertTrue(entries.get(3).getOuterClass().isEmpty());
    assertThrows(UnsupportedOperationException.class, entries::clear);
    assertTrue(clazz.getOuterClass().isEmpty());
  }

  @Test
  void resolvesMemberAndStaticNestedClassesFromTheirOwnInnerClassesEntry() throws Exception {
    for (boolean isStatic : List.of(false, true)) {
      String name = OWNER + (isStatic ? "$Nested" : "$Member");
      ClassWriter writer = writer(name, Opcodes.ACC_PUBLIC);
      writer.visitInnerClass("fixtures/Unrelated$Entry", "fixtures/Unrelated", "Entry", 0);
      writer.visitInnerClass(
          name, OWNER, isStatic ? "Nested" : "Member", isStatic ? Opcodes.ACC_STATIC : 0);
      JavaSootClass clazz = load(writer);
      assertEquals(type(OWNER), clazz.getOuterClass().orElseThrow());
      assertTrue(clazz.hasOuterClass());
      assertTrue(clazz.getEnclosingClass().isEmpty());
      assertTrue(clazz.getEnclosingMethod().isEmpty());
    }
  }

  @Test
  void readsLocalClassEnclosingMethodWithoutResolvingTheOwner() throws Exception {
    ClassWriter writer = writer(OWNER + "$1Local", Opcodes.ACC_PUBLIC);
    writer.visitOuterClass(OWNER, "make", "(I[Ljava/lang/String;)Ljava/lang/Object;");
    writer.visitInnerClass(OWNER + "$1Local", null, "Local", 0);
    JavaSootClass clazz = load(writer);
    assertEquals(type(OWNER), clazz.getEnclosingClass().orElseThrow());
    assertEquals(type(OWNER), clazz.getOuterClass().orElseThrow());
    assertEquals(
        factory.getMethodSignature(
            type(OWNER),
            "make",
            factory.getClassType("java.lang.Object"),
            List.of(PrimitiveType.getInt(), factory.getType("java.lang.String[]"))),
        clazz.getEnclosingMethod().orElseThrow());
    assertTrue(clazz.getInnerClasses().get(0).getOuterClass().isEmpty());
    assertFalse(Files.exists(directory.resolve(OWNER + ".class")));
  }

  @Test
  void readsAnonymousClassEnclosedByAConstructor() throws Exception {
    ClassWriter writer = writer(OWNER + "$1", Opcodes.ACC_PUBLIC);
    writer.visitOuterClass(OWNER, "<init>", "(Ljava/lang/String;)V");
    writer.visitInnerClass(OWNER + "$1", null, null, 0);
    JavaSootClass clazz = load(writer);
    assertEquals(
        factory.getMethodSignature(
            type(OWNER),
            "<init>",
            VoidType.getInstance(),
            List.of(factory.getClassType("java.lang.String"))),
        clazz.getEnclosingMethod().orElseThrow());
    assertTrue(clazz.getInnerClasses().get(0).getInnerName().isEmpty());
    assertEquals(type(OWNER), clazz.getOuterClass().orElseThrow());
  }

  @Test
  void preservesEnclosingClassWhenMethodIndexIsZeroForAnInitializer() throws Exception {
    ClassWriter writer = writer(OWNER + "$1", Opcodes.ACC_PUBLIC);
    writer.visitOuterClass(OWNER, null, null);
    writer.visitInnerClass(OWNER + "$1", null, null, 0);
    JavaSootClass clazz = load(writer);
    assertEquals(type(OWNER), clazz.getEnclosingClass().orElseThrow());
    assertEquals(type(OWNER), clazz.getOuterClass().orElseThrow());
    assertTrue(clazz.getEnclosingMethod().isEmpty());
  }

  @Test
  void absentAttributesStayAbsentEvenWithDollarInTheClassName() throws Exception {
    JavaSootClass clazz = load(writer("fixtures/Price$Tag", Opcodes.ACC_PUBLIC));
    assertTrue(clazz.getSourceFile().isEmpty());
    assertTrue(clazz.getSourceDebugExtension().isEmpty());
    assertTrue(clazz.getEnclosingClass().isEmpty());
    assertTrue(clazz.getEnclosingMethod().isEmpty());
    assertTrue(clazz.getInnerClasses().isEmpty());
    assertTrue(clazz.getOuterClass().isEmpty());
    assertFalse(clazz.isInnerClass());
  }

  @Test
  void annotationClassesPreserveSourceAndNestingMetadata() throws Exception {
    String name = OWNER + "$NestedAnnotation";
    int flags =
        Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT | Opcodes.ACC_INTERFACE | Opcodes.ACC_ANNOTATION;
    ClassWriter writer = new ClassWriter(0);
    writer.visit(
        Opcodes.V1_8,
        flags,
        name,
        null,
        "java/lang/Object",
        new String[] {"java/lang/annotation/Annotation"});
    writer.visitSource("Outer.java", SMAP);
    writer.visitInnerClass(name, OWNER, "NestedAnnotation", flags | Opcodes.ACC_STATIC);
    JavaSootClass clazz = load(writer);
    assertTrue(clazz.isAnnotation());
    assertEquals("Outer.java", clazz.getSourceFile().orElseThrow());
    assertEquals(SMAP, clazz.getSourceDebugExtension().orElseThrow());
    assertEquals(type(OWNER), clazz.getOuterClass().orElseThrow());
    assertEquals("NestedAnnotation", clazz.getInnerClasses().get(0).getInnerName().orElseThrow());
    assertEquals(flags | Opcodes.ACC_STATIC, clazz.getInnerClasses().get(0).getAccessFlags());
  }

  @Test
  void eagerSourcesWrappersBuildersAndCopiesPreserveMetadata() throws Exception {
    ClassWriter writer = writer(OWNER + "$1Local", Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT);
    writer.visitSource("Outer.java", SMAP);
    writer.visitOuterClass(OWNER, "make", "()V");
    writer.visitInnerClass(OWNER + "$1Local", null, "Local", 0);
    writer.visitField(Opcodes.ACC_PUBLIC, "value", "I", null, null).visitEnd();
    writer
        .visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT, "declared", "()V", null, null)
        .visitEnd();
    JavaSootClass clazz = load(writer);
    assertEquals("Outer.java", clazz.getSourceFile().orElseThrow());
    var source = assertInstanceOf(OverridingJavaClassSource.class, clazz.getClassSource());
    var field = clazz.getField("value").orElseThrow();
    var method = clazz.getMethodsByName("declared").iterator().next();
    List<JavaSootClass> copies =
        List.of(
            clazz.withFields(Collections.emptyList()),
            clazz.withMethods(Collections.emptyList()),
            clazz.withModifiers(EnumSet.of(ClassModifier.PUBLIC)),
            clazz.withSuperclass(Optional.empty()),
            clazz.withOuterClass(Optional.empty()),
            clazz.withPosition(NoPositionInformation.getInstance()),
            clazz.withSourceType(SourceType.Library),
            clazz.withReplacedField(field, field.withModifiers(EnumSet.of(FieldModifier.PRIVATE))),
            clazz.withReplacedMethod(
                method,
                method.withModifiers(
                    EnumSet.of(MethodModifier.PROTECTED, MethodModifier.ABSTRACT))));
    for (JavaSootClass copy : copies) {
      assertMetadataEquals(clazz, copy);
      assertMetadataEquals(
          clazz, copy.withMethods(Collections.emptyList()).withFields(Collections.emptyList()));
    }
    List<OverridingJavaClassSource> sourceCopies =
        List.of(
            new OverridingJavaClassSource(source),
            source.withFields(Collections.emptyList()),
            source.withMethods(Collections.emptyList()),
            source.withModifiers(EnumSet.of(ClassModifier.PUBLIC)),
            source.withInterfaces(Collections.emptySet()),
            source.withSuperclass(Optional.empty()),
            source.withOuterClass(Optional.empty()),
            source.withPosition(NoPositionInformation.getInstance()),
            OverridingJavaClassSource.OverridingJavaClassSourceBuilder.builder()
                .withSootClassSource(source)
                .build());
    for (var copy : sourceCopies) {
      assertSame(source.getClassFileMetadata(), copy.getClassFileMetadata());
      assertMetadataEquals(clazz, copy.buildClass(SourceType.Application));
    }
    assertEquals(type(OWNER), clazz.getOuterClass().orElseThrow());
    assertTrue(copies.get(4).getOuterClass().isEmpty());
    assertTrue(copies.get(6).isLibraryClass());
  }

  private static void assertMetadataEquals(JavaSootClass expected, JavaSootClass actual) {
    assertEquals(expected.getSourceFile(), actual.getSourceFile());
    assertEquals(expected.getSourceDebugExtension(), actual.getSourceDebugExtension());
    assertEquals(expected.getEnclosingClass(), actual.getEnclosingClass());
    assertEquals(expected.getEnclosingMethod(), actual.getEnclosingMethod());
    assertEquals(expected.getInnerClasses(), actual.getInnerClasses());
  }

  private JavaClassType type(String internalName) {
    return factory.getClassType(internalName.replace('/', '.'));
  }

  private static ClassWriter writer(String name, int access) {
    ClassWriter writer = new ClassWriter(0);
    writer.visit(Opcodes.V1_8, access, name, null, "java/lang/Object", null);
    return writer;
  }

  private JavaSootClass load(ClassWriter writer) throws Exception {
    writer.visitEnd();
    byte[] bytes = writer.toByteArray();
    ClassNode node = new ClassNode();
    new ClassReader(bytes).accept(node, 0);
    // Every fixture deliberately omits LocalVariableTable: class metadata is independent of LVT.
    assertTrue(
        node.methods.stream()
            .allMatch(method -> method.localVariables == null || method.localVariables.isEmpty()));
    Path file = directory.resolve(node.name + ".class");
    Files.createDirectories(file.getParent());
    Files.write(file, bytes);
    JavaView view =
        new JavaView(
            new JavaClassPathAnalysisInputLocation(
                directory.toString(), SourceType.Application, List.of()));
    return view.getClass(type(node.name)).orElseThrow();
  }
}
