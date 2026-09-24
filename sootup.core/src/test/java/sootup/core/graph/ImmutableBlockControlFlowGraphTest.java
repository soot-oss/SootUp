package sootup.core.graph;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import org.junit.jupiter.api.Test;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.expr.JLeExpr;
import sootup.core.jimple.common.ref.JCaughtExceptionRef;
import sootup.core.jimple.common.stmt.*;
import sootup.core.signatures.PackageName;
import sootup.core.types.ClassType;
import sootup.core.types.PrimitiveType;

public class ImmutableBlockControlFlowGraphTest {

  private final StmtPositionInfo noPosInfo = StmtPositionInfo.getNoStmtPositionInfo();

  private final ClassType throwableType =
      new ClassType() {
        @Override
        public String getFullyQualifiedName() {
          return "java.lang.Throwable";
        }

        @Override
        public String getClassName() {
          return "Throwable";
        }

        @Override
        public PackageName getPackageName() {
          return new PackageName("java.lang");
        }
      };

  // ── helpers ────────────────────────────────────────────────────────────────────────────────

  private MutableBlockControlFlowGraph buildLinearGraph() {
    MutableBlockControlFlowGraph g = new MutableBlockControlFlowGraph();
    JNopStmt n1 = new JNopStmt(noPosInfo);
    JNopStmt n2 = new JNopStmt(noPosInfo);
    JNopStmt n3 = new JNopStmt(noPosInfo);
    JReturnVoidStmt ret = new JReturnVoidStmt(noPosInfo);

    g.setStartingStmt(n1);
    g.putEdge(n1, n2);
    g.putEdge(n2, n3);
    g.putEdge(n3, ret);
    return g;
  }

  private MutableBlockControlFlowGraph buildBranchingGraph() {
    Local l = new Local("i", PrimitiveType.IntType.getInstance());
    MutableBlockControlFlowGraph g = new MutableBlockControlFlowGraph();

    JNopStmt entry = new JNopStmt(noPosInfo);
    JIfStmt ifStmt = new JIfStmt(new JLeExpr(l, IntConstant.getInstance(10)), noPosInfo);
    JNopStmt trueTarget = new JNopStmt(noPosInfo);
    JNopStmt falseTarget = new JNopStmt(noPosInfo);
    JReturnVoidStmt ret1 = new JReturnVoidStmt(noPosInfo);
    JReturnVoidStmt ret2 = new JReturnVoidStmt(noPosInfo);

    g.setStartingStmt(entry);
    g.putEdge(entry, ifStmt);
    g.putEdge(ifStmt, JIfStmt.FALSE_BRANCH_IDX, falseTarget);
    g.putEdge(ifStmt, JIfStmt.TRUE_BRANCH_IDX, trueTarget);
    g.putEdge(trueTarget, ret1);
    g.putEdge(falseTarget, ret2);
    return g;
  }

  // ── tests ─────────────────────────────────────────────────────────────────────────────────

  @Test
  void construction_fromMutableGraph_succeeds() {
    ImmutableBlockControlFlowGraph imm = new ImmutableBlockControlFlowGraph(buildLinearGraph());
    assertNotNull(imm.getStartingStmt());
    assertEquals(4, imm.getNodeCount());
  }

  @Test
  void indexOf_and_getStmt_areConsistent() {
    MutableBlockControlFlowGraph mutable = buildLinearGraph();
    ImmutableBlockControlFlowGraph imm = new ImmutableBlockControlFlowGraph(mutable);

    for (Stmt stmt : imm.getNodes()) {
      int idx = imm.indexOf(stmt);
      assertSame(stmt, imm.getStmt(idx), "round-trip indexOf/getStmt must return same object");
    }
  }

  @Test
  void indexOf_uniqueIndices() {
    ImmutableBlockControlFlowGraph imm = new ImmutableBlockControlFlowGraph(buildLinearGraph());
    Set<Integer> seen = new HashSet<>();
    for (Stmt stmt : imm.getNodes()) {
      assertTrue(seen.add(imm.indexOf(stmt)), "each stmt must have a unique index");
    }
    assertEquals(imm.getNodeCount(), seen.size());
  }

  @Test
  void indexOf_unknownStmt_throwsNoSuchElement() {
    ImmutableBlockControlFlowGraph imm = new ImmutableBlockControlFlowGraph(buildLinearGraph());
    JNopStmt foreign = new JNopStmt(noPosInfo);
    assertThrows(NoSuchElementException.class, () -> imm.indexOf(foreign));
  }

