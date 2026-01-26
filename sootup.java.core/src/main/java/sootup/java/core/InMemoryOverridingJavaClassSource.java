package sootup.java.core;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2026 Ashik Mogasavara Ravikumar
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
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import sootup.core.frontend.ResolveException;
import sootup.core.frontend.SootClassSource;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.jimple.basic.NoPositionInformation;
import sootup.core.model.ClassModifier;
import sootup.core.model.Position;
import sootup.core.model.SootField;
import sootup.core.model.SootMethod;
import sootup.core.types.ClassType;
import sootup.java.core.types.JavaClassType;

/**
 * An in-memory overriding implementation of {@link SootClassSource} for Java classes.
 *
 * <p>This class supplies fully controlled data (methods, fields, modifiers, interfaces, superclass,
 * outer class, annotations and position) for use in analyses or tests where a class source should
 * be provided. * @author Ashik Mogasavara Ravikumar
 */
public class InMemoryOverridingJavaClassSource extends JavaSootClassSource {

  @NonNull private final Collection<JavaSootMethod> overriddenSootMethods;
  @NonNull private final Collection<JavaSootField> overriddenSootFields;
  @NonNull private final Set<ClassModifier> overriddenModifiers;
  @NonNull private final Set<JavaClassType> overriddenInterfaces;
  @Nullable private final Optional<JavaClassType> overriddenSuperclass;
  @Nullable private final Optional<JavaClassType> overriddenOuterClass;
  @NonNull private final Position position;

  @NonNull private final Iterable<AnnotationUsage> annotations;
  @NonNull private final Iterable<AnnotationUsage> methodAnnotations;
  @NonNull private final Iterable<AnnotationUsage> fieldAnnotations;

  public InMemoryOverridingJavaClassSource(
      @NonNull AnalysisInputLocation srcNamespace,
      @NonNull Path sourcePath,
      @NonNull ClassType classType,
      @Nullable JavaClassType superClass,
      @NonNull Set<JavaClassType> interfaces,
      @Nullable JavaClassType outerClass,
      @NonNull Set<JavaSootField> sootFields,
      @NonNull Set<JavaSootMethod> sootMethods,
      @NonNull Position position,
      @NonNull EnumSet<ClassModifier> modifiers,
      @NonNull Iterable<AnnotationUsage> annotations,
      @NonNull Iterable<AnnotationUsage> methodAnnotations,
      @NonNull Iterable<AnnotationUsage> fieldAnnotations) {
    super(srcNamespace, classType, sourcePath);
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

  public InMemoryOverridingJavaClassSource(
      @NonNull Set<JavaSootMethod> sootMethods,
      @NonNull Set<JavaSootField> sootFields,
      @NonNull EnumSet<ClassModifier> modifiers,
      @NonNull Set<JavaClassType> interfaces,
      @Nullable JavaClassType superClass,
      @Nullable JavaClassType outerClass,
      @NonNull Position position,
      @NonNull Path sourcePath,
      @NonNull ClassType classType,
      @NonNull AnalysisInputLocation srcNamespace) {
    super(srcNamespace, classType, sourcePath);
    this.overriddenSootMethods = sootMethods;
    this.overriddenSootFields = sootFields;
    this.overriddenModifiers = modifiers;
    this.overriddenInterfaces = interfaces;
    this.overriddenSuperclass = Optional.ofNullable(superClass);
    this.overriddenOuterClass = Optional.ofNullable(outerClass);
    this.position = position;
    this.annotations = Collections.emptyList();
    this.methodAnnotations = Collections.emptyList();
    this.fieldAnnotations = Collections.emptyList();
  }

  @Override
  protected Iterable<AnnotationUsage> resolveAnnotations() {
    return this.annotations != null ? this.annotations : Collections.emptyList();
  }

  @Override
  public @NonNull Collection<? extends SootMethod> resolveMethods() throws ResolveException {
    return this.overriddenSootMethods != null ? this.overriddenSootMethods : List.of();
  }

  @Override
  public @NonNull Collection<? extends SootField> resolveFields() throws ResolveException {
    return this.overriddenSootFields != null ? this.overriddenSootFields : List.of();
  }

  @Override
  public @NonNull Set<ClassModifier> resolveModifiers() {
    return this.overriddenModifiers != null ? this.overriddenModifiers : Set.of();
  }

  @Override
  public @NonNull Set<? extends ClassType> resolveInterfaces() {
    return this.overriddenInterfaces != null ? this.overriddenInterfaces : Set.of();
  }

  @Override
  public @NonNull Optional<? extends ClassType> resolveSuperclass() {
    return this.overriddenSuperclass != null ? this.overriddenSuperclass : Optional.empty();
  }

  @Override
  public @NonNull Optional<? extends ClassType> resolveOuterClass() {
    return this.overriddenOuterClass != null ? this.overriddenOuterClass : Optional.empty();
  }

  @Override
  public @NonNull Position resolvePosition() {
    return this.position != null ? this.position : NoPositionInformation.getInstance();
  }
}
