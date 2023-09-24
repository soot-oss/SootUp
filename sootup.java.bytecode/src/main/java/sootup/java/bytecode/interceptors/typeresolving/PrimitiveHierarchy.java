package sootup.java.bytecode.interceptors.typeresolving;
/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2019-2022 Zun Wang
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

import java.util.Collection;
import java.util.Collections;
import javax.annotation.Nonnull;
import sootup.core.types.ArrayType;
import sootup.core.types.PrimitiveType;
import sootup.core.types.Type;
import sootup.java.bytecode.interceptors.typeresolving.types.AugIntegerTypes;
import sootup.java.bytecode.interceptors.typeresolving.types.BottomType;

/** @author Zun Wang */
public class PrimitiveHierarchy {

  /**
   * Calculate the least common ancestor of two types(primitive or BottomType). If there's a = b
   * then a is the least common ancestor of a and b; If there's b = a then b is the least common
   * ancestor of a and b; If there are c = a and c = b, but there's no b = a or a = b then c is the
   * least common ancestor of a and b;
   */
  @Nonnull
  public static Collection<Type> getLeastCommonAncestor(@Nonnull Type a, @Nonnull Type b) {
    if (a == b) {
      return Collections.singleton(a);
    }

    if (arePrimitives(a, b)) {
      if (isAncestor(a, b)) {
        return Collections.singleton(a);
      }
      if (isAncestor(b, a)) {
        return Collections.singleton(b);
      }
      if (a.getClass() == PrimitiveType.ByteType.class) {
        if (b.getClass() == PrimitiveType.ShortType.class
            || b.getClass() == PrimitiveType.CharType.class
            || b.getClass() == AugIntegerTypes.Integer32767Type.class) {
          return Collections.singleton(PrimitiveType.getInt());
        }
        return Collections.emptySet();
      }
      if (a.getClass() == PrimitiveType.ShortType.class) {
        if (b.getClass() == PrimitiveType.ByteType.class
            || b.getClass() == PrimitiveType.CharType.class) {
          return Collections.singleton(PrimitiveType.getInt());
        }
        return Collections.emptySet();
      }
      if (a.getClass() == PrimitiveType.CharType.class) {
        if (b.getClass() == PrimitiveType.ByteType.class
            || b.getClass() == PrimitiveType.ShortType.class) {
          return Collections.singleton(PrimitiveType.getInt());
        }
        return Collections.emptySet();
      }
      return Collections.emptySet();
    }
    return Collections.emptySet();
  }

  /**
   * Check the ancestor-relationship between two primitive types <code>ancestor</code> and <code>
   * child</code>, namely, whether child can be assigned to ancestor directly to obtain: ancestor =
   * child.
   */
  public static boolean isAncestor(@Nonnull Type ancestor, @Nonnull Type child) {

    if (ancestor == child) {
      return true;
    }

    if (arePrimitives(ancestor, child)) {
      if (ancestor.getClass() == AugIntegerTypes.Integer1Type.class) {
        return child.getClass() == BottomType.class;
      }
      if (ancestor.getClass() == PrimitiveType.BooleanType.class
          || ancestor.getClass() == AugIntegerTypes.Integer127Type.class) {
        return child.getClass() == AugIntegerTypes.Integer1Type.class
            || child.getClass() == BottomType.class;
      }
      if (ancestor.getClass() == PrimitiveType.ByteType.class
          || ancestor.getClass() == AugIntegerTypes.Integer32767Type.class) {
        return child.getClass() == AugIntegerTypes.Integer127Type.class
            || child.getClass() == AugIntegerTypes.Integer1Type.class
            || child.getClass() == BottomType.class;
      }
      if (ancestor.getClass() == PrimitiveType.CharType.class
          || ancestor.getClass() == PrimitiveType.ShortType.class) {
        return child.getClass() == AugIntegerTypes.Integer32767Type.class
            || child.getClass() == AugIntegerTypes.Integer127Type.class
            || child.getClass() == AugIntegerTypes.Integer1Type.class
            || child.getClass() == BottomType.class;
      }
      if (ancestor instanceof PrimitiveType.IntType) {
        return (!(child.getClass() == PrimitiveType.BooleanType.class)
                && (child instanceof PrimitiveType.IntType))
            || child.getClass() == BottomType.class;
      }
      return child.getClass() == BottomType.class;
    }

    if (ancestor instanceof ArrayType && child instanceof ArrayType) {
      Type ancestorBase = ((ArrayType) ancestor).getBaseType();
      Type childBase = ((ArrayType) child).getBaseType();
      int ancestorDim = ((ArrayType) ancestor).getDimension();
      int childDim = ((ArrayType) child).getDimension();

      if (ancestorDim == childDim && arePrimitives(ancestorBase, childBase)) {
        // TODO: [ms] dry? looks quite similar to the if-else-tree above.. why are they differing in
        // structure?
        if (ancestorBase.getClass() == AugIntegerTypes.Integer1Type.class) {
          return childBase.getClass() == BottomType.class;
        }
        if (ancestorBase.getClass() == PrimitiveType.BooleanType.class
            || ancestorBase.getClass() == AugIntegerTypes.Integer127Type.class) {
          return childBase.getClass() == AugIntegerTypes.Integer1Type.class
              || childBase.getClass() == BottomType.class;
        }
        if (ancestorBase.getClass() == PrimitiveType.ByteType.class
            || ancestorBase.getClass() == AugIntegerTypes.Integer32767Type.class) {
          return childBase.getClass() == AugIntegerTypes.Integer127Type.class
              || childBase.getClass() == AugIntegerTypes.Integer1Type.class
              || childBase.getClass() == BottomType.class;
        }
        if (ancestorBase.getClass() == PrimitiveType.CharType.class
            || ancestorBase.getClass() == PrimitiveType.ShortType.class
            || ancestorBase instanceof PrimitiveType.IntType) {
          return childBase.getClass() == AugIntegerTypes.Integer32767Type.class
              || childBase.getClass() == AugIntegerTypes.Integer127Type.class
              || childBase.getClass() == AugIntegerTypes.Integer1Type.class
              || childBase.getClass() == BottomType.class;
        }
      }
      return childBase.getClass() == BottomType.class;
    }

    return child.getClass() == BottomType.class;
  }

  /** Check whether the two given types are primitives or BottomType */
  public static boolean arePrimitives(@Nonnull Type a, @Nonnull Type b) {
    return (a instanceof PrimitiveType || a.getClass() == BottomType.class)
        && (b instanceof PrimitiveType || b.getClass() == BottomType.class);
  }
}
