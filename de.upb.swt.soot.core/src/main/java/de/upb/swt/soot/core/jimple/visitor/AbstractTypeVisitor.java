package de.upb.swt.soot.core.jimple.visitor;

import de.upb.swt.soot.core.types.*;

public abstract class AbstractTypeVisitor<V> implements TypeVisitor {
  private V result;

  @Override
  public void caseBooleanType(PrimitiveType t) {
    caseDefault(t);
  }

  @Override
  public void caseByteType(PrimitiveType t) {
    caseDefault(t);
  }

  @Override
  public void caseCharType(PrimitiveType t) {
    caseDefault(t);
  }

  @Override
  public void caseShortType(PrimitiveType t) {
    caseDefault(t);
  }

  @Override
  public void caseIntType(PrimitiveType t) {
    caseDefault(t);
  }

  @Override
  public void caseLongType(PrimitiveType t) {
    caseDefault(t);
  }

  @Override
  public void caseDoubleType(PrimitiveType t) {
    caseDefault(t);
  }

  @Override
  public void caseFloatType(PrimitiveType t) {
    caseDefault(t);
  }

  @Override
  public void caseArrayType(ArrayType t) {
    caseDefault(t);
  }

  @Override
  public void caseClassType(ClassType t) {
    caseDefault(t);
  }

  @Override
  public void caseNullType(NullType t) {
    caseDefault(t);
  }

  @Override
  public void caseVoidType(VoidType t) {
    caseDefault(t);
  }

  @Override
  public void caseUnknownType(UnknownType t) {
    caseDefault(t);
  }

  @Override
  public void caseDefault(Type t) {}

  public V getResult() {
    return result;
  }

  protected void setResult(V result) {
    this.result = result;
  }
}
