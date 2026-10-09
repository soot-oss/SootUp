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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import sootup.core.jimple.common.constant.Constant;
import sootup.core.jimple.common.constant.DoubleConstant;
import sootup.core.jimple.common.constant.FloatConstant;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.constant.LongConstant;
import sootup.core.model.SourceType;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaSootClass;
import sootup.java.core.language.JavaJimple;
import sootup.java.core.views.JavaView;

class ConstantValueTest {
  @TempDir Path directory;

  @Test
  void readsAllConstantValueKindsIncludingZeroAndEmptyString() throws Exception {
    ClassWriter writer = writer();
    constant(writer, "intValue", "I", -42);
    constant(writer, "zero", "I", 0);
    constant(writer, "booleanValue", "Z", 1);
    constant(writer, "falseValue", "Z", 0);
    constant(writer, "byteValue", "B", -128);
    constant(writer, "shortValue", "S", -32768);
    constant(writer, "charValue", "C", 65535);
    constant(writer, "longValue", "J", Long.MIN_VALUE);
    constant(writer, "floatValue", "F", -0.0f);
    constant(writer, "doubleValue", "D", Double.POSITIVE_INFINITY);
    constant(writer, "text", "Ljava/lang/String;", "metadata\u0000\n\u03bb");
    constant(writer, "emptyText", "Ljava/lang/String;", "");
    JavaSootClass clazz = load(writer);
    assertConstant(clazz, "intValue", IntConstant.getInstance(-42));
    assertConstant(clazz, "zero", IntConstant.getInstance(0));
    assertConstant(clazz, "booleanValue", IntConstant.getInstance(1));
    assertConstant(clazz, "falseValue", IntConstant.getInstance(0));
    assertConstant(clazz, "byteValue", IntConstant.getInstance(-128));
    assertConstant(clazz, "shortValue", IntConstant.getInstance(-32768));
    assertConstant(clazz, "charValue", IntConstant.getInstance(65535));
    assertConstant(clazz, "longValue", LongConstant.getInstance(Long.MIN_VALUE));
    assertConstant(clazz, "floatValue", FloatConstant.getInstance(-0.0f));
    assertConstant(clazz, "doubleValue", DoubleConstant.getInstance(Double.POSITIVE_INFINITY));
    var factory = new JavaView(Collections.emptyList()).getIdentifierFactory();
    assertConstant(clazz, "text", JavaJimple.newStringConstant("metadata\u0000\n\u03bb", factory));
    assertConstant(clazz, "emptyText", JavaJimple.newStringConstant("", factory));
    assertEquals(
        factory.getClassType("java.lang.String"),
        clazz.getField("text").orElseThrow().getConstantValue().orElseThrow().getType());
  }

  @Test
  void absentConstantsStayAbsentEvenForInitializedFields() throws Exception {
    ClassWriter writer = writer();
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
    for (var field : clazz.getFields()) {
      assertTrue(field.getConstantValue().isEmpty());
    }
  }

  @Test
  void annotationClassesPreserveConstants() throws Exception {
    ClassWriter writer = new ClassWriter(0);
    writer.visit(
        Opcodes.V1_8,
        Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT | Opcodes.ACC_INTERFACE | Opcodes.ACC_ANNOTATION,
        "MetadataSubject",
        null,
        "java/lang/Object",
        new String[] {"java/lang/annotation/Annotation"});
    constant(writer, "VERSION", "I", 42);
    JavaSootClass clazz = load(writer);
    assertTrue(clazz.isAnnotation());
    assertConstant(clazz, "VERSION", IntConstant.getInstance(42));
  }

  private static void constant(ClassWriter writer, String name, String descriptor, Object value) {
    writer
        .visitField(
            Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL,
            name,
            descriptor,
            null,
            value)
        .visitEnd();
  }

  private static void assertConstant(JavaSootClass clazz, String name, Constant expected) {
    assertEquals(
        expected, clazz.getField(name).orElseThrow().getConstantValue().orElseThrow(), name);
  }

  private static ClassWriter writer() {
    ClassWriter writer = new ClassWriter(0);
    writer.visit(
        Opcodes.V1_8,
        Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT | Opcodes.ACC_SUPER,
        "MetadataSubject",
        null,
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
