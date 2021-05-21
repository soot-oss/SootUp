package de.upb.swt.soot.core.jimple.visitor;

import de.upb.swt.soot.core.types.*;

/** @author Markus Schmidt */
public abstract class AbstractTypeVisitor<V> extends AbstractVisitor<V> implements TypeVisitor {

  @Override
  public void caseBooleanType(PrimitiveType.BooleanType type) {
    defaultCaseType(type);
  }

  @Override
  public void caseByteType(PrimitiveType.ByteType type) {
    defaultCaseType(type);
  }

  @Override
  public void caseCharType(PrimitiveType.CharType type) {
    defaultCaseType(type);
  }

  @Override
  public void caseShortType(PrimitiveType.ShortType type) {
    defaultCaseType(type);
  }

  @Override
  public void caseIntType(PrimitiveType.IntType type) {
    defaultCaseType(type);
  }

  @Override
  public void caseLongType(PrimitiveType.LongType type) {
    defaultCaseType(type);
  }

  @Override
  public void caseDoubleType(PrimitiveType.DoubleType type) {
    defaultCaseType(type);
  }

  @Override
  public void caseFloatType(PrimitiveType.FloatType type) {
    defaultCaseType(type);
  }

  @Override
  public void caseArrayType(ArrayType type) {
    defaultCaseType(type);
  }

  @Override
  public void caseClassType(ClassType type) {
    defaultCaseType(type);
  }

  @Override
  public void caseNullType(NullType type) {
    defaultCaseType(type);
  }

  @Override
  public void caseVoidType(VoidType type) {
    defaultCaseType(type);
  }

  @Override
  public void caseUnknownType(UnknownType type) {
    defaultCaseType(type);
  }

  @Override
  public void defaultCaseType(Type type) {}
}
