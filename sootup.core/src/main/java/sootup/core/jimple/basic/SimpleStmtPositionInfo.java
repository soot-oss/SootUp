package sootup.core.jimple.basic;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2019-2020 Markus Schmidt
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
import javax.annotation.Nullable;
import sootup.core.model.LinePosition;
import sootup.core.model.Position;

/**
 * This class stores position information (the line number + first/last column) stored for a
 * statement.
 *
 * @author Markus Schmidt
 */
public class SimpleStmtPositionInfo extends StmtPositionInfo {

  @Nonnull protected final Position stmtPosition;

  public SimpleStmtPositionInfo(@Nonnull Position stmtPosition) {
    this.stmtPosition = stmtPosition;
  }

  /**
   * Create an instance only from line number, this is usually the case from byte code front-end.
   *
   * @param lineNumber the line number of the statement.
   */
  public SimpleStmtPositionInfo(int lineNumber) {
    stmtPosition = new LinePosition(lineNumber);
  }

  @Nonnull
  @Override
  public Position getStmtPosition() {
    return stmtPosition;
  }

  @Nullable
  @Override
  public Position getOperandPosition(int index) {
    return null;
  }
}
