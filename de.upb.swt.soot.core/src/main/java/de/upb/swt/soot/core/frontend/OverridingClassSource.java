package de.upb.swt.soot.core.frontend;

import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.model.Modifier;
import de.upb.swt.soot.core.model.Position;
import de.upb.swt.soot.core.model.SootField;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.core.util.CollectionUtils;
import java.nio.file.Path;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** @author: Hasitha Rajapakse */

/**
 * Allows for replacing specific parts of a class, such as fields and methods or, allows to resolve
 * classes that are batchparsed like .java files using wala java source frontend or in tests where
 * all information is already existing.
 *
 * <p>When replacing specific parts of a class by default, it delegates to the {@link ClassSource}
 * delegate provided in the constructor.
 *
 * <p>To alter the results of invocations to e.g. {@link #resolveFields()}, simply call {@link
 * #withFields(Collection)} to obtain a new {@link OverridingClassSource}. The new instance will
 * then use the supplied value instead of calling {@link #resolveFields()} on the delegate.
 */
@SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "OptionalAssignedToNull"})
public class OverridingClassSource extends ClassSource {

  @Nullable private final Collection<SootMethod> overriddenSootMethods;
  @Nullable private final Collection<SootField> overriddenSootFields;
  @Nullable private final Set<Modifier> overriddenModifiers;
  @Nullable private final Set<ClassType> overriddenInterfaces;
  @Nullable private final Optional<ClassType> overriddenSuperclass;
  @Nullable private final Optional<ClassType> overriddenOuterClass;

  // Since resolvePosition may return null, we cannot use `null` here to indicate that `position`
  // is not overridden.
  private final boolean overriddenPosition;
  @Nullable private final Position position;

  private final ClassSource delegate;

  public OverridingClassSource(@Nonnull ClassSource delegate) {
    super(delegate.srcNamespace, delegate.classSignature, delegate.sourcePath);
    this.delegate = delegate;
    overriddenSootMethods = null;
    overriddenSootFields = null;
    overriddenModifiers = null;
    overriddenInterfaces = null;
    overriddenSuperclass = null;
    overriddenOuterClass = null;
    overriddenPosition = false;
    position = null;
  }

  private OverridingClassSource(
      @Nullable Collection<SootMethod> overriddenSootMethods,
      @Nullable Collection<SootField> overriddenSootFields,
      @Nullable Set<Modifier> overriddenModifiers,
      @Nullable Set<ClassType> overriddenInterfaces,
      @Nullable Optional<ClassType> overriddenSuperclass,
      @Nullable Optional<ClassType> overriddenOuterClass,
      boolean overriddenPosition,
      @Nullable Position position,
      @Nonnull ClassSource delegate) {
    super(delegate.srcNamespace, delegate.classSignature, delegate.sourcePath);
    this.overriddenSootMethods = overriddenSootMethods;
    this.overriddenSootFields = overriddenSootFields;
    this.overriddenModifiers = overriddenModifiers;
    this.overriddenInterfaces = overriddenInterfaces;
    this.overriddenSuperclass = overriddenSuperclass;
    this.overriddenOuterClass = overriddenOuterClass;
    this.overriddenPosition = overriddenPosition;
    this.position = position;
    this.delegate = delegate;
  }

  /** Class source where all information already available */
  public OverridingClassSource(
      AnalysisInputLocation srcNamespace,
      Path sourcePath,
      ClassType classType,
      ClassType superClass,
      Set<ClassType> interfaces,
      ClassType outerClass,
      Set<SootField> sootFields,
      Set<SootMethod> sootMethods,
      Position position,
      EnumSet<Modifier> modifiers) {
    super(srcNamespace, classType, sourcePath);

    this.delegate = null;
    this.overriddenSootMethods = sootMethods;
    this.overriddenSootFields = sootFields;
    this.overriddenModifiers = modifiers;
    this.overriddenInterfaces = interfaces;
    this.overriddenSuperclass = Optional.ofNullable(superClass);
    this.overriddenOuterClass = Optional.ofNullable(outerClass);
    this.overriddenPosition = true;
    this.position = position;
  }

  @Nonnull
  @Override
  public Collection<SootMethod> resolveMethods() throws ResolveException {
    return overriddenSootMethods != null ? overriddenSootMethods : delegate.resolveMethods();
  }

  @Nonnull
  @Override
  public Collection<SootField> resolveFields() throws ResolveException {
    return overriddenSootFields != null ? overriddenSootFields : delegate.resolveFields();
  }

  @Nonnull
  @Override
  public Set<Modifier> resolveModifiers() {
    return overriddenModifiers != null ? overriddenModifiers : delegate.resolveModifiers();
  }

  @Nonnull
  @Override
  public Set<ClassType> resolveInterfaces() {
    return overriddenInterfaces != null ? overriddenInterfaces : delegate.resolveInterfaces();
  }

  @Nonnull
  @Override
  public Optional<ClassType> resolveSuperclass() {
    return overriddenSuperclass != null ? overriddenSuperclass : delegate.resolveSuperclass();
  }

  @Nonnull
  @Override
  public Optional<ClassType> resolveOuterClass() {
    return overriddenOuterClass != null ? overriddenOuterClass : delegate.resolveOuterClass();
  }

  @Override
  public Position resolvePosition() {
    return overriddenPosition ? position : delegate.resolvePosition();
  }

  /** */
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
        overriddenPosition,
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
        overriddenPosition,
        position,
        delegate);
  }

  @Nonnull
  public OverridingClassSource withModifiers(@Nonnull Set<Modifier> overriddenModifiers) {
    return new OverridingClassSource(
        overriddenSootMethods,
        overriddenSootFields,
        overriddenModifiers,
        overriddenInterfaces,
        overriddenSuperclass,
        overriddenOuterClass,
        overriddenPosition,
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
        overriddenPosition,
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
        overriddenPosition,
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
        overriddenPosition,
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
        true,
        position,
        delegate);
  }
}