  @Test
  void getStmt_outOfBounds_throwsArrayIndexOutOfBounds() {
    ImmutableBlockControlFlowGraph imm = new ImmutableBlockControlFlowGraph(buildLinearGraph());
    assertThrows(ArrayIndexOutOfBoundsException.class, () -> imm.getStmt(imm.getNodeCount()));
    assertThrows(ArrayIndexOutOfBoundsException.class, () -> imm.getStmt(-1));
  }

  @Test
  void startingStmt_matches_source() {
    MutableBlockControlFlowGraph mutable = buildLinearGraph();
    ImmutableBlockControlFlowGraph imm = new ImmutableBlockControlFlowGraph(mutable);
    assertSame(mutable.getStartingStmt(), imm.getStartingStmt());
  }

  @Test
  void successors_matchMutableGraph_linear() {
    MutableBlockControlFlowGraph mutable = buildLinearGraph();
    ImmutableBlockControlFlowGraph imm = new ImmutableBlockControlFlowGraph(mutable);

    for (Stmt stmt : mutable.getNodes()) {
      assertEquals(
          mutable.successors(stmt), imm.successors(stmt), "successors of " + stmt + " must match");
    }
  }

  @Test
  void predecessors_matchMutableGraph_linear() {
    MutableBlockControlFlowGraph mutable = buildLinearGraph();
    ImmutableBlockControlFlowGraph imm = new ImmutableBlockControlFlowGraph(mutable);

    for (Stmt stmt : mutable.getNodes()) {
      assertEquals(
          mutable.predecessors(stmt),
          imm.predecessors(stmt),
          "predecessors of " + stmt + " must match");
    }
  }

  @Test
  void successors_branchingGraph_preservesOrder() {
    MutableBlockControlFlowGraph mutable = buildBranchingGraph();
    ImmutableBlockControlFlowGraph imm = new ImmutableBlockControlFlowGraph(mutable);

    for (Stmt stmt : mutable.getNodes()) {
      List<Stmt> mSucc = mutable.successors(stmt);
      List<Stmt> iSucc = imm.successors(stmt);
      assertEquals(mSucc.size(), iSucc.size(), "successor count must match for " + stmt);
      for (int i = 0; i < mSucc.size(); i++) {
        assertSame(mSucc.get(i), iSucc.get(i), "successor[" + i + "] identity must match");
      }
    }
  }

  @Test
  void containsNode_trueForKnownFalseForForeign() {
    MutableBlockControlFlowGraph mutable = buildLinearGraph();
    ImmutableBlockControlFlowGraph imm = new ImmutableBlockControlFlowGraph(mutable);

    for (Stmt stmt : mutable.getNodes()) {
      assertTrue(imm.containsNode(stmt));
    }
    assertFalse(imm.containsNode(new JNopStmt(noPosInfo)));
  }

  @Test
  void getBlocks_sizeMatchesSource() {
    MutableBlockControlFlowGraph mutable = buildBranchingGraph();
    ImmutableBlockControlFlowGraph imm = new ImmutableBlockControlFlowGraph(mutable);
    assertEquals(mutable.getBlocks().size(), imm.getBlocks().size());
  }

  @Test
  void getStartingStmtBlock_containsStartingStmt() {
    ImmutableBlockControlFlowGraph imm = new ImmutableBlockControlFlowGraph(buildLinearGraph());
    BasicBlock<?> startBlock = imm.getStartingStmtBlock();
    assertNotNull(startBlock);
    assertSame(imm.getStartingStmt(), startBlock.getHead());
  }

  @Test
  void getTailStmtBlocks_containsReturnBlock() {
    ImmutableBlockControlFlowGraph imm = new ImmutableBlockControlFlowGraph(buildLinearGraph());
    List<BasicBlock<?>> tails = imm.getTailStmtBlocks();
    assertEquals(1, tails.size());
    assertInstanceOf(JReturnVoidStmt.class, tails.get(0).getTail());
  }

  @Test
  void getTailStmtBlocks_branchingGraph_hasTwoTails() {
    ImmutableBlockControlFlowGraph imm = new ImmutableBlockControlFlowGraph(buildBranchingGraph());
    assertEquals(2, imm.getTailStmtBlocks().size());
  }

  @Test
  void inDegree_outDegree_matchMutableGraph() {
    MutableBlockControlFlowGraph mutable = buildBranchingGraph();
    ImmutableBlockControlFlowGraph imm = new ImmutableBlockControlFlowGraph(mutable);

    for (Stmt stmt : mutable.getNodes()) {
      assertEquals(mutable.inDegree(stmt), imm.inDegree(stmt), "inDegree mismatch for " + stmt);
      assertEquals(mutable.outDegree(stmt), imm.outDegree(stmt), "outDegree mismatch for " + stmt);
    }
  }

