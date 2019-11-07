package de.upb.swt.soot.core.types;

import com.google.common.base.Objects;

public class ArrayType extends ReferenceType {

  private final Type baseType;

  private final int dimension;

  public ArrayType(Type baseType, int dimension) {
    this.baseType = baseType;
    this.dimension = dimension;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(baseType);
    for (int i = 0; i < dimension; i++) {
      sb.append("[]");
    }
    return sb.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ArrayType that = (ArrayType) o;
    return dimension == that.dimension && Objects.equal(baseType, that.baseType);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(baseType, dimension);
  }

  public Type getBaseType() {
    return baseType;
  }

  public int getDimension() {
    return dimension;
  }
}
