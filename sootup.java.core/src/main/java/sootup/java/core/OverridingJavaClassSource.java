package sootup.java.core;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2019-2020 Christian Brüggemann, Hasitha Rajapakse, Markus Schmidt
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
import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import sootup.core.frontend.ResolveException;
import sootup.core.frontend.SootClassSource;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.ClassModifier;
import sootup.core.model.Position;
import sootup.core.model.SootField;
import sootup.core.model.SootMethod;
import sootup.core.types.ClassType;
import sootup.core.util.CollectionUtils;

/**
 * Allows for replacing specific parts of a class, such as fields and methods or, allows to resolve
 * classes that are batchparsed like .java files using wala java source frontend or in tests where
 * all information is already existing.
 *
 * <p>When replacing specific parts of a class by default, it delegates to the {@link
 * SootClassSource} delegate provided in the constructor.
 *
 * <p>To alter the results of invocations to e.g. {@link #resolveFields()}, simply call {@link
 * #withFields(Collection)} to obtain a new {@link OverridingJavaClassSource}. The new instance will
 * then use the supplied value instead of calling {@link #resolveFields()} on the delegate.
 *
 * @author Christian Brüggemann, Hasitha Rajapakse
 */
@SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "OptionalAssignedToNull"})
public class OverridingJavaClassSource extends JavaSootClassSource {

  @Nullable private final Collection<SootMethod> overriddenSootMethods;
  @Nullable private final Collection<SootField> overriddenSootFields;
  @Nullable private final Set<ClassModifier> overriddenModifiers;
  @Nullable private final Set<ClassType> overriddenInterfaces;
  @Nullable private final Optional<ClassType> overriddenSuperclass;
  @Nullable private final Optional<ClassType> overriddenOuterClass;
  @Nullable private final Position position;

  @Nullable private final JavaSootClassSource delegate;
  @Nullable private final Iterable<AnnotationUsage> annotations;
  @Nullable private final Iterable<AnnotationUsage> methodAnnotations;
  @Nullable private final Iterable<AnnotationUsage> fieldAnnotations;

  public OverridingJavaClassSource(@Nonnull JavaSootClassSource delegate) {
    super(delegate);
    this.delegate = delegate;
    overriddenSootMethods = null;
    overriddenSootFields = null;
    overriddenModifiers = null;
    overriddenInterfaces = null;
    overriddenSuperclass = null;
    overriddenOuterClass = null;
    position = null;
    annotations = null;
    methodAnnotations = null;
    fieldAnnotations = null;
  }

  private OverridingJavaClassSource(
      @Nullable Collection<SootMethod> overriddenSootMethods,
      @Nullable Collection<SootField> overriddenSootFields,
      @Nullable Set<ClassModifier> overriddenModifiers,
      @Nullable Set<ClassType> overriddenInterfaces,
      @Nullable Optional<ClassType> overriddenSuperclass,
      @Nullable Optional<ClassType> overriddenOuterClass,
      @Nullable Position position,
      @Nullable Iterable<AnnotationUsage> annotations,
      @Nullable Iterable<AnnotationUsage> methodAnnotations,
      @Nullable Iterable<AnnotationUsage> fieldAnnotations,
      @Nonnull JavaSootClassSource delegate) {
    super(delegate);
    this.overriddenSootMethods = overriddenSootMethods;
    this.overriddenSootFields = overriddenSootFields;
    this.overriddenModifiers = overriddenModifiers;
    this.overriddenInterfaces = overriddenInterfaces;
    this.overriddenSuperclass = overriddenSuperclass;
    this.overriddenOuterClass = overriddenOuterClass;
    this.position = position;
    this.delegate = delegate;
    this.annotations = annotations;
    this.methodAnnotations = methodAnnotations;
    this.fieldAnnotations = fieldAnnotations;
  }

