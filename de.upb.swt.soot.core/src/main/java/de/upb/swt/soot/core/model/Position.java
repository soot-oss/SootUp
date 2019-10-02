package de.upb.swt.soot.core.model;

public class Position {

  private int firstLine;
  private int lastLine;
  private int firstCol;
  private int lastCol;

  public Position(int firstLine, int firstCol, int lastLine, int lastCol) {
    this.firstLine = firstLine;
    this.lastLine = lastLine;
    this.firstCol = firstCol;
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
