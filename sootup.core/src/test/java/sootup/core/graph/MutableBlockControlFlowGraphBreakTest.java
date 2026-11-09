package sootup.core.graph;

import static org.junit.jupiter.api.Assertions.*;
import static sootup.core.graph.CfgTestKit.*;

import java.util.*;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import sootup.core.jimple.common.Trap;
import sootup.core.jimple.common.stmt.*;
import sootup.core.jimple.javabytecode.stmt.JSwitchStmt;

/**
 * Targeted attempts to break {@link MutableBlockControlFlowGraph}. Each test pins one edge case of
 * a mutation (split/merge at block head, middle, tail; traps; self loops; duplicate edges) and
 * checks the stmt-level result plus the structural invariants of {@link
 * CfgTestKit#assertConsistent}.
 */
public class MutableBlockControlFlowGraphBreakTest {

  private static MutableBlockControlFlowGraph init(
      List<List<Stmt>> blocks, Map<BranchingStmt, List<Stmt>> succs, Trap... traps) {
    MutableBlockControlFlowGraph g = new MutableBlockControlFlowGraph();
    g.initializeWith(blocks, succs, new ArrayList<>(Arrays.asList(traps)));
    assertConsistent(g);
    return g;
  }

  private static List<List<Stmt>> blocks(List<Stmt>... b) {
    return Arrays.asList(b);
  }

  private static Map<BranchingStmt, List<Stmt>> succ(Object... kv) {
    return map(kv);
  }

  // ===========================================================================================
  @Nested
  class ExceptionalEdges {

    @Test
    void addExceptionalEdgeOnHeadOfMultiStmtBlock() {
      JNopStmt a = nop(), b = nop();
      JReturnVoidStmt r = ret();
      JIdentityStmt h = handler(EX_A);
      MutableBlockControlFlowGraph g = chain(a, b, r);

      g.addExceptionalEdge(a, EX_A, h);

      assertEquals(map(EX_A, h), g.exceptionalSuccessors(a));
      assertTrue(g.exceptionalSuccessors(b).isEmpty());
      assertEquals(list(b), g.successors(a));
      assertEquals(list(r), g.successors(b));
      assertConsistent(g);
    }

    @Test
    void addExceptionalEdgeOnMiddleStmt() {
      JNopStmt a = nop(), b = nop(), c = nop();
      JReturnVoidStmt r = ret();
      JIdentityStmt h = handler(EX_A);
      MutableBlockControlFlowGraph g = chain(a, b, c, r);

      g.addExceptionalEdge(b, EX_A, h);

      assertEquals(list(b), g.successors(a), "edge into the split-off stmt lost");
      assertEquals(list(c), g.successors(b));
      assertEquals(list(r), g.successors(c));
      assertTrue(g.exceptionalSuccessors(a).isEmpty());
      assertEquals(map(EX_A, h), g.exceptionalSuccessors(b));
      assertTrue(g.exceptionalSuccessors(c).isEmpty());
      assertConsistent(g);
    }

    @Test
    void addExceptionalEdgeOnTailStmt() {
      JNopStmt a = nop(), b = nop();
      JReturnVoidStmt r = ret();
      JIdentityStmt h = handler(EX_A);
      MutableBlockControlFlowGraph g = chain(a, b, r);

      g.addExceptionalEdge(r, EX_A, h);

      assertEquals(list(r), g.successors(b));
      assertEquals(map(EX_A, h), g.exceptionalSuccessors(r));
      assertTrue(g.exceptionalSuccessors(b).isEmpty());
      assertConsistent(g);
    }

    @Test
    void addExceptionalEdgeOnMiddleStmtOfTrappedBlock() {
      JNopStmt a = nop(), b = nop(), c = nop();
      JReturnVoidStmt r = ret(), r2 = ret();
      JIdentityStmt h = handler(EX_A), h2 = handler(EX_B);
      MutableBlockControlFlowGraph g =
          init(blocks(list(a, b, c, r), list(h, r2)), succ(), new Trap(EX_A, a, h, h));

      g.addExceptionalEdge(b, EX_B, h2);

      assertEquals(map(EX_A, h), g.exceptionalSuccessors(a));
      assertEquals(map(EX_A, h, EX_B, h2), g.exceptionalSuccessors(b));
      assertEquals(map(EX_A, h), g.exceptionalSuccessors(c));
      assertConsistent(g); // handler h must have exactly one pred entry per covering block
    }

    @Test
    void addExceptionalEdgeTwiceIsIdempotent() {
      JNopStmt a = nop();
      JReturnVoidStmt r = ret();
      JIdentityStmt h = handler(EX_A);
      MutableBlockControlFlowGraph g = chain(a, r);
      g.addExceptionalEdge(r, EX_A, h);
      g.addExceptionalEdge(r, EX_A, h);
      assertEquals(1, g.inDegree(h));
      assertConsistent(g);
    }

