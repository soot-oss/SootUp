package de.upb.soot.core;
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

import static de.upb.soot.util.Utils.ImmutableCollectors.toImmutableSet;
import static de.upb.soot.util.Utils.iterableToStream;
import static de.upb.soot.util.concurrent.Lazy.synchronizedLazy;

import com.google.common.collect.Iterables;
import com.ibm.wala.cast.tree.CAstSourcePositionMap.Position;
import de.upb.soot.frontends.ClassSource;
import de.upb.soot.frontends.ResolveException;
import de.upb.soot.signatures.FieldSubSignature;
import de.upb.soot.signatures.MethodSignature;
import de.upb.soot.signatures.MethodSubSignature;
import de.upb.soot.types.JavaClassType;
import de.upb.soot.types.Type;
import de.upb.soot.util.Utils;
import de.upb.soot.util.concurrent.Lazy;
import java.io.Serializable;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;

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
public class SootClass extends AbstractClass<ClassSource> implements Serializable {

  public SootClass(ClassSource classSource, SourceType sourceType) {
    super(classSource);
    this.sourceType = sourceType;
    this.classSignature = classSource.getClassType();
  }

  private static final long serialVersionUID = -4145583783298080555L;

  private final SourceType sourceType;
  @Nonnull private final JavaClassType classSignature;

  // TODO: [JMP] Create type signature for this dummy type and move it closer to its usage.
  @Nonnull public static final String INVOKEDYNAMIC_DUMMY_CLASS_NAME = "soot.dummy.InvokeDynamic";

  @Nonnull
  private <M extends SootClassMember> Set<M> initializeClassMembers(
      @Nonnull Iterable<? extends M> items) {
    return iterableToStream(items).peek(it -> it.setDeclaringClass(this)).collect(toImmutableSet());
  }

  @Nonnull
  private Set<SootField> lazyFieldInitializer() {
    Iterable<SootField> fields;

    try {
      fields = this.classSource.resolveFields();
    } catch (ResolveException e) {
      fields = Utils.emptyImmutableSet();

      // TODO: [JMP] Exception handling
      e.printStackTrace();
      throw new IllegalStateException(e);
    }

    return this.initializeClassMembers(fields);
  }

  @Nonnull
  private Set<SootMethod> lazyMethodInitializer() {
    Iterable<SootMethod> methods;

    try {
      methods = this.classSource.resolveMethods();
    } catch (ResolveException e) {
      methods = Utils.emptyImmutableSet();

      // TODO: [JMP] Exception handling
      e.printStackTrace();
      throw new IllegalStateException(e);
    }

    return this.initializeClassMembers(methods);
  }

  @Nonnull
  private final Lazy<Set<SootMethod>> _lazyMethods = synchronizedLazy(this::lazyMethodInitializer);

  /** Gets the {@link Method methods} of this {@link SootClass} in an immutable set. */
  @Nonnull
  public Set<SootMethod> getMethods() {
    return this._lazyMethods.get();
  }

  @Nonnull
  private final Lazy<Set<SootField>> _lazyFields = synchronizedLazy(this::lazyFieldInitializer);

  /** Gets the {@link Field fields} of this {@link SootClass} in an immutable set. */
  @Override
  @Nonnull
  public Set<SootField> getFields() {
    return this._lazyFields.get();
  }

