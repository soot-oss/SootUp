package sootup.core.frontend;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2019 - 2020 Christian Brüggemann, Hasitha Rajapakse, Markus Schmidt
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
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.*;
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
 * #withFields(Collection)} to obtain a new {@link OverridingClassSource}. The new instance will
 * then use the supplied value instead of calling {@link #resolveFields()} on the delegate.
 *
 * @author Christian Brüggemann, Hasitha Rajapakse
 */
@SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "OptionalAssignedToNull"})
public class OverridingClassSource extends SootClassSource {

  @Nullable private final Collection<SootMethod> overriddenSootMethods;
  @Nullable private final Collection<SootField> overriddenSootFields;
  @Nullable private final Set<ClassModifier> overriddenModifiers;
  @Nullable private final Set<ClassType> overriddenInterfaces;
  @Nullable private final Optional<ClassType> overriddenSuperclass;
  @Nullable private final Optional<ClassType> overriddenOuterClass;
  @Nullable private final Position position;

  @Nullable private final SootClassSource delegate;

  public OverridingClassSource(@NonNull SootClassSource delegate) {
    super(delegate);
    this.delegate = delegate;
    overriddenSootMethods = null;
    overriddenSootFields = null;
    overriddenModifiers = null;
    overriddenInterfaces = null;
    overriddenSuperclass = null;
    overriddenOuterClass = null;
    position = null;
  }

  private OverridingClassSource(
      @Nullable Collection<SootMethod> overriddenSootMethods,
      @Nullable Collection<SootField> overriddenSootFields,
      @Nullable Set<ClassModifier> overriddenModifiers,
      @Nullable Set<ClassType> overriddenInterfaces,
      @Nullable Optional<ClassType> overriddenSuperclass,
      @Nullable Optional<ClassType> overriddenOuterClass,
      @Nullable Position position,
      @NonNull SootClassSource delegate) {
    super(delegate);
    this.overriddenSootMethods = overriddenSootMethods;
    this.overriddenSootFields = overriddenSootFields;
    this.overriddenModifiers = overriddenModifiers;
    this.overriddenInterfaces = overriddenInterfaces;
    this.overriddenSuperclass = overriddenSuperclass;
    this.overriddenOuterClass = overriddenOuterClass;
    this.position = position;
    this.delegate = delegate;
  }

  /** Class source where all information already available */
  public OverridingClassSource(
      @NonNull Set<SootMethod> sootMethods,
      @NonNull Set<SootField> sootFields,
      @NonNull EnumSet<ClassModifier> modifiers,
      @NonNull Set<ClassType> interfaces,
      @NonNull ClassType superClass,
      @NonNull ClassType outerClass,
      @NonNull Position position,
      @NonNull Path sourcePath,
      @NonNull ClassType classType,
      @NonNull AnalysisInputLocation srcNamespace) {
    super(srcNamespace, classType, sourcePath);

    this.delegate = null;
    this.overriddenSootMethods = sootMethods;
    this.overriddenSootFields = sootFields;
    this.overriddenModifiers = modifiers;
    this.overriddenInterfaces = interfaces;
    this.overriddenSuperclass = Optional.ofNullable(superClass);
    this.overriddenOuterClass = Optional.ofNullable(outerClass);
    this.position = position;
  }

  @NonNull
  @Override
  public Collection<SootMethod> resolveMethods() throws ResolveException {
    return overriddenSootMethods != null
        ? overriddenSootMethods
        : (Collection<SootMethod>) delegate.resolveMethods();
  }

  @NonNull
  @Override
  public Collection<SootField> resolveFields() throws ResolveException {
    return overriddenSootFields != null
        ? overriddenSootFields
        : (Collection<SootField>) delegate.resolveFields();
  }

  @NonNull
  @Override
  public Set<ClassModifier> resolveModifiers() {
    return overriddenModifiers != null ? overriddenModifiers : delegate.resolveModifiers();
  }

  @NonNull
  @Override
  public Set<ClassType> resolveInterfaces() {
    return overriddenInterfaces != null
        ? overriddenInterfaces
        : (Set<ClassType>) delegate.resolveInterfaces();
  }

  @NonNull
  @Override
  public Optional<ClassType> resolveSuperclass() {
    return overriddenSuperclass != null
        ? overriddenSuperclass
        : (Optional<ClassType>) delegate.resolveSuperclass();
  }

  @NonNull
  @Override
  public Optional<ClassType> resolveOuterClass() {
    return overriddenOuterClass != null
        ? overriddenOuterClass
        : (Optional<ClassType>) delegate.resolveOuterClass();
  }

  @NonNull
  @Override
  public Position resolvePosition() {
    return position != null ? position : delegate.resolvePosition();
  }

