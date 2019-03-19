package de.upb.soot.signatures;

import com.google.common.base.Objects;

public class ArrayTypeSignature extends ReferenceTypeSignature {

  private final TypeSignature baseType;

  private final int dimension;

  protected ArrayTypeSignature(TypeSignature baseType, int dimension) {
    this.baseType = baseType;
    this.dimension = dimension;
  }

  // Todo please implemnt it

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
    ArrayTypeSignature that = (ArrayTypeSignature) o;
    return dimension == that.dimension && Objects.equal(baseType, that.baseType);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(baseType, dimension);
  }

  public TypeSignature getBaseType() {
    return baseType;
  }

  public int getDimension() {
    return dimension;
  }
}
