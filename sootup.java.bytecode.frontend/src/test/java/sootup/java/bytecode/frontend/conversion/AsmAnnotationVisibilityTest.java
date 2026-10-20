package sootup.java.bytecode.frontend.conversion;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.AnnotationNode;
import sootup.core.model.FieldModifier;
import sootup.core.model.SourceType;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.AnnotationUsage;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.JavaSootClass;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.jimple.basic.JavaLocal;
import sootup.java.core.views.JavaView;

class AsmAnnotationVisibilityTest {
  private static final String MARKER = "Lexample/Marker;";
  private static final String TYPE_MARKER = "Lexample/TypeMarker;";
  private final JavaIdentifierFactory factory = new JavaIdentifierFactory();
  @TempDir Path directory;

  @Test
  void classFieldAndMethodDeclarationsRetainVisibility() throws Exception {
    ClassWriter writer = writer("Subject", false);
    annotate(writer.visitAnnotation(MARKER, true));
    annotate(writer.visitAnnotation(MARKER, false));
    int superType = TypeReference.newSuperTypeReference(-1).getValue();
    annotate(writer.visitTypeAnnotation(superType, null, TYPE_MARKER, true));
    annotate(writer.visitTypeAnnotation(superType, null, TYPE_MARKER, false));
    FieldVisitor field = writer.visitField(Opcodes.ACC_PUBLIC, "field", "I", null, null);
    annotate(field.visitAnnotation(MARKER, true));
    annotate(field.visitAnnotation(MARKER, false));
    field.visitEnd();
    MethodVisitor method =
        writer.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT, "method", "()I", null, null);
    annotate(method.visitAnnotation(MARKER, true));
    annotate(method.visitAnnotation(MARKER, false));
    annotateReturnType(method);
    method.visitEnd();

