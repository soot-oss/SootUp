package sootup.core.jimple.common;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;
import sootup.core.graph.MutableBlockControlFlowGraph;
import sootup.core.jimple.Jimple;
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
    Local x = Jimple.newLocal("x", PrimitiveType.getInt());
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
    Local x = Jimple.newLocal("x", PrimitiveType.getInt());
    Local y = Jimple.newLocal("y", PrimitiveType.getInt());
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
    Local i = Jimple.newLocal("i", PrimitiveType.getInt());
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

  @Test
  void testLocalSlotIndexWithers() {
    SlotLocal local = Jimple.newSlotLocal("a", PrimitiveType.getInt(), 3);
    assertEquals(3, local.getSlotIndex());
    assertEquals(3, local.getIndex());
    assertEquals(5, local.withSlotIndex(5).getSlotIndex());
    assertEquals(8, local.withIndex(8).getSlotIndex());
    assertEquals(3, local.withName("b").getSlotIndex());
    assertEquals("b", local.withName("b").getName());
    assertEquals(3, local.withType(PrimitiveType.getFloat()).getSlotIndex());
    assertEquals(PrimitiveType.getFloat(), local.withType(PrimitiveType.getFloat()).getType());
    assertEquals(3, local.getSlotIndex());
  }

  @Test
  void testStackWithersPreserveCategory() {
    Local stack = Jimple.newStackLocal("temporary", PrimitiveType.getInt());
    assertInstanceOf(StackLocal.class, stack.withName("renamed"));
    assertInstanceOf(StackLocal.class, stack.withType(PrimitiveType.getFloat()));
    assertFalse(stack instanceof SlotLocal);
    assertFalse(Jimple.newLocal("generic", PrimitiveType.getInt()) instanceof StackLocal);
  }

  @Test
  void testEqualityAcrossCategories() {
    Local generic = Jimple.newLocal("same", PrimitiveType.getInt());
    Local stack = Jimple.newStackLocal("same", PrimitiveType.getInt());
    Local slot = Jimple.newSlotLocal("same", PrimitiveType.getInt(), 2);
    for (Local left : List.of(generic, stack, slot)) {
      for (Local right : List.of(generic, stack, slot)) {
        assertEquals(left, right);
        assertEquals(left.hashCode(), right.hashCode());
        assertTrue(left.equivTo(right));
        assertEquals(left.equivHashCode(), right.equivHashCode());
      }
    }
    assertEquals(1, new java.util.HashSet<>(List.of(generic, stack, slot)).size());
    var map = new java.util.HashMap<Local, String>();
    map.put(slot, "value");
    assertEquals("value", map.get(stack));
    assertEquals(generic, slot.withType(PrimitiveType.getFloat()));
    assertFalse(generic.equivTo(slot.withType(PrimitiveType.getFloat())));
    assertNotEquals(generic, slot.withName("other"));
  }

  @Test
  void testVisitorDispatch() {
    for (Local local :
        List.of(
            Jimple.newLocal("generic", PrimitiveType.getInt()),
            Jimple.newStackLocal("stack", PrimitiveType.getInt()),
            Jimple.newSlotLocal("slot", PrimitiveType.getInt(), 0))) {
      var visited = new java.util.ArrayList<Local>();
      var visitor =
          new sootup.core.jimple.visitor.AbstractImmediateVisitor() {
            @Override
            public void caseLocal(Local value) {
              visited.add(value);
            }
          };
      assertSame(visitor, local.accept(visitor));
      assertEquals(List.of(local), visited);
    }
  }

  @Test
  @SuppressWarnings("deprecation")
  void testSlotValidationAndLegacyFactories() {
    assertInstanceOf(SlotLocal.class, Jimple.newLocal("a", PrimitiveType.getInt(), 0));
    Local generic = Jimple.newLocal("a", PrimitiveType.getInt(), -1);
    assertFalse(generic instanceof SlotLocal);
    assertFalse(generic instanceof StackLocal);
    assertThrows(
        IllegalArgumentException.class, () -> Jimple.newLocal("a", PrimitiveType.getInt(), -2));
    assertThrows(
        IllegalArgumentException.class, () -> Jimple.newSlotLocal("a", PrimitiveType.getInt(), -1));
    assertThrows(
        IllegalArgumentException.class,
        () -> Jimple.newSlotLocal("a", PrimitiveType.getInt(), 0).withSlotIndex(-1));
    assertThrows(
        RuntimeException.class,
        () -> Jimple.newStackLocal("void", sootup.core.types.VoidType.getInstance()));
  }
}
