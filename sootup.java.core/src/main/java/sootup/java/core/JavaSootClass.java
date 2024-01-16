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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import sootup.core.model.*;
import sootup.core.signatures.FieldSubSignature;
import sootup.core.signatures.MethodSubSignature;
import sootup.core.types.ClassType;
import sootup.core.types.Type;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;

public class JavaSootClass extends SootClass {

  public JavaSootClass(JavaSootClassSource classSource, SourceType sourceType) {
    super(classSource, sourceType);
  }

  @Nonnull
  @Override
  public JavaClassType getType() {
    return (JavaClassType) super.getType();
  }

  /**
   * Get all annotations on this class. If provided with a View, will also resolve all inherited
   * annotations from super classes.
   *
   * @param view
   * @return
   */
  @Nonnull
  public Iterable<AnnotationUsage> getAnnotations(@Nonnull Optional<JavaView> view) {
    List<AnnotationUsage> annotationUsages = new ArrayList<>();

    if (view.isPresent()) {
      JavaView javaView = view.get();
      if (this.getSuperclass().isPresent()) {

        ClassType superClass = this.getSuperclass().get();

        if (javaView.getClass(superClass).isPresent()) {
          JavaSootClass superJavaSootClass = javaView.getClass(superClass).get();

          Collection<AnnotationUsage> annos =
              StreamSupport.stream(superJavaSootClass.getAnnotations(view).spliterator(), false)
                  .filter(annotationUsage -> annotationUsage.getAnnotation().isInherited(view))
                  .collect(Collectors.toList());

          annotationUsages.addAll(annos);
        }
      }
    }

    ((JavaSootClassSource) classSource).resolveAnnotations().forEach(annotationUsages::add);

    annotationUsages.forEach(e -> e.getAnnotation().getDefaultValues(view));

    for (AnnotationUsage annotationUsage : annotationUsages) {
      for (Object value : annotationUsage.getValuesWithDefaults().values()) {
        if (value instanceof ArrayList
            && !((ArrayList<?>) value).isEmpty()
            && ((ArrayList<?>) value).get(0) instanceof AnnotationUsage) {
          ((ArrayList<AnnotationUsage>) value)
              .forEach(au -> au.getAnnotation().getDefaultValues(view));
        }
      }
    }

    return annotationUsages;
  }

  @Nonnull
  @Override
  public Set<JavaSootMethod> getMethods() {
    return super.getMethods().stream()
        .map(method -> (JavaSootMethod) method)
        .collect(Collectors.toSet());
  }

  @Nonnull
  @Override
  public Set<JavaSootField> getFields() {
    return super.getFields().stream()
        .map(field -> (JavaSootField) field)
        .collect(Collectors.toSet());
  }

  @Nonnull
  @Override
  public Optional<JavaSootField> getField(@Nonnull String name) {
    return super.getField(name).map(field -> (JavaSootField) field);
  }

  @Nonnull
  @Override
  public Optional<JavaSootField> getField(@Nonnull FieldSubSignature subSignature) {
    return super.getField(subSignature).map(field -> (JavaSootField) field);
  }

  @Nonnull
  @Override
  public Optional<JavaSootMethod> getMethod(
      @Nonnull String name, @Nonnull Iterable<? extends Type> parameterTypes) {
    return super.getMethod(name, parameterTypes).map(method -> (JavaSootMethod) method);
  }

  @Nonnull
  @Override
  public Set<JavaSootMethod> getMethodsByName(@Nonnull String name) {
    return super.getMethodsByName(name).stream()
        .map(method -> (JavaSootMethod) method)
        .collect(Collectors.toSet());
  }

  @Nonnull
  @Override
  public Optional<JavaSootMethod> getMethod(@Nonnull MethodSubSignature subSignature) {
    return super.getMethod(subSignature).map(method -> (JavaSootMethod) method);
  }

  @Nonnull
  @Override
  public JavaSootClassSource getClassSource() {
    return (JavaSootClassSource) super.getClassSource();
  }

  @Nonnull
  @Override
  public Optional<JavaClassType> getOuterClass() {
    return super.getOuterClass().map(ct -> (JavaClassType) ct);
  }

  @Nonnull
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
  @Nonnull
  public JavaSootClass withOverridingClassSource(
      Function<OverridingJavaClassSource, OverridingJavaClassSource> overrider) {
    return new JavaSootClass(
        overrider.apply(new OverridingJavaClassSource(getClassSource())), sourceType);
  }

  @Nonnull
  public JavaSootClass withReplacedMethod(
      @Nonnull JavaSootMethod toReplace, @Nonnull JavaSootMethod replacement) {
    return new JavaSootClass(
        new OverridingJavaClassSource(getClassSource()).withReplacedMethod(toReplace, replacement),
        sourceType);
  }

  @Nonnull
  public JavaSootClass withMethods(@Nonnull Collection<JavaSootMethod> methods) {
    return new JavaSootClass(
        new OverridingJavaClassSource(getClassSource()).withMethods(methods), sourceType);
  }

  @Nonnull
  public JavaSootClass withReplacedField(
      @Nonnull JavaSootField toReplace, @Nonnull JavaSootField replacement) {
    return new JavaSootClass(
        new OverridingJavaClassSource(getClassSource()).withReplacedField(toReplace, replacement),
        sourceType);
  }

  @Nonnull
  public JavaSootClass withFields(@Nonnull Collection<JavaSootField> fields) {
    return new JavaSootClass(
        new OverridingJavaClassSource(getClassSource()).withFields(fields), sourceType);
  }

  @Nonnull
  public JavaSootClass withModifiers(@Nonnull Set<ClassModifier> modifiers) {
    return new JavaSootClass(
        new OverridingJavaClassSource(getClassSource()).withModifiers(modifiers), sourceType);
  }

  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  @Nonnull
  public JavaSootClass withSuperclass(@Nonnull Optional<JavaClassType> superclass) {
    return new JavaSootClass(
        new OverridingJavaClassSource(getClassSource()).withSuperclass(superclass), sourceType);
  }

  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  @Nonnull
  public JavaSootClass withOuterClass(@Nonnull Optional<JavaClassType> outerClass) {
    return new JavaSootClass(
        new OverridingJavaClassSource(getClassSource()).withOuterClass(outerClass), sourceType);
  }

  @Nonnull
  public JavaSootClass withPosition(@Nullable Position position) {
    return new JavaSootClass(
        new OverridingJavaClassSource(getClassSource()).withPosition(position), sourceType);
  }

  public boolean isJavaLibraryClass() {
    return this.classSignature.isBuiltInClass();
  }
}
