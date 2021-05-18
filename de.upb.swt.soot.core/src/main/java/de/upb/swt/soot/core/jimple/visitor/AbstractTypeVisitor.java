package de.upb.swt.soot.core.jimple.visitor;

import de.upb.swt.soot.core.types.*;

/** @author Markus Schmidt */
public abstract class AbstractTypeVisitor<V> extends AbstractVisitor<V> implements TypeVisitor {

  @Override
  public void caseBooleanType(PrimitiveType t) {
    defaultCaseType(t);
  }

  @Override
  public void caseByteType(PrimitiveType t) {
    defaultCaseType(t);
  }

  @Override
  public void caseCharType(PrimitiveType t) {
    defaultCaseType(t);
  }

  @Override
  public void caseShortType(PrimitiveType t) {
    defaultCaseType(t);
  }

  @Override
  public void caseIntType(PrimitiveType t) {
    defaultCaseType(t);
  }

  @Override
  public void caseLongType(PrimitiveType t) {
    defaultCaseType(t);
  }

  @Override
  public void caseDoubleType(PrimitiveType t) {
    defaultCaseType(t);
  }

  @Override
  public void caseFloatType(PrimitiveType t) {
    defaultCaseType(t);
  }

  @Override
  public void caseArrayType(ArrayType t) {
    defaultCaseType(t);
  }

  @Override
  public void caseClassType(ClassType t) {
    defaultCaseType(t);
  }

  @Override
  public void caseNullType(NullType t) {
    defaultCaseType(t);
  }

  @Override
  public void caseVoidType(VoidType t) {
    defaultCaseType(t);
  }

  @Override
  public void caseUnknownType(UnknownType t) {
    defaultCaseType(t);
  }

  @Override
  public void defaultCaseType(Type t) {}
}