    @Test
    void overwriteHandlerOfSameTypeUnlinksOldHandler() {
      JNopStmt a = nop();
      JIdentityStmt h1 = handler(EX_A), h2 = handler(EX_A);
      MutableBlockControlFlowGraph g = new MutableBlockControlFlowGraph();
      g.setStartingStmt(a);

      g.addExceptionalEdge(a, EX_A, h1);
      g.addExceptionalEdge(a, EX_A, h2);

      assertEquals(map(EX_A, h2), g.exceptionalSuccessors(a));
      assertTrue(g.predecessors(h1).isEmpty(), "old handler keeps a stale predecessor");
      assertTrue(g.exceptionalPredecessors(h1).isEmpty());
      assertConsistent(g);
    }

    @Test
    void twoExceptionTypesToSameHandler() {
      JNopStmt a = nop();
      JReturnVoidStmt r = ret();
      JIdentityStmt h = handler(EX_A);
      MutableBlockControlFlowGraph g = chain(a, r);
      g.addExceptionalEdge(a, EX_A, h);
      g.addExceptionalEdge(a, EX_B, h);
      assertConsistent(g);
      g.removeExceptionalEdge(a, EX_A);
      assertEquals(map(EX_B, h), g.exceptionalSuccessors(a));
      assertConsistent(g);
    }

    @Test
    void clearExceptionalEdgesOnlyAffectsGivenStmt() {
      JNopStmt a = nop(), b = nop();
      JReturnVoidStmt r = ret(), r2 = ret();
      JIdentityStmt h = handler(EX_A);
      MutableBlockControlFlowGraph g =
          init(blocks(list(a, b, r), list(h, r2)), succ(), new Trap(EX_A, a, h, h));

      g.clearExceptionalEdges(b);

      assertTrue(g.exceptionalSuccessors(b).isEmpty());
      assertEquals(map(EX_A, h), g.exceptionalSuccessors(a), "neighbour lost its trap");
      assertEquals(map(EX_A, h), g.exceptionalSuccessors(r), "neighbour lost its trap");
      assertConsistent(g);
    }

    @Test
    void removeExceptionalEdgeOnlyAffectsGivenStmt() {
      JNopStmt a = nop(), b = nop();
      JReturnVoidStmt r = ret(), r2 = ret();
      JIdentityStmt h = handler(EX_A);
      MutableBlockControlFlowGraph g =
          init(blocks(list(a, b, r), list(h, r2)), succ(), new Trap(EX_A, a, h, h));

      g.removeExceptionalEdge(b, EX_A);

      assertTrue(g.exceptionalSuccessors(b).isEmpty());
      assertEquals(map(EX_A, h), g.exceptionalSuccessors(a), "neighbour lost its trap");
      assertConsistent(g);
    }

    @Test
    void removeExceptionalFlowFromAllBlocksWhileBlocksMerge() {
      JNopStmt a = nop(), b = nop(), c = nop(), d = nop();
      JReturnVoidStmt r = ret(), r2 = ret();
      JIdentityStmt h = handler(EX_A);
      MutableBlockControlFlowGraph g =
          init(
              blocks(list(a), list(b), list(c), list(d, r), list(h, r2)),
              succ(),
              new Trap(EX_A, a, b, h),
              new Trap(EX_A, c, d, h));

      g.removeExceptionalFlowFromAllBlocks(EX_A, h);

      for (Stmt s : list(a, b, c, d, r)) {
        assertTrue(g.exceptionalSuccessors(s).isEmpty(), s + " still has a trap");
      }
      assertTrue(g.predecessors(h).isEmpty());
      assertConsistent(g);
    }

    @Test
    void removeNormalEdgeKeepsExceptionalEdgeToSameHandler() {
      JNopStmt p = nop();
      JIdentityStmt h = handler(EX_A);
      JReturnVoidStmt r = ret();
      MutableBlockControlFlowGraph g = new MutableBlockControlFlowGraph();
      g.setStartingStmt(p);
      g.addBlock(list(h, r));
      g.addExceptionalEdge(p, EX_A, h);
      g.putEdge(p, h); // p falls through into the handler as well
      assertConsistent(g);

      g.removeEdge(p, h);

      assertTrue(g.successors(p).isEmpty());
      assertEquals(map(EX_A, h), g.exceptionalSuccessors(p));
      assertEquals(list(p), g.exceptionalPredecessors(h));
      assertConsistent(g);
    }

    @Test
    void removeHandlerStmtUnlinksTraps() {
      JNopStmt a = nop();
      JReturnVoidStmt r = ret(), r2 = ret();
      JIdentityStmt h = handler(EX_A);
      MutableBlockControlFlowGraph g =
          init(blocks(list(a, r), list(h, r2)), succ(), new Trap(EX_A, a, h, h));
      g.removeNode(h, false);
      assertTrue(g.exceptionalSuccessors(a).isEmpty());
      assertConsistent(g);
    }

