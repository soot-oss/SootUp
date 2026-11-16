package sootup.java.core;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2019-2026 Christian Brüggemann, Hasitha Rajapakse, Markus Schmidt, Ashik Mogasavara Ravikumar
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
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.jimple.basic.NoPositionInformation;
import sootup.core.model.ClassModifier;
import sootup.core.model.Position;
import sootup.core.model.SootField;
import sootup.core.model.SootMethod;
import sootup.core.types.ClassType;
import sootup.java.core.types.JavaClassType;

/**
 * A class source where all information is already available in memory, e.g. classes that are
 * batchparsed (like .jimple files) or synthesized in tests.
 *
 * <p>To replace parts of an existing class source, wrap it in an {@link OverridingJavaClassSource}.
 *
 * @author Christian Brüggemann, Hasitha Rajapakse, Ashik Mogasavara Ravikumar
 */
public class InMemoryJavaClassSource extends JavaSootClassSource {

  @NonNull private final Collection<JavaSootMethod> sootMethods;
  @NonNull private final Collection<JavaSootField> sootFields;
  @NonNull private final Set<ClassModifier> modifiers;
  @NonNull private final Set<JavaClassType> interfaces;
  @NonNull private final Optional<JavaClassType> superClass;
  @NonNull private final Optional<JavaClassType> outerClass;
  @NonNull private final Position position;

  @NonNull private final Iterable<AnnotationUsage> annotations;
  @NonNull private final Iterable<AnnotationUsage> methodAnnotations;
  @Nullable private final Iterable<AnnotationUsage> fieldAnnotations;

  public InMemoryJavaClassSource(
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
      @Nullable Iterable<AnnotationUsage> fieldAnnotations) {
    this(
        srcNamespace,
        sourcePath,
        classType,
        superClass,
        interfaces,
        outerClass,
        sootFields,
        sootMethods,
        position,
        modifiers,
        annotations,
        methodAnnotations,
        fieldAnnotations,
        JavaClassFileMetadata.empty());
  }

  /**
   * Creates a fully resolved class source with immutable source and nesting attributes.
   *
   * @param srcNamespace the input location containing the class
   * @param sourcePath the path to the classfile
   * @param classType the class type
   * @param superClass the superclass, or {@code null} if absent
   * @param interfaces the implemented interfaces
   * @param outerClass the outer class, or {@code null} if absent
   * @param sootFields the fields
   * @param sootMethods the methods
   * @param position the source position
   * @param modifiers the class modifiers
   * @param annotations the class annotations
   * @param methodAnnotations the method annotations
   * @param fieldAnnotations the field annotations
   * @param classFileMetadata the source and nesting attributes
   */
  public InMemoryJavaClassSource(
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
      @Nullable Iterable<AnnotationUsage> fieldAnnotations,
      @NonNull JavaClassFileMetadata classFileMetadata) {
    super(srcNamespace, classType, sourcePath, classFileMetadata);
    this.sootMethods = sootMethods;
    this.sootFields = sootFields;
    this.modifiers = modifiers;
    this.interfaces = interfaces;
    this.superClass = Optional.ofNullable(superClass);
    this.outerClass = Optional.ofNullable(outerClass);
    this.position = position;
    this.annotations = annotations;
    this.methodAnnotations = methodAnnotations;
    this.fieldAnnotations = fieldAnnotations;
  }

  public InMemoryJavaClassSource(
      @NonNull Set<JavaSootMethod> sootMethods,
      @NonNull Set<JavaSootField> sootFields,
      @NonNull EnumSet<ClassModifier> modifiers,
      @NonNull Set<JavaClassType> interfaces,
      @Nullable JavaClassType superClass,
      @Nullable JavaClassType outerClass,
      @NonNull Position position,
      @NonNull Path sourcePath,
      @NonNull ClassType classType,
      @NonNull AnalysisInputLocation inputLocation) {
    this(
        inputLocation,
        sourcePath,
        classType,
        superClass,
        interfaces,
        outerClass,
        sootFields,
        sootMethods,
        position,
        modifiers,
        Collections.emptyList(),
        Collections.emptyList(),
        Collections.emptyList());
  }

