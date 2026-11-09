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

import java.util.Optional;
import org.jspecify.annotations.NonNull;
import sootup.core.jimple.visitor.AbstractTypeVisitor;
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
    return type instanceof PrimitiveType.IntType;
    /* type == PrimitiveType.IntType.getInstance()
       || type == PrimitiveType.ByteType.getInstance()
       || type == PrimitiveType.ShortType.getInstance()
       || type == PrimitiveType.CharType.getInstance()
       || type == PrimitiveType.BooleanType.getInstance();
    */
  }

  public static boolean isObject(Type type) {
    return type.toString().equals("java.lang.Object");
  }

  /**
   * This method is used to make an array type for the given type. If the given type is an array
   * type, then increase its dimension with given dim
   */
  public static ArrayType createArrayType(@NonNull Type type, int dim) {
    if (type instanceof ArrayType) {
      return new ArrayType(
          ((ArrayType) type).getBaseType(), ((ArrayType) type).getDimension() + dim);
    } else {
      return new ArrayType(type, dim);
    }
  }

  /**
   * Returns the size in bits of a value of the given type: 1 for boolean, 8 for byte, 16 for
   * short/char, 32 for int/float, 64 for long/double and class types, and 0 for every other type.
   */
  public static int getValueBitSize(Type type) {
    BitSizeVisitor visitor = new BitSizeVisitor();
    type.accept(visitor);
    return visitor.bitSize;
  }

  /** Computes the bit size of a type via dispatch instead of an instanceof chain. */
  private static final class BitSizeVisitor extends AbstractTypeVisitor {
    private int bitSize = 0;

    @Override
    public void caseBooleanType() {
      bitSize = 1;
    }

    @Override
    public void caseByteType() {
      bitSize = 8;
    }

    @Override
    public void caseShortType() {
      bitSize = 16;
    }

    @Override
    public void caseCharType() {
      bitSize = 16;
    }

    @Override
    public void caseIntType() {
      bitSize = 32;
    }

    @Override
    public void caseFloatType() {
      bitSize = 32;
    }

    @Override
    public void caseLongType() {
      bitSize = 64;
    }

    @Override
    public void caseDoubleType() {
      bitSize = 64;
    }

    @Override
    public void caseClassType(@NonNull ClassType classType) {
      bitSize = 64;
    }
  }

  protected boolean isPrimitiveType() {
    return false;
  }

  protected boolean isReferenceType() {
    return false;
  }

  protected boolean isBottomType() {
    return false;
  }

  protected boolean isTopType() {
    return false;
  }

  protected boolean isUnknownType() {
    return false;
  }

  protected boolean isVoidType() {
    return false;
  }

  protected PrimitiveType asPrimitiveType() {
    return null;
  }

  protected ReferenceType asReferenceType() {
    return null;
  }

  protected Type asBottomType() {
    return null;
  }

  protected Type asTopType() {
    return null;
  }

  protected UnknownType asUnknownType() {
    return null;
  }

  protected VoidType asVoidType() {
    return null;
  }

  protected Optional<PrimitiveType> toPrimitiveType() {
    return Optional.empty();
  }

  protected Optional<ReferenceType> toReferenceType() {
    return Optional.empty();
  }

  protected Optional<Type> toBottomType() {
    return Optional.empty();
  }

  protected Optional<Type> toTopType() {
    return Optional.empty();
  }

  protected Optional<UnknownType> toUnknownType() {
    return Optional.empty();
  }

  protected Optional<VoidType> toVoidType() {
    return Optional.empty();
  }
}