  @Override
  public SootClass buildClass(@NonNull SourceType sourceType) {
    return new SootClass(this, sourceType);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OverridingClassSource that = (OverridingClassSource) o;
    return Objects.equals(this.overriddenSuperclass, that.overriddenSuperclass)
        && Objects.equals(this.overriddenInterfaces, that.overriddenInterfaces)
        && Objects.equals(this.overriddenOuterClass, that.overriddenOuterClass)
        && Objects.equals(this.overriddenSootFields, that.overriddenSootFields)
        && Objects.equals(this.overriddenSootMethods, that.overriddenSootMethods)
        && Objects.equals(position, that.position)
        && Objects.equals(this.overriddenModifiers, that.overriddenModifiers)
        && Objects.equals(this.classSignature, that.classSignature);
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
        this.classSignature);
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

  @NonNull
  public OverridingClassSource withReplacedMethod(
      @NonNull SootMethod toReplace, @NonNull SootMethod replacement) {
    Set<SootMethod> newMethods = new HashSet<>(resolveMethods());
    CollectionUtils.replace(newMethods, toReplace, replacement);
    return withMethods(newMethods);
  }

  @NonNull
  public OverridingClassSource withMethods(@NonNull Collection<SootMethod> overriddenSootMethods) {
    return new OverridingClassSource(
        overriddenSootMethods,
        overriddenSootFields,
        overriddenModifiers,
        overriddenInterfaces,
        overriddenSuperclass,
        overriddenOuterClass,
        position,
        delegate);
  }

  @NonNull
  public OverridingClassSource withReplacedField(
      @NonNull SootField toReplace, @NonNull SootField replacement) {
    Set<SootField> newFields = new HashSet<>(resolveFields());
    CollectionUtils.replace(newFields, toReplace, replacement);
    return withFields(newFields);
  }

  @NonNull
  public OverridingClassSource withFields(@NonNull Collection<SootField> overriddenSootFields) {
    return new OverridingClassSource(
        overriddenSootMethods,
        overriddenSootFields,
        overriddenModifiers,
        overriddenInterfaces,
        overriddenSuperclass,
        overriddenOuterClass,
        position,
        delegate);
  }

  @NonNull
  public OverridingClassSource withModifiers(@NonNull Set<ClassModifier> overriddenModifiers) {
    return new OverridingClassSource(
        overriddenSootMethods,
        overriddenSootFields,
        overriddenModifiers,
        overriddenInterfaces,
        overriddenSuperclass,
        overriddenOuterClass,
        position,
        delegate);
  }

  @NonNull
  public OverridingClassSource withInterfaces(@NonNull Set<ClassType> overriddenInterfaces) {
    return new OverridingClassSource(
        overriddenSootMethods,
        overriddenSootFields,
        overriddenModifiers,
        overriddenInterfaces,
        overriddenSuperclass,
        overriddenOuterClass,
        position,
        delegate);
  }

  @NonNull
  public OverridingClassSource withSuperclass(@NonNull Optional<ClassType> overriddenSuperclass) {
    return new OverridingClassSource(
        overriddenSootMethods,
        overriddenSootFields,
        overriddenModifiers,
        overriddenInterfaces,
        overriddenSuperclass,
        overriddenOuterClass,
        position,
        delegate);
  }

  @NonNull
  public OverridingClassSource withOuterClass(@NonNull Optional<ClassType> overriddenOuterClass) {
    return new OverridingClassSource(
        overriddenSootMethods,
        overriddenSootFields,
        overriddenModifiers,
        overriddenInterfaces,
        overriddenSuperclass,
        overriddenOuterClass,
        position,
        delegate);
  }

  @NonNull
  public OverridingClassSource withPosition(@Nullable Position position) {
    return new OverridingClassSource(
        overriddenSootMethods,
        overriddenSootFields,
        overriddenModifiers,
        overriddenInterfaces,
        overriddenSuperclass,
        overriddenOuterClass,
        position,
        delegate);
  }

  /** Defines a {@link OverridingClassSourceBuilder} builder. */
  public static class OverridingClassSourceBuilder {

    @NonNull private SootClassSource delegate;
    @Nullable private Set<SootMethod> overriddenSootMethods = new HashSet<>();
    @Nullable private Set<SootField> overriddenSootFields = new HashSet<>();

    @Nullable
    private EnumSet<ClassModifier> overriddenModifiers = EnumSet.noneOf(ClassModifier.class);

    @Nullable private Set<ClassType> overriddenInterfaces = new HashSet<>();
    @Nullable private Optional<ClassType> overriddenSuperclass = Optional.empty();
    @Nullable private Optional<ClassType> overriddenOuterClass = Optional.empty();
    @Nullable private Position position;
    @Nullable Path sourcePath;
    @Nullable ClassType classType;
    @Nullable AnalysisInputLocation srcNamespace;

