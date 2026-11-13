package sootup.core.jimple.basic;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2019-2023 Linghui Luo, Markus Schmidt
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
import org.jspecify.annotations.Nullable;
import sootup.core.model.LocalVariableScope;
import sootup.core.model.Position;

/**
 * Copies a family of statement position-info implementations. Implementations must preserve
 * metadata other than the requested change, leave the input unchanged, and return variants with the
 * appropriate {@link StmtPositionInfo#getFactory() factory}.
 *
 * <p>Custom position-info types supply their own factory to preserve additional state and implement
 * transitions between variants with and without operand positions or local variable metadata.
 * Unsupported operations should throw {@link UnsupportedOperationException} rather than discard
 * metadata.
 */
public interface StmtPositionInfoFactory {

  /**
   * Copies the statement position, retaining operand positions, captured scopes and custom data.
   */
  @NonNull StmtPositionInfo withStmtPosition(
      @NonNull StmtPositionInfo info, @NonNull Position stmtPosition);

  /**
   * Attaches or replaces operand positions, retaining other metadata and defensively copying the
   * supplied array. A non-null array, including an empty one, requires a result implementing {@link
   * FullStmtPositionInfo}; null removes operand positions and requires a result without that
   * interface.
   */
  @NonNull StmtPositionInfo withOperandPositions(
      @NonNull StmtPositionInfo info, @Nullable Position[] operandPositions);

  /**
   * Attaches or replaces a captured scope, retaining other metadata. A null scope removes capture;
   * {@link LocalVariableScope#empty()} records a captured empty scope. Non-null scopes require a
   * result implementing {@link LocalVariableStmtPositionInfo}; null scopes require a result without
   * that interface.
   */
  @NonNull StmtPositionInfo withLocalVariables(
      @NonNull StmtPositionInfo info, @Nullable LocalVariableScope scope);
}
