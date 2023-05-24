package sootup.core.model;

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

import java.util.Objects;

/** This class represents Position Information i.e. for IDEs to locate positions in sources. */
// TODO: [ms] it represents a range - rename?
public class FullPosition extends Position {
  private final int firstLine;
  private final int firstCol;
  private final int lastLine;
  private final int lastCol;

  public FullPosition(int firstLine, int firstCol, int lastLine, int lastCol) {
    this.firstLine = firstLine;
    this.firstCol = firstCol;
    this.lastLine = lastLine;
    this.lastCol = lastCol;
  }

  @Override
  public int getFirstLine() {
    return firstLine;
  }

  @Override
  public int getLastLine() {
    return lastLine;
  }

  @Override
  public int getFirstCol() {
    return firstCol;
  }

  @Override
  public int getLastCol() {
    return lastCol;
  }

  @Override
  public int hashCode() {
    return Objects.hash(firstLine, firstCol, lastLine, lastCol);
  }
}
