package sootup.java.bytecode.frontend.conversion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import sootup.core.jimple.common.expr.JInstanceOfExpr;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.model.SourceType;
import sootup.core.types.ArrayType;
import sootup.core.types.PrimitiveType;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaSootClass;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;

class AsmInstanceOfArrayTypeTest {

  @TempDir Path tempDir;

  /**
   * Reproduces incorrect instanceof array type resolution:
   *
   * <p>Subject pattern: Java methods that check `x instanceof byte[]`, `x instanceof Object[]`, or
   * multidimensional `x instanceof int[][]`. In bytecode, the instanceof opcode receives an array
   * descriptor (e.g. "[B", "[Ljava/lang/Object;", "[[I").
   *
   * <p>Previously, AsmMethodSource parsed this with toJimpleClassType, resulting in a JavaClassType
   * named "[B" rather than an ArrayType(PrimitiveType.ByteType, 1).
   *
   * <p>With arrayTypetoJimpleType, the descriptor is properly parsed into an ArrayType with correct
   * base type and dimension.
   */
  @Test
  void convertsArrayInstanceOfDescriptorsToArrayTypes() throws Exception {
    Files.write(tempDir.resolve("ArrayInstanceOf.class"), makeClass());

    JavaClassPathAnalysisInputLocation location =
        new JavaClassPathAnalysisInputLocation(
            tempDir.toString(), SourceType.Application, Collections.emptyList());
    JavaView view = new JavaView(location);
    JavaSootClass clazz =
        view.getClass(view.getIdentifierFactory().getClassType("ArrayInstanceOf")).orElseThrow();

    ArrayType byteArray = checkType(clazz, "isByteArray");
    assertEquals(1, byteArray.getDimension());
    assertInstanceOf(PrimitiveType.ByteType.class, byteArray.getBaseType());

    ArrayType objectArray = checkType(clazz, "isObjectArray");
    assertEquals(1, objectArray.getDimension());
    JavaClassType objectType = assertInstanceOf(JavaClassType.class, objectArray.getBaseType());
    assertEquals("java.lang.Object", objectType.getFullyQualifiedName());

    ArrayType intMatrix = checkType(clazz, "isIntMatrix");
    assertEquals(2, intMatrix.getDimension());
    assertInstanceOf(PrimitiveType.IntType.class, intMatrix.getBaseType());
  }

  private static ArrayType checkType(JavaSootClass clazz, String methodName) {
    List<JAssignStmt> assignments =
        clazz.getMethods().stream()
            .filter(method -> method.getName().equals(methodName))
            .findFirst()
            .orElseThrow()
            .getBody()
            .getStmts()
            .stream()
            .filter(JAssignStmt.class::isInstance)
            .map(JAssignStmt.class::cast)
            .toList();
    JInstanceOfExpr expression =
        assignments.stream()
            .map(JAssignStmt::getRightOp)
            .filter(JInstanceOfExpr.class::isInstance)
            .map(JInstanceOfExpr.class::cast)
            .findFirst()
            .orElseThrow();
    return assertInstanceOf(ArrayType.class, expression.getCheckType());
  }

  private static byte[] makeClass() {
    ClassWriter writer = new ClassWriter(0);
    writer.visit(
        Opcodes.V1_8, Opcodes.ACC_PUBLIC, "ArrayInstanceOf", null, "java/lang/Object", null);
    addInstanceOfMethod(writer, "isByteArray", "[B");
    addInstanceOfMethod(writer, "isObjectArray", "[Ljava/lang/Object;");
    addInstanceOfMethod(writer, "isIntMatrix", "[[I");
    writer.visitEnd();
    return writer.toByteArray();
  }

  private static void addInstanceOfMethod(ClassWriter writer, String name, String descriptor) {
    MethodVisitor method =
        writer.visitMethod(
            Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, name, "(Ljava/lang/Object;)Z", null, null);
    method.visitCode();
    method.visitVarInsn(Opcodes.ALOAD, 0);
    method.visitTypeInsn(Opcodes.INSTANCEOF, descriptor);
    method.visitInsn(Opcodes.IRETURN);
    method.visitMaxs(1, 1);
    method.visitEnd();
  }
}