    @Test
    void addNodeWithDifferentTrapsSplitsBlock() {
      JNopStmt a = nop(), b = nop(), c = nop();
      JReturnVoidStmt r = ret();
      JIdentityStmt h = handler(EX_A);
      MutableBlockControlFlowGraph g = chain(a, b, c, r);
      g.addNode(b, map(EX_A, h));
      assertEquals(list(b), g.successors(a));
      assertEquals(list(c), g.successors(b));
      assertEquals(map(EX_A, h), g.exceptionalSuccessors(b));
      assertTrue(g.exceptionalSuccessors(c).isEmpty());
      assertConsistent(g);
    }
  }

  // ===========================================================================================
  @Nested
  class RemoveNode {

    @Test
    void removeMiddleWithoutKeepingFlowMovesSuccessorsToSecondHalf() {
      JIfStmt i1 = ifStmt();
      JNopStmt a = nop(), b = nop(), c = nop(), d = nop(), x = nop();
      JReturnVoidStmt r = ret();
      // i1 falls to a, branches to x; [a b c d] -> x
      MutableBlockControlFlowGraph g =
          init(blocks(list(i1), list(a, b, c, d), list(x, r)), succ(i1, list(x)));

      g.removeNode(c, false);

      assertEquals(list(b), g.successors(a));
      assertTrue(g.successors(b).isEmpty(), "first half must not keep the old tail's successors");
      assertEquals(list(x), g.successors(d), "second half lost the old tail's successors");
      assertConsistent(g);
    }

    @Test
    void removeMiddleWithoutKeepingFlowKeepsTrapsOfSecondHalf() {
      JNopStmt a = nop(), b = nop(), c = nop();
      JReturnVoidStmt r = ret(), r2 = ret();
      JIdentityStmt h = handler(EX_A);
      MutableBlockControlFlowGraph g =
          init(blocks(list(a, b, c, r), list(h, r2)), succ(), new Trap(EX_A, a, h, h));

      g.removeNode(b, false);

      assertEquals(map(EX_A, h), g.exceptionalSuccessors(c));
      assertEquals(map(EX_A, h), g.exceptionalSuccessors(r));
      assertConsistent(g);
    }

    @Test
    void removeMiddleKeepingFlow() {
      JNopStmt a = nop(), b = nop(), c = nop();
      JReturnVoidStmt r = ret();
      MutableBlockControlFlowGraph g = chain(a, b, c, r);
      g.removeNode(b, true);
      assertEquals(list(c), g.successors(a));
      assertConsistent(g);
    }

    @Test
    void removeGotoTailKeepingFlow() {
      JNopStmt a = nop(), y = nop();
      JGotoStmt gt = gotoStmt();
      JNopStmt t = nop();
      JReturnVoidStmt r1 = ret(), r2 = ret();
      MutableBlockControlFlowGraph g =
          init(blocks(list(a, gt), list(y, r1), list(t, r2)), succ(gt, list(t)));

      g.removeNode(gt, true);

      assertEquals(list(t), g.successors(a));
      assertConsistent(g);
    }

    @Test
    void removeSingleStmtBlockWithManyPredsKeepingFlow() {
      JIfStmt i1 = ifStmt();
      JNopStmt n = nop();
      JNopStmt m = nop();
      JReturnVoidStmt r = ret();
      // i1 -> [n, n]; n -> m; m -> r : n is its own block (two edges from i1)
      MutableBlockControlFlowGraph g =
          init(blocks(list(i1), list(n), list(m, r)), succ(i1, list(n)));
      assertEquals(2, g.inDegree(n));

      g.removeNode(n, true);

      assertEquals(list(m, m), g.successors(i1));
      assertEquals(2, g.inDegree(m));
      assertConsistent(g);
    }

    @Test
    void removeStartingStmtKeepingFlow() {
      JNopStmt a = nop(), b = nop();
      JReturnVoidStmt r = ret();
      MutableBlockControlFlowGraph g = chain(a, b, r);
      g.removeNode(a, true);
      assertSame(b, g.getStartingStmt());
      assertConsistent(g);
    }

    @Test
    void removeStartingStmtWithoutKeepingFlow() {
      JNopStmt a = nop(), b = nop();
      JReturnVoidStmt r = ret();
      MutableBlockControlFlowGraph g = chain(a, b, r);
      g.removeNode(a, false);
      assertNull(g.getStartingStmt());
      assertConsistent(g);
    }

    @Test
    void removeEveryStmtLeavesEmptyGraph() {
      JNopStmt a = nop(), b = nop();
      JReturnVoidStmt r = ret();
      MutableBlockControlFlowGraph g = chain(a, b, r);
      g.removeNode(r, false);
      g.removeNode(b, false);
      g.removeNode(a, false);
      assertTrue(g.getNodes().isEmpty());
      assertTrue(g.getBlocks().isEmpty());
      assertTrue(g.getStmts().isEmpty());
      assertNull(g.getStartingStmt());
    }

    @Test
    void removeUnknownStmtThrows() {
      MutableBlockControlFlowGraph g = chain(nop(), ret());
      assertThrows(IllegalArgumentException.class, () -> g.removeNode(nop(), false));
    }

