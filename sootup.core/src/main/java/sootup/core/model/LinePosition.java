package sootup.core.model;

/*-
 * #%L
 * SootUp
 * %%
 * Copyright (C) 1997 - 2024 Raja Vallée-Rai and others
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

public class LinePosition extends Position {
  private final int lineNo;

  public LinePosition(int lineNo) {
    this.lineNo = lineNo;
  }

  @Override
  public int getFirstLine() {
    return lineNo;
  }

  @Override
  public int getLastLine() {
    return lineNo + 1;
  }

  @Override
  public int getFirstCol() {
    return 0;
  }

  @Override
  public int getLastCol() {
    return -1;
  }
}
