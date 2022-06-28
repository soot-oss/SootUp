package de.upb.swt.soot.java.bytecode.frontend;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2020 Raja Vallée-Rai, Christian Brüggemann, Markus Schmidt and others
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

import de.upb.swt.soot.core.IdentifierFactory;
import de.upb.swt.soot.core.frontend.ResolveException;
import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.jimple.basic.NoPositionInformation;
import de.upb.swt.soot.core.model.*;
import de.upb.swt.soot.core.signatures.FieldSignature;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.java.core.*;
import de.upb.swt.soot.java.core.types.JavaClassType;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.annotation.Nonnull;
import org.objectweb.asm.tree.*;

/** A ClassSource that reads from Java bytecode */
class AsmClassSource extends JavaSootClassSource {

  @Nonnull private final ClassNode classNode;

  public AsmClassSource(
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

  @Nonnull
  public Collection<? extends SootMethod> resolveMethods() throws ResolveException {
    IdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();
    return classNode.methods.stream()
        .map(
            methodSource -> {
              AsmMethodSource asmClassClassSourceContent = (AsmMethodSource) methodSource;
              asmClassClassSourceContent.setDeclaringClass(classSignature);

              List<ClassType> exceptions = new ArrayList<>();
              exceptions.addAll(AsmUtil.asmIdToSignature(methodSource.exceptions));

              String methodName = methodSource.name;
              EnumSet<Modifier> modifiers = AsmUtil.getModifiers(methodSource.access);
              List<Type> sigTypes = AsmUtil.toJimpleSignatureDesc(methodSource.desc);
              Type retType = sigTypes.remove(sigTypes.size() - 1);

              MethodSignature methodSignature =
                  identifierFactory.getMethodSignature(
                      methodName, classSignature, retType, sigTypes);

              // TODO: position/line numbers if possible
              return new JavaSootMethod(
                  asmClassClassSourceContent,
                  methodSignature,
                  modifiers,
                  exceptions,
                  convertAnnotation(methodSource.invisibleAnnotations),
                  NoPositionInformation.getInstance());
            })
        .collect(Collectors.toSet());
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
    return Optional.ofNullable(AsmUtil.toJimpleClassType(classNode.superName));
  }

  @Nonnull
  public Optional<? extends ClassType> resolveOuterClass() {
    return Optional.ofNullable(AsmUtil.toJimpleClassType(classNode.outerClass));
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
