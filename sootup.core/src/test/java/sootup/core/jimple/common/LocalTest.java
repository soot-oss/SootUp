package sootup.core.jimple.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import sootup.core.graph.MutableBlockControlFlowGraph;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.expr.JAddExpr;
import sootup.core.jimple.common.expr.JGtExpr;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.JGotoStmt;
import sootup.core.jimple.common.stmt.JIfStmt;
import sootup.core.jimple.common.stmt.JReturnVoidStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.types.PrimitiveType;

class LocalTest {

  /**
   * Reproduces self-definition bug in Local.getDefsForLocalUse:
   *
   * <p>Subject pattern: Any statement of the form `x = x + 1;` where variable `x` is initialized
   * earlier (e.g. `x = 5;`).
   *
   * <p>Under the old implementation, getDefsForLocalUse seeded its BFS queue with the statement
   * itself. Because `s2` defines `x`, the BFS immediately stopped and reported `s2` as its own
   * definition, completely hiding the prior definition `s1` (`x = 5`).
   *
   * <p>By seeding with predecessors of `stmt`, `s1` is correctly identified as the reaching
   * definition.
   */
  @Test
  void testGetDefsForLocalUseExcludesSelfDefinition() {
    Local x = new Local("x", PrimitiveType.getInt());
    JAssignStmt s1 =
        new JAssignStmt(x, IntConstant.getInstance(5), StmtPositionInfo.getNoStmtPositionInfo());
    JAssignStmt s2 =
        new JAssignStmt(
            x,
            new JAddExpr(x, IntConstant.getInstance(1)),
            StmtPositionInfo.getNoStmtPositionInfo());

    MutableBlockControlFlowGraph graph = new MutableBlockControlFlowGraph();
    graph.addNode(s1);
    graph.addNode(s2);
    graph.putEdge(s1, s2);

    List<Stmt> defs = x.getDefsForLocalUse(graph, s2);
    assertEquals(1, defs.size());
    assertEquals(s1, defs.get(0));
  }

  @Test
  void testGetDefsForLocalUseBranches() {
    Local x = new Local("x", PrimitiveType.getInt());
    Local y = new Local("y", PrimitiveType.getInt());
    JAssignStmt s1 =
        new JAssignStmt(x, IntConstant.getInstance(5), StmtPositionInfo.getNoStmtPositionInfo());
    JGotoStmt goto1 = new JGotoStmt(StmtPositionInfo.getNoStmtPositionInfo());
    JAssignStmt s2 =
        new JAssignStmt(x, IntConstant.getInstance(10), StmtPositionInfo.getNoStmtPositionInfo());
    JGotoStmt goto2 = new JGotoStmt(StmtPositionInfo.getNoStmtPositionInfo());
    JAssignStmt s3 = new JAssignStmt(y, x, StmtPositionInfo.getNoStmtPositionInfo());

    MutableBlockControlFlowGraph graph = new MutableBlockControlFlowGraph();
    graph.addNode(s1);
    graph.addNode(goto1);
    graph.putEdge(s1, goto1);
    graph.addNode(s2);
    graph.addNode(goto2);
    graph.putEdge(s2, goto2);
    graph.addNode(s3);
    graph.putEdge(goto1, JGotoStmt.BRANCH_IDX, s3);
    graph.putEdge(goto2, JGotoStmt.BRANCH_IDX, s3);

    List<Stmt> defs = x.getDefsForLocalUse(graph, s3);
    assertEquals(2, defs.size());
    assertTrue(defs.contains(s1));
    assertTrue(defs.contains(s2));
  }

  @Test
  void testGetDefsForLocalUseReachesSelfThroughLoop() {
    Local i = new Local("i", PrimitiveType.getInt());
    StmtPositionInfo pos = StmtPositionInfo.getNoStmtPositionInfo();
    JAssignStmt init = new JAssignStmt(i, IntConstant.getInstance(0), pos);
    JIfStmt cond = new JIfStmt(new JGtExpr(i, IntConstant.getInstance(5)), pos);
    JAssignStmt step = new JAssignStmt(i, new JAddExpr(i, IntConstant.getInstance(1)), pos);
    JGotoStmt back = new JGotoStmt(pos);
    JReturnVoidStmt ret = new JReturnVoidStmt(pos);

    MutableBlockControlFlowGraph graph = new MutableBlockControlFlowGraph();
    graph.putEdge(init, cond);
    graph.putEdge(cond, JIfStmt.FALSE_BRANCH_IDX, step);
    graph.putEdge(cond, JIfStmt.TRUE_BRANCH_IDX, ret);
    graph.putEdge(step, back);
    graph.putEdge(back, JGotoStmt.BRANCH_IDX, cond);
    graph.setStartingStmt(init);

    // At step (i = i + 1), reaching definitions of i must include both the initial assignment
    // (init: i = 0) and the previous loop iteration's assignment (step: i = i + 1).
    List<Stmt> defs = i.getDefsForLocalUse(graph, step);
    assertEquals(2, defs.size());
    assertTrue(defs.contains(init));
    assertTrue(defs.contains(step));
  }
}
