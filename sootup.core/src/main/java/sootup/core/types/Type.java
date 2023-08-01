package sootup.core.types;

/*-
 * #%L
 * Soot
 * %%
 * Copyright (C) 2018-2020 Markus Schmidt
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

import javax.annotation.Nonnull;
import sootup.core.jimple.visitor.Acceptor;
import sootup.core.jimple.visitor.TypeVisitor;

/** Represents the signature of a Java type, e.g., a class, a primitive type, void, or null. */
public abstract class Type implements Acceptor<TypeVisitor> {

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
   * This method is used to make an array type for the given type. If the given type is an array
   * type, then increase its dimension with given dim
   */
  public static ArrayType createArrayType(@Nonnull Type type, int dim) {
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
