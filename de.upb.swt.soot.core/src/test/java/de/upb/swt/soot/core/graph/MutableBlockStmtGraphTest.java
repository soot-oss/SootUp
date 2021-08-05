package de.upb.swt.soot.core.graph;

import static org.junit.Assert.*;

import de.upb.swt.soot.core.jimple.basic.StmtPositionInfo;
import de.upb.swt.soot.core.jimple.common.constant.IntConstant;
import de.upb.swt.soot.core.jimple.common.expr.JLeExpr;
import de.upb.swt.soot.core.jimple.common.stmt.BranchingStmt;
import de.upb.swt.soot.core.jimple.common.stmt.JIfStmt;
import de.upb.swt.soot.core.jimple.common.stmt.JNopStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import org.junit.Test;

public class MutableBlockStmtGraphTest {

  Stmt firstNop = new JNopStmt(StmtPositionInfo.createNoStmtPositionInfo());
  Stmt secondNop = new JNopStmt(StmtPositionInfo.createNoStmtPositionInfo());
  Stmt thirdNop = new JNopStmt(StmtPositionInfo.createNoStmtPositionInfo());

  BranchingStmt conditionalStmt =
      new JIfStmt(
          new JLeExpr(IntConstant.getInstance(2), IntConstant.getInstance(3)),
          StmtPositionInfo.createNoStmtPositionInfo());

  @Test
  public void addNodeTest() {

    MutableBlockStmtGraph graph = new MutableBlockStmtGraph();
    assertEquals(0, graph.getBlocks().size());
    graph.addNode(firstNop);
    assertEquals(1, graph.getBlocks().size());
  }

  @Test
  public void modifyStmtToBlockAtTail() {
    MutableBlockStmtGraph graph = new MutableBlockStmtGraph();
    assertEquals(0, graph.getBlocks().size());
    graph.addNode(firstNop);
    assertEquals(1, graph.getBlocks().size());
    assertEquals(1, graph.getBlocks().stream().findFirst().get().getStmts().size());

    graph.putEdge(firstNop, secondNop);
    assertEquals(1, graph.getBlocks().size());

    graph.putEdge(secondNop, thirdNop);
    assertEquals(1, graph.getBlocks().size());

    // insert branchingstmt at end
    graph.putEdge(firstNop, conditionalStmt);
    assertEquals(2, graph.getBlocks().size());

    // insert branchingstmt at head
    graph.putEdge(conditionalStmt, firstNop);
    graph.putEdge(conditionalStmt, secondNop);
    assertEquals(2, graph.getBlocks().size());

    // remove branchingstmt at end
    graph.removeEdge(firstNop, conditionalStmt);
    assertEquals(1, graph.getBlocks().size());

    // remove branchingstmt at head
    graph.removeEdge(conditionalStmt, secondNop);
    assertEquals(1, graph.getBlocks().size());
  }

  @Test
  public void modifyTrapToCompleteBlock() {
    fail("implement adding");
    fail("implement removal test");
  }

  @Test
  public void modifyTrapToBeginningOfABlock() {
    fail("implement adding");
    fail("implement removal test");
  }

  @Test
  public void modifyTrapToEndOfABlock() {
    fail("implement adding");
    fail("implement removal test");
  }
}
