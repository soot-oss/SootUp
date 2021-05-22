package de.upb.swt.soot.core.jimple.visitor;

import de.upb.swt.soot.core.types.*;
import javax.annotation.Nonnull;

/** @author Markus Schmidt */
public abstract class AbstractTypeVisitor<V> extends AbstractVisitor<V> implements TypeVisitor {

  @Override
  public void caseBooleanType(@Nonnull PrimitiveType.BooleanType type) {
    defaultCaseType(type);
  }

  @Override
  public void caseByteType(@Nonnull PrimitiveType.ByteType type) {
    defaultCaseType(type);
  }

  @Override
  public void caseCharType(@Nonnull PrimitiveType.CharType type) {
    defaultCaseType(type);
  }

  @Override
  public void caseShortType(@Nonnull PrimitiveType.ShortType type) {
    defaultCaseType(type);
  }

  @Override
  public void caseIntType(@Nonnull PrimitiveType.IntType type) {
    defaultCaseType(type);
  }

  @Override
  public void caseLongType(@Nonnull PrimitiveType.LongType type) {
    defaultCaseType(type);
  }

  @Override
  public void caseDoubleType(@Nonnull PrimitiveType.DoubleType type) {
    defaultCaseType(type);
  }

  @Override
  public void caseFloatType(@Nonnull PrimitiveType.FloatType type) {
    defaultCaseType(type);
  }

  @Override
  public void caseArrayType(@Nonnull ArrayType type) {
    defaultCaseType(type);
  }

  @Override
  public void caseClassType(@Nonnull ClassType type) {
    defaultCaseType(type);
  }

  @Override
  public void caseNullType(@Nonnull NullType type) {
    defaultCaseType(type);
  }

  @Override
  public void caseVoidType(@Nonnull VoidType type) {
    defaultCaseType(type);
  }

  @Override
  public void caseUnknownType(@Nonnull UnknownType type) {
    defaultCaseType(type);
  }

  @Override
  public void defaultCaseType(@Nonnull Type type) {}
}
