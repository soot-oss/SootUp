package sootup.java.bytecode.frontend;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2021 Raja Vallée-Rai, Christian Brüggemann, Markus Schmidt, Bastian Haverkamp and others
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
import sootup.core.IdentifierFactory;
import sootup.core.frontend.ResolveException;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.jimple.basic.NoPositionInformation;
import sootup.core.model.*;
import sootup.core.signatures.FieldSignature;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.types.Type;
import sootup.java.core.AnnotationUsage;
import sootup.java.core.JavaAnnotationSootClassSource;
import sootup.java.core.JavaAnnotationSootMethod;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.JavaSootField;
import sootup.java.core.types.JavaClassType;

public class AsmAnnotationClassSource extends JavaAnnotationSootClassSource {

  @Nonnull protected final ClassNode classNode;

  public AsmAnnotationClassSource(
      AnalysisInputLocation<? extends SootClass<?>> inputLocation,
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
              EnumSet<FieldModifier> modifiers = AsmUtil.getFieldModifiers(fieldNode.access);

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
              EnumSet<MethodModifier> modifiers = AsmUtil.getMethodModifiers(methodSource.access);
              List<Type> sigTypes = AsmUtil.toJimpleSignatureDesc(methodSource.desc);
              Type retType = sigTypes.remove(sigTypes.size() - 1);

              MethodSignature methodSignature =
                  signatureFactory.getMethodSignature(cs, methodName, retType, sigTypes);

              List<AnnotationNode> annotations = new ArrayList<>();
              if (methodSource.visibleAnnotations != null)
                annotations.addAll(methodSource.visibleAnnotations);
              if (methodSource.invisibleAnnotations != null)
                annotations.addAll(methodSource.invisibleAnnotations);

              // TODO: position/line numbers if possible

              return new JavaAnnotationSootMethod(
                  asmClassClassSourceContent,
                  methodSignature,
                  modifiers,
                  exceptions,
                  convertAnnotation(annotations),
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
  public EnumSet<ClassModifier> resolveModifiers() {
    return AsmUtil.getClassModifiers(classNode.access);
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
    return Optional.ofNullable(AsmUtil.toJimpleClassType(classNode.superName));
  }

  @Nonnull
  public Optional<? extends ClassType> resolveOuterClass() {
    if (classNode.outerClass == null) {
      return Optional.empty();
    }
    return Optional.of(AsmUtil.toJimpleClassType(classNode.outerClass));
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
