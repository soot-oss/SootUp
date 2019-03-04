package de.upb.soot.jimple.basic;

import com.ibm.wala.cast.tree.CAstSourcePositionMap.Position;
import com.ibm.wala.cast.tree.impl.LineNumberPosition;

/**
 * This class stores position information stored for a statement.
 *
 * @author Linghui Luo
 */
public class PositionInfo {
  private final Position stmtPosition;
  private final Position[] operandPositions;

  /**
   * Create an instance with no position information.
   *
   * @return an instance with no position information.
   */
  public static PositionInfo createNoPositionInfo() {
    return new PositionInfo(null, null);
  }

  /**
   * Create an instance only from line number, this is usually the case from byte code front-end.
   *
   * @param lineNumber the line number of the statement.
   */
  public PositionInfo(int lineNumber) {
    this.stmtPosition = new LineNumberPosition(null, null, lineNumber);
    this.operandPositions = null;
  }

  /**
   * Create an instance from given statement position and operand positions.
   *
   * @param stmtPosition the position of the statement
   * @param operandPositions the operand positions
   */
  public PositionInfo(Position stmtPosition, Position[] operandPositions) {
    this.stmtPosition = stmtPosition;
    this.operandPositions = operandPositions;
  }

  /**
   * Return the position of the statement.
   *
   * @return the position of the statement
   */
  public Position getStmtPosition() {
    if (this.stmtPosition != null) {
      return this.stmtPosition;
    } else {
      return new NoPositionInformation();
    }
  }

  /**
   * Return the precise position of the given operand in the statement.
   *
   * @param index the operand index
   * @return the position of the given operand
   */
  public Position getOperandPosition(int index) {
    if (this.operandPositions != null && index >= 0 && index < this.operandPositions.length) {
      return this.operandPositions[index];
    } else {
      return new NoPositionInformation();
    }
  }

  @Override
  public PositionInfo clone() {
    return new PositionInfo(stmtPosition, operandPositions);
  }

  @Override
  public String toString() {
    StringBuilder s = new StringBuilder();
    s.append("stmtPosition: " + getStmtPosition().toString() + "\n");
    s.append("operandPositions: ");
    if (operandPositions != null) {
      s.append("\n");
      for (int i = 0; i < operandPositions.length; i++) {
        s.append(i + ": " + operandPositions[i] + " ");
      }
    } else {
      s.append("No position info");
    }
    return s.toString();
  }
}
