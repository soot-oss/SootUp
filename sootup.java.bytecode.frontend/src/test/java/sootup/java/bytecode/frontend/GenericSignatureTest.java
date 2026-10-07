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
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import sootup.core.jimple.basic.NoPositionInformation;
import sootup.core.model.ClassModifier;
import sootup.core.model.SourceType;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaSootClass;
import sootup.java.core.JavaSootClassSource;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.OverridingJavaClassSource;
import sootup.java.core.views.JavaView;

class GenericSignatureTest {
  private static final String CLASS_SIGNATURE = "<T:Ljava/lang/Number;>Ljava/lang/Object;";
  private static final String METHOD_SIGNATURE = "<U:Ljava/lang/Object;>(TU;)TU;";
  @TempDir Path directory;

  @Test
  void readsGenericDeclarationsWithoutChangingErasedTypes() throws Exception {
    ClassWriter writer = writer(CLASS_SIGNATURE);
    writer.visitField(Opcodes.ACC_PUBLIC, "value", "Ljava/lang/Number;", "TT;", null).visitEnd();
    writer
        .visitField(
            Opcodes.ACC_PUBLIC,
            "names",
            "Ljava/util/List;",
            "Ljava/util/List<Ljava/lang/String;>;",
            null)
        .visitEnd();
    MethodVisitor method =
        writer.visitMethod(
            Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
            "identity",
            "(Ljava/lang/Object;)Ljava/lang/Object;",
            METHOD_SIGNATURE,
            null);
    method.visitCode();
    method.visitVarInsn(Opcodes.ALOAD, 0);
    method.visitInsn(Opcodes.ARETURN);
    method.visitMaxs(1, 1);
    method.visitEnd();
    JavaSootClass clazz = load(writer);
    assertEquals(CLASS_SIGNATURE, clazz.getGenericSignature().orElseThrow());
    var value = clazz.getField("value").orElseThrow();
    assertEquals("TT;", value.getGenericSignature().orElseThrow());
    assertEquals("java.lang.Number", value.getType().toString());
    assertEquals(
        "Ljava/util/List<Ljava/lang/String;>;",
        clazz.getField("names").orElseThrow().getGenericSignature().orElseThrow());
    JavaSootMethod identity = clazz.getMethodsByName("identity").iterator().next();
    assertEquals(METHOD_SIGNATURE, identity.getGenericSignature().orElseThrow());
    assertEquals("java.lang.Object", identity.getReturnType().toString());
    assertEquals("java.lang.Object", identity.getParameterType(0).toString());
    assertEquals(
        identity.getGenericSignature(),
        identity.withBody(identity.getBody()).getGenericSignature());
  }

