package de.upb.swt.soot.core.model;
/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997 - 1999 Raja Vallee-Rai
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

import com.google.common.base.Suppliers;
import com.google.common.collect.Iterables;
import de.upb.swt.soot.core.frontend.ClassSource;
import de.upb.swt.soot.core.frontend.OverridingClassSource;
import de.upb.swt.soot.core.frontend.ResolveException;
import de.upb.swt.soot.core.signatures.FieldSubSignature;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.signatures.MethodSubSignature;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.core.util.ImmutableUtils;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/*
 * Incomplete and inefficient implementation.
 *
 * Implementation notes:
 *
 * 1. The getFieldOf() methodRef is slow because it traverses the list of fields, comparing the names,
 * one by one.  If you establish a Dictionary of Name->Field, you will need to add a
 * notifyOfNameChange() methodRef, and register fields which belong to classes, because the hashtable
 * will need to be updated.  I will do this later. - kor  16-Sep-97
 *
 * 2. Note 1 is kept for historical (i.e. amusement) reasons.  In fact, there is no longer a list of fields;
 * these are kept in a Chain now.  But that's ok; there is no longer a getFieldOf() methodRef,
 * either.  There still is no efficient way to get a field by name, although one could establish
 * a Chain of EquivalentValue-like objects and do an O(1) search on that.  - plam 2-24-00
 */

/**
 * Soot's counterpart of the source languages class concept. Soot representation of a Java class.
 * They are usually created by a Scene, but can also be constructed manually through the given
 * constructors.
 *
 * @author Manuel Benz
 * @author Linghui Luo
 * @author Jan Martin Persch
 */
public class SootClass extends AbstractClass<ClassSource> {

  @Nonnull protected final SourceType sourceType;

  @Nonnull protected final ClassType classSignature;

  public SootClass(ClassSource classSource, SourceType sourceType) {
    super(classSource);
    this.sourceType = sourceType;
    this.classSignature = classSource.getClassType();
  }

  // TODO: [JMP] Create type signature for this dummy type and move it closer to
  // its usage.
  @Nonnull public static final String INVOKEDYNAMIC_DUMMY_CLASS_NAME = "soot.dummy.InvokeDynamic";

  @Nonnull
  private Set<SootField> lazyFieldInitializer() {
    Set<SootField> fields;

    try {
      fields = ImmutableUtils.immutableSetOf(this.classSource.resolveFields());
    } catch (ResolveException e) {
      fields = ImmutableUtils.emptyImmutableSet();

      // TODO: [JMP] Exception handling
      e.printStackTrace();
      throw new IllegalStateException(e);
    }

    return fields;
  }

  @Nonnull
  private Set<SootMethod> lazyMethodInitializer() {
    Set<SootMethod> methods;

    try {
      methods = ImmutableUtils.immutableSetOf(this.classSource.resolveMethods());
    } catch (ResolveException e) {
      methods = ImmutableUtils.emptyImmutableSet();

      // TODO: [JMP] Exception handling
      e.printStackTrace();
      throw new IllegalStateException(e);
    }

    return methods;
  }

  @Nonnull
  private final Supplier<Set<SootMethod>> _lazyMethods =
      Suppliers.memoize(this::lazyMethodInitializer);

  /** Gets the {@link Method methods} of this {@link SootClass} in an immutable set. */
  @Nonnull
  public Set<SootMethod> getMethods() {
    return this._lazyMethods.get();
  }

  @Nonnull
  private final Supplier<Set<SootField>> _lazyFields =
      Suppliers.memoize(this::lazyFieldInitializer);

  /** Gets the {@link Field fields} of this {@link SootClass} in an immutable set. */
  @Override
  @Nonnull
  public Set<SootField> getFields() {
    return this._lazyFields.get();
  }

  /** Returns the number of fields in this class. */
  public int getFieldCount() {
    return getFields().size();
  }

  /**
   * Returns the field of this class with the given name. Throws a RuntimeException if there is more
   * than one field with the given name. Returns null if no field with the given name exists.
   */
  @Nonnull
  public Optional<SootField> getField(String name) {
    return this.getFields().stream()
        .filter(field -> field.getSignature().getName().equals(name))
        .reduce(
            (l, r) -> {
              throw new RuntimeException("ambiguous field: " + name);
            });
  }

  /**
   * Returns the field of this class with the given sub-signature. If such a field does not exist,
   * null is returned.
   */
  @Nonnull
  public Optional<SootField> getField(@Nonnull FieldSubSignature subSignature) {
    return this.getFields().stream()
        .filter(field -> field.getSubSignature().equals(subSignature))
        .findAny();
  }