    @Test
    void removeBlockOfOtherGraphThrowsIllegalArgument() {
      MutableBlockControlFlowGraph g = chain(nop(), ret());
      MutableBlockControlFlowGraph other = chain(nop(), ret());
      BasicBlock<?> foreign = other.getStartingStmtBlock();
      assertThrows(IllegalArgumentException.class, () -> g.removeBlock(foreign));
    }
  }

  // ===========================================================================================
  @Nested
  class ReplaceNode {

    @Test
    void replaceMiddleNopByGoto() {
      JNopStmt a = nop(), b = nop(), c = nop();
      JReturnVoidStmt r = ret();
      JGotoStmt gt = gotoStmt();
      MutableBlockControlFlowGraph g = chain(a, b, c, r);

      g.replaceNode(b, gt);

      assertFalse(g.containsNode(b));
      assertEquals(list(gt), g.successors(a));
      assertEquals(list(c), g.successors(gt));
      assertConsistent(g);
    }

    @Test
    void replaceHeadNopByGoto() {
      JNopStmt a = nop(), b = nop();
      JReturnVoidStmt r = ret();
      JGotoStmt gt = gotoStmt();
      MutableBlockControlFlowGraph g = chain(a, b, r);

      g.replaceNode(a, gt);

      assertSame(gt, g.getStartingStmt());
      assertEquals(list(b), g.successors(gt));
      assertConsistent(g);
    }

    @Test
    void replaceTailNopByGoto() {
      JIfStmt i1 = ifStmt();
      JNopStmt a = nop(), b = nop(), x = nop();
      JReturnVoidStmt r = ret();
      JGotoStmt gt = gotoStmt();
      // i1 falls to a, branches to x; a -> b -> x
      MutableBlockControlFlowGraph g =
          init(blocks(list(i1), list(a, b), list(x, r)), succ(i1, list(x)));

      g.replaceNode(b, gt);

      assertEquals(list(gt), g.successors(a));
      assertEquals(list(x), g.successors(gt));
      assertConsistent(g);
    }

    @Test
    void replaceNopByIfKeepsFallThroughThenAcceptsBranch() {
      JGotoStmt g0 = gotoStmt();
      JNopStmt n = nop(), x = nop();
      JReturnVoidStmt r = ret();
      JIfStmt i1 = ifStmt();
      MutableBlockControlFlowGraph g =
          init(blocks(list(g0), list(n), list(x, r)), succ(g0, list(x)));

      g.replaceNode(n, i1);
      assertEquals(list(x), g.successors(i1));
      assertConsistent(g);

      g.putEdge(i1, 1, x);
      assertEquals(list(x, x), g.successors(i1));
      assertEquals(3, g.inDegree(x));
      assertConsistent(g);
    }

    @Test
    void replaceIfByNopKeepsFallThroughEdge() {
      JNopStmt s = nop(), a = nop(), t = nop();
      JIfStmt i1 = ifStmt();
      JReturnVoidStmt r1 = ret(), r2 = ret();
      JNopStmt n = nop();
      MutableBlockControlFlowGraph g =
          init(blocks(list(s, i1), list(a, r1), list(t, r2)), succ(i1, list(t)));

      g.replaceNode(i1, n);

      assertEquals(list(a), g.successors(n));
      assertEquals(list(n), g.predecessors(a), "fall-through target lost its predecessor");
      assertTrue(g.predecessors(t).isEmpty());
      assertConsistent(g);
    }

    @Test
    void replaceGotoByNopMergesWithTarget() {
      JNopStmt a = nop();
      JGotoStmt gt = gotoStmt();
      JNopStmt t = nop();
      JReturnVoidStmt r = ret();
      MutableBlockControlFlowGraph g = init(blocks(list(a, gt), list(t, r)), succ(gt, list(t)));
      JNopStmt n = nop();

      g.replaceNode(gt, n);

      assertEquals(list(n), g.successors(a));
      assertEquals(list(t), g.successors(n));
      assertConsistent(g);
    }

    @Test
    void replaceSwitchBySwitchWithSameArity() {
      JSwitchStmt sw = switchStmt(1);
      JSwitchStmt sw2 = switchStmt(1);
      JNopStmt a = nop(), b = nop();
      JReturnVoidStmt r1 = ret(), r2 = ret();
      MutableBlockControlFlowGraph g =
          init(blocks(list(sw), list(a, r1), list(b, r2)), succ(sw, list(a, b)));
      g.replaceNode(sw, sw2);
      assertEquals(list(a, b), g.successors(sw2));
      assertConsistent(g);
    }

    @Test
    void replaceBackwardGotoByNopMustNotHangIteration() {
      // a: nop ; b: goto a  ==> b := nop  gives the fall-through cycle a -> b -> a
      JNopStmt a = nop();
      JGotoStmt b = gotoStmt();
      MutableBlockControlFlowGraph g = new MutableBlockControlFlowGraph();
      g.setStartingStmt(a);
      g.putEdge(a, b);
      g.putEdge(b, 0, a);
      JNopStmt n = nop();

      assertTimeoutPreemptively(
          java.time.Duration.ofSeconds(5),
          () -> {
            try {
              g.replaceNode(b, n);
              g.getStmts(); // must terminate: return or reject the unlinearizable cycle
            } catch (IllegalArgumentException | IllegalStateException expected) {
              // rejecting is fine
            }
          });
    }