  @Test
  void readsSignaturesForConstructorsAndMethodsWithoutBodies() throws Exception {
    ClassWriter writer = writer(CLASS_SIGNATURE);
    writer
        .visitMethod(
            Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT,
            "abstractValue",
            "(Ljava/lang/Number;)Ljava/lang/Number;",
            "<E:Ljava/lang/Exception;>(TT;)TT;^TE;",
            new String[] {"java/lang/Exception"})
        .visitEnd();
    writer
        .visitMethod(
            Opcodes.ACC_PUBLIC | Opcodes.ACC_NATIVE,
            "nativeValue",
            "(Ljava/lang/Number;)Ljava/lang/Number;",
            "(TT;)TT;",
            null)
        .visitEnd();
    MethodVisitor constructor =
        writer.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "(Ljava/lang/Number;)V", "(TT;)V", null);
    constructor.visitCode();
    constructor.visitVarInsn(Opcodes.ALOAD, 0);
    constructor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
    constructor.visitInsn(Opcodes.RETURN);
    constructor.visitMaxs(1, 2);
    constructor.visitEnd();
    JavaSootClass clazz = load(writer);
    JavaSootMethod abstractValue = clazz.getMethodsByName("abstractValue").iterator().next();
    JavaSootMethod nativeValue = clazz.getMethodsByName("nativeValue").iterator().next();
    assertFalse(abstractValue.hasBody());
    assertFalse(nativeValue.hasBody());
    assertEquals(
        "<E:Ljava/lang/Exception;>(TT;)TT;^TE;", abstractValue.getGenericSignature().orElseThrow());
    assertEquals("(TT;)TT;", nativeValue.getGenericSignature().orElseThrow());
    assertEquals(
        "(TT;)V",
        clazz.getMethodsByName("<init>").iterator().next().getGenericSignature().orElseThrow());
  }

  @Test
  void absentSignaturesStayAbsent() throws Exception {
    ClassWriter writer = writer(null);
    writer.visitField(Opcodes.ACC_PUBLIC, "instanceValue", "I", null, null).visitEnd();
    writer
        .visitField(
            Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL,
            "runtimeValue",
            "I",
            null,
            null)
        .visitEnd();
    MethodVisitor initializer =
        writer.visitMethod(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
    initializer.visitCode();
    initializer.visitIntInsn(Opcodes.BIPUSH, 9);
    initializer.visitFieldInsn(Opcodes.PUTSTATIC, "MetadataSubject", "runtimeValue", "I");
    initializer.visitInsn(Opcodes.RETURN);
    initializer.visitMaxs(1, 0);
    initializer.visitEnd();
    JavaSootClass clazz = load(writer);
    assertTrue(clazz.getGenericSignature().isEmpty());
    for (var field : clazz.getFields()) {
      assertTrue(field.getGenericSignature().isEmpty());
    }
    assertTrue(
        clazz.getMethodsByName("<clinit>").iterator().next().getGenericSignature().isEmpty());
  }

  @Test
  void classWrappersAndCopiesPreserveGenericSignature() throws Exception {
    JavaSootClass clazz = load(writer(CLASS_SIGNATURE));
    assertEquals(CLASS_SIGNATURE, clazz.getGenericSignature().orElseThrow());
    JavaSootClassSource source = (JavaSootClassSource) clazz.getClassSource();
    assertEquals(
        clazz.getGenericSignature(), new OverridingJavaClassSource(source).getGenericSignature());
    List<JavaSootClass> copies =
        List.of(
            clazz.withMethods(Collections.emptyList()),
            clazz.withFields(Collections.emptyList()),
            clazz.withModifiers(EnumSet.of(ClassModifier.PUBLIC)),
            clazz.withSuperclass(Optional.empty()),
            clazz.withOuterClass(Optional.empty()),
            clazz.withPosition(NoPositionInformation.getInstance()),
            clazz.withSourceType(SourceType.Library));
    for (JavaSootClass copy : copies) {
      assertEquals(clazz.getGenericSignature(), copy.getGenericSignature());
      assertEquals(
          clazz.getGenericSignature(),
          copy.withFields(Collections.emptyList()).getGenericSignature());
    }
  }

  @Test
  void annotationClassesPreserveSignaturesAndDefaults() throws Exception {
    ClassWriter writer = new ClassWriter(0);
    writer.visit(
        Opcodes.V1_8,
        Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT | Opcodes.ACC_INTERFACE | Opcodes.ACC_ANNOTATION,
        "MetadataSubject",
        null,
        "java/lang/Object",
        new String[] {"java/lang/annotation/Annotation"});
    MethodVisitor type =
        writer.visitMethod(
            Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT,
            "type",
            "()Ljava/lang/Class;",
            "()Ljava/lang/Class<*>;",
            null);
    var defaultValue = type.visitAnnotationDefault();
    defaultValue.visit(null, org.objectweb.asm.Type.getType("Ljava/lang/String;"));
    defaultValue.visitEnd();
    type.visitEnd();
    JavaSootClass clazz = load(writer);
    assertTrue(clazz.isAnnotation());
    assertTrue(clazz.getGenericSignature().isEmpty());
    JavaSootMethod method = clazz.getMethodsByName("type").iterator().next();
    assertEquals("()Ljava/lang/Class<*>;", method.getGenericSignature().orElseThrow());
    assertFalse(method.hasBody());
    assertTrue(method.getDefaultValue().isPresent());
  }

  private static ClassWriter writer(String signature) {
    ClassWriter writer = new ClassWriter(0);
    writer.visit(
        Opcodes.V1_8,
        Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT | Opcodes.ACC_SUPER,
        "MetadataSubject",
        signature,
        "java/lang/Object",
        null);
    return writer;
  }

  private JavaSootClass load(ClassWriter writer) throws Exception {
    writer.visitEnd();
    Files.write(directory.resolve("MetadataSubject.class"), writer.toByteArray());
    JavaView view =
        new JavaView(
            new JavaClassPathAnalysisInputLocation(
                directory.toString(), SourceType.Application, Collections.emptyList()));
    return view.getClass(view.getIdentifierFactory().getClassType("MetadataSubject")).orElseThrow();
  }
}
