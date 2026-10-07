package sootup.core.typehierarchy;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2019-2020 Linghui Luo, Ben Hermann, Christian Brüggemann
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
import java.util.stream.Stream;
import org.jspecify.annotations.NonNull;
import sootup.core.model.SootClass;
import sootup.core.types.*;
import sootup.core.views.View;

/**
 * Represents a type hierarchy. It can be created from a {@link View}.
 *
 * @author Linghui Luo
 * @author Ben Hermann
 * @author Christian Brüggemann
 */
public interface TypeHierarchy {

  /**
   * Returns all classes that implement the specified interface. This is transitive: If class <code>
   * A extends B</code> and <code>B implements interface</code>, then this method will return both A
   * and B as implementers of <code>interfaceType</code>.
   *
   * <p>This includes interfaces extending <code>interfaceType</code> as they may contain default
   * implementations of methods.
   */
  @NonNull Stream<ClassType> implementersOf(@NonNull ClassType interfaceType);

  /**
   * Returns all classes that extend the specified class. This is transitive: If <code>A extends B
   * </code> and <code>B extends classType</code>, then this method will return both A and B as
   * extenders of <code>classType</code>.
   */
  @NonNull Stream<ClassType> subclassesOf(@NonNull ClassType classType);

  /**
   * Returns all interfaces that extend the specified class. This is transitive: If <code>
   * A extends B
   * </code> and <code>B extends classType</code>, then this method will return both A and B as
   * extenders of <code>classType</code>.
   */
  @NonNull Stream<ClassType> subinterfacesOf(@NonNull ClassType classType);

  /**
   * Returns the interfaces implemented by <code>type</code> if it is a class or extended by <code>
   * type</code> if it is an interface. This includes interfaces implemented by superclasses and
   * also covers the case where <code>classType</code> directly or indirectly implements an
   * interface <code>I1</code> that extends another interface <code>I2
   * </code>. <code>I2</code> will be considered an implemented interface of <code>classType</code>.
   */
  @NonNull Stream<ClassType> implementedInterfacesOf(@NonNull ClassType type);

  /**
   * For an interface type, this does the same as {@link #implementersOf(ClassType)}. For a class
   * type, this does the same as {@link #subclassesOf(ClassType)}.
   */
  // TODO: [ms] check! not sure this method makes sense in the interface..
  @NonNull Stream<ClassType> subtypesOf(@NonNull ClassType type);

  /**
   * Equivalent to resolving every element of {@link #subtypesOf(ClassType)} to its {@link
   * SootClass} via the underlying {@link View}, i.e. {@code subtypesOf(type).flatMap(ct ->
   * view.getClass(ct).stream())} - a subtype the {@link View} cannot resolve on its own (e.g. one
   * added via {@link MutableTypeHierarchy#addType} with a class instance the view doesn't know
   * about) is silently excluded, same as that composition would produce. Implementations are
   * expected to cache this resolution (invalidated the same way as {@link #subtypesOf(ClassType)}),
   * since callers such as call graph algorithms query the same declaring type repeatedly across
   * many call sites.
   */
  @NonNull Stream<? extends SootClass> subtypeClassesOf(@NonNull ClassType type);

  /** Returns the direct implementers of an interface or direct subclasses of a class. */
  @NonNull Stream<ClassType> directSubtypesOf(@NonNull ClassType type);

  /**
   * Returns the direct superclass of <code>classType</code>. If <code>classType == java.lang.Object
   * </code>, this method returns null.
   */
  @NonNull Optional<ClassType> superClassOf(@NonNull ClassType classType);

