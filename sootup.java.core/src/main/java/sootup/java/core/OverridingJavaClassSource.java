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

import com.google.common.collect.ImmutableSet;
import java.nio.file.Path;
import java.util.*;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import sootup.core.frontend.ResolveException;
import sootup.core.frontend.SootClassSource;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.ClassModifier;
import sootup.core.model.Position;
import sootup.core.model.SootField;
import sootup.core.model.SootMethod;
import sootup.core.types.ClassType;
import sootup.core.util.CollectionUtils;
import sootup.java.core.types.JavaClassType;

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

  @Nullable private final Collection<JavaSootMethod> overriddenSootMethods;
  @Nullable private final Collection<JavaSootField> overriddenSootFields;
  @Nullable private final Set<ClassModifier> overriddenModifiers;
  @Nullable private final Set<JavaClassType> overriddenInterfaces;
  @Nullable private final Optional<JavaClassType> overriddenSuperclass;
  @Nullable private final Optional<JavaClassType> overriddenOuterClass;
  @Nullable private final Position position;

  @Nullable private final JavaSootClassSource delegate;
  @Nullable private final Iterable<AnnotationUsage> annotations;
  @Nullable private final Iterable<AnnotationUsage> methodAnnotations;
  @Nullable private final Iterable<AnnotationUsage> fieldAnnotations;

  public OverridingJavaClassSource(@NonNull JavaSootClassSource delegate) {
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
      @Nullable Collection<JavaSootMethod> overriddenSootMethods,
      @Nullable Collection<JavaSootField> overriddenSootFields,
      @Nullable Set<ClassModifier> overriddenModifiers,
      @Nullable Set<JavaClassType> overriddenInterfaces,
      @Nullable Optional<JavaClassType> overriddenSuperclass,
      @Nullable Optional<JavaClassType> overriddenOuterClass,
      @Nullable Position position,
      @Nullable Iterable<AnnotationUsage> annotations,
      @Nullable Iterable<AnnotationUsage> methodAnnotations,
      @Nullable Iterable<AnnotationUsage> fieldAnnotations,
      @Nullable JavaSootClassSource delegate) {
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

  @NonNull
  @Override
  public Collection<? extends SootMethod> resolveMethods() throws ResolveException {
    if (overriddenSootMethods != null) {
      return overriddenSootMethods;
    }
    return delegate.resolveMethods();
  }

  @NonNull
  @Override
  public Collection<? extends SootField> resolveFields() throws ResolveException {
    if (overriddenSootFields != null) {
      return overriddenSootFields;
    }
    return delegate.resolveFields();
  }

  @NonNull
  @Override
  public Set<ClassModifier> resolveModifiers() {
    if (overriddenModifiers != null) {
      return overriddenModifiers;
    }
    return delegate.resolveModifiers();
  }

  @NonNull
  @Override
  public Set<? extends ClassType> resolveInterfaces() {
    if (overriddenInterfaces != null) {
      return overriddenInterfaces;
    }
    return delegate.resolveInterfaces();
  }

  @NonNull
  @Override
  public Optional<? extends ClassType> resolveSuperclass() {
    if (overriddenSuperclass != null) {
      return overriddenSuperclass;
    }
    return delegate.resolveSuperclass();
  }

  @NonNull
  @Override
  public Optional<? extends ClassType> resolveOuterClass() {
    if (overriddenOuterClass != null) {
      return overriddenOuterClass;
    }
    return delegate.resolveOuterClass();
  }

  @NonNull
  @Override
  public Position resolvePosition() {
    if (position != null) {
      return position;
    } else {
      return delegate.resolvePosition();
    }
  }

  @Override
  @NonNull
  public Iterable<AnnotationUsage> resolveAnnotations() {
    if (annotations != null) {
      return annotations;
    } else {
      return delegate.resolveAnnotations();
    }
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
    return "frontend.OverridingJavaClassSource{"
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

  @NonNull
  public OverridingJavaClassSource withReplacedMethod(
      @NonNull JavaSootMethod toReplace, @NonNull JavaSootMethod replacement) {
    Set<JavaSootMethod> newMethods = new HashSet<>((Collection<JavaSootMethod>) resolveMethods());
    CollectionUtils.replace(newMethods, toReplace, replacement);
    return withMethods(newMethods);
  }

  @NonNull
  public OverridingJavaClassSource withMethods(
      @NonNull Collection<JavaSootMethod> overriddenSootMethods) {
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

  @NonNull
  public OverridingJavaClassSource withReplacedField(
      @NonNull JavaSootField toReplace, @NonNull JavaSootField replacement) {
    Set<JavaSootField> newFields = new HashSet<>((Collection<JavaSootField>) resolveFields());
    CollectionUtils.replace(newFields, toReplace, replacement);
    return withFields(newFields);
  }

  @NonNull
  public OverridingJavaClassSource withFields(
      @NonNull Collection<JavaSootField> overriddenSootFields) {
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

  @NonNull
  public OverridingJavaClassSource withModifiers(@NonNull Set<ClassModifier> overriddenModifiers) {
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

  @NonNull
  public OverridingJavaClassSource withInterfaces(
      @NonNull Set<JavaClassType> overriddenInterfaces) {
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

  @NonNull
  public OverridingJavaClassSource withSuperclass(
      @NonNull Optional<JavaClassType> overriddenSuperclass) {
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

  @NonNull
  public OverridingJavaClassSource withOuterClass(
      @NonNull Optional<JavaClassType> overriddenOuterClass) {
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

  @NonNull
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

  /** Defines a {@link OverridingJavaClassSourceBuilder} builder. */
  public static class OverridingJavaClassSourceBuilder {

    @Nullable private JavaSootClassSource delegate;
    @Nullable private Set<JavaSootMethod> overriddenSootMethods = new HashSet<>();
    @Nullable private Set<JavaSootField> overriddenSootFields = new HashSet<>();

    @Nullable
    private EnumSet<ClassModifier> overriddenModifiers = EnumSet.noneOf(ClassModifier.class);

    @Nullable private Set<JavaClassType> overriddenInterfaces = new HashSet<>();
    @Nullable private Optional<JavaClassType> overriddenSuperclass = Optional.empty();
    @Nullable private Optional<JavaClassType> overriddenOuterClass = Optional.empty();
    @Nullable private Position position;
    @Nullable private Path sourcePath;
    @Nullable private ClassType classType;
    @Nullable private AnalysisInputLocation srcNamespace;
    @Nullable private Iterable<AnnotationUsage> annotations;
    @Nullable private Iterable<AnnotationUsage> methodAnnotations;
    @Nullable private Iterable<AnnotationUsage> fieldAnnotations;

    private OverridingJavaClassSourceBuilder() {}

    public static CompleteStep builder() {
      return new Steps();
    }

    public interface SootClassSourceStep {
      CompleteStep withSootClassSource(@NonNull JavaSootClassSource sootClassSource);
    }

    public interface MethodsStep {
      CompleteStep withMethod(@NonNull JavaSootMethod method);

      CompleteStep withMethods(@NonNull Set<JavaSootMethod> methods);
    }

    public interface FieldsStep {
      CompleteStep withField(@NonNull JavaSootField field);

      CompleteStep withFields(@NonNull Set<JavaSootField> fields);
    }

    public interface ModifiersStep {
      CompleteStep withModifier(@NonNull ClassModifier modifier);

      CompleteStep withModifiers(@NonNull EnumSet<ClassModifier> modifiers);
    }

    public interface InterfacesStep {
      CompleteStep withInterface(@NonNull JavaClassType interfaceType);

      CompleteStep withInterfaces(@NonNull Set<JavaClassType> interfaces);
    }

    public interface SuperclassStep {
      CompleteStep withSuperclass(@NonNull Optional<JavaClassType> superclass);
    }

    public interface OuterClassStep {
      CompleteStep withOuterClass(@NonNull Optional<JavaClassType> outerClass);
    }

    public interface PositionStep {
      CompleteStep withPosition(@Nullable Position position);
    }

    public interface AnnotationStep {
      CompleteStep withAnnotation(@NonNull Iterable<AnnotationUsage> annotations);
    }

    public interface MethodAnnotationStep {
      CompleteStep withMethodAnnotation(@NonNull Iterable<AnnotationUsage> methodAnnotations);
    }

    public interface FieldAnnotationStep {
      CompleteStep withFieldAnnotation(@NonNull Iterable<AnnotationUsage> fieldAnnotations);
    }

    public interface CompleteStep
        extends SootClassSourceStep,
            MethodsStep,
            FieldsStep,
            ModifiersStep,
            InterfacesStep,
            SuperclassStep,
            OuterClassStep,
            PositionStep,
            AnnotationStep,
            MethodAnnotationStep,
            FieldAnnotationStep,
            Build {}

    public interface Build {
      OverridingJavaClassSource build();
    }

    /** Concrete implementation of the step builder. */
    private static class Steps implements CompleteStep {
      private final OverridingJavaClassSourceBuilder instance =
          new OverridingJavaClassSourceBuilder();

      @Override
      public CompleteStep withSootClassSource(@NonNull JavaSootClassSource sootClassSource) {
        instance.delegate = sootClassSource;
        return this;
      }

      @Override
      public CompleteStep withMethod(@NonNull JavaSootMethod method) {
        instance.overriddenSootMethods = ImmutableSet.<JavaSootMethod>builder().add(method).build();
        return this;
      }

      @Override
      public CompleteStep withMethods(@NonNull Set<JavaSootMethod> methods) {
        if (instance.overriddenSootMethods == null) {
          instance.overriddenSootMethods = new HashSet<>(methods);
          return this;
        }
        instance.overriddenSootMethods.addAll(methods);
        return this;
      }

      @Override
      public CompleteStep withField(@NonNull JavaSootField field) {
        instance.overriddenSootFields = ImmutableSet.<JavaSootField>builder().add(field).build();
        return this;
      }

      @Override
      public CompleteStep withFields(@NonNull Set<JavaSootField> fields) {
        if (instance.overriddenSootFields == null) {
          instance.overriddenSootFields = new HashSet<>(fields);
          return this;
        }
        instance.overriddenSootFields.addAll(fields);
        return this;
      }

      @Override
      public CompleteStep withModifier(@NonNull ClassModifier modifier) {
        instance.overriddenModifiers = EnumSet.of(modifier);
        return this;
      }

      @Override
      public CompleteStep withModifiers(@NonNull EnumSet<ClassModifier> modifiers) {
        if (instance.overriddenModifiers == null) {
          instance.overriddenModifiers = EnumSet.noneOf(ClassModifier.class);
        }
        instance.overriddenModifiers.addAll(modifiers);
        return this;
      }

      @Override
      public CompleteStep withInterface(@NonNull JavaClassType interfaceType) {
        instance.overriddenInterfaces =
            ImmutableSet.<JavaClassType>builder().add(interfaceType).build();
        return this;
      }

      @Override
      public CompleteStep withInterfaces(@NonNull Set<JavaClassType> interfaces) {
        if (instance.overriddenInterfaces == null) {
          instance.overriddenInterfaces = new HashSet<>(interfaces);
          return this;
        }
        instance.overriddenInterfaces.addAll(interfaces);
        return this;
      }

      @Override
      public CompleteStep withSuperclass(@NonNull Optional<JavaClassType> superclass) {
        instance.overriddenSuperclass = superclass;
        return this;
      }

      @Override
      public CompleteStep withOuterClass(@NonNull Optional<JavaClassType> outerClass) {
        instance.overriddenOuterClass = outerClass;
        return this;
      }

      @Override
      public CompleteStep withPosition(@Nullable Position position) {
        instance.position = position;
        return this;
      }

      @Override
      public CompleteStep withAnnotation(@NonNull Iterable<AnnotationUsage> annotations) {
        instance.annotations = annotations;
        return this;
      }

      @Override
      public CompleteStep withMethodAnnotation(
          @NonNull Iterable<AnnotationUsage> methodAnnotation) {
        instance.methodAnnotations = methodAnnotation;
        return this;
      }

      @Override
      public CompleteStep withFieldAnnotation(@NonNull Iterable<AnnotationUsage> fieldAnnotations) {
        instance.fieldAnnotations = fieldAnnotations;
        return this;
      }

      @Override
      public OverridingJavaClassSource build() {
        if (instance.delegate != null) {
          return new OverridingJavaClassSource(
              instance.overriddenSootMethods,
              instance.overriddenSootFields,
              instance.overriddenModifiers,
              instance.overriddenInterfaces,
              instance.overriddenSuperclass,
              instance.overriddenOuterClass,
              instance.position,
              instance.annotations,
              instance.methodAnnotations,
              instance.fieldAnnotations,
              instance.delegate);
        } else {
          throw new IllegalStateException(
              "OverridingJavaClassSourceBuilder requires a delegate. "
                  + "Use InMemoryOverridingJavaClassSource for standalone/in-memory construction.");
        }
      }
    }
  }
}
