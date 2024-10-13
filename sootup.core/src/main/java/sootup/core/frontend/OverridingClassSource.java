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
import java.nio.file.Path;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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

  public OverridingClassSource(@Nonnull SootClassSource delegate) {
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
      @Nonnull SootClassSource delegate) {
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
      @Nonnull Set<SootMethod> sootMethods,
      @Nonnull Set<SootField> sootFields,
      @Nonnull EnumSet<ClassModifier> modifiers,
      @Nonnull Set<ClassType> interfaces,
      @Nonnull ClassType superClass,
      @Nonnull ClassType outerClass,
      @Nonnull Position position,
      @Nonnull Path sourcePath,
      @Nonnull ClassType classType,
      @Nonnull AnalysisInputLocation srcNamespace) {
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

  @Nonnull
  @Override
  public Collection<SootMethod> resolveMethods() throws ResolveException {
    return overriddenSootMethods != null
        ? overriddenSootMethods
        : (Collection<SootMethod>) delegate.resolveMethods();
  }

  @Nonnull
  @Override
  public Collection<SootField> resolveFields() throws ResolveException {
    return overriddenSootFields != null
        ? overriddenSootFields
        : (Collection<SootField>) delegate.resolveFields();
  }

  @Nonnull
  @Override
  public Set<ClassModifier> resolveModifiers() {
    return overriddenModifiers != null ? overriddenModifiers : delegate.resolveModifiers();
  }

  @Nonnull
  @Override
  public Set<ClassType> resolveInterfaces() {
    return overriddenInterfaces != null
        ? overriddenInterfaces
        : (Set<ClassType>) delegate.resolveInterfaces();
  }

  @Nonnull
  @Override
  public Optional<ClassType> resolveSuperclass() {
    return overriddenSuperclass != null
        ? overriddenSuperclass
        : (Optional<ClassType>) delegate.resolveSuperclass();
  }

  @Nonnull
  @Override
  public Optional<ClassType> resolveOuterClass() {
    return overriddenOuterClass != null
        ? overriddenOuterClass
        : (Optional<ClassType>) delegate.resolveOuterClass();
  }

  @Nonnull
  @Override
  public Position resolvePosition() {
    return position != null ? position : delegate.resolvePosition();
  }

  @Override
  public SootClass buildClass(@Nonnull SourceType sourceType) {
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

  @Nonnull
  public OverridingClassSource withReplacedMethod(
      @Nonnull SootMethod toReplace, @Nonnull SootMethod replacement) {
    Set<SootMethod> newMethods = new HashSet<>(resolveMethods());
    CollectionUtils.replace(newMethods, toReplace, replacement);
    return withMethods(newMethods);
  }

  @Nonnull
  public OverridingClassSource withMethods(@Nonnull Collection<SootMethod> overriddenSootMethods) {
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

  @Nonnull
  public OverridingClassSource withReplacedField(
      @Nonnull SootField toReplace, @Nonnull SootField replacement) {
    Set<SootField> newFields = new HashSet<>(resolveFields());
    CollectionUtils.replace(newFields, toReplace, replacement);
    return withFields(newFields);
  }

  @Nonnull
  public OverridingClassSource withFields(@Nonnull Collection<SootField> overriddenSootFields) {
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

  @Nonnull
  public OverridingClassSource withModifiers(@Nonnull Set<ClassModifier> overriddenModifiers) {
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

  @Nonnull
  public OverridingClassSource withInterfaces(@Nonnull Set<ClassType> overriddenInterfaces) {
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

  @Nonnull
  public OverridingClassSource withSuperclass(@Nonnull Optional<ClassType> overriddenSuperclass) {
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

  @Nonnull
  public OverridingClassSource withOuterClass(@Nonnull Optional<ClassType> overriddenOuterClass) {
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

  @Nonnull
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

  /**
   * Creates a builder for {@link OverridingClassSource}.
   *
   * @return a {@link OverridingClassSourceBuilder}
   */
  @Nonnull
  public static MethodsStep builder() {
    return new OverridingClassSourceBuilder();
  }

  public interface MethodsStep {
    @Nonnull
    FieldsStep withMethods(@Nonnull Collection<SootMethod> overriddenSootMethods);
  }

  public interface FieldsStep {
    @Nonnull
    ModifiersStep withFields(@Nonnull Collection<SootField> overriddenSootFields);
  }

  public interface ModifiersStep {
    @Nonnull
    InterfacesStep withModifiers(@Nonnull Set<ClassModifier> overriddenModifiers);
  }

  public interface InterfacesStep {
    @Nonnull
    SuperclassStep withInterfaces(@Nonnull Set<ClassType> overriddenInterfaces);
  }

  public interface SuperclassStep {
    @Nonnull
    OuterClassStep withSuperclass(@Nonnull Optional<ClassType> overriddenSuperclass);
  }

  public interface OuterClassStep {
    @Nonnull
    PositionStep withOuterClass(@Nonnull Optional<ClassType> overriddenOuterClass);
  }

  public interface PositionStep {
    @Nonnull
    Build withPosition(@Nullable Position position);
  }

  public interface Build {
    @Nonnull
    OverridingClassSource build();
  }

  /** Defines a {@link OverridingClassSource} builder. */
  public static class OverridingClassSourceBuilder
      implements MethodsStep,
          FieldsStep,
          ModifiersStep,
          InterfacesStep,
          SuperclassStep,
          OuterClassStep,
          PositionStep,
          Build {
    @Nullable private Collection<SootMethod> overriddenSootMethods;
    @Nullable private Collection<SootField> overriddenSootFields;
    @Nullable private Set<ClassModifier> overriddenModifiers;
    @Nullable private Set<ClassType> overriddenInterfaces;
    @Nullable private Optional<ClassType> overriddenSuperclass;
    @Nullable private Optional<ClassType> overriddenOuterClass;
    @Nullable private Position position;

    @Nullable
    public Collection<SootMethod> getMethods() {
      return overriddenSootMethods;
    }

    @Nullable
    public Collection<SootField> getFields() {
      return overriddenSootFields;
    }

    @Nullable
    public Set<ClassModifier> getModifiers() {
      return overriddenModifiers;
    }

    @Nullable
    public Set<ClassType> getInterfaces() {
      return overriddenInterfaces;
    }

    @Nullable
    public Optional<ClassType> getSuperclass() {
      return overriddenSuperclass;
    }

    @Nullable
    public Optional<ClassType> getOuterClass() {
      return overriddenOuterClass;
    }

    @Nullable
    public Position getPosition() {
      return position;
    }

    @Override
    @Nonnull
    public FieldsStep withMethods(@Nonnull Collection<SootMethod> overriddenSootMethods) {
      this.overriddenSootMethods = overriddenSootMethods;
      return this;
    }

    @Override
    @Nonnull
    public ModifiersStep withFields(@Nonnull Collection<SootField> overriddenSootFields) {
      this.overriddenSootFields = overriddenSootFields;
      return this;
    }

    @Override
    @Nonnull
    public InterfacesStep withModifiers(@Nonnull Set<ClassModifier> overriddenModifiers) {
      this.overriddenModifiers = overriddenModifiers;
      return this;
    }

    @Override
    @Nonnull
    public SuperclassStep withInterfaces(@Nonnull Set<ClassType> overriddenInterfaces) {
      this.overriddenInterfaces = overriddenInterfaces;
      return this;
    }

    @Override
    @Nonnull
    public OuterClassStep withSuperclass(@Nonnull Optional<ClassType> overriddenSuperclass) {
      this.overriddenSuperclass = overriddenSuperclass;
      return this;
    }

    @Override
    @Nonnull
    public PositionStep withOuterClass(@Nonnull Optional<ClassType> overriddenOuterClass) {
      this.overriddenOuterClass = overriddenOuterClass;
      return this;
    }

    @Override
    @Nonnull
    public Build withPosition(@Nullable Position position) {
      this.position = position;
      return this;
    }

    @Override
    @Nonnull
    public OverridingClassSource build() {
      return new OverridingClassSource(
        getMethods(),
        getFields(),
        getModifiers(),
        getInterfaces(),
        getSuperclass(),
        getOuterClass(),
        getPosition(),
        null
      );
    }
  }
}