    @Test
    void replaceByStmtAlreadyInGraphIsRejected() {
      JNopStmt a = nop(), b = nop();
      JReturnVoidStmt r = ret();
      MutableBlockControlFlowGraph g = chain(a, b, r);
      assertThrows(IllegalArgumentException.class, () -> g.replaceNode(a, b));
      assertConsistent(g);
    }
  }

  // ===========================================================================================
  @Nested
  class Edges {

    @Test
    void replaceSuccessorEdgeChangesTheGraph() {
      JIfStmt i1 = ifStmt();
      JNopStmt a = nop(), t = nop(), u = nop();
      JReturnVoidStmt r1 = ret(), r2 = ret(), r3 = ret();
      MutableBlockControlFlowGraph g =
          init(blocks(list(i1), list(a, r1), list(t, r2), list(u, r3)), succ(i1, list(t)));

      assertTrue(g.replaceSuccessorEdge(i1, t, u));

      assertEquals(list(a, u), g.successors(i1));
      assertTrue(g.predecessors(t).isEmpty());
      assertEquals(list(i1), g.predecessors(u));
      assertConsistent(g);
    }

    @Test
    void branchIntoMiddleOfTrappedBlock() {
      JNopStmt s = nop(), x = nop(), y = nop(), z = nop();
      JGotoStmt gt = gotoStmt();
      JReturnVoidStmt r = ret(), r2 = ret();
      JIdentityStmt h = handler(EX_A);
      MutableBlockControlFlowGraph g =
          init(
              blocks(list(s, gt), list(x, y, z, r), list(h, r2)),
              succ(gt, list(x)),
              new Trap(EX_A, x, h, h));

      g.setEdges(gt, y);

      assertEquals(list(y), g.successors(gt));
      assertEquals(list(y), g.successors(x));
      assertEquals(map(EX_A, h), g.exceptionalSuccessors(x));
      assertEquals(map(EX_A, h), g.exceptionalSuccessors(y));
      assertConsistent(g); // h: one pred entry per covering block
    }

    @Test
    void branchIntoMiddleOfOwnBlock() {
      JNopStmt a = nop(), b = nop();
      JGotoStmt gt = gotoStmt();
      MutableBlockControlFlowGraph g = new MutableBlockControlFlowGraph();
      g.setStartingStmt(a);
      g.putEdge(a, b);
      g.putEdge(b, gt);
      g.putEdge(gt, 0, b); // loop back into own block

      assertEquals(list(b), g.successors(a));
      assertEquals(list(gt), g.successors(b));
      assertEquals(list(b), g.successors(gt));
      assertEquals(2, g.inDegree(b));
      assertConsistent(g);
    }

    @Test
    void fallThroughIntoHeadOfBlockKeepsThatBlocksSuccessors() {
      JNopStmt a = nop(), b = nop(), c = nop();
      JReturnVoidStmt r = ret();
      JIdentityStmt h = handler(EX_A);
      MutableBlockControlFlowGraph g = new MutableBlockControlFlowGraph();
      g.addBlock(list(c, r), map(EX_A, h));
      g.addBlock(list(b));
      g.putEdge(b, c); // different traps -> separate blocks b | c,r
      g.setStartingStmt(a);

      g.putEdge(a, b); // b has no preds and same traps as a -> merged into a's block

      assertEquals(list(b), g.successors(a));
      assertEquals(list(c), g.successors(b), "merged block lost the successor of b");
      assertEquals(list(b), g.predecessors(c));
      assertConsistent(g);
    }

    @Test
    void switchWithDuplicateTargets() {
      JSwitchStmt sw = switchStmt(2);
      JNopStmt a = nop(), t = nop();
      JReturnVoidStmt r1 = ret(), r2 = ret();
      MutableBlockControlFlowGraph g =
          init(blocks(list(sw), list(a, r1), list(t, r2)), succ(sw, list(t, a, t)));
      assertEquals(2, g.inDegree(t));
      assertEquals(list(t, a, t), g.successors(sw));

      List<Integer> removed = g.removeEdge(sw, t);

      assertEquals(list2(0, 2), removed);
      assertEquals(list(a), g.successors(sw));
      assertEquals(0, g.inDegree(t));
      assertConsistent(g);
    }

    @Test
    void setEdgesReplacesAllEdges() {
      JIfStmt i1 = ifStmt();
      JNopStmt a = nop(), t = nop(), u = nop();
      JReturnVoidStmt r1 = ret(), r2 = ret(), r3 = ret();
      MutableBlockControlFlowGraph g =
          init(blocks(list(i1), list(a, r1), list(t, r2), list(u, r3)), succ(i1, list(t)));
      g.setEdges(i1, u, u);
      assertEquals(list(u, u), g.successors(i1));
      assertEquals(0, g.inDegree(a));
      assertEquals(0, g.inDegree(t));
      assertConsistent(g);
    }

