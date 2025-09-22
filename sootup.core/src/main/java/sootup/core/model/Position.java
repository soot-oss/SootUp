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

import org.jspecify.annotations.NonNull;

public abstract class Position implements Comparable<Position> {
  public abstract int getFirstLine();

  public abstract int getLastLine();

  public abstract int getFirstCol();

  public abstract int getLastCol();

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append("[").append(getFirstLine());
    if (getFirstCol() >= 0) {
      sb.append(":").append(getFirstCol());
    }
    sb.append("-").append(getLastLine());
    if (getLastCol() >= 0) {
      sb.append(":").append(getLastCol());
    }
    sb.append("]");
    return sb.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Position position = (Position) o;
    return getFirstLine() == position.getFirstLine()
        && getFirstCol() == position.getFirstCol()
        && getLastLine() == position.getLastLine()
        && getLastCol() == position.getLastCol();
  }

  /**
   * Compares "Positions" by their starting line/column Note: this class has a natural ordering that
   * is inconsistent with equals
   */
  public int compareTo(@NonNull Position position) {
    if (getFirstLine() < position.getFirstLine()) {
      return -1;
    } else if (getFirstLine() == position.getFirstLine()) {
      if (getFirstCol() < position.getFirstCol()) {
        return -1;
      } else if (getFirstCol() == position.getFirstCol()) {
        return 0;
      }
      return 1;
    }
    return 1;
  }
}