  /** Class source where all information already available */
  public OverridingJavaClassSource(
      @Nonnull AnalysisInputLocation<JavaSootClass> srcNamespace,
      @Nonnull Path sourcePath,
      @Nonnull ClassType classType,
      @Nullable ClassType superClass,
      @Nonnull Set<ClassType> interfaces,
      @Nullable ClassType outerClass,
      @Nonnull Set<SootField> sootFields,
      @Nonnull Set<SootMethod> sootMethods,
      @Nonnull Position position,
      @Nonnull EnumSet<ClassModifier> modifiers,
      @Nonnull Iterable<AnnotationUsage> annotations,
      @Nonnull Iterable<AnnotationUsage> methodAnnotations,
      @Nullable Iterable<AnnotationUsage> fieldAnnotations) {
    super(srcNamespace, classType, sourcePath);

    this.delegate = null;
    this.overriddenSootMethods = sootMethods;
    this.overriddenSootFields = sootFields;
    this.overriddenModifiers = modifiers;
    this.overriddenInterfaces = interfaces;
    this.overriddenSuperclass = Optional.ofNullable(superClass);
    this.overriddenOuterClass = Optional.ofNullable(outerClass);
    this.position = position;
    this.annotations = annotations;
    this.methodAnnotations = methodAnnotations;
    this.fieldAnnotations = fieldAnnotations;
  }

  @Nonnull
  @Override
  public Collection<? extends SootMethod> resolveMethods() throws ResolveException {
    return overriddenSootMethods != null ? overriddenSootMethods : delegate.resolveMethods();
  }

  @Nonnull
  @Override
  public Collection<? extends SootField> resolveFields() throws ResolveException {
    return overriddenSootFields != null ? overriddenSootFields : delegate.resolveFields();
  }

  @Nonnull
  @Override
  public Set<ClassModifier> resolveModifiers() {
    return overriddenModifiers != null ? overriddenModifiers : delegate.resolveModifiers();
  }

  @Nonnull
  @Override
  public Set<? extends ClassType> resolveInterfaces() {
    return overriddenInterfaces != null ? overriddenInterfaces : delegate.resolveInterfaces();
  }

  @Nonnull
  @Override
  public Optional<? extends ClassType> resolveSuperclass() {
    return overriddenSuperclass != null ? overriddenSuperclass : delegate.resolveSuperclass();
  }

  @Nonnull
  @Override
  public Optional<? extends ClassType> resolveOuterClass() {
    return overriddenOuterClass != null ? overriddenOuterClass : delegate.resolveOuterClass();
  }

  @Nonnull
  @Override
  public Position resolvePosition() {
    return position != null ? position : delegate.resolvePosition();
  }