    @Test
    void removeEdgeInsideBlockSplitsIt() {
      JNopStmt a = nop(), b = nop();
      JReturnVoidStmt r = ret();
      MutableBlockControlFlowGraph g = chain(a, b, r);
      assertEquals(list2(0), g.removeEdge(a, b));
      assertTrue(g.successors(a).isEmpty());
      assertEquals(list(r), g.successors(b));
      assertConsistent(g);
    }

    @Test
    void removeEdgeOfNonAdjacentStmtsIsNoop() {
      JNopStmt a = nop(), b = nop();
      JReturnVoidStmt r = ret();
      MutableBlockControlFlowGraph g = chain(a, b, r);
      Snapshot before = Snapshot.of(g);
      assertTrue(g.removeEdge(a, r).isEmpty());
      before.assertSameAs(Snapshot.of(g), "after no-op removeEdge");
      assertConsistent(g);
    }

    @Test
    void putEdgeFromNonTailIsRejected() {
      JNopStmt a = nop(), b = nop(), x = nop();
      JReturnVoidStmt r = ret();
      MutableBlockControlFlowGraph g = chain(a, b, r);
      assertThrows(IllegalArgumentException.class, () -> g.putEdge(a, x));
      assertFalse(g.containsNode(x));
      assertConsistent(g);
    }

    @Test
    void putEdgeOutOfBoundsIsRejected() {
      JIfStmt i1 = ifStmt();
      MutableBlockControlFlowGraph g = new MutableBlockControlFlowGraph();
      g.setStartingStmt(i1);
      assertThrows(IllegalArgumentException.class, () -> g.putEdge(i1, 2, ret()));
      assertThrows(IllegalArgumentException.class, () -> g.putEdge(i1, -1, ret()));
    }
  }

  // ===========================================================================================
  @Nested
  class Insert {

    @Test
    void insertAfterTailIntoBlockWithDifferentTraps() {
      JNopStmt a = nop(), c = nop(), x = nop();
      JReturnVoidStmt r = ret();
      JIdentityStmt h = handler(EX_A), h2 = handler(EX_B);
      MutableBlockControlFlowGraph g = new MutableBlockControlFlowGraph();
      g.addBlock(list(c, r), map(EX_A, h));
      g.setStartingStmt(a);
      g.putEdge(a, c);

      g.insertAfter(a, Collections.singletonList(x), map(EX_B, h2));

      assertEquals(list(x), g.successors(a));
      assertEquals(list(c), g.successors(x));
      assertEquals(list(x), g.predecessors(c), "duplicate predecessor entry");
      assertConsistent(g);
    }

    @Test
    void insertAfterStmtWithoutSuccessor() {
      JNopStmt a = nop(), x = nop();
      MutableBlockControlFlowGraph g = new MutableBlockControlFlowGraph();
      g.setStartingStmt(a);
      g.insertAfter(a, x);
      assertEquals(list(x), g.successors(a));
      assertConsistent(g);
    }

    @Test
    void insertAfterInsideTrappedBlockWithSameTraps() {
      JNopStmt a = nop(), b = nop(), x = nop();
      JReturnVoidStmt r = ret(), r2 = ret();
      JIdentityStmt h = handler(EX_A);
      MutableBlockControlFlowGraph g =
          init(blocks(list(a, b, r), list(h, r2)), succ(), new Trap(EX_A, a, h, h));

      g.insertAfter(a, Collections.singletonList(x), map(EX_A, h));

      assertEquals(list(x), g.successors(a));
      assertEquals(list(b), g.successors(x));
      assertEquals(1, g.inDegree(h), "handler collected duplicate predecessor entries");
      assertConsistent(g);
    }

    @Test
    void insertAfterInsideBlockWithDifferentTraps() {
      JNopStmt a = nop(), b = nop(), x = nop();
      JReturnVoidStmt r = ret();
      JIdentityStmt h = handler(EX_A);
      MutableBlockControlFlowGraph g = chain(a, b, r);

      g.insertAfter(a, Collections.singletonList(x), map(EX_A, h));

      assertEquals(list(x), g.successors(a));
      assertEquals(list(b), g.successors(x));
      assertEquals(map(EX_A, h), g.exceptionalSuccessors(x));
      assertTrue(g.exceptionalSuccessors(b).isEmpty());
      assertConsistent(g);
    }

    @Test
    void insertBeforeMiddleWithSameTraps() {
      JNopStmt a = nop(), b = nop(), x = nop();
      JReturnVoidStmt r = ret(), r2 = ret();
      JIdentityStmt h = handler(EX_A);
      MutableBlockControlFlowGraph g =
          init(blocks(list(a, b, r), list(h, r2)), succ(), new Trap(EX_A, a, h, h));

      g.insertBefore(b, Collections.singletonList(x), map(EX_A, h));

      assertEquals(list(x), g.successors(a));
      assertEquals(list(b), g.successors(x));
      assertEquals(1, g.inDegree(h));
      assertConsistent(g);
    }

