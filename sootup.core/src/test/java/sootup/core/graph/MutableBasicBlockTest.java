package sootup.core.graph;

import static org.junit.jupiter.api.Assertions.*;
import static sootup.core.graph.CfgTestKit.*;

import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import sootup.core.jimple.common.stmt.JIfStmt;
import sootup.core.jimple.common.stmt.Stmt;

/** edge primitives of {@link MutableBasicBlockImpl}: both ends stay in sync */
public class MutableBasicBlockTest {

  private static MutableBasicBlockImpl block(Stmt... stmts) {
    MutableBasicBlockImpl b = new MutableBasicBlockImpl();
    for (Stmt s : stmts) b.addStmt(s);
    return b;
  }

  @Test
  public void addStmtAfterBranchingIsRejected() {
    MutableBasicBlockImpl b = block(nop(), gotoStmt());
    assertThrows(IllegalArgumentException.class, () -> b.addStmt(nop()));
    MutableBasicBlockImpl r = block(nop(), ret());
    assertThrows(IllegalArgumentException.class, () -> r.addStmt(nop()));
  }

  @Test
  public void addStmtWithLinkedSuccessorIsRejected() {
    MutableBasicBlockImpl a = block(nop());
    a.link(0, block(ret()));
    assertThrows(IllegalStateException.class, () -> a.addStmt(nop()));
  }

  @Test
  public void successorSlotsFollowTail() {
    MutableBasicBlockImpl b = block(nop());
    assertEquals(1, b.successorSlots());
    b.addStmt(ifStmt());
    assertEquals(2, b.successorSlots());
    MutableBasicBlockImpl t = block(ret());
    assertEquals(0, t.successorSlots());
  }

  @Test
  public void linkIsSymmetricAndCountsEdges() {
    JIfStmt i = ifStmt();
    MutableBasicBlockImpl a = block(i);
    MutableBasicBlockImpl t = block(ret());
    a.link(0, t);
    a.link(1, t);
    assertEquals(Arrays.asList(t, t), a.getSuccessors());
    assertEquals(Arrays.asList(a, a), t.getPredecessors());
    a.unlink(0);
    assertEquals(Collections.singletonList(a), t.getPredecessors());
    assertEquals(Collections.singletonList(t), a.getSuccessors());
  }

  @Test
  public void relinkReplacesOldTarget() {
    MutableBasicBlockImpl a = block(gotoStmt());
    MutableBasicBlockImpl t1 = block(ret()), t2 = block(ret());
    a.link(0, t1);
    a.link(0, t2);
    assertTrue(t1.getPredecessors().isEmpty());
    assertEquals(Collections.singletonList(a), t2.getPredecessors());
  }

  @Test
  public void replacingTailShrinksSlotsAndUnlinksDropped() {
    MutableBasicBlockImpl a = block(ifStmt());
    MutableBasicBlockImpl t0 = block(ret()), t1 = block(ret());
    a.link(0, t0);
    a.link(1, t1);
    a.setStmt(0, nop());
    assertEquals(Collections.singletonList(t0), a.getSuccessors());
    assertEquals(Collections.singletonList(a), t0.getPredecessors());
    assertTrue(t1.getPredecessors().isEmpty());
  }

  @Test
  public void exceptionalEdgesAreKeptApartFromNormalEdges() {
    MutableBasicBlockImpl a = block(nop());
    MutableBasicBlockImpl h = block(handler(EX_A), ret());
    a.link(0, h);
    a.linkExc(EX_A, h);
    a.linkExc(EX_B, h);
    assertEquals(3, h.getPredecessors().size());
    a.unlink(0);
    assertEquals(2, h.getPredecessors().size());
    assertEquals(2, h.getExceptionalPredecessors().size());
    a.unlinkExc(EX_A);
    assertEquals(Collections.singletonList(a), h.getPredecessors());
  }

  @Test
  public void relinkExceptionalReplacesOldHandler() {
    MutableBasicBlockImpl a = block(nop());
    MutableBasicBlockImpl h1 = block(handler(EX_A)), h2 = block(handler(EX_A));
    a.linkExc(EX_A, h1);
    a.linkExc(EX_A, h1); // idempotent
    assertEquals(1, h1.getPredecessors().size());
    a.linkExc(EX_A, h2);
    assertTrue(h1.getPredecessors().isEmpty());
    assertEquals(1, h2.getPredecessors().size());
  }

  @Test
  public void removeStmtsFromNeedsUnlinkedSuccessors() {
    MutableBasicBlockImpl a = block(nop(), nop(), nop());
    a.link(0, block(ret()));
    assertThrows(IllegalStateException.class, () -> a.removeStmtsFrom(1));
    a.unlinkAllSuccessors();
    assertEquals(2, a.removeStmtsFrom(1).size());
    assertEquals(1, a.size());
  }
}
