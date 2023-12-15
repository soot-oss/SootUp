package sootup.core.model;

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
