package de.upb.swt.soot.callgraph.typehierarchy;
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
import com.google.common.base.Suppliers;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.types.*;
import de.upb.swt.soot.core.views.View;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Represents a type hierarchy. It can be created from a {@link View} using {@link #fromView(View)}.
 *
 * @author Linghui Luo
 * @author Ben Hermann
 * @author Christian Brüggemann
 */
public interface TypeHierarchy {

  static TypeHierarchy fromView(View<? extends SootClass> view) {
    return view.computeModuleDataIfAbsent(
            TypeHierarchyKey.getInstance(),
            () -> Suppliers.memoize(() -> new ViewTypeHierarchy(view)))
        .get();
  }

  /**
   * Returns all classes that implement the specified interface. This is transitive: If class <code>
   * A extends B</code> and <code>B implements interface</code>, then this method will return both A
   * and B as implementers of <code>interfaceType</code>.
   *
   * <p>This includes interfaces extending <code>interfaceType</code> as they may contain default
   * implementations of methods.
   */
  @Nonnull
  Set<ClassType> implementersOf(@Nonnull ClassType interfaceType);

  /**
   * Returns all classes that extend the specified class. This is transitive: If <code>A extends B
   * </code> and <code>B extends classType</code>, then this method will return both A and B as
   * extenders of <code>classType</code>.
   */
  @Nonnull
  Set<ClassType> subclassesOf(@Nonnull ClassType classType);

  /**
   * Returns the interfaces implemented by <code>type</code> if it is a class or extended by <code>
   * type</code> if it is an interface. This includes interfaces implemented by superclasses and
   * also covers the case where <code>classType</code> directly or indirectly implements an
   * interface <code>I1</code> that extends another interface <code>I2
   * </code>. <code>I2</code> will be considered an implemented interface of <code>classType</code>.
   */
  @Nonnull
  Set<ClassType> implementedInterfacesOf(@Nonnull ClassType type);

  /**
   * For an interface type, this does the same as {@link #implementersOf(ClassType)}. For a class
   * type, this does the same as {@link #subclassesOf(ClassType)}.
   */
  @Nonnull
  Set<ClassType> subtypesOf(@Nonnull ClassType type);

  /** Returns the direct implementers of an interface or direct subclasses of a class. */
  @Nonnull
  Set<ClassType> directSubtypesOf(@Nonnull ClassType type);

  /**
   * Returns the direct superclass of <code>classType</code>. If <code>classType == java.lang.Object
   * </code>, this method returns null.
   */
  @Nullable
  ClassType superClassOf(@Nonnull ClassType classType);

  /**
   * Returns true if <code>storeType</code> is a subtype of <code>CanidateType</code>. If they
   * are identical, this will return true.
   */
  default boolean canStore(@Nonnull Type storeType, @Nonnull Type candidateType){
    return storeType == candidateType || isSubtype(storeType, candidateType);
  }

  /**
   * Returns true if <code>potentialSubtype</code> is a subtype of <code>supertype</code>. If they
   * are identical, this will return false.
   *
   * <p>This method relies on {@link #implementedInterfacesOf(ClassType)} and {@link
   * #superClassOf(ClassType)}.
   */
  default boolean isSubtype(@Nonnull Type supertype, @Nonnull Type potentialSubtype) {
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

    if (supertype instanceof ArrayType) {
      if (!(potentialSubtype instanceof ArrayType)) {
        return false;
      }

      ArrayType superArrayType = (ArrayType) supertype;
      ArrayType potentialSubArrayType = (ArrayType) potentialSubtype;
      if (superArrayType.getBaseType() instanceof PrimitiveType) {
        // Arrays of primitives have no subtypes
        return false;
      }

      assert superArrayType.getBaseType() instanceof ReferenceType;

      if (superArrayType.getBaseType().equals(potentialSubArrayType.getBaseType())) {
        // Object[] x = new Object[0][0];
        return potentialSubArrayType.getDimension() > superArrayType.getDimension();
      } else if (isSubtype(superArrayType.getBaseType(), potentialSubArrayType.getBaseType())
          && potentialSubArrayType.getDimension() == superArrayType.getDimension()) {
        // Arrays are covariant: Object[] x = new String[0];
        return true;
      } else if (superArrayType.getBaseType() instanceof ClassType
          && (((ClassType) superArrayType.getBaseType())
                  .getFullyQualifiedName()
                  .equals("java.lang.Object")
              || ((ClassType) superArrayType.getBaseType())
                  .getFullyQualifiedName()
                  .equals("java.io.Serializable")
              || ((ClassType) superArrayType.getBaseType())
                  .getFullyQualifiedName()
                  .equals("java.lang.Cloneable"))) {
        // Special case: Object[] x = new double[0][0], Object[][] y = new double[0][0][0], ...
        return potentialSubArrayType.getDimension() > superArrayType.getDimension();
      } else if (superArrayType.getDimension() > 1
          && potentialSubArrayType.getDimension() == superArrayType.getDimension()) {
        ArrayType superArrayTypeLessDim =
            new ArrayType(superArrayType.getBaseType(), superArrayType.getDimension() - 1);
        ArrayType potentialSubArrayTypeLessDim =
            new ArrayType(
                potentialSubArrayType.getBaseType(), potentialSubArrayType.getDimension() - 1);
        return isSubtype(superArrayTypeLessDim, potentialSubArrayTypeLessDim);
      } else {
        return false;
      }
    } else if (supertype instanceof ClassType) {
      if (potentialSubtype instanceof ClassType) {
        // First condition is a fast path
        return supertype.equals(superClassOf((ClassType) potentialSubtype))
            || superClassesOf((ClassType) potentialSubtype).contains(supertype)
            || implementedInterfacesOf((ClassType) potentialSubtype).contains(supertype);
      } else if (potentialSubtype instanceof ArrayType) {
        // Arrays are subtypes of java.lang.Object, java.io.Serializable and java.lang.Cloneable
        String fullyQualifiedName = ((ClassType) supertype).getFullyQualifiedName();
        return fullyQualifiedName.equals("java.lang.Object")
            || fullyQualifiedName.equals("java.io.Serializable")
            || fullyQualifiedName.equals("java.lang.Cloneable");
      } else {
        throw new AssertionError("potentialSubtype has unexpected type");
      }
    } else {
      throw new AssertionError("supertype has unexpected type");
    }
  }

  /**
   * Returns all superclasses of <code>classType</code> up to <code>java.lang.Object</code>, which
   * will be the last entry in the list.
   */
  @Nonnull
  default List<ClassType> superClassesOf(@Nonnull ClassType classType) {
    List<ClassType> superClasses = new ArrayList<>();
    ClassType currentSuperClass = superClassOf(classType);
    while (currentSuperClass != null) {
      superClasses.add(currentSuperClass);
      currentSuperClass = superClassOf(currentSuperClass);
    }
    return superClasses;
  }
}