  @NonNull
  @Override
  public Collection<? extends SootMethod> resolveMethods() {
    return sootMethods;
  }

  @NonNull
  @Override
  public Collection<? extends SootField> resolveFields() {
    return sootFields;
  }

  @NonNull
  @Override
  public Set<ClassModifier> resolveModifiers() {
    return modifiers;
  }

  @NonNull
  @Override
  public Set<? extends ClassType> resolveInterfaces() {
    return interfaces;
  }

  @NonNull
  @Override
  public Optional<? extends ClassType> resolveSuperclass() {
    return superClass;
  }

  @NonNull
  @Override
  public Optional<? extends ClassType> resolveOuterClass() {
    return outerClass;
  }

  @NonNull
  @Override
  public Position resolvePosition() {
    return position;
  }

  @Override
  @NonNull
  public Iterable<AnnotationUsage> resolveAnnotations() {
    return annotations;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    InMemoryJavaClassSource that = (InMemoryJavaClassSource) o;
    return Objects.equals(this.superClass, that.superClass)
        && Objects.equals(this.interfaces, that.interfaces)
        && Objects.equals(this.outerClass, that.outerClass)
        && Objects.equals(this.sootFields, that.sootFields)
        && Objects.equals(this.sootMethods, that.sootMethods)
        && Objects.equals(this.position, that.position)
        && Objects.equals(this.modifiers, that.modifiers)
        && Objects.equals(this.classSignature, that.classSignature)
        && Objects.equals(this.annotations, that.annotations)
        && Objects.equals(this.methodAnnotations, that.methodAnnotations);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        superClass,
        interfaces,
        outerClass,
        sootFields,
        sootMethods,
        position,
        modifiers,
        classSignature,
        annotations,
        methodAnnotations);
  }

  @Override
  public String toString() {
    return "frontend.InMemoryJavaClassSource{"
        + "superClass="
        + superClass
        + ", interfaces="
        + interfaces
        + ", outerClass="
        + outerClass
        + ", sootFields="
        + sootFields
        + ", sootMethods="
        + sootMethods
        + ", position="
        + position
        + ", modifiers="
        + modifiers
        + ", classType="
        + classSignature
        + '}';
  }

  /** Starts a stepwise builder: the input location and the class type are mandatory. */
  @NonNull
  public static AnalysisInputLocationStep builder() {
    return new Steps();
  }

  public interface AnalysisInputLocationStep {
    @NonNull ClassTypeStep withAnalysisInputLocation(@NonNull AnalysisInputLocation inputLocation);
  }

  public interface ClassTypeStep {
    @NonNull OptionalStep withClassType(@NonNull ClassType classType);
  }

  /** Optional parts; every part that is not set defaults to absent/empty. */
  public interface OptionalStep {
    @NonNull OptionalStep withSourcePath(@Nullable Path sourcePath);

    @NonNull OptionalStep withSuperclass(@Nullable JavaClassType superClass);

    @NonNull OptionalStep withOuterClass(@Nullable JavaClassType outerClass);

    @NonNull OptionalStep withInterfaces(@NonNull Set<JavaClassType> interfaces);

    @NonNull OptionalStep withFields(@NonNull Set<JavaSootField> fields);

    @NonNull OptionalStep withMethods(@NonNull Set<JavaSootMethod> methods);

    @NonNull OptionalStep withModifiers(@NonNull EnumSet<ClassModifier> modifiers);

    @NonNull OptionalStep withPosition(@NonNull Position position);

    @NonNull OptionalStep withAnnotations(@NonNull Iterable<AnnotationUsage> annotations);

    @NonNull OptionalStep withMethodAnnotations(
        @NonNull Iterable<AnnotationUsage> methodAnnotations);

    @NonNull OptionalStep withFieldAnnotations(@NonNull Iterable<AnnotationUsage> fieldAnnotations);

    @NonNull OptionalStep withClassFileMetadata(@NonNull JavaClassFileMetadata classFileMetadata);

    @NonNull InMemoryJavaClassSource build();
  }

  private static class Steps implements AnalysisInputLocationStep, ClassTypeStep, OptionalStep {
    private AnalysisInputLocation inputLocation;
    private ClassType classType;
    @Nullable private Path sourcePath;
    @Nullable private JavaClassType superClass;
    @Nullable private JavaClassType outerClass;
    @NonNull private Set<JavaClassType> interfaces = new LinkedHashSet<>();
    @NonNull private Set<JavaSootField> fields = new LinkedHashSet<>();
    @NonNull private Set<JavaSootMethod> methods = new LinkedHashSet<>();
    @NonNull private EnumSet<ClassModifier> modifiers = EnumSet.noneOf(ClassModifier.class);
    @NonNull private Position position = NoPositionInformation.getInstance();
    @NonNull private Iterable<AnnotationUsage> annotations = Collections.emptyList();
    @NonNull private Iterable<AnnotationUsage> methodAnnotations = Collections.emptyList();
    @NonNull private Iterable<AnnotationUsage> fieldAnnotations = Collections.emptyList();
    @NonNull private JavaClassFileMetadata classFileMetadata = JavaClassFileMetadata.empty();

    @Override
    public @NonNull ClassTypeStep withAnalysisInputLocation(
        @NonNull AnalysisInputLocation inputLocation) {
      this.inputLocation = inputLocation;
      return this;
    }

    @Override
    public @NonNull OptionalStep withClassType(@NonNull ClassType classType) {
      this.classType = classType;
      return this;
    }

    @Override
    public @NonNull OptionalStep withSourcePath(@Nullable Path sourcePath) {
      this.sourcePath = sourcePath;
      return this;
    }

    @Override
    public @NonNull OptionalStep withSuperclass(@Nullable JavaClassType superClass) {
      this.superClass = superClass;
      return this;
    }

    @Override
    public @NonNull OptionalStep withOuterClass(@Nullable JavaClassType outerClass) {
      this.outerClass = outerClass;
      return this;
    }

    @Override
    public @NonNull OptionalStep withInterfaces(@NonNull Set<JavaClassType> interfaces) {
      this.interfaces = interfaces;
      return this;
    }

    @Override
    public @NonNull OptionalStep withFields(@NonNull Set<JavaSootField> fields) {
      this.fields = fields;
      return this;
    }

    @Override
    public @NonNull OptionalStep withMethods(@NonNull Set<JavaSootMethod> methods) {
      this.methods = methods;
      return this;
    }

    @Override
    public @NonNull OptionalStep withModifiers(@NonNull EnumSet<ClassModifier> modifiers) {
      this.modifiers = modifiers;
      return this;
    }

    @Override
    public @NonNull OptionalStep withPosition(@NonNull Position position) {
      this.position = position;
      return this;
    }

    @Override
    public @NonNull OptionalStep withAnnotations(@NonNull Iterable<AnnotationUsage> annotations) {
      this.annotations = annotations;
      return this;
    }

    @Override
    public @NonNull OptionalStep withMethodAnnotations(
        @NonNull Iterable<AnnotationUsage> methodAnnotations) {
      this.methodAnnotations = methodAnnotations;
      return this;
    }

    @Override
    public @NonNull OptionalStep withFieldAnnotations(
        @NonNull Iterable<AnnotationUsage> fieldAnnotations) {
      this.fieldAnnotations = fieldAnnotations;
      return this;
    }

    @Override
    public @NonNull OptionalStep withClassFileMetadata(
        @NonNull JavaClassFileMetadata classFileMetadata) {
      this.classFileMetadata = classFileMetadata;
      return this;
    }

    @Override
    public @NonNull InMemoryJavaClassSource build() {
      return new InMemoryJavaClassSource(
          inputLocation,
          sourcePath,
          classType,
          superClass,
          interfaces,
          outerClass,
          fields,
          methods,
          position,
          modifiers,
          annotations,
          methodAnnotations,
          fieldAnnotations,
          classFileMetadata);
    }
  }
}