  @Test
  void hasEdgeConnecting_matchesMutableGraph() {
    MutableBlockControlFlowGraph mutable = buildLinearGraph();
    ImmutableBlockControlFlowGraph imm = new ImmutableBlockControlFlowGraph(mutable);
    List<Stmt> stmtList = mutable.getStmts();

    for (int i = 0; i < stmtList.size() - 1; i++) {
      assertTrue(imm.hasEdgeConnecting(stmtList.get(i), stmtList.get(i + 1)));
    }
    // no back-edge in a linear graph
    assertFalse(imm.hasEdgeConnecting(stmtList.get(1), stmtList.get(0)));
  }

  @Test
  void immutableBlock_getStmts_returnsCorrectSubrange() {
    MutableBlockControlFlowGraph mutable = buildLinearGraph();
    ImmutableBlockControlFlowGraph imm = new ImmutableBlockControlFlowGraph(mutable);

    for (ImmutableBlockControlFlowGraph.ImmutableBasicBlock block : imm.getBlocksSorted()) {
      List<Stmt> blockStmts = block.getStmts();
      assertFalse(blockStmts.isEmpty());
      assertSame(block.getHead(), blockStmts.get(0));
      assertSame(block.getTail(), blockStmts.get(blockStmts.size() - 1));
    }
  }

  @Test
  void exceptionalSuccessors_preservedFromMutableGraph() {
    // Use a single-stmt block (return) as the guarded stmt so no block-split is needed
    MutableBlockControlFlowGraph mutable = new MutableBlockControlFlowGraph();
    JReturnVoidStmt body = new JReturnVoidStmt(noPosInfo);

    Local caughtLocal = new Local("$e", throwableType);
    JIdentityStmt catchHandler =
        new JIdentityStmt(caughtLocal, new JCaughtExceptionRef(throwableType), noPosInfo);
    JReturnVoidStmt catchRet = new JReturnVoidStmt(noPosInfo);

    mutable.setStartingStmt(body);
    mutable.addExceptionalEdge(body, throwableType, catchHandler);
    mutable.putEdge(catchHandler, catchRet);

    ImmutableBlockControlFlowGraph imm = new ImmutableBlockControlFlowGraph(mutable);

    Map<ClassType, Stmt> exSuccs = imm.exceptionalSuccessors(body);
    assertEquals(1, exSuccs.size());
    assertSame(catchHandler, exSuccs.get(throwableType));
  }

  @Test
  void mutation_throwsUnsupportedOperation() {
    ImmutableBlockControlFlowGraph imm = new ImmutableBlockControlFlowGraph(buildLinearGraph());
    assertThrows(
        UnsupportedOperationException.class,
        () -> imm.removeExceptionalFlowFromAllBlocks(throwableType, imm.getStartingStmt()));
  }

  @Test
  void getBlocksSorted_fallthroughTarget_isConsecutive() {
    // CFG: entry -> ifStmt --false(fallthrough)--> falseTarget -> ret1
    //                      --true(branch)---------> trueTarget  -> ret2
    MutableBlockControlFlowGraph g = buildBranchingGraph();
    List<? extends BasicBlock<?>> sorted = g.getBlocksSorted();

    BasicBlock<?> ifBlock =
        sorted.stream().filter(b -> b.getTail() instanceof JIfStmt).findFirst().orElseThrow();
    int ifPos = sorted.indexOf(ifBlock);

    BasicBlock<?> fallthroughSucc = ifBlock.getSuccessors().get(JIfStmt.FALSE_BRANCH_IDX);
    assertEquals(
        ifPos + 1,
        sorted.indexOf(fallthroughSucc),
        "Fallthrough successor must immediately follow its source block in getBlocksSorted()");
  }

  @Test
  void roundTrip_mutableToImmutableToMutable_sameGraph() {
    MutableBlockControlFlowGraph original = buildBranchingGraph();
    ImmutableBlockControlFlowGraph imm = new ImmutableBlockControlFlowGraph(original);
    MutableBlockControlFlowGraph copy = new MutableBlockControlFlowGraph(imm);

    assertEquals(original.getNodes().size(), copy.getNodes().size());
    for (Stmt stmt : original.getNodes()) {
      assertEquals(
          original.successors(stmt),
          copy.successors(stmt),
          "successors must survive mutable → immutable → mutable round-trip");
    }
  }
}
