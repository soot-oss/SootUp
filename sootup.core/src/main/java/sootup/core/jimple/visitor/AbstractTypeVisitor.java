package sootup.core.jimple.visitor;

import javax.annotation.Nonnull;
import sootup.core.types.*;

/** @author Markus Schmidt */
public abstract class AbstractTypeVisitor<V> extends AbstractVisitor<V> implements TypeVisitor {

  @Override
  public void caseBooleanType() {
    defaultCaseType();
  }

  @Override
  public void caseByteType() {
    defaultCaseType();
  }

  @Override
  public void caseCharType() {
    defaultCaseType();
  }

  @Override
  public void caseShortType() {
    defaultCaseType();
  }

  @Override
  public void caseIntType() {
    defaultCaseType();
  }

  @Override
  public void caseLongType() {
    defaultCaseType();
  }

  @Override
  public void caseDoubleType() {
    defaultCaseType();
  }

  @Override
  public void caseFloatType() {
    defaultCaseType();
  }

  @Override
  public void caseArrayType() {
    defaultCaseType();
  }

  @Override
  public void caseClassType(@Nonnull ClassType classType) {
    defaultCaseType();
  }

  @Override
  public void caseNullType() {
    defaultCaseType();
  }

  @Override
  public void caseVoidType() {
    defaultCaseType();
  }

  @Override
  public void caseUnknownType() {
    defaultCaseType();
  }

  @Override
  public void defaultCaseType() {}
}