  /**
   * Attempts to retrieve the methodRef with the given signature, parameters and return type. If no
   * matching method can be found, null is returned.
   */
  @Nonnull
  public Optional<SootMethod> getMethod(@Nonnull MethodSignature signature) {
    return this.getMethods().stream()
        .filter(method -> method.getSignature().equals(signature))
        .findAny();
  }

  /**
   * Attempts to retrieve the method with the given name and parameters. This method may throw an
   * AmbiguousMethodException if there is more than one method with the given name and parameter.
   */
  @Nonnull
  public Optional<SootMethod> getMethod(String name, Iterable<? extends Type> parameterTypes) {
    return this.getMethods().stream()
        .filter(
            method ->
                method.getSignature().getName().equals(name)
                    && Iterables.elementsEqual(parameterTypes, method.getParameterTypes()))
        .reduce(
            (l, r) -> {
              throw new RuntimeException("ambiguous method: " + name);
            });
  }

  /**
   * Attempts to retrieve the method with the given subSignature. This method may throw an
   * AmbiguousMethodException if there are more than one method with the given subSignature. If no
   * method with the given is found, null is returned.
   */
  @Nonnull
  public Optional<SootMethod> getMethod(@Nonnull MethodSubSignature subSignature) {
    return this.getMethods().stream()
        .filter(method -> method.getSubSignature().equals(subSignature))
        .findAny();
  }

  private final Supplier<Set<Modifier>> lazyModifiers =
      Suppliers.memoize(classSource::resolveModifiers);

  /** Returns the modifiers of this class in an immutable set. */
  @Nonnull
  public Set<Modifier> getModifiers() {
    return lazyModifiers.get();
  }

  private final Supplier<Set<ClassType>> lazyInterfaces =
      Suppliers.memoize(classSource::resolveInterfaces);

  /**
   * Returns the number of interfaces being directly implemented by this class. Note that direct
   * implementation corresponds to an "implements" keyword in the Java class file and that this
   * class may still be implementing additional interfaces in the usual sense by being a subclass of
   * a class which directly implements some interfaces.
   */
  public int getInterfaceCount() {
    return lazyInterfaces.get().size();
  }

  /**
   * Returns a backed Chain of the interfaces that are directly implemented by this class. (see
   * getInterfaceCount())
   */
  public Set<ClassType> getInterfaces() {
    return lazyInterfaces.get();
  }

  /** Does this class directly implement the given interface? (see getInterfaceCount()) */
  public boolean implementsInterface(ClassType classSignature) {
    for (ClassType sc : getInterfaces()) {
      if (sc.equals(classSignature)) {
        return true;
      }
    }
    return false;
  }

  private final Supplier<Optional<ClassType>> lazySuperclass =
      Suppliers.memoize(classSource::resolveSuperclass);

  /**
   * WARNING: interfaces are subclasses of the java.lang.Object class! Does this class have a
   * superclass? False implies that this is the java.lang.Object class. Note that interfaces are
   * subclasses of the java.lang.Object class.
   */
  public boolean hasSuperclass() {
    return lazySuperclass.get().isPresent();
  }

  /**
   * WARNING: interfaces in Java are subclasses of the java.lang.Object class! Returns the
   * superclass of this class. (see hasSuperclass())
   */
  public Optional<ClassType> getSuperclass() {
    return lazySuperclass.get();
  }

  private final Supplier<Optional<ClassType>> lazyOuterClass =
      Suppliers.memoize(classSource::resolveOuterClass);

  public boolean hasOuterClass() {
    return lazyOuterClass.get().isPresent();
  }

  /** This method returns the outer class. */
  public @Nonnull Optional<ClassType> getOuterClass() {
    return lazyOuterClass.get();
  }

  public boolean isInnerClass() {
    return hasOuterClass();
  }

  /** Returns the ClassSignature of this class. */
  @Override
  public ClassType getType() {
    return classSignature;
  }

  /** Convenience method; returns true if this class is an interface. */
  public boolean isInterface() {
    return Modifier.isInterface(this.getModifiers());
  }

  /** Convenience method; returns true if this class is an enumeration. */
  public boolean isEnum() {
    return Modifier.isEnum(this.getModifiers());
  }

  /** Convenience method; returns true if this class is synchronized. */
  public boolean isSynchronized() {
    return Modifier.isSynchronized(this.getModifiers());
  }

