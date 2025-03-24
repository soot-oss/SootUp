package sootup.java.core;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2019-2020 Linghui Luo, Markus Schmidt
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

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import sootup.core.model.*;
import sootup.core.signatures.FieldSubSignature;
import sootup.core.signatures.MethodSubSignature;
import sootup.core.types.Type;
import sootup.java.core.types.JavaClassType;

public class JavaSootClass extends SootClass implements HasAnnotation {

  public JavaSootClass(JavaSootClassSource classSource, SourceType sourceType) {
    super(classSource, sourceType);
  }

  @NonNull
  @Override
  public JavaClassType getType() {
    return (JavaClassType) super.getType();
  }

  /**
   * Get all annotations that are directly attached to this class.
   *
   * <p>This includes "visible" and "invisible" annotations. Note that inherited annotations are not
   * part of this iterable.
   *
   * @return
   */
  @NonNull
  public Iterable<AnnotationUsage> getAnnotations() {
    // we should cache it in the future: for now, we do not cache it
    // because the underlying data structure might be mutable
    return ((JavaSootClassSource) classSource).resolveAnnotations();
  }

  @NonNull
  @Override
  public Set<JavaSootMethod> getMethods() {
    return super.getMethods().stream()
        .map(method -> (JavaSootMethod) method)
        .collect(Collectors.toSet());
  }

  @NonNull
  @Override
  public Set<JavaSootField> getFields() {
    return super.getFields().stream()
        .map(field -> (JavaSootField) field)
        .collect(Collectors.toSet());
  }

  @NonNull
  @Override
  public Optional<JavaSootField> getField(@NonNull String name) {
    return super.getField(name).map(field -> (JavaSootField) field);
  }

  @NonNull
  @Override
  public Optional<JavaSootField> getField(@NonNull FieldSubSignature subSignature) {
    return super.getField(subSignature).map(field -> (JavaSootField) field);
  }

  @NonNull
  @Override
  public Optional<JavaSootMethod> getMethod(
      @NonNull String name, @NonNull Iterable<? extends Type> parameterTypes) {
    return super.getMethod(name, parameterTypes).map(method -> (JavaSootMethod) method);
  }

  @NonNull
  @Override
  public Set<JavaSootMethod> getMethodsByName(@NonNull String name) {
    return super.getMethodsByName(name).stream()
        .map(method -> (JavaSootMethod) method)
        .collect(Collectors.toSet());
  }

  @NonNull
  @Override
  public Optional<JavaSootMethod> getMethod(@NonNull MethodSubSignature subSignature) {
    return super.getMethod(subSignature).map(method -> (JavaSootMethod) method);
  }

  @NonNull
  @Override
  public JavaSootClassSource getClassSource() {
    return (JavaSootClassSource) super.getClassSource();
  }

  @NonNull
  @Override
  public Optional<JavaClassType> getOuterClass() {
    return super.getOuterClass().map(ct -> (JavaClassType) ct);
  }

  @NonNull
  @Override
  public Optional<JavaClassType> getSuperclass() {
    return super.getSuperclass().map(ct -> (JavaClassType) ct);
  }

  // Convenience withers that delegate to an OverridingClassSource

  /**
   * Creates a new JavaSootClass based on a new {@link OverridingJavaClassSource}. This is useful to
   * change selected parts of a {@link SootClass} without recreating a {@link JavaSootClassSource}
   * completely. {@link OverridingJavaClassSource} allows for replacing specific parts of a class,
   * such as fields and methods.
   */
  @NonNull
  public JavaSootClass withOverridingClassSource(
      Function<OverridingJavaClassSource, OverridingJavaClassSource> overrider) {
    return new JavaSootClass(
        overrider.apply(new OverridingJavaClassSource(getClassSource())), sourceType);
  }

  @NonNull
  public JavaSootClass withReplacedMethod(
      @NonNull JavaSootMethod toReplace, @NonNull JavaSootMethod replacement) {
    return new JavaSootClass(
        new OverridingJavaClassSource(getClassSource()).withReplacedMethod(toReplace, replacement),
        sourceType);
  }

  @NonNull
  public JavaSootClass withMethods(@NonNull Collection<JavaSootMethod> methods) {
    return new JavaSootClass(
        new OverridingJavaClassSource(getClassSource()).withMethods(methods), sourceType);
  }

  @NonNull
  public JavaSootClass withReplacedField(
      @NonNull JavaSootField toReplace, @NonNull JavaSootField replacement) {
    return new JavaSootClass(
        new OverridingJavaClassSource(getClassSource()).withReplacedField(toReplace, replacement),
        sourceType);
  }

  @NonNull
  public JavaSootClass withFields(@NonNull Collection<JavaSootField> fields) {
    return new JavaSootClass(
        new OverridingJavaClassSource(getClassSource()).withFields(fields), sourceType);
  }

  @NonNull
  public JavaSootClass withModifiers(@NonNull Set<ClassModifier> modifiers) {
    return new JavaSootClass(
        new OverridingJavaClassSource(getClassSource()).withModifiers(modifiers), sourceType);
  }

  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  @NonNull
  public JavaSootClass withSuperclass(@NonNull Optional<JavaClassType> superclass) {
    return new JavaSootClass(
        new OverridingJavaClassSource(getClassSource()).withSuperclass(superclass), sourceType);
  }

  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  @NonNull
  public JavaSootClass withOuterClass(@NonNull Optional<JavaClassType> outerClass) {
    return new JavaSootClass(
        new OverridingJavaClassSource(getClassSource()).withOuterClass(outerClass), sourceType);
  }

  @NonNull
  public JavaSootClass withPosition(@Nullable Position position) {
    return new JavaSootClass(
        new OverridingJavaClassSource(getClassSource()).withPosition(position), sourceType);
  }
}
