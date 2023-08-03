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

import javax.annotation.Nonnull;
import sootup.core.model.Position;
import sootup.core.util.Copyable;

/**
 * This class stores position information stored for a statement. line number + information about
 * its operands
 *
 * @author Linghui Luo, Markus Schmidt
 */
public class FullStmtPositionInfo extends SimpleStmtPositionInfo implements Copyable {
  @Nonnull protected final Position[] operandPositions;

  /**
   * Create an instance from given statement position and operand positions.
   *
   * @param stmtPosition the position of the statement
   * @param operandPositions the operand positions
   */
  public FullStmtPositionInfo(
      @Nonnull Position stmtPosition, @Nonnull Position[] operandPositions) {
    super(stmtPosition);
    this.operandPositions = operandPositions;
  }

  /**
   * Return the position of the statement.
   *
   * @return the position of the statement
   */
  @Nonnull
  public Position getStmtPosition() {
    return this.stmtPosition;
  }

  /**
   * Return the precise position of the given operand in the statement.
   *
   * @param index the operand index
   * @return the position of the given operand
   */
  public Position getOperandPosition(int index) {
    if (index >= 0 && index < this.operandPositions.length) {
      return this.operandPositions[index];
    } else {
      return NoPositionInformation.getInstance();
    }
  }

  @Override
  public String toString() {
    StringBuilder s = new StringBuilder();
    s.append(super.toString());
    s.append("operands at: ");
    for (int i = 0; i < operandPositions.length; i++) {
      s.append(i).append(": ").append(operandPositions[i]).append(" ");
    }
    return s.toString();
  }

  @Nonnull
  public StmtPositionInfo withStmtPosition(@Nonnull Position stmtPosition) {
    return new FullStmtPositionInfo(stmtPosition, operandPositions);
  }

  @Nonnull
  public StmtPositionInfo withOperandPositions(@Nonnull Position[] operandPositions) {
    return new FullStmtPositionInfo(stmtPosition, operandPositions);
  }
}
