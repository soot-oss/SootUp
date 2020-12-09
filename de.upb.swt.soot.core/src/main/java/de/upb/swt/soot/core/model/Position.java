package de.upb.swt.soot.core.model;

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

/** This class represents Position Information i.e. for IDEs to locate positions in sources. */
// TODO: [ms] it represents a range - rename?
public class Position {

  private final int firstLine;
  private final int firstCol;
  private final int lastLine;
  private final int lastCol;

  public Position(int firstLine, int firstCol, int lastLine, int lastCol) {
    this.firstLine = firstLine;
    this.firstCol = firstCol;
    this.lastLine = lastLine;
    this.lastCol = lastCol;
  }

  public int getFirstLine() {
    return firstLine;
  }

  public int getLastLine() {
    return lastLine;
  }

  public int getFirstCol() {
    return firstCol;
  }

  public int getLastCol() {
    return lastCol;
  }

  public String toString() {
    return "[" + firstLine + ":" + firstCol + "-" + lastLine + ":" + lastCol + "]";
  }
}
