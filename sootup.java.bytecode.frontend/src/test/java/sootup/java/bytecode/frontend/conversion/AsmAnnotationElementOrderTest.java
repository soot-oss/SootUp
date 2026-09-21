package sootup.java.bytecode.frontend.conversion;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AnnotationNode;
import sootup.core.model.SourceType;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.AnnotationUsage;
import sootup.java.core.JavaSootClass;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.views.JavaView;

class AsmAnnotationElementOrderTest {

  @TempDir Path tempDir;

  /**
   * Reproduces annotation element ordering non-determinism:
   *
   * <p>Subject pattern: An annotation with multiple element-value pairs,
   * e.g.: @SampleAnnotation(selector = "setDelegate:", strongRef = true, optional = false,
   * dereference = true)
   *
   * <p>In bytecode (JVMS §4.7.16), element-value pairs are stored in an ordered table. Previously,
   * AsmUtil.createAnnotationUsage stored elements in a java.util.HashMap. HashMap hash-bucket
   * iteration order scrambled key order, breaking deterministic builds and parity tests.
   *
   * <p>With LinkedHashMap, the original declaration/bytecode ordering is preserved.
   */
  @Test
  void directAsmUtilPreservesElementOrder() {
    AnnotationNode node = new AnnotationNode("Lcom/example/SampleAnnotation;");
    node.values =
        Arrays.asList(
            "selector", "setDelegate:",
            "strongRef", Boolean.TRUE,
            "optional", Boolean.FALSE,
            "dereference", Boolean.TRUE);

    AnnotationUsage usage = AsmUtil.createAnnotationUsage(node);
    List<String> keys = new ArrayList<>(usage.getValues().keySet());
    assertEquals(Arrays.asList("selector", "strongRef", "optional", "dereference"), keys);
  }

  @Test
  void classLoadingPreservesAnnotationElementOrder() throws Exception {
    ClassWriter cw = new ClassWriter(0);
    cw.visit(
        Opcodes.V1_8,
        Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER,
        "AnnotatedSubject",
        null,
        "java/lang/Object",
        null);

    // Class-level annotation with multiple elements
    AnnotationVisitor cav = cw.visitAnnotation("Lcom/example/ClassAnnotation;", true);
    cav.visit("first", "1st");
    cav.visit("second", "2nd");
    cav.visit("third", "3rd");
    cav.visitEnd();

    // Method with multi-element annotation
    MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "annotatedMethod", "()V", null, null);
    AnnotationVisitor mav = mv.visitAnnotation("Lcom/example/MethodAnnotation;", true);
    mav.visit("selector", "setDelegate:");
    mav.visit("strongRef", Boolean.TRUE);
    mav.visitEnd();
    mv.visitCode();
    mv.visitInsn(Opcodes.RETURN);
    mv.visitMaxs(1, 1);
    mv.visitEnd();

    cw.visitEnd();
    Files.write(tempDir.resolve("AnnotatedSubject.class"), cw.toByteArray());

    JavaClassPathAnalysisInputLocation location =
        new JavaClassPathAnalysisInputLocation(
            tempDir.toString(), SourceType.Application, Collections.emptyList());
    JavaView view = new JavaView(location);
    JavaSootClass clazz =
        view.getClass(view.getIdentifierFactory().getClassType("AnnotatedSubject")).orElseThrow();

    // Verify class annotation element order
    AnnotationUsage classUsage = clazz.getAnnotations().iterator().next();
    assertEquals(
        Arrays.asList("first", "second", "third"),
        new ArrayList<>(classUsage.getValues().keySet()));

    // Verify method annotation element order
    JavaSootMethod method =
        clazz.getMethod("annotatedMethod", Collections.emptyList()).orElseThrow();
    AnnotationUsage methodUsage = method.getAnnotations().iterator().next();
    assertEquals(
        Arrays.asList("selector", "strongRef"), new ArrayList<>(methodUsage.getValues().keySet()));
  }
}
