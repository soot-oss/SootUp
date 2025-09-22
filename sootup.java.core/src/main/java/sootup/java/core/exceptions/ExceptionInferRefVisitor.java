package sootup.java.core.exceptions;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2019-2025 Zun Wang
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
import org.jspecify.annotations.NonNull;
import sootup.core.jimple.common.ref.*;
import sootup.core.jimple.visitor.AbstractRefVisitor;
import sootup.core.util.ImmutableUtils;

public class ExceptionInferRefVisitor extends AbstractRefVisitor {

  private ExceptionInferResult result;

  public ExceptionInferRefVisitor() {}

  @Override
  public void caseStaticFieldRef(@NonNull JStaticFieldRef ref) {
    result = new ExceptionInferResult(ExceptionInferResult.ErrorType.INITIALIZATION_ERROR);
  }

  @Override
  public void caseInstanceFieldRef(@NonNull JInstanceFieldRef ref) {
    result =
        new ExceptionInferResult(
            ImmutableUtils.immutableSet(
                ExceptionInferResult.ErrorType.RESOLVE_FIELD_ERROR,
                ExceptionInferResult.ExceptionType.NUll_POINTER_EXCEPTION));
  }

  @Override
  public void caseArrayRef(@NonNull JArrayRef ref) {
    result =
        new ExceptionInferResult(
            ImmutableUtils.immutableSet(
                ExceptionInferResult.ExceptionType.INDEX_OUT_OF_BOUNDS_EXCEPTION,
                ExceptionInferResult.ExceptionType.NUll_POINTER_EXCEPTION));
  }

  @Override
  public void caseParameterRef(@NonNull JParameterRef ref) {
    defaultCaseRef(ref);
  }

  @Override
  public void caseCaughtExceptionRef(@NonNull JCaughtExceptionRef ref) {
    defaultCaseRef(ref);
  }

  @Override
  public void caseThisRef(@NonNull JThisRef ref) {
    defaultCaseRef(ref);
  }

  @Override
  public void defaultCaseRef(@NonNull Ref ref) {
    result = ExceptionInferResult.createEmptyException();
  }

  public ExceptionInferResult getResult() {
    return result;
  }
}
