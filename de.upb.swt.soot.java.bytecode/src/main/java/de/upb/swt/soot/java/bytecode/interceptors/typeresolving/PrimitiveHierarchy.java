package de.upb.swt.soot.java.bytecode.interceptors.typeresolving;
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

/** @author Zun Wang */
import de.upb.swt.soot.core.types.ArrayType;
import de.upb.swt.soot.core.types.IntegerType;
import de.upb.swt.soot.core.types.PrimitiveType;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.java.bytecode.interceptors.typeresolving.types.BottomType;
import java.util.Collection;
import java.util.Collections;

public class PrimitiveHierarchy implements IHierarchy {

  /**
   * Calculate the least common ancestor of two types(primitive or BottomType). If there's a = b ==>
   * a is the least common ancestor of a and b; If there's b = a ==> b is the least common ancestor
   * of a and b; If there are c = a and c = b, but there's no b = a or a = b ==> c is the least
   * common ancestor of a and b;
   */
  @Override
  public Collection<Type> getLeastCommonAncestor(Type a, Type b) {
    if (a.equals(b)) {
      return Collections.singleton(a);
    } else if (arePrimitives(a, b)) {
      if (isAncestor(a, b)) {
        return Collections.singleton(a);
      } else if (isAncestor(b, a)) {
        return Collections.singleton(b);
      } else if (a instanceof PrimitiveType.ByteType) {
        if (b instanceof PrimitiveType.ShortType
            || b instanceof PrimitiveType.CharType
            || b instanceof PrimitiveType.Integer32767Type) {
          return Collections.singleton(PrimitiveType.getInt());
        } else {
          return Collections.emptySet();
        }
      } else if (a instanceof PrimitiveType.ShortType) {
        if (b instanceof PrimitiveType.ByteType || b instanceof PrimitiveType.CharType) {
          return Collections.singleton(PrimitiveType.getInt());
        } else {
          return Collections.emptySet();
        }
      } else if (a instanceof PrimitiveType.CharType) {
        if (b instanceof PrimitiveType.ByteType || b instanceof PrimitiveType.ShortType) {
          return Collections.singleton(PrimitiveType.getInt());
        } else {
          return Collections.emptySet();
        }
      } else {
        return Collections.emptySet();
      }
    } else {
      return Collections.emptySet();
    }
  }

  /**
   * Check the ancestor-relationship between two primitive types <code>ancestor</code> and <code>
   * child</code>, namely, whether child can be assigned to ancestor directly to obtain: ancestor =
   * child.
   */
  @Override
  public boolean isAncestor(Type ancestor, Type child) {

    if (ancestor.equals(child)) {
      return true;
    } else if (arePrimitives(ancestor, child)) {
      if (ancestor instanceof PrimitiveType.Integer1Type) {
        return child instanceof BottomType;
      } else if (ancestor instanceof PrimitiveType.BooleanType
          || ancestor instanceof PrimitiveType.Integer127Type) {
        return child instanceof PrimitiveType.Integer1Type || child instanceof BottomType;
      } else if (ancestor instanceof PrimitiveType.ByteType
          || ancestor instanceof PrimitiveType.Integer32767Type) {
        return child instanceof PrimitiveType.Integer127Type
            || child instanceof PrimitiveType.Integer1Type
            || child instanceof BottomType;
      } else if (ancestor instanceof PrimitiveType.CharType
          || ancestor instanceof PrimitiveType.ShortType) {
        return child instanceof PrimitiveType.Integer32767Type
            || child instanceof PrimitiveType.Integer127Type
            || child instanceof PrimitiveType.Integer1Type
            || child instanceof BottomType;
      } else if (ancestor instanceof PrimitiveType.IntType) {
        return (!(child instanceof PrimitiveType.BooleanType) && (child instanceof IntegerType))
            || child instanceof BottomType;
      } else {
        return child instanceof BottomType;
      }
    } else if (ancestor instanceof ArrayType && child instanceof ArrayType) {
      Type ancestorBase = ((ArrayType) ancestor).getBaseType();
      Type childBase = ((ArrayType) child).getBaseType();
      int ancestorDim = ((ArrayType) ancestor).getDimension();
      int childDim = ((ArrayType) child).getDimension();

      if (ancestorDim == childDim && arePrimitives(ancestorBase, childBase)) {
        if (ancestorBase instanceof PrimitiveType.Integer1Type) {
          return childBase instanceof BottomType;
        } else if (ancestorBase instanceof PrimitiveType.BooleanType
            || ancestorBase instanceof PrimitiveType.Integer127Type) {
          return childBase instanceof PrimitiveType.Integer1Type || childBase instanceof BottomType;
        } else if (ancestorBase instanceof PrimitiveType.ByteType
            || ancestorBase instanceof PrimitiveType.Integer32767Type) {
          return childBase instanceof PrimitiveType.Integer127Type
              || childBase instanceof PrimitiveType.Integer1Type
              || childBase instanceof BottomType;
        } else if (ancestorBase instanceof PrimitiveType.CharType
            || ancestorBase instanceof PrimitiveType.ShortType
            || ancestorBase instanceof PrimitiveType.IntType) {
          return childBase instanceof PrimitiveType.Integer32767Type
              || childBase instanceof PrimitiveType.Integer127Type
              || childBase instanceof PrimitiveType.Integer1Type
              || childBase instanceof BottomType;
        } else {
          return childBase instanceof BottomType;
        }
      } else {
        return childBase instanceof BottomType;
      }
    } else {
      return child instanceof BottomType;
    }
  }

  /** Check whether the two given types are primitives or BottomType */
  public boolean arePrimitives(Type a, Type b) {
    if (a instanceof PrimitiveType || a instanceof BottomType) {
      if (b instanceof PrimitiveType || b instanceof BottomType) {
        return true;
      } else {
        return false;
      }
    } else {
      return false;
    }
  }
}
