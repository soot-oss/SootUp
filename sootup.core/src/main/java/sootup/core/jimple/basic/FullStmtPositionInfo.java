package sootup.core.jimple.basic;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2019-2023 Linghui Luo and others
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
import sootup.core.model.Position;

/**
 * Statement position information with precise operand coordinates. Instances can be created with
 * {@link DefaultStmtPositionInfoFactory#create(Position, Position[])}.
 *
 * @author Linghui Luo, Markus Schmidt
 */
public interface FullStmtPositionInfo extends StmtPositionInfo {

  /**
   * Return the precise position of the given operand in the statement.
   *
   * @param index the operand index
   * @return the operand position, or {@link NoPositionInformation} if the index is out of bounds
   */
  @NonNull Position getOperandPosition(int index);

  @NonNull
  @Override
  default FullStmtPositionInfo withStmtPosition(@NonNull Position stmtPosition) {
    return (FullStmtPositionInfo) getFactory().withStmtPosition(this, stmtPosition);
  }

  /** Returns a copy with different operand positions, retaining all other metadata. */
  @NonNull
  default FullStmtPositionInfo withOperandPositions(@NonNull Position[] operandPositions) {
    return (FullStmtPositionInfo) getFactory().withOperandPositions(this, operandPositions);
  }
}
