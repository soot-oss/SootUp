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
import sootup.core.model.Position;

/**
 * Source position information for a statement. Additional metadata is exposed through interfaces
 * such as {@link FullStmtPositionInfo} and {@link LocalVariableStmtPositionInfo}.
 *
 * @author Linghui Luo, Markus Schmidt
 */
public interface StmtPositionInfo {

  /**
   * Create an instance with no position information.
   *
   * @return an instance with no position information.
   */
  @NonNull
  static StmtPositionInfo getNoStmtPositionInfo() {
    return DefaultStmtPositionInfoFactory.getNoStmtPositionInfo();
  }

  /**
   * Return the position of the statement.
   *
   * @return the position of the statement
   */
  @NonNull Position getStmtPosition();

  /** Returns the factory responsible for copying this implementation and its metadata variants. */
  @NonNull StmtPositionInfoFactory getFactory();

  /**
   * Returns a copy with a different statement position, retaining all other metadata and supported
   * metadata interfaces.
   */
  @NonNull
  default StmtPositionInfo withStmtPosition(@NonNull Position stmtPosition) {
    return getFactory().withStmtPosition(this, stmtPosition);
  }
}
