package de.upb.swt.soot.core.jimple.basic;

import de.upb.swt.soot.core.model.Position;
import de.upb.swt.soot.core.util.Copyable;
import javax.annotation.Nonnull;

/**
 * This class stores position information stored for a statement.
 *
 * @author Linghui Luo
 */
public final class StmtPositionInfo implements Copyable {
  private final Position stmtPosition;
  private final Position[] operandPositions;

  /**
   * Create an instance with no position information.
   *
   * @return an instance with no position information.
   */
  public static StmtPositionInfo createNoStmtPositionInfo() {
    return new StmtPositionInfo(null, null);
  }

  /**
   * Create an instance only from line number, this is usually the case from byte code front-end.
   *
   * @param lineNumber the line number of the statement.
   */
  public StmtPositionInfo(int lineNumber) {
    this.stmtPosition = new Position(lineNumber, -1, lineNumber, -1);
    this.operandPositions = null;
  }

  /**
   * Create an instance from given statement position and operand positions.
   *
   * @param stmtPosition the position of the statement
   * @param operandPositions the operand positions
   */
  public StmtPositionInfo(Position stmtPosition, Position[] operandPositions) {
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
      return NoPositionInformation.getInstance();
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
      return NoPositionInformation.getInstance();
    }
  }

  @Override
  public String toString() {
    StringBuilder s = new StringBuilder();
    s.append("stmtPosition: ").append(getStmtPosition().toString()).append("\n");
    s.append("operandPositions: ");
    if (operandPositions != null) {
      s.append("\n");
      for (int i = 0; i < operandPositions.length; i++) {
        s.append(i).append(": ").append(operandPositions[i]).append(" ");
      }
    } else {
      s.append("No position info");
    }
    return s.toString();
  }

  @Nonnull
  public StmtPositionInfo withStmtPosition(Position stmtPosition) {
    return new StmtPositionInfo(stmtPosition, operandPositions);
  }

  @Nonnull
  public StmtPositionInfo withOperandPositions(Position[] operandPositions) {
    return new StmtPositionInfo(stmtPosition, operandPositions);
  }
}