    private OverridingClassSourceBuilder() {}

    public static CompleteStep builder() {
      return new Steps();
    }

    public interface SootClassSourceStep {
      CompleteStep withSootClassSource(@NonNull SootClassSource sootClassSource);
    }

    public interface MethodsStep {
      CompleteStep withMethod(@NonNull SootMethod method);

      CompleteStep withMethods(@NonNull Set<SootMethod> methods);
    }

    public interface FieldsStep {
      CompleteStep withField(@NonNull SootField field);

      CompleteStep withFields(@NonNull Set<SootField> fields);
    }

    public interface ModifiersStep {
      CompleteStep withModifier(@NonNull ClassModifier modifier);

      CompleteStep withModifiers(@NonNull EnumSet<ClassModifier> modifiers);
    }

    public interface InterfacesStep {
      CompleteStep withInterface(@NonNull ClassType interfaceType);

      CompleteStep withInterfaces(@NonNull Set<ClassType> interfaces);
    }

    public interface SuperclassStep {
      CompleteStep withSuperclass(@NonNull Optional<ClassType> superclass);
    }

    public interface OuterClassStep {
      CompleteStep withOuterClass(@NonNull Optional<ClassType> outerClass);
    }

    public interface PositionStep {
      CompleteStep withPosition(@Nullable Position position);
    }

    public interface SourcePathStep {
      CompleteStep withSourcePath(@Nullable Path sourcePath);
    }

    public interface ClassTypeStep {
      CompleteStep withClassType(@Nullable ClassType classType);
    }

    public interface AnalysisInputLocationStep {
      CompleteStep withAnalysisInputLocation(@Nullable AnalysisInputLocation analysisInputLocation);
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
            SourcePathStep,
            ClassTypeStep,
            AnalysisInputLocationStep,
            Build {}

    public interface Build {
      OverridingClassSource build();
    }

    /** Concrete implementation of the step builder. */
    private static class Steps implements CompleteStep {
      private final OverridingClassSourceBuilder instance = new OverridingClassSourceBuilder();

      @Override
      public CompleteStep withSootClassSource(@NonNull SootClassSource sootClassSource) {
        instance.delegate = sootClassSource;
        return this;
      }

      @Override
      public CompleteStep withMethod(@NonNull SootMethod method) {
        instance.overriddenSootMethods = ImmutableSet.<SootMethod>builder().add(method).build();
        return this;
      }

      @Override
      public CompleteStep withMethods(@NonNull Set<SootMethod> methods) {
        instance.overriddenSootMethods.addAll(methods);
        return this;
      }

      @Override
      public CompleteStep withField(@NonNull SootField field) {
        instance.overriddenSootFields = ImmutableSet.<SootField>builder().add(field).build();
        return this;
      }

      @Override
      public CompleteStep withFields(@NonNull Set<SootField> fields) {
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
        instance.overriddenModifiers.addAll(modifiers);
        return this;
      }

      @Override
      public CompleteStep withInterface(@NonNull ClassType interfaceType) {
        instance.overriddenInterfaces =
            ImmutableSet.<ClassType>builder().add(interfaceType).build();
        return this;
      }

      @Override
      public CompleteStep withInterfaces(@NonNull Set<ClassType> interfaces) {
        instance.overriddenInterfaces.addAll(interfaces);
        return this;
      }

      @Override
      public CompleteStep withSuperclass(@NonNull Optional<ClassType> superclass) {
        instance.overriddenSuperclass = superclass;
        return this;
      }

      @Override
      public CompleteStep withOuterClass(@NonNull Optional<ClassType> outerClass) {
        instance.overriddenOuterClass = outerClass;
        return this;
      }

      @Override
      public CompleteStep withPosition(@Nullable Position position) {
        instance.position = position;
        return this;
      }

      @Override
      public CompleteStep withSourcePath(@Nullable Path sourcePath) {
        instance.sourcePath = sourcePath;
        return this;
      }

      @Override
      public CompleteStep withClassType(@Nullable ClassType classType) {
        instance.classType = classType;
        return this;
      }

      @Override
      public CompleteStep withAnalysisInputLocation(
          @Nullable AnalysisInputLocation analysisInputLocation) {
        instance.srcNamespace = analysisInputLocation;
        return this;
      }

      @Override
      public OverridingClassSource build() {
        return new OverridingClassSource(
            instance.overriddenSootMethods,
            instance.overriddenSootFields,
            instance.overriddenModifiers,
            instance.overriddenInterfaces,
            instance.overriddenSuperclass.orElse(null),
            instance.overriddenOuterClass.orElse(null),
            instance.position,
            instance.sourcePath,
            instance.classType,
            instance.srcNamespace);
      }
    }
  }
}
