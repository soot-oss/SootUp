package sootup.java.bytecode.frontend.conversion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.JIdentityStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.SourceType;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaSootClass;
import sootup.java.core.views.JavaView;

class AsmLineNumberAttributionTest {

  @TempDir Path tempDir;

  @Test
  void attributesPrecedingLineNumberInBytecodeOrderAtBranchTarget() throws Exception {
    Files.write(tempDir.resolve("LineBranch.class"), makeClass());

    JavaClassPathAnalysisInputLocation location =
        new JavaClassPathAnalysisInputLocation(
            tempDir.toString(), SourceType.Application, Collections.emptyList());
    JavaView view = new JavaView(location);
    JavaSootClass clazz =
        view.getClass(view.getIdentifierFactory().getClassType("LineBranch")).orElseThrow();

    List<Stmt> stmts =
        clazz.getMethods().stream()
            .filter(method -> method.getName().equals("branch"))
            .findFirst()
            .orElseThrow()
            .getBody()
            .getStmts();

    // Verify the return statement at the merge label retains line 200 from the preceding
    // instruction
    Stmt returnStmt = stmts.get(stmts.size() - 1);
    assertNotNull(returnStmt);
    assertEquals(200, returnStmt.getPositionInfo().getStmtPosition().getFirstLine());

    // Verify the assignment in the branch at line 200 retains line 200
    Stmt branch2Assign =
        stmts.stream()
            .filter(
                s -> s instanceof JAssignStmt assign && assign.getRightOp().toString().equals("2"))
            .findFirst()
            .orElseThrow();
    assertEquals(200, branch2Assign.getPositionInfo().getStmtPosition().getFirstLine());
  }

  @Test
  void attributesPrecedingLineNumberOfFirstInstructionToPreamble() throws Exception {
    Files.write(tempDir.resolve("LinePreamble.class"), makePreambleClass());

    JavaClassPathAnalysisInputLocation location =
        new JavaClassPathAnalysisInputLocation(
            tempDir.toString(), SourceType.Application, Collections.emptyList());
    JavaView view = new JavaView(location);
    JavaSootClass clazz =
        view.getClass(view.getIdentifierFactory().getClassType("LinePreamble")).orElseThrow();

    List<Stmt> stmts =
        clazz.getMethods().stream()
            .filter(method -> method.getName().equals("paramMethod"))
            .findFirst()
            .orElseThrow()
            .getBody()
            .getStmts();

    Stmt firstIdentity = stmts.get(0);
    assertEquals(225, firstIdentity.getPositionInfo().getStmtPosition().getFirstLine());
  }

  @Test
  void doesNotLeakLoopBackEdgeLineNumberToInstructionsPrecedingFirstLineNumberNode()
      throws Exception {
    Files.write(tempDir.resolve("LineLoop.class"), makeLoopClass());

    JavaClassPathAnalysisInputLocation location =
        new JavaClassPathAnalysisInputLocation(
            tempDir.toString(), SourceType.Application, Collections.emptyList());
    JavaView view = new JavaView(location);
    JavaSootClass clazz =
        view.getClass(view.getIdentifierFactory().getClassType("LineLoop")).orElseThrow();

    List<Stmt> stmts =
        clazz.getMethods().stream()
            .filter(method -> method.getName().equals("loop"))
            .findFirst()
            .orElseThrow()
            .getBody()
            .getStmts();

    Stmt storeStmt = null;
    for (Stmt s : stmts) {
      if (s instanceof JAssignStmt assign && assign.getLeftOp().toString().equals("l1")) {
        storeStmt = s;
        break;
      }
    }

    assertNotNull(storeStmt);
    assertEquals(-1, storeStmt.getPositionInfo().getStmtPosition().getFirstLine());
  }

  @Test
  void attributesCatchBlockLineNumberToCaughtExceptionIdentityStatement() throws Exception {
    Files.write(tempDir.resolve("LineCatch.class"), makeCatchClass());

    JavaClassPathAnalysisInputLocation location =
        new JavaClassPathAnalysisInputLocation(
            tempDir.toString(), SourceType.Application, Collections.emptyList());
    JavaView view = new JavaView(location);
    JavaSootClass clazz =
        view.getClass(view.getIdentifierFactory().getClassType("LineCatch")).orElseThrow();

    List<Stmt> stmts =
        clazz.getMethods().stream()
            .filter(method -> method.getName().equals("tryCatch"))
            .findFirst()
            .orElseThrow()
            .getBody()
            .getStmts();

    JIdentityStmt caughtExceptionStmt = null;
    for (Stmt s : stmts) {
      if (s instanceof JIdentityStmt id
          && id.getRightOp() instanceof sootup.core.jimple.common.ref.JCaughtExceptionRef) {
        caughtExceptionStmt = id;
        break;
      }
    }

    assertNotNull(caughtExceptionStmt);
    assertEquals(50, caughtExceptionStmt.getPositionInfo().getStmtPosition().getFirstLine());
  }

  private static byte[] makeCatchClass() {
    ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
    writer.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, "LineCatch", null, "java/lang/Object", null);