  /**
   * Returns true if <code>potentialSubtype</code> is a subtype of <code>supertype</code>. If they
   * are identical, this will return false.
   *
   * <p>This method relies on {@link #implementedInterfacesOf(ClassType)} and {@link
   * #superClassOf(ClassType)}.
   */
  default boolean isSubtype(@NonNull Type supertype, @NonNull Type potentialSubtype) {
    if (!(supertype instanceof ReferenceType) || !(potentialSubtype instanceof ReferenceType)) {
      // Subtyping applies to ReferenceTypes only
      return false;
    }

    if (supertype instanceof NullType) {
      // NullType has no subtypes
      return false;
    }

    if (potentialSubtype instanceof NullType) {
      // Null can be assigned to any type
      return true;
    }

    if (supertype instanceof ArrayType superArrayType) {
      if (!(potentialSubtype instanceof ArrayType potentialSubArrayType)) {
        return false;
      }

      if (superArrayType.getBaseType() instanceof PrimitiveType) {
        // Arrays of primitives have no subtypes
        return false;
      }

      assert superArrayType.getBaseType() instanceof ReferenceType;

      if (isSubtype(superArrayType.getBaseType(), potentialSubArrayType.getBaseType())
          && potentialSubArrayType.getDimension() == superArrayType.getDimension()) {
        // Arrays are covariant: Object[] x = new String[0];
        return true;
      } else if (superArrayType.getBaseType() instanceof ClassType baseClassType
          && isArraySupertype(baseClassType)) {
        // Special case: Object[] x = new double[0][0], Object[][] y = new double[0][0][0], ...
        return potentialSubArrayType.getDimension() > superArrayType.getDimension();
      } else {
        return false;
      }
    } else if (supertype instanceof ClassType supertypeClassType) {
      if (potentialSubtype instanceof ClassType) {
        return isClassSubtype(supertypeClassType, (ClassType) potentialSubtype);
      } else if (potentialSubtype instanceof ArrayType) {
        // Arrays are subtypes of java.lang.Object, java.io.Serializable and java.lang.Cloneable
        return isArraySupertype(supertypeClassType);
      } else {
        throw new AssertionError("potentialSubtype has unexpected type");
      }
    } else {
      throw new AssertionError("supertype has unexpected type");
    }
  }

  /**
   * Returns all superclasses of <code>classType</code> up to <code>java.lang.Object</code>, which
   * will be the last entry in the list, or till one of the superclasses is not contained in view.
   */
  @NonNull
  default Stream<ClassType> superClassesOf(@NonNull ClassType classType) {
    List<ClassType> superClasses = new ArrayList<>();
    Optional<ClassType> currentSuperClass = superClassOf(classType);
    while (currentSuperClass.isPresent()) {
      ClassType superClassType = currentSuperClass.get();
      superClasses.add(superClassType);
      currentSuperClass = superClassOf(superClassType);
    }
    return superClasses.stream();
  }

  /**
   * Returns true if <code>potentialSubtype</code> is a (possibly indirect) subtype of <code>
   * supertype</code>, both of which are class types (not arrays). Extracted out of {@link
   * #isSubtype(Type, Type)} so implementations can override it with a check faster than the
   * default's linear {@link #superClassesOf(ClassType)}/{@link #implementedInterfacesOf(ClassType)}
   * walk.
   */
  default boolean isClassSubtype(
      @NonNull ClassType supertype, @NonNull ClassType potentialSubtype) {
    // any potential subtype is a subtype of java.lang.Object except java.lang.Object itself
    // superClassOf() check is a fast path
    return (isJavaLangObject(supertype) && !isJavaLangObject(potentialSubtype))
        || superClassesOf(potentialSubtype).anyMatch(t -> t == supertype)
        || implementedInterfacesOf(potentialSubtype).anyMatch(t -> t == supertype);
  }

  /**
   * Returns whether the given type is one of the three types every array is a subtype of: {@code
   * java.lang.Object}, {@code java.io.Serializable} and {@code java.lang.Cloneable}.
   */
  private static boolean isArraySupertype(@NonNull ClassType type) {
    return isJavaLangObject(type)
        || hasName(type, "java.io", "Serializable")
        || hasName(type, "java.lang", "Cloneable");
  }

  private static boolean isJavaLangObject(@NonNull ClassType type) {
    return hasName(type, "java.lang", "Object");
  }

  /**
   * Compares the class and package name separately instead of going through {@link
   * ClassType#getFullyQualifiedName()}, which builds a new String on every call - this runs per
   * subtype query.
   */
  private static boolean hasName(
      @NonNull ClassType type, @NonNull String packageName, @NonNull String className) {
    return type.getClassName().equals(className)
        && type.getPackageName().getName().equals(packageName);
  }

  Stream<ClassType> directlyImplementedInterfacesOf(@NonNull ClassType type);

  boolean isInterface(@NonNull ClassType type);

  Stream<ClassType> directlyExtendedInterfacesOf(@NonNull ClassType type);

  // checks if a Type is contained int the TypeHierarchy - should return the equivalent to
  // View.getClass(...).isPresent()
  boolean contains(ClassType type);

  Collection<ClassType> getLowestCommonAncestors(ClassType a, ClassType b);

  /**
   * Returns a counter that increases every time the hierarchy's structure is mutated (e.g. via
   * {@link MutableTypeHierarchy#addType}, including mutations triggered internally by lazy
   * resolution of previously-unseen types). Callers that build their own caches derived from
   * hierarchy queries (e.g. call graph algorithms caching resolved dispatch targets) can compare
   * this value across calls to detect staleness without needing to be notified of every individual
   * mutation.
   */
  long getModificationCount();
}
