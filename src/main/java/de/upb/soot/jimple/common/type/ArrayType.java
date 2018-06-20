package de.upb.soot.jimple.common.type;

import de.upb.soot.jimple.visitor.IVisitor;

public class ArrayType extends Type {

  public int numDimensions;
  public Type baseType;

  @Override
  public void accept(IVisitor sw) {
    // TODO Auto-generated method stub

  }

  public Type getElementType() {
    // TODO Auto-generated method stub
    return null;
  }

  public static ArrayType v(Type baseType, int numDimensions) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String toString() {
    // TODO Auto-generated method stub
    return null;
  }

}