  /** Returns the number of fields in this class. */
  public int getFieldCount() {
    // FIXME "This has to be refactored later. I'm unsure whether we still need the resolving
    // levels."
    // https://github.com/secure-software-engineering/soot-reloaded/pull/89#discussion_r267007069
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
   * matching methodRef can be found, null is returned.
   */
  @Nonnull
  public Optional<SootMethod> getMethod(MethodSignature signature) {
    return this.getMethods().stream()
        .filter(method -> method.getSignature().equals(signature))
        .findAny();
  }

  /**
   * Attempts to retrieve the methodRef with the given name and parameters. This methodRef may throw
   * an AmbiguousMethodException if there is more than one methodRef with the given name and
   * parameter.
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
              throw new RuntimeException("ambiguous methodRef: " + name);
            });
  }

  /**
   * Attempts to retrieve the methodRef with the given subSignature. This methodRef may throw an
   * AmbiguousMethodException if there are more than one methodRef with the given subSignature. If
   * no methodRef with the given is found, null is returned.
   */
  @Nonnull
  public Optional<SootMethod> getMethod(@Nonnull MethodSubSignature subSignature) {
    return this.getMethods().stream()
        .filter(method -> method.getSubSignature().equals(subSignature))
        .findAny();
  }

  private final Lazy<Set<Modifier>> lazyModifiers =
      synchronizedLazy(() -> classSource.resolveModifiers());

  /** Returns the modifiers of this class in an immutable set. */
  @Nonnull
  public Set<Modifier> getModifiers() {
    return lazyModifiers.get();
  }

  private final Lazy<Set<JavaClassType>> lazyInterfaces =
      synchronizedLazy(() -> classSource.resolveInterfaces());

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
  public Set<JavaClassType> getInterfaces() {
    return lazyInterfaces.get();
  }

  /** Does this class directly implement the given interface? (see getInterfaceCount()) */
  public boolean implementsInterface(JavaClassType classSignature) {
    for (JavaClassType sc : getInterfaces()) {
      if (sc.equals(classSignature)) {
        return true;
      }
    }
    return false;
  }

  private final Lazy<Optional<JavaClassType>> lazySuperclass =
      synchronizedLazy(() -> classSource.resolveSuperclass());

  /**
   * WARNING: interfaces are subclasses of the java.lang.Object class! Does this class have a
   * superclass? False implies that this is the java.lang.Object class. Note that interfaces are
   * subclasses of the java.lang.Object class.
   */
  public boolean hasSuperclass() {
    return lazySuperclass.get().isPresent();
  }

  /**
   * WARNING: interfaces are subclasses of the java.lang.Object class! Returns the superclass of
   * this class. (see hasSuperclass())
   */
  public Optional<JavaClassType> getSuperclass() {
    return lazySuperclass.get();
  }

  private final Lazy<Optional<JavaClassType>> lazyOuterClass =
      synchronizedLazy(() -> classSource.resolveOuterClass());

  public boolean hasOuterClass() {
    return lazyOuterClass.get().isPresent();
  }

  /** This methodRef returns the outer class. */
  public @Nonnull Optional<JavaClassType> getOuterClass() {
    return lazyOuterClass.get();
  }

  public boolean isInnerClass() {
    return hasOuterClass();
  }

  /** Returns the ClassSignature of this class. */
  @Override
  public JavaClassType getType() {
    return classSignature;
  }

  /** Convenience methodRef; returns true if this class is an interface. */
  public boolean isInterface() {
    return Modifier.isInterface(this.getModifiers());
  }

  /** Convenience methodRef; returns true if this class is an enumeration. */
  public boolean isEnum() {
    return Modifier.isEnum(this.getModifiers());
  }

  /** Convenience methodRef; returns true if this class is synchronized. */
  public boolean isSynchronized() {
    return Modifier.isSynchronized(this.getModifiers());
  }

  /** Returns true if this class is not an interface and not abstract. */
  public boolean isConcrete() {
    return !isInterface() && !isAbstract();
  }

  /** Convenience methodRef; returns true if this class is public. */
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

  // FIXME: get rid of these logic
  private static final class LibraryClassPatternHolder {
    /**
     * Sometimes we need to know which class is a JDK class. There is no simple way to distinguish a
     * user class and a JDK class, here we use the package prefix as the heuristic.
     */
    private static final Pattern LIBRARY_CLASS_PATTERN =
        Pattern.compile(
            "^(?:java\\.|sun\\.|javax\\.|com\\.sun\\.|org\\.omg\\.|org\\.xml\\.|org\\.w3c\\.dom)");
  }

  public boolean isJavaLibraryClass() {
    return LibraryClassPatternHolder.LIBRARY_CLASS_PATTERN
        .matcher(classSignature.getClassName())
        .find();
  }

  /** Returns true if this class is a phantom class. */
  public boolean isPhantomClass() {
    return sourceType.equals(SourceType.Phantom);
  }

  /** Convenience methodRef returning true if this class is private. */
  public boolean isPrivate() {
    return Modifier.isPrivate(this.getModifiers());
  }

  /** Convenience methodRef returning true if this class is protected. */
  public boolean isProtected() {
    return Modifier.isProtected(this.getModifiers());
  }

  /** Convenience methodRef returning true if this class is abstract. */
  public boolean isAbstract() {
    return Modifier.isAbstract(this.getModifiers());
  }

  /** Convenience methodRef returning true if this class is final. */
  public boolean isFinal() {
    return Modifier.isFinal(this.getModifiers());
  }

  /** Convenience methodRef returning true if this class is static. */
  public boolean isStatic() {
    return Modifier.isStatic(this.getModifiers());
  }

  protected int number = 0;

  // FIXME The following code is commented out due to incompatibility, but
  // may still be needed.
  // https://github.com/secure-software-engineering/soot-reloaded/pull/89#discussion_r266971653

  // /**
  // * An array containing some validators in order to validate the SootClass
  // */
  // private static final List<ClassValidator> validators
  // = Arrays.asList(new OuterClassValidator(), new MethodDeclarationValidator(), new
  // ClassFlagsValidator());
  //
  // /**
  // * Validates this SootClass for logical errors. Note that this does not validate the methodRef
  // bodies, only the class
  // * structure.
  // */
  // public void validate() {
  // final List<ValidationException> exceptionList = new ArrayList<>();
  // validate(exceptionList);
  // if (!exceptionList.isEmpty()) {
  // throw exceptionList.get(0);
  // }
  // }
  //
  // /**
  // * Validates this SootClass for logical errors. Note that this does not validate the methodRef
  // bodies, only the class
  // * structure. All found errors are saved into the given list.
  // */
  // public void validate(List<ValidationException> exceptionList) {
  // final boolean runAllValidators = this.getView().getOptions().debug() ||
  // this.getView().getOptions().validate();
  // for (ClassValidator validator : validators) {
  // if (!validator.isBasicValidator() && !runAllValidators) {
  // continue;
  // }
  // validator.validate(this, exceptionList);
  // }
  // }

  // FIXME: get rid of the wala class position
  private final Lazy<Position> lazyPosition = synchronizedLazy(() -> classSource.resolvePosition());

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
}