    @Test
    void insertBeforeStartingStmt() {
      JNopStmt a = nop(), x = nop();
      JReturnVoidStmt r = ret();
      MutableBlockControlFlowGraph g = chain(a, r);
      g.insertBefore(a, x);
      assertSame(x, g.getStartingStmt());
      assertSame(x, g.getStmts().get(0));
      assertEquals(list(a), g.successors(x));
      assertConsistent(g);
    }

    @Test
    void insertBeforeJoinNodeRedirectsAllPreds() {
      JIfStmt i1 = ifStmt();
      JNopStmt t = nop(), x = nop();
      JReturnVoidStmt r = ret();
      MutableBlockControlFlowGraph g = init(blocks(list(i1), list(t, r)), succ(i1, list(t)));
      assertEquals(list(t, t), g.successors(i1));

      g.insertBefore(t, x);

      assertEquals(list(x, x), g.successors(i1));
      assertEquals(2, g.inDegree(x));
      assertEquals(1, g.inDegree(t));
      assertConsistent(g);
    }

    @Test
    void insertBeforeSelfLoop() {
      JGotoStmt gt = gotoStmt();
      JNopStmt x = nop();
      MutableBlockControlFlowGraph g = new MutableBlockControlFlowGraph();
      g.setStartingStmt(gt);
      g.putEdge(gt, 0, gt);

      g.insertBefore(gt, x);

      assertSame(x, g.getStartingStmt());
      assertEquals(list(gt), g.successors(x));
      assertEquals(list(x), g.successors(gt));
      assertConsistent(g);
    }

    @Test
    void insertBeforeHandlerRedirectsExceptionalEdges() {
      JNopStmt a = nop();
      JReturnVoidStmt r = ret(), r2 = ret();
      JIdentityStmt h = handler(EX_A);
      JNopStmt x = nop();
      MutableBlockControlFlowGraph g =
          init(blocks(list(a, r), list(h, r2)), succ(), new Trap(EX_A, a, h, h));
      g.insertBefore(h, x);
      assertEquals(map(EX_A, x), g.exceptionalSuccessors(a));
      assertEquals(list(h), g.successors(x));
      assertConsistent(g);
    }

    @Test
    void insertBeforeUnknownStmtWithEmptyListIsRejected() {
      MutableBlockControlFlowGraph g = chain(nop(), ret());
      assertThrows(
          IllegalArgumentException.class,
          () -> g.insertBefore(nop(), Collections.emptyList(), Collections.emptyMap()));
    }

    @Test
    void insertAfterUnknownStmtWithEmptyListIsRejected() {
      MutableBlockControlFlowGraph g = chain(nop(), ret());
      assertThrows(
          IllegalArgumentException.class,
          () -> g.insertAfter(nop(), Collections.emptyList(), Collections.emptyMap()));
    }
  }

  // ===========================================================================================
  @Nested
  class Construction {

    @Test
    void setStartingStmtToMiddleOfBlock() {
      JNopStmt a = nop(), b = nop();
      JReturnVoidStmt r = ret();
      MutableBlockControlFlowGraph g = chain(a, b, r);
      g.setStartingStmt(b);
      assertConsistent(g);
    }

    @Test
    void failedAddBlockLeavesGraphUntouched() {
      JNopStmt a = nop(), x = nop(), y = nop();
      JReturnVoidStmt r = ret();
      MutableBlockControlFlowGraph g = chain(a, r);
      Snapshot before = Snapshot.of(g);

      assertThrows(IllegalArgumentException.class, () -> g.addBlock(list(x, a, y)));

      before.assertSameAs(Snapshot.of(g), "after rejected addBlock");
      assertConsistent(g);
    }

    @Test
    void copyConstructorRoundTrip() {
      JSwitchStmt sw = switchStmt(2);
      JNopStmt a = nop(), t = nop();
      JReturnVoidStmt r1 = ret(), r2 = ret(), r3 = ret();
      JIdentityStmt h = handler(EX_A);
      MutableBlockControlFlowGraph g =
          init(
              blocks(list(sw), list(a, r1), list(t, r2), list(h, r3)),
              succ(sw, list(t, a, t)),
              new Trap(EX_A, a, t, h));

      Snapshot orig = Snapshot.of(g);
      MutableBlockControlFlowGraph copy = new MutableBlockControlFlowGraph(g);
      orig.assertSameAs(Snapshot.of(copy), "mutable copy");
      assertConsistent(copy);
      orig.assertSameAs(Snapshot.of(new ImmutableBlockControlFlowGraph(g)), "immutable copy");
    }

    @Test
    void initializeWithNestedTrapsOfDifferentTypes() {
      JNopStmt a = nop(), b = nop(), c = nop();
      JReturnVoidStmt r = ret(), r2 = ret(), r3 = ret();
      JIdentityStmt h1 = handler(EX_A), h2 = handler(EX_B);
      MutableBlockControlFlowGraph g =
          init(
              blocks(list(a), list(b), list(c, r), list(h1, r2), list(h2, r3)),
              succ(),
              new Trap(EX_A, b, c, h1),
              new Trap(EX_B, a, h1, h2));
      assertEquals(map(EX_B, h2), g.exceptionalSuccessors(a));
      assertEquals(map(EX_A, h1, EX_B, h2), g.exceptionalSuccessors(b));
      assertEquals(map(EX_B, h2), g.exceptionalSuccessors(c));
      assertEquals(map(EX_B, h2), g.exceptionalSuccessors(r));
    }

