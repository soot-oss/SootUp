package de.upb.swt.soot.core.types;
/*-
 * #%L
 * Soot
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

/** @author Zun Wang Some util-methods for Type */
public class TypeUtils {
  public static boolean isObjectLikeType(Type type) {
    return type.toString().equals("java.lang.Object")
        || type.toString().equals("java.io.Serializable")
        || type.toString().equals("java.lang.Cloneable");
  }

  public static boolean isIntLikeType(Type type) {
    return type == PrimitiveType.IntType.getInstance()
        || type == PrimitiveType.ByteType.getInstance()
        || type == PrimitiveType.ShortType.getInstance()
        || type == PrimitiveType.CharType.getInstance()
        || type == PrimitiveType.BooleanType.getInstance();
  }

  public static boolean isObject(Type type) {
    return type.toString().equals("java.lang.Object");
  }

  /**
   * This method is used to make an array type for the given <c>type</c>. If the given <c>type</c>
   * is an array type, then increase its dimension with given <c>dim</c>
   */
  public static ArrayType makeArrayType(Type type, int dim) {
    if (type instanceof ArrayType) {
      return new ArrayType(
          ((ArrayType) type).getBaseType(), ((ArrayType) type).getDimension() + dim);
    } else {
      return new ArrayType(type, dim);
    }
  }

  public static int getValueBitSize(Type type) {
    if (type instanceof PrimitiveType.BooleanType) {
      return 1;
    }
    if (type instanceof PrimitiveType.ByteType) {
      return 8;
    }
    if (type instanceof PrimitiveType.ShortType) {
      return 16;
    }
    if (type instanceof PrimitiveType.IntType || type instanceof PrimitiveType.FloatType) {
      return 32;
    }
    if (type instanceof PrimitiveType.LongType
        || type instanceof PrimitiveType.DoubleType
        || type instanceof ClassType) {
      return 64;
    }
    return 0;
  }
}