    MethodVisitor method =
        writer.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "tryCatch", "()V", null, null);
    method.visitCode();

    Label tryStart = new Label();
    Label tryEnd = new Label();
    Label catchHandler = new Label();

    method.visitTryCatchBlock(tryStart, tryEnd, catchHandler, "java/lang/Exception");

    method.visitLabel(tryStart);
    method.visitLineNumber(10, tryStart);
    method.visitInsn(Opcodes.NOP);
    method.visitLabel(tryEnd);
    method.visitInsn(Opcodes.RETURN);

    method.visitLabel(catchHandler);
    method.visitLineNumber(50, catchHandler);
    method.visitVarInsn(Opcodes.ASTORE, 0);
    method.visitInsn(Opcodes.RETURN);

    method.visitMaxs(1, 1);
    method.visitEnd();
    writer.visitEnd();
    return writer.toByteArray();
  }

  private static byte[] makeLoopClass() {
    ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
    writer.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, "LineLoop", null, "java/lang/Object", null);

    MethodVisitor method =
        writer.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "loop", "(I)V", null, null);
    method.visitCode();

    Label start = new Label();
    method.visitLabel(start);
    method.visitVarInsn(Opcodes.ILOAD, 0);
    method.visitVarInsn(Opcodes.ISTORE, 1);

    Label loopBody = new Label();
    method.visitLabel(loopBody);
    method.visitLineNumber(50, loopBody);
    method.visitVarInsn(Opcodes.ILOAD, 1);
    method.visitJumpInsn(Opcodes.IFNE, start);
    method.visitInsn(Opcodes.RETURN);

    method.visitMaxs(1, 2);
    method.visitEnd();
    writer.visitEnd();
    return writer.toByteArray();
  }

  private static byte[] makePreambleClass() {
    ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
    writer.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, "LinePreamble", null, "java/lang/Object", null);

    MethodVisitor method =
        writer.visitMethod(
            Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "paramMethod", "(SS)S", null, null);
    method.visitCode();

    Label l0 = new Label();
    method.visitLabel(l0);
    method.visitLineNumber(226, l0);
    method.visitLineNumber(225, l0);
    method.visitVarInsn(Opcodes.ILOAD, 0);
    method.visitInsn(Opcodes.IRETURN);

    method.visitMaxs(1, 2);
    method.visitEnd();

    writer.visitEnd();
    return writer.toByteArray();
  }

  private static byte[] makeClass() {
    ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
    writer.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, "LineBranch", null, "java/lang/Object", null);

    MethodVisitor method =
        writer.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "branch", "(I)I", null, null);
    method.visitCode();

    Label l0 = new Label();
    Label l1 = new Label();
    Label l2 = new Label();

    method.visitLabel(l0);
    method.visitLineNumber(100, l0);
    method.visitVarInsn(Opcodes.ILOAD, 0);
    method.visitJumpInsn(Opcodes.IFNE, l1);
    method.visitInsn(Opcodes.ICONST_1);
    method.visitJumpInsn(Opcodes.GOTO, l2);

    method.visitLabel(l1);
    method.visitLineNumber(200, l1);
    method.visitInsn(Opcodes.ICONST_2);

    method.visitLabel(l2);
    method.visitVarInsn(Opcodes.ISTORE, 1);
    method.visitVarInsn(Opcodes.ILOAD, 1);
    method.visitInsn(Opcodes.IRETURN);

    method.visitMaxs(2, 2);
    method.visitEnd();

    writer.visitEnd();
    return writer.toByteArray();
  }

  @Test
  void attributesProducingInstructionLineNumberWhenDirectlyStoringToLocal() throws Exception {
    Files.write(tempDir.resolve("LineDirectStore.class"), makeDirectStoreClass());

    JavaClassPathAnalysisInputLocation location =
        new JavaClassPathAnalysisInputLocation(
            tempDir.toString(), SourceType.Application, Collections.emptyList());
    JavaView view = new JavaView(location);
    JavaSootClass clazz =
        view.getClass(view.getIdentifierFactory().getClassType("LineDirectStore")).orElseThrow();

    List<Stmt> stmts =
        clazz.getMethods().stream()
            .filter(method -> method.getName().equals("callAndStore"))
            .findFirst()
            .orElseThrow()
            .getBody()
            .getStmts();

    // Verify the assignment of the method call result retains line 92 (the call's line number),
    // rather than line 95 (where the store instruction is located).
    Stmt callAssignStmt =
        stmts.stream()
            .filter(s -> s instanceof JAssignStmt assign && assign.isInvokableStmt())
            .findFirst()
            .orElseThrow();

    assertNotNull(callAssignStmt);
    assertEquals(92, callAssignStmt.getPositionInfo().getStmtPosition().getFirstLine());
  }

  private static byte[] makeDirectStoreClass() {
    ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
    writer.visit(
        Opcodes.V1_8, Opcodes.ACC_PUBLIC, "LineDirectStore", null, "java/lang/Object", null);

    MethodVisitor target =
        writer.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "dummyCall", "()I", null, null);
    target.visitCode();
    target.visitInsn(Opcodes.ICONST_0);
    target.visitInsn(Opcodes.IRETURN);
    target.visitMaxs(1, 0);
    target.visitEnd();

    MethodVisitor method =
        writer.visitMethod(
            Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "callAndStore", "()I", null, null);
    method.visitCode();

    Label l0 = new Label();
    method.visitLabel(l0);
    method.visitLineNumber(92, l0);
    method.visitMethodInsn(Opcodes.INVOKESTATIC, "LineDirectStore", "dummyCall", "()I", false);

    Label l1 = new Label();
    method.visitLabel(l1);
    method.visitLineNumber(95, l1);
    method.visitVarInsn(Opcodes.ISTORE, 0);
    method.visitVarInsn(Opcodes.ILOAD, 0);
    method.visitInsn(Opcodes.IRETURN);

    method.visitMaxs(1, 1);
    method.visitEnd();

    writer.visitEnd();
    return writer.toByteArray();
  }
}
