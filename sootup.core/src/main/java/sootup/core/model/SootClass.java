package sootup.core.model;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2020 Raja Vallee-Rai, Linghui Luo, Jan Martin Persch and others
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

import java.util.Optional;
import java.util.Set;
import org.jspecify.annotations.NonNull;
import sootup.core.frontend.SootClassSource;
import sootup.core.signatures.FieldSubSignature;
import sootup.core.signatures.MethodSubSignature;
import sootup.core.signatures.Signature;
import sootup.core.types.ClassType;
import sootup.core.types.Type;
import sootup.core.views.View;

/**
 * Soot's counterpart of the source languages class concept. Soot representation of a Java class.
 * They are usually created by a Scene, but can also be constructed manually through the given
 * constructors.
 *
 * <p>SootClass represents a class/module lives in {@link View}. It may have different
 * implementations, since we want to support multiple languages. A SootClass must be uniquely
 * identified by its {@link Signature}
 *
 * @author Manuel Benz
 * @author Linghui Luo
 * @author Jan Martin Persch
 */
public interface SootClass extends HasPosition {

  @NonNull SootClassSource getClassSource();

  @NonNull String getName();

  ClassType getType();

  @NonNull Set<? extends SootField> getFields();

  @NonNull Set<? extends SootMethod> getMethods();

  /**
   * Attempts to retrieve the method with the given subSignature. This method may throw an
   * AmbiguousStateException if there are more than one method with the given subSignature. If no
   * method with the given is found, null is returned.
   */
  @NonNull Optional<? extends SootMethod> getMethod(@NonNull MethodSubSignature subSignature);

  /** Attemtps to retrieve the field with the given FieldSubSignature. */
  @NonNull Optional<? extends SootField> getField(@NonNull FieldSubSignature subSignature);

  /**
   * Returns the field of this class with the given name. Throws a ResolveException if there is more
   * than one field with the given name. Returns null if no field with the given name exists.
   */
  @NonNull Optional<? extends SootField> getField(@NonNull String name);

  /**
   * Attempts to retrieve the method with the given name and parameters. This method may throw an
   * ResolveException if there is more than one method with the given name and parameter.
   */
  @NonNull Optional<? extends SootMethod> getMethod(
      @NonNull String name, @NonNull Iterable<? extends Type> parameterTypes);

  /**
   * Attempts to retrieve the method with the given name. This method will return an empty Set if
   * there is no method with the given name.
   *
   * @param name the name of the method
   * @return a set of methods that have the given name
   */
  @NonNull Set<? extends SootMethod> getMethodsByName(@NonNull String name);

  /** Returns the modifiers of this class in an immutable set. */
  Set<ClassModifier> getModifiers();

  /**
   * Returns a backed Chain of the interfaces that are directly implemented by this class. Note that
   * direct implementation corresponds to an "implements" keyword in the Java class file and that
   * this class may still be implementing additional interfaces in the usual sense by being a
   * subclass of a class which directly implements some interfaces.
   */
  Set<? extends ClassType> getInterfaces();

  /** This method returns the outer class. */
  Optional<? extends ClassType> getOuterClass();

  /**
   * WARNING: interfaces in Java are subclasses of the java.lang.Object class! Returns the
   * superclass of this class. (see hasSuperclass())
   */
  Optional<? extends ClassType> getSuperclass();

  boolean isInterface();

  boolean isEnum();

  boolean isSuper();

  boolean isConcrete();

  boolean isPublic();

  boolean isApplicationClass();

  boolean isLibraryClass();

  boolean isPrivate();

  boolean isProtected();

  boolean isAbstract();

  boolean isFinal();

  boolean isStatic();

  boolean isAnnotation();

  boolean isInnerClass();

  boolean hasSuperclass();

  boolean hasOuterClass();

  Position getPosition();

  String print();
}