    @Test
    void initializeWithBackEdgeIntoMergedTrappedBlock() {
      // s0 and s1 share the trap -> merged while linking; s1 falls into the handler h;
      // the later goto -> s1 splits the merged block again
      JNopStmt s0 = nop();
      JIfStmt s1 = ifStmt();
      JIdentityStmt h = handler(EX_A);
      JReturnVoidStmt r = ret(), r2 = ret();
      JGotoStmt gt = gotoStmt();
      MutableBlockControlFlowGraph g = new MutableBlockControlFlowGraph();
      g.initializeWith(
          blocks(list(s0), list(s1), list(h, r), list(gt), list(r2)),
          succ(s1, list(r2), gt, list(s1)),
          new ArrayList<>(Collections.singletonList(new Trap(EX_A, s0, h, h))));

      assertEquals(map(EX_A, h), g.exceptionalSuccessors(s0));
      assertEquals(map(EX_A, h), g.exceptionalSuccessors(s1));
      assertEquals(list(h, r2), g.successors(s1));
      assertEquals(list(s1), g.successors(gt));
      assertConsistent(g);
    }

    @Test
    void initializeWithTooFewSwitchTargetsIsRejected() {
      JSwitchStmt sw = switchStmt(2);
      JNopStmt a = nop();
      JReturnVoidStmt r = ret();
      MutableBlockControlFlowGraph g = new MutableBlockControlFlowGraph();
      assertThrows(
          IllegalArgumentException.class,
          () ->
              g.initializeWith(blocks(list(sw), list(a, r)), succ(sw, list(a)), new ArrayList<>()));
    }

    @Test
    void initializeWithFallThroughIntoAbyssIsRejected() {
      MutableBlockControlFlowGraph g = new MutableBlockControlFlowGraph();
      assertThrows(
          IllegalStateException.class,
          () -> g.initializeWith(blocks(list(nop(), nop())), succ(), new ArrayList<>()));
    }
  }

  @Nested
  class Batch {

    @Test
    void mergesAreDeferredUntilEndBatch() {
      JNopStmt a = nop(), b = nop(), c = nop();
      JReturnVoidStmt r = ret();
      JIdentityStmt h = handler(EX_A);
      MutableBlockControlFlowGraph g = chain(a, b, c, r);
      g.addExceptionalEdge(b, EX_A, h);
      g.putEdge(h, ret());
      int blocksWithTrap = g.getBlocks().size();

      g.beginBatch();
      g.removeExceptionalEdge(b, EX_A);
      // b keeps its own block within the batch, the flows are exact
      assertEquals(blocksWithTrap, g.getBlocks().size());
      assertEquals(list(c), g.successors(b));
      assertTrue(g.exceptionalSuccessors(b).isEmpty());
      assertConsistent(g);

      g.endBatch();
      assertEquals(blocksWithTrap - 2, g.getBlocks().size());
      assertSame(g.getBlockOf(a), g.getBlockOf(r));
      assertConsistent(g);
    }

    @Test
    void nestedBatchesMergeAtTheOutermostEnd() {
      JNopStmt a = nop(), b = nop();
      JReturnVoidStmt r = ret();
      JIdentityStmt h = handler(EX_A);
      MutableBlockControlFlowGraph g = chain(a, b, r);
      g.putEdge(h, ret());

      g.beginBatch();
      g.beginBatch();
      g.addExceptionalEdge(b, EX_A, h);
      g.removeExceptionalEdge(b, EX_A);
      g.endBatch();
      assertNotSame(g.getBlockOf(a), g.getBlockOf(b));
      g.endBatch();

      assertSame(g.getBlockOf(a), g.getBlockOf(r));
      assertConsistent(g);
    }

    @Test
    void removedStmtsOfABatchAreSkipped() {
      JNopStmt a = nop(), b = nop(), c = nop();
      JReturnVoidStmt r = ret();
      JIdentityStmt h = handler(EX_A);
      MutableBlockControlFlowGraph g = chain(a, b, c, r);
      g.putEdge(h, ret());

      g.beginBatch();
      g.addExceptionalEdge(b, EX_A, h);
      g.removeExceptionalEdge(b, EX_A);
      g.removeNode(b);
      g.endBatch();

      assertEquals(list(c), g.successors(a));
      assertSame(g.getBlockOf(a), g.getBlockOf(r));
      assertConsistent(g);
    }

    @Test
    void endBatchWithoutBeginBatchIsRejected() {
      assertThrows(
          IllegalStateException.class, () -> new MutableBlockControlFlowGraph().endBatch());
    }
  }

  private static List<Integer> list2(Integer... i) {
    return Arrays.asList(i);
  }
}