  /** Returns true if this class is not an interface and not abstract. */
  public boolean isConcrete() {
    return !isInterface() && !isAbstract();
  }

  /** Convenience method; returns true if this class is public. */
  public boolean isPublic() {
    return Modifier.isPublic(this.getModifiers());
  }

  /** Returns the name of this class. */
  @Override
  @Nonnull
  public String toString() {
    return classSignature.toString();
  }

  /** Returns true if this class is an application class. */
  public boolean isApplicationClass() {
    return sourceType.equals(SourceType.Application);
  }

  /** Returns true if this class is a library class. */
  public boolean isLibraryClass() {
    return sourceType.equals(SourceType.Library);
  }

  /** Returns true if this class is a phantom class. */
  public boolean isPhantomClass() {
    return sourceType.equals(SourceType.Phantom);
  }

  /** Convenience method returning true if this class is private. */
  public boolean isPrivate() {
    return Modifier.isPrivate(this.getModifiers());
  }

  /** Convenience method returning true if this class is protected. */
  public boolean isProtected() {
    return Modifier.isProtected(this.getModifiers());
  }

  /** Convenience method returning true if this class is abstract. */
  public boolean isAbstract() {
    return Modifier.isAbstract(this.getModifiers());
  }

  /** Convenience method returning true if this class is final. */
  public boolean isFinal() {
    return Modifier.isFinal(this.getModifiers());
  }

  /** Convenience method returning true if this class is static. */
  public boolean isStatic() {
    return Modifier.isStatic(this.getModifiers());
  }

  private final Supplier<Position> lazyPosition = Suppliers.memoize(classSource::resolvePosition);

  @Nonnull
  public Position getPosition() {
    return lazyPosition.get();
  }

  @Override
  public ClassSource getClassSource() {
    return classSource;
  }

  @Override
  @Nonnull
  public String getName() {
    return this.classSignature.getFullyQualifiedName();
  }

  /**
   * Creates a new SootClass based on a new {@link OverridingClassSource}. This is useful to change
   * selected parts of a {@link SootClass} without recreating a {@link ClassSource} completely.
   * {@link OverridingClassSource} allows for replacing specific parts of a class, such as fields
   * and methods.
   */
  @Nonnull
  public SootClass withOverridingClassSource(
      Function<OverridingClassSource, OverridingClassSource> overrider) {
    return new SootClass(overrider.apply(new OverridingClassSource(classSource)), sourceType);
  }

  @Nonnull
  public SootClass withClassSource(ClassSource classSource) {
    return new SootClass(classSource, sourceType);
  }

  @Nonnull
  public SootClass withSourceType(SourceType sourceType) {
    return new SootClass(classSource, sourceType);
  }

  // Convenience withers that delegate to an OverridingClassSource

  @Nonnull
  public SootClass withReplacedMethod(
      @Nonnull SootMethod toReplace, @Nonnull SootMethod replacement) {
    return new SootClass(
        new OverridingClassSource(classSource).withReplacedMethod(toReplace, replacement),
        sourceType);
  }

  @Nonnull
  public SootClass withMethods(@Nonnull Collection<SootMethod> methods) {
    return new SootClass(new OverridingClassSource(classSource).withMethods(methods), sourceType);
  }

  @Nonnull
  public SootClass withReplacedField(@Nonnull SootField toReplace, @Nonnull SootField replacement) {
    return new SootClass(
        new OverridingClassSource(classSource).withReplacedField(toReplace, replacement),
        sourceType);
  }

  @Nonnull
  public SootClass withFields(@Nonnull Collection<SootField> fields) {
    return new SootClass(new OverridingClassSource(classSource).withFields(fields), sourceType);
  }

  @Nonnull
  public SootClass withModifiers(@Nonnull Set<Modifier> modifiers) {
    return new SootClass(
        new OverridingClassSource(classSource).withModifiers(modifiers), sourceType);
  }

  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  @Nonnull
  public SootClass withSuperclass(@Nonnull Optional<ClassType> superclass) {
    return new SootClass(
        new OverridingClassSource(classSource).withSuperclass(superclass), sourceType);
  }

  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  @Nonnull
  public SootClass withOuterClass(@Nonnull Optional<ClassType> outerClass) {
    return new SootClass(
        new OverridingClassSource(classSource).withOuterClass(outerClass), sourceType);
  }

  @Nonnull
  public SootClass withPosition(@Nullable Position position) {
    return new SootClass(new OverridingClassSource(classSource).withPosition(position), sourceType);
  }
}
