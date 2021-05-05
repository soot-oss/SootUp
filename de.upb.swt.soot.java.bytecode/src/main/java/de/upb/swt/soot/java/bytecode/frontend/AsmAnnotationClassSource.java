package de.upb.swt.soot.java.bytecode.frontend;

import de.upb.swt.soot.core.IdentifierFactory;
import de.upb.swt.soot.core.frontend.ResolveException;
import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.jimple.basic.NoPositionInformation;
import de.upb.swt.soot.core.model.Modifier;
import de.upb.swt.soot.core.model.Position;
import de.upb.swt.soot.core.model.SootField;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.FieldSignature;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.java.core.AnnotationUsage;
import de.upb.swt.soot.java.core.JavaAnnotationSootClassSource;
import de.upb.swt.soot.java.core.JavaAnnotationSootMethod;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import de.upb.swt.soot.java.core.JavaSootClass;
import de.upb.swt.soot.java.core.JavaSootField;
import de.upb.swt.soot.java.core.types.JavaClassType;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nonnull;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

public class AsmAnnotationClassSource extends JavaAnnotationSootClassSource {

  @Nonnull protected final ClassNode classNode;

  public AsmAnnotationClassSource(
      AnalysisInputLocation<JavaSootClass> inputLocation,
      Path sourcePath,
      JavaClassType javaClassType,
      @Nonnull ClassNode classNode) {
    super(inputLocation, javaClassType, sourcePath);
    this.classNode = classNode;
  }

  private static Set<JavaSootField> resolveFields(
      List<FieldNode> fieldNodes, IdentifierFactory signatureFactory, ClassType classSignature) {
    return fieldNodes.stream()
        .map(
            fieldNode -> {
              String fieldName = fieldNode.name;
              Type fieldType = AsmUtil.toJimpleType(fieldNode.desc);
              FieldSignature fieldSignature =
                  signatureFactory.getFieldSignature(fieldName, classSignature, fieldType);
              EnumSet<Modifier> modifiers = AsmUtil.getModifiers(fieldNode.access);

              // TODO: add Position info
              return new JavaSootField(
                  fieldSignature,
                  modifiers,
                  convertAnnotation(fieldNode.invisibleAnnotations),
                  NoPositionInformation.getInstance());
            })
        .collect(Collectors.toSet());
  }

  @Nonnull
  public Collection<? extends SootMethod> resolveMethods() throws ResolveException {
    IdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();
    return resolveMethods(classNode.methods, identifierFactory, classSignature)
        .collect(Collectors.toSet());
  }

  private static Stream<JavaAnnotationSootMethod> resolveMethods(
      List<MethodNode> methodNodes, IdentifierFactory signatureFactory, ClassType cs) {
    return methodNodes.stream()
        .map(
            methodSource -> {
              AsmMethodSource asmClassClassSourceContent = (AsmMethodSource) methodSource;
              asmClassClassSourceContent.setDeclaringClass(cs);

              List<ClassType> exceptions = new ArrayList<>();
              exceptions.addAll(AsmUtil.asmIdToSignature(methodSource.exceptions));

              String methodName = methodSource.name;
              EnumSet<Modifier> modifiers = AsmUtil.getModifiers(methodSource.access);
              List<Type> sigTypes = AsmUtil.toJimpleSignatureDesc(methodSource.desc);
              Type retType = sigTypes.remove(sigTypes.size() - 1);

              MethodSignature methodSignature =
                  signatureFactory.getMethodSignature(methodName, cs, retType, sigTypes);

              // TODO: position/line numbers if possible

              return new JavaAnnotationSootMethod(
                  asmClassClassSourceContent,
                  methodSignature,
                  modifiers,
                  exceptions,
                  convertAnnotation(methodSource.invisibleAnnotations),
                  NoPositionInformation.getInstance());
            });
  }

  protected static List<AnnotationUsage> convertAnnotation(List<AnnotationNode> nodes) {
    if (nodes == null) {
      return Collections.emptyList();
    }
    return StreamSupport.stream(AsmUtil.createAnnotationUsage(nodes).spliterator(), false)
        .collect(Collectors.toList());
  }

  @Override
  protected Iterable<AnnotationUsage> resolveAnnotations() {
    List<AnnotationNode> annotationNodes = new ArrayList<>();

    annotationNodes.addAll(
        classNode.visibleAnnotations != null
            ? classNode.visibleAnnotations
            : Collections.emptyList());
    annotationNodes.addAll(
        classNode.visibleTypeAnnotations != null
            ? classNode.visibleTypeAnnotations
            : Collections.emptyList());
    annotationNodes.addAll(
        classNode.invisibleAnnotations != null
            ? classNode.invisibleAnnotations
            : Collections.emptyList());
    annotationNodes.addAll(
        classNode.invisibleTypeAnnotations != null
            ? classNode.invisibleTypeAnnotations
            : Collections.emptyList());

    return convertAnnotation(annotationNodes);
  }

  @Override
  @Nonnull
  public Collection<? extends SootField> resolveFields() throws ResolveException {
    IdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();
    return resolveFields(classNode.fields, identifierFactory, classSignature);
  }

  @Nonnull
  public EnumSet<Modifier> resolveModifiers() {
    return AsmUtil.getModifiers(classNode.access);
  }

  @Nonnull
  public Set<? extends ClassType> resolveInterfaces() {
    return new HashSet<>(AsmUtil.asmIdToSignature(classNode.interfaces));
  }

  @Nonnull
  public Optional<? extends ClassType> resolveSuperclass() {
    if (classNode.superName == null) {
      return Optional.empty();
    }
    return Optional.ofNullable(AsmUtil.asmIDToSignature(classNode.superName));
  }

  @Nonnull
  public Optional<? extends ClassType> resolveOuterClass() {
    return Optional.ofNullable(AsmUtil.asmIDToSignature(classNode.outerClass));
  }

  @Nonnull
  public Position resolvePosition() {
    // TODO [ms]: implement line numbers for bytecode
    return NoPositionInformation.getInstance();
  }

  @Override
  public String toString() {
    return getSourcePath().toString();
  }
}
