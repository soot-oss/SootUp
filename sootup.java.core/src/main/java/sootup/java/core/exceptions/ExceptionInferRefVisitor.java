package sootup.java.core.exceptions;

import javax.annotation.Nonnull;
import sootup.core.jimple.common.ref.*;
import sootup.core.jimple.visitor.AbstractRefVisitor;
import sootup.core.util.ImmutableUtils;

public class ExceptionInferRefVisitor extends AbstractRefVisitor {

  private ExceptionInferResult result;

  public ExceptionInferRefVisitor() {}

  @Override
  public void caseStaticFieldRef(@Nonnull JStaticFieldRef ref) {
    result = new ExceptionInferResult(ExceptionInferResult.ErrorType.INITIALIZATION_ERROR);
  }

  @Override
  public void caseInstanceFieldRef(@Nonnull JInstanceFieldRef ref) {
    result =
        new ExceptionInferResult(
            ImmutableUtils.immutableSet(
                ExceptionInferResult.ErrorType.RESOLVE_FIELD_ERROR,
                ExceptionInferResult.ExceptionType.NUll_POINTER_EXCEPTION));
  }

  @Override
  public void caseArrayRef(@Nonnull JArrayRef ref) {
    result =
        new ExceptionInferResult(
            ImmutableUtils.immutableSet(
                ExceptionInferResult.ExceptionType.INDEX_OUT_OF_BOUNDS_EXCEPTION,
                ExceptionInferResult.ExceptionType.NUll_POINTER_EXCEPTION));
  }

  @Override
  public void caseParameterRef(@Nonnull JParameterRef ref) {
    defaultCaseRef(ref);
  }

  @Override
  public void caseCaughtExceptionRef(@Nonnull JCaughtExceptionRef ref) {
    defaultCaseRef(ref);
  }

  @Override
  public void caseThisRef(@Nonnull JThisRef ref) {
    defaultCaseRef(ref);
  }

  @Override
  public void defaultCaseRef(@Nonnull Ref ref) {
    result = ExceptionInferResult.createEmptyException();
  }

  public ExceptionInferResult getResult() {
    return result;
  }
}