    JavaSootClass clazz = load(writer, "Subject");
    assertEquals(
        List.of(
            usage(MARKER, true),
            usage(MARKER, false),
            usage(TYPE_MARKER, true),
            usage(TYPE_MARKER, false)),
        list(clazz.getAnnotations()));
    assertEquals(declarations(), list(clazz.getField("field").orElseThrow().getAnnotations()));
    JavaSootMethod sootMethod = method(clazz, "method");
    assertTrue(sootMethod.isAbstract());
    assertEquals(allAnnotations(), list(sootMethod.getAnnotations()));
  }

  @Test
  void annotationClassesHandleDeclarationsAndReturnTypesConsistently() throws Exception {
    ClassWriter writer = writer("AnnotationSubject", true);
    annotate(writer.visitAnnotation(MARKER, true));
    annotate(writer.visitAnnotation(MARKER, false));
    FieldVisitor field =
        writer.visitField(
            Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL, "VALUE", "I", null, 1);
    annotate(field.visitAnnotation(MARKER, true));
    annotate(field.visitAnnotation(MARKER, false));
    field.visitEnd();
    MethodVisitor method =
        writer.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT, "value", "()I", null, null);
    annotate(method.visitAnnotation(MARKER, true));
    annotate(method.visitAnnotation(MARKER, false));
    annotateReturnType(method);
    method.visitEnd();

    JavaSootClass clazz = load(writer, "AnnotationSubject");
    assertEquals(declarations(), list(clazz.getAnnotations()));
    assertEquals(declarations(), list(clazz.getField("VALUE").orElseThrow().getAnnotations()));
    assertEquals(allAnnotations(), list(method(clazz, "value").getAnnotations()));
  }

  @Test
  void abstractMethodParameterQueriesFilterBothGroupsAndExcludeTypeUse() throws Exception {
    ClassWriter writer = writer("AbstractParameters", false);
    MethodVisitor method =
        writer.visitMethod(
            Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT,
            "method",
            "(IJLjava/lang/String;)I",
            null,
            null);
    annotateParameters(method);
    method.visitEnd();
    JavaSootMethod sootMethod = method(load(writer, "AbstractParameters"), "method");
    assertEquals(declarations(), sootMethod.getParameterAnnotations(0));
    assertEquals(declarations(), sootMethod.getParameterAnnotations(0, "Any"));
    assertEquals(
        List.of(usage(MARKER, true)), sootMethod.getParameterAnnotations(0, "RuntimeVisible"));
    assertEquals(
        List.of(usage(MARKER, false)), sootMethod.getParameterAnnotations(0, "RuntimeInvisible"));
    assertEquals(List.of(usage(MARKER, false)), sootMethod.getParameterAnnotations(1));
    assertTrue(sootMethod.getParameterAnnotations(1, "RuntimeVisible").isEmpty());
    assertTrue(sootMethod.getParameterAnnotations(2).isEmpty());
  }

  @Test
  void parameterLocalsCombineDeclarationAndTypeUseWithTheirVisibility() throws Exception {
    ClassWriter writer = writer("ParameterLocals", false);
    MethodVisitor method =
        writer.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "method", "(IJ)I", null, null);
    annotateParameters(method);
    method.visitCode();
    method.visitVarInsn(Opcodes.ILOAD, 0);
    method.visitInsn(Opcodes.IRETURN);
    method.visitMaxs(1, 3);
    method.visitEnd();
    JavaSootMethod sootMethod = method(load(writer, "ParameterLocals"), "method");
    var body = sootMethod.getBody();
    assertEquals(allAnnotations(), list(((JavaLocal) body.getParameterLocal(0)).getAnnotations()));
    assertEquals(
        List.of(usage(MARKER, false)),
        list(((JavaLocal) body.getParameterLocal(1)).getAnnotations()));
    assertEquals(declarations(), sootMethod.getParameterAnnotations(0));
    JavaSootMethod copy = sootMethod.withBody(body);
    assertEquals(declarations(), copy.getParameterAnnotations(0));
    assertEquals(
        List.of(usage(MARKER, false)), copy.getParameterAnnotations(0, "RuntimeInvisible"));
  }

  @Test
  void localVariableTypeAnnotationsRetainBothVisibilities() throws Exception {
    ClassWriter writer = writer("LocalTypes", false);
    MethodVisitor method =
        writer.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "method", "(I)I", null, null);
    Label start = new Label();
    Label end = new Label();
    method.visitCode();
    method.visitLabel(start);
    method.visitVarInsn(Opcodes.ILOAD, 0);
    method.visitVarInsn(Opcodes.ISTORE, 1);
    method.visitVarInsn(Opcodes.ILOAD, 1);
    method.visitInsn(Opcodes.IRETURN);
    method.visitLabel(end);
    int target = TypeReference.newTypeReference(TypeReference.LOCAL_VARIABLE).getValue();
    for (boolean visible : new boolean[] {true, false}) {
      annotate(
          method.visitLocalVariableAnnotation(
              target,
              null,
              new Label[] {start},
              new Label[] {end},
              new int[] {1},
              TYPE_MARKER,
              visible));
    }
    method.visitMaxs(1, 2);
    method.visitEnd();
    var body = method(load(writer, "LocalTypes"), "method").getBody();
    List<AnnotationUsage> annotations = new ArrayList<>();
    body.getLocals()
        .forEach(
            local -> {
              if (local instanceof JavaLocal) {
                ((JavaLocal) local).getAnnotations().forEach(annotations::add);
              }
            });
    assertEquals(List.of(usage(TYPE_MARKER, true), usage(TYPE_MARKER, false)), annotations);
  }

  @Test
  void nestedValuesAndRepeatableContainersInheritAttributeVisibility() throws Exception {
    ClassWriter writer = writer("NestedValues", false);
    for (boolean visible : new boolean[] {true, false}) {
      AnnotationVisitor outer = writer.visitAnnotation("Lexample/Container;", visible);
      annotate(outer.visitAnnotation("nested", MARKER));
      AnnotationVisitor array = outer.visitArray("value");
      annotate(array.visitAnnotation(null, MARKER));
      annotate(array.visitAnnotation(null, MARKER));
      array.visitEnd();
      outer.visitEnd();
    }
    List<AnnotationUsage> usages = list(load(writer, "NestedValues").getAnnotations());
    assertEquals(2, usages.size());
    for (int i = 0; i < usages.size(); i++) {
      AnnotationUsage outer = usages.get(i);
      boolean visible = i == 0;
      assertEquals(visible, outer.isRuntimeVisible());
      assertEquals(usage(MARKER, visible), outer.getValues().get("nested"));
      assertEquals(
          List.of(usage(MARKER, visible), usage(MARKER, visible)), outer.getValues().get("value"));
    }
  }

  @Test
  void memberCopiesPreserveVisibilityAndExplicitAnnotationRemoval() throws Exception {
    ClassWriter writer = writer("Copies", false);
    FieldVisitor field = writer.visitField(Opcodes.ACC_PUBLIC, "field", "I", null, null);
    annotate(field.visitAnnotation(MARKER, true));
    annotate(field.visitAnnotation(MARKER, false));
    field.visitEnd();
    MethodVisitor method =
        writer.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT, "method", "()I", null, null);
    annotate(method.visitAnnotation(MARKER, true));
    annotate(method.visitAnnotation(MARKER, false));
    method.visitEnd();
    JavaSootClass clazz = load(writer, "Copies");
    var sootField = clazz.getField("field").orElseThrow();
    var renamed =
        sootField.withSignature(
            factory.getFieldSignature("renamed", clazz.getType(), sootField.getType()));
    assertEquals(declarations(), list(renamed.getAnnotations()));
    assertEquals(
        declarations(),
        list(sootField.withModifiers(EnumSet.of(FieldModifier.PRIVATE)).getAnnotations()));
    assertTrue(list(sootField.withAnnotations(List.of()).getAnnotations()).isEmpty());
    JavaSootMethod sootMethod = method(clazz, "method");
    assertEquals(
        declarations(), list(sootMethod.withModifiers(sootMethod.getModifiers()).getAnnotations()));
    assertEquals(
        declarations(),
        list(sootMethod.withOverridingMethodSource(source -> source).getAnnotations()));
    assertTrue(list(sootMethod.withAnnotations(List.of()).getAnnotations()).isEmpty());
  }

  @Test
  void absentAttributesProduceEmptyAnnotations() throws Exception {
    ClassWriter writer = writer("Unannotated", false);
    writer.visitField(Opcodes.ACC_PUBLIC, "field", "I", null, null).visitEnd();
    writer
        .visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT, "method", "(I)I", null, null)
        .visitEnd();
    JavaSootClass clazz = load(writer, "Unannotated");
    assertTrue(list(clazz.getAnnotations()).isEmpty());
    assertTrue(list(clazz.getField("field").orElseThrow().getAnnotations()).isEmpty());
    JavaSootMethod method = method(clazz, "method");
    assertTrue(list(method.getAnnotations()).isEmpty());
    for (String visibility : List.of("Any", "RuntimeVisible", "RuntimeInvisible")) {
      assertTrue(method.getParameterAnnotations(0, visibility).isEmpty());
    }
  }

  @Test
  void existingConversionOverloadsAndAnnotationDefaultsKeepTheirDefaultVisibility()
      throws Exception {
    AnnotationNode node = new AnnotationNode(MARKER);
    node.visit("value", "same");
    assertEquals(usage(MARKER, true), AsmUtil.createAnnotationUsage(node, factory));
    assertEquals(
        List.of(usage(MARKER, true)), list(AsmUtil.createAnnotationUsage(List.of(node), factory)));
    assertEquals(usage(MARKER, false), AsmUtil.createAnnotationUsage(node, factory, false));
    assertTrue(
        list(AsmUtil.createAnnotationUsage(Collections.<AnnotationNode>emptyList(), factory, false))
            .isEmpty());

    ClassWriter writer = writer("AnnotationDefaults", true);
    MethodVisitor method =
        writer.visitMethod(
            Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT, "value", "()Lexample/Marker;", null, null);
    AnnotationVisitor defaultValue = method.visitAnnotationDefault();
    annotate(defaultValue.visitAnnotation(null, MARKER));
    defaultValue.visitEnd();
    method.visitEnd();
    assertEquals(
        usage(MARKER, true),
        load(writer, "AnnotationDefaults").getAnnotationDefaultValues().get("value"));
  }

  private static ClassWriter writer(String name, boolean annotation) {
    ClassWriter writer = new ClassWriter(0);
    int access = Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT;
    if (annotation) {
      access |= Opcodes.ACC_INTERFACE | Opcodes.ACC_ANNOTATION;
    }
    writer.visit(
        Opcodes.V1_8,
        access,
        name,
        null,
        "java/lang/Object",
        annotation ? new String[] {"java/lang/annotation/Annotation"} : null);
    return writer;
  }

  private static void annotate(AnnotationVisitor visitor) {
    visitor.visit("value", "same");
    visitor.visitEnd();
  }

  private static void annotateReturnType(MethodVisitor method) {
    int target = TypeReference.newTypeReference(TypeReference.METHOD_RETURN).getValue();
    annotate(method.visitTypeAnnotation(target, null, TYPE_MARKER, true));
    annotate(method.visitTypeAnnotation(target, null, TYPE_MARKER, false));
  }

  private static void annotateParameters(MethodVisitor method) {
    annotate(method.visitParameterAnnotation(0, MARKER, true));
    annotate(method.visitParameterAnnotation(0, MARKER, false));
    annotate(method.visitParameterAnnotation(1, MARKER, false));
    int target = TypeReference.newFormalParameterReference(0).getValue();
    annotate(method.visitTypeAnnotation(target, null, TYPE_MARKER, true));
    annotate(method.visitTypeAnnotation(target, null, TYPE_MARKER, false));
  }

  private AnnotationUsage usage(String descriptor, boolean visible) {
    return new AnnotationUsage(
        factory.getClassType(AsmUtil.toQualifiedName(descriptor)),
        Map.of("value", sootup.java.core.language.JavaJimple.newStringConstant("same", factory)),
        visible);
  }

  private List<AnnotationUsage> declarations() {
    return List.of(usage(MARKER, true), usage(MARKER, false));
  }

  private List<AnnotationUsage> allAnnotations() {
    return List.of(
        usage(MARKER, true),
        usage(MARKER, false),
        usage(TYPE_MARKER, true),
        usage(TYPE_MARKER, false));
  }

  private static List<AnnotationUsage> list(Iterable<AnnotationUsage> usages) {
    List<AnnotationUsage> result = new ArrayList<>();
    usages.forEach(result::add);
    return result;
  }

  private static JavaSootMethod method(JavaSootClass clazz, String name) {
    return clazz.getMethodsByName(name).iterator().next();
  }

  private JavaSootClass load(ClassWriter writer, String name) throws Exception {
    writer.visitEnd();
    Files.write(directory.resolve(name + ".class"), writer.toByteArray());
    JavaView view =
        new JavaView(
            new JavaClassPathAnalysisInputLocation(
                directory.toString(), SourceType.Application, List.of()));
    // Annotation declarations are deliberately absent: visibility comes from the attributes.
    return view.getClass(factory.getClassType(name)).orElseThrow();
  }
}