  @Override
  @Nonnull
  protected Iterable<AnnotationUsage> resolveAnnotations() {
    return annotations != null ? annotations : delegate.resolveAnnotations();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OverridingJavaClassSource that = (OverridingJavaClassSource) o;
    return Objects.equals(this.overriddenSuperclass, that.overriddenSuperclass)
        && Objects.equals(this.overriddenInterfaces, that.overriddenInterfaces)
        && Objects.equals(this.overriddenOuterClass, that.overriddenOuterClass)
        && Objects.equals(this.overriddenSootFields, that.overriddenSootFields)
        && Objects.equals(this.overriddenSootMethods, that.overriddenSootMethods)
        && Objects.equals(position, that.position)
        && Objects.equals(this.overriddenModifiers, that.overriddenModifiers)
        && Objects.equals(this.classSignature, that.classSignature)
        && Objects.equals(this.annotations, that.annotations)
        && Objects.equals(this.methodAnnotations, that.methodAnnotations);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        this.overriddenSuperclass,
        this.overriddenInterfaces,
        this.overriddenOuterClass,
        this.overriddenSootFields,
        this.overriddenSootMethods,
        this.position,
        this.overriddenModifiers,
        this.classSignature,
        annotations,
        methodAnnotations);
  }

  @Override
  public String toString() {
    return "frontend.OverridingClassSource{"
        + "superClass="
        + this.overriddenSuperclass
        + ", interfaces="
        + this.overriddenInterfaces
        + ", outerClass="
        + this.overriddenOuterClass
        + ", sootFields="
        + this.overriddenSootFields
        + ", sootMethods="
        + this.overriddenSootMethods
        + ", position="
        + this.position
        + ", modifiers="
        + this.overriddenModifiers
        + ", classType="
        + this.classSignature
        + '}';
  }

  @Nonnull
  public OverridingJavaClassSource withReplacedMethod(
      @Nonnull SootMethod toReplace, @Nonnull SootMethod replacement) {
    Set<SootMethod> newMethods = new HashSet<>(resolveMethods());
    CollectionUtils.replace(newMethods, toReplace, replacement);
    return withMethods(newMethods);
  }

  @Nonnull
  public OverridingJavaClassSource withMethods(
      @Nonnull Collection<SootMethod> overriddenSootMethods) {
    return new OverridingJavaClassSource(
        overriddenSootMethods,
        overriddenSootFields,
        overriddenModifiers,
        overriddenInterfaces,
        overriddenSuperclass,
        overriddenOuterClass,
        position,
        annotations,
        methodAnnotations,
        fieldAnnotations,
        delegate);
  }

  @Nonnull
  public OverridingJavaClassSource withReplacedField(
      @Nonnull SootField toReplace, @Nonnull SootField replacement) {
    Set<SootField> newFields = new HashSet<>(resolveFields());
    CollectionUtils.replace(newFields, toReplace, replacement);
    return withFields(newFields);
  }

  @Nonnull
  public OverridingJavaClassSource withFields(@Nonnull Collection<SootField> overriddenSootFields) {
    return new OverridingJavaClassSource(
        overriddenSootMethods,
        overriddenSootFields,
        overriddenModifiers,
        overriddenInterfaces,
        overriddenSuperclass,
        overriddenOuterClass,
        position,
        annotations,
        methodAnnotations,
        fieldAnnotations,
        delegate);
  }

  @Nonnull
  public OverridingJavaClassSource withModifiers(@Nonnull Set<ClassModifier> overriddenModifiers) {
    return new OverridingJavaClassSource(
        overriddenSootMethods,
        overriddenSootFields,
        overriddenModifiers,
        overriddenInterfaces,
        overriddenSuperclass,
        overriddenOuterClass,
        position,
        annotations,
        methodAnnotations,
        fieldAnnotations,
        delegate);
  }

  @Nonnull
  public OverridingJavaClassSource withInterfaces(@Nonnull Set<ClassType> overriddenInterfaces) {
    return new OverridingJavaClassSource(
        overriddenSootMethods,
        overriddenSootFields,
        overriddenModifiers,
        overriddenInterfaces,
        overriddenSuperclass,
        overriddenOuterClass,
        position,
        annotations,
        methodAnnotations,
        fieldAnnotations,
        delegate);
  }

  @Nonnull
  public OverridingJavaClassSource withSuperclass(
      @Nonnull Optional<ClassType> overriddenSuperclass) {
    return new OverridingJavaClassSource(
        overriddenSootMethods,
        overriddenSootFields,
        overriddenModifiers,
        overriddenInterfaces,
        overriddenSuperclass,
        overriddenOuterClass,
        position,
        annotations,
        methodAnnotations,
        fieldAnnotations,
        delegate);
  }

  @Nonnull
  public OverridingJavaClassSource withOuterClass(
      @Nonnull Optional<ClassType> overriddenOuterClass) {
    return new OverridingJavaClassSource(
        overriddenSootMethods,
        overriddenSootFields,
        overriddenModifiers,
        overriddenInterfaces,
        overriddenSuperclass,
        overriddenOuterClass,
        position,
        annotations,
        methodAnnotations,
        fieldAnnotations,
        delegate);
  }

  @Nonnull
  public OverridingJavaClassSource withPosition(@Nullable Position position) {
    return new OverridingJavaClassSource(
        overriddenSootMethods,
        overriddenSootFields,
        overriddenModifiers,
        overriddenInterfaces,
        overriddenSuperclass,
        overriddenOuterClass,
        position,
        annotations,
        methodAnnotations,
        fieldAnnotations,
        delegate);
  }
}
