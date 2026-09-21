package sootup.core.jimple.common;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;
import sootup.core.TestUtil;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.stmt.JGotoStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.types.ClassType;

class TrapTest {

  /**
   * Reproduces failure when constructing or withering a Trap with empty range (beginStmt ==
   * endStmt).
   *
   * <p>Subject pattern: Any try-catch block where all protected statements are removed during
   * optimization or dead code elimination: try { // dead statement removed } catch (Exception e) {
   * ... }
   *
   * <p>Previously, Trap threw IllegalArgumentException("The covered Trap range is empty. Trap is of
   * no use.") whenever beginStmt == endStmt, crashing intermediate transformation passes or input
   * parsers.
   */
  @Test
  void testAllowsEmptyTrapRange() {
    ClassType exType = TestUtil.createDummyClassType();
    Stmt stmt = new JGotoStmt(StmtPositionInfo.getNoStmtPositionInfo());
    Stmt handler = new JGotoStmt(StmtPositionInfo.getNoStmtPositionInfo());

    Trap trap = assertDoesNotThrow(() -> new Trap(exType, stmt, stmt, handler));
    assertSame(stmt, trap.getBeginStmt());
    assertSame(stmt, trap.getEndStmt());
    assertSame(handler, trap.getHandlerStmt());
    assertEquals(exType, trap.getExceptionType());
  }

  @Test
  void testWithersAllowEmptyRange() {
    ClassType exType = TestUtil.createDummyClassType();
    Stmt stmt1 = new JGotoStmt(StmtPositionInfo.getNoStmtPositionInfo());
    Stmt stmt2 = new JGotoStmt(StmtPositionInfo.getNoStmtPositionInfo());
    Stmt handler = new JGotoStmt(StmtPositionInfo.getNoStmtPositionInfo());

    Trap trap = new Trap(exType, stmt1, stmt2, handler);
    Trap emptyViaBegin = assertDoesNotThrow(() -> trap.withBeginStmt(stmt2));
    assertSame(stmt2, emptyViaBegin.getBeginStmt());
    assertSame(stmt2, emptyViaBegin.getEndStmt());

    Trap emptyViaEnd = assertDoesNotThrow(() -> trap.withEndStmt(stmt1));
    assertSame(stmt1, emptyViaEnd.getBeginStmt());
    assertSame(stmt1, emptyViaEnd.getEndStmt());
  }
}
