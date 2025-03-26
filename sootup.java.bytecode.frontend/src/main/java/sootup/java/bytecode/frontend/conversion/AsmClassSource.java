package sootup.java.bytecode.frontend.conversion;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2020 Raja Vallée-Rai, Christian Brüggemann, Markus Schmidt, Kadiray Karakaya and others
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

import com.google.common.collect.Streams;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.jspecify.annotations.NonNull;
import org.objectweb.asm.tree.*;
import sootup.core.IdentifierFactory;
import sootup.core.frontend.ResolveException;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.jimple.basic.NoPositionInformation;
import sootup.core.model.*;
import sootup.core.signatures.FieldSignature;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.types.Type;
import sootup.core.util.Modifiers;
import sootup.java.core.*;
import sootup.java.core.types.JavaClassType;

/** A ClassSource that reads from Java bytecode */
class AsmClassSource extends JavaSootClassSource {

  @NonNull private final ClassNode classNode;

  public AsmClassSource(
      @NonNull final AnalysisInputLocation inputLocation,
      @NonNull final Path sourcePath,
      @NonNull final ClassType javaClassType,
      @NonNull final ClassNode classNode) {
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
                  Streams.concat(
                          convertAnnotation(fieldNode.visibleAnnotations),
                          convertAnnotation(fieldNode.invisibleAnnotations))
                      .collect(Collectors.toList()),
                  NoPositionInformation.getInstance());
            })
        .collect(Collectors.toSet());
  }

  protected static Stream<AnnotationUsage> convertAnnotation(List<? extends AnnotationNode> nodes) {
    if (nodes == null) {
      return Stream.empty();
    }
    return StreamSupport.stream(AsmUtil.createAnnotationUsage(nodes).spliterator(), false);
  }

  @Override
  protected Iterable<AnnotationUsage> resolveAnnotations() {
    Stream<AnnotationUsage> annotations =
        Streams.concat(
            convertAnnotation(classNode.visibleAnnotations),
            convertAnnotation(classNode.invisibleAnnotations),
            convertAnnotation(classNode.visibleTypeAnnotations),
            convertAnnotation(classNode.invisibleTypeAnnotations));
    return annotations.collect(Collectors.toList());
  }

  @NonNull
  public Collection<JavaSootMethod> resolveMethods() throws ResolveException {
    IdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();
    return classNode.methods.stream()
        .map(
            methodSource -> {
              AsmMethodSource asmClassClassSourceContent = (AsmMethodSource) methodSource;
              asmClassClassSourceContent.setDeclaringClass(classSignature);

              List<ClassType> exceptions =
                  new ArrayList<>(AsmUtil.asmIdToSignature(methodSource.exceptions));

              String methodName = methodSource.name;
              EnumSet<MethodModifier> modifiers = Modifiers.getMethodModifiers(methodSource.access);
              List<Type> sigTypes = AsmUtil.toJimpleSignatureDesc(methodSource.desc);
              Type retType = sigTypes.remove(sigTypes.size() - 1);

              MethodSignature methodSignature =
                  identifierFactory.getMethodSignature(
                      classSignature, methodName, retType, sigTypes);

              // TODO: position/line numbers if possible
              return new JavaSootMethod(
                  asmClassClassSourceContent,
                  methodSignature,
                  modifiers,
                  exceptions,
                  Streams.concat(
                          convertAnnotation(methodSource.visibleAnnotations),
                          convertAnnotation(methodSource.invisibleAnnotations))
                      .collect(Collectors.toList()),
                  NoPositionInformation.getInstance());
            })
        .collect(Collectors.toSet());
  }

  @Override
  @NonNull
  public Collection<JavaSootField> resolveFields() throws ResolveException {
    IdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();
    return resolveFields(classNode.fields, identifierFactory, classSignature);
  }

  @NonNull
  public EnumSet<ClassModifier> resolveModifiers() {
    return Modifiers.getClassModifiers(classNode.access);
  }

  @NonNull
  public Set<JavaClassType> resolveInterfaces() {
    return new HashSet<>(AsmUtil.asmIdToSignature(classNode.interfaces));
  }

  @NonNull
  public Optional<JavaClassType> resolveSuperclass() {
    if (classNode.superName == null) {
      return Optional.empty();
    }
    return Optional.of(AsmUtil.toJimpleClassType(classNode.superName));
  }

  @NonNull
  public Optional<JavaClassType> resolveOuterClass() {
    if (classNode.outerClass == null) {
      return Optional.empty();
    }
    return Optional.of(AsmUtil.toJimpleClassType(classNode.outerClass));
  }

  @NonNull
  public Position resolvePosition() {
    // TODO [ms]: implement line numbers for bytecode
    return NoPositionInformation.getInstance();
  }

  @Override
  public String toString() {
    return getSourcePath().toString();
  }
}
