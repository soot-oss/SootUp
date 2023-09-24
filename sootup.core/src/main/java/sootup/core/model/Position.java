package sootup.core.model;

import javax.annotation.Nonnull;

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
    FullPosition position = (FullPosition) o;
    return getFirstLine() == position.getFirstLine()
        && getFirstCol() == position.getFirstCol()
        && getLastLine() == position.getLastLine()
        && getLastCol() == position.getLastCol();
  }

  /**
   * Compares "Positions" by their starting line/column Note: this class has a natural ordering that
   * is inconsistent with equals
   */
  public int compareTo(@Nonnull Position position) {
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
