package sootup.core.graph;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.expr.JEqExpr;
import sootup.core.jimple.common.ref.JCaughtExceptionRef;
import sootup.core.jimple.common.stmt.*;
import sootup.core.jimple.javabytecode.stmt.JSwitchStmt;
import sootup.core.signatures.PackageName;
import sootup.core.signatures.SignatureInterner;
import sootup.core.types.ClassType;

/** Stmt factories, structural invariant checker and stmt-level snapshots for CFG tests. */
final class CfgTestKit {

  private CfgTestKit() {}

  static final StmtPositionInfo NOPOS = StmtPositionInfo.getNoStmtPositionInfo();

  static final class TestClassType extends ClassType {
    private final String pkg;
    private final String name;

    TestClassType(String pkg, String name) {
      this.pkg = pkg;
      this.name = name;
    }

    @Override
    public String getFullyQualifiedName() {
      return pkg + "." + name;
    }

    @Override
    public String getClassName() {
      return name;
    }

    @Override
    public PackageName getPackageName() {
      return SignatureInterner.getPackageName(pkg);
    }

    @Override
    public String toString() {
      return getFullyQualifiedName();
    }
  }

  static final ClassType EX_A = new TestClassType("test", "ExA");
  static final ClassType EX_B = new TestClassType("test", "ExB");
  static final ClassType EX_C = new TestClassType("test", "ExC");

  private static int counter = 0;

  static JNopStmt nop() {
    return new JNopStmt(NOPOS);
  }

  static JGotoStmt gotoStmt() {
    return new JGotoStmt(NOPOS);
  }

  static JIfStmt ifStmt() {
    return new JIfStmt(
        new JEqExpr(IntConstant.getInstance(counter++), IntConstant.getInstance(0)), NOPOS);
  }

  /** switch with {@code cases} case labels + default, i.e. cases+1 successors */
  static JSwitchStmt switchStmt(int cases) {
    List<IntConstant> vals = new ArrayList<>();
    for (int i = 0; i < cases; i++) {
      vals.add(IntConstant.getInstance(i));
    }
    return new JSwitchStmt(IntConstant.getInstance(counter++), vals, NOPOS);
  }

  static JReturnVoidStmt ret() {
    return new JReturnVoidStmt(NOPOS);
  }

  static JThrowStmt throwStmt() {
    return new JThrowStmt(Jimple.newLocal("t" + counter++, EX_A), NOPOS);
  }

  static JIdentityStmt handler(ClassType type) {
    return Jimple.newIdentityStmt(
        Jimple.newLocal("ex" + counter++, type), new JCaughtExceptionRef(type), NOPOS);
  }

  static boolean isHandler(Stmt s) {
    return s instanceof JIdentityStmt
        && ((JIdentityStmt) s).getRightOp() instanceof JCaughtExceptionRef;
  }

  static <K, V> Map<K, V> map(Object... kv) {
    Map<K, V> m = new HashMap<>();
    for (int i = 0; i < kv.length; i += 2) {
      @SuppressWarnings("unchecked")
      K k = (K) kv[i];
      @SuppressWarnings("unchecked")
      V v = (V) kv[i + 1];
      m.put(k, v);
    }
    return m;
  }

  static List<Stmt> list(Stmt... s) {
    return new ArrayList<>(Arrays.asList(s));
  }

  // ---------------------------------------------------------------------------------------------
  // structural invariants
  // ---------------------------------------------------------------------------------------------

  private static int countIdentity(Collection<?> c, Object o) {
    int n = 0;
    for (Object x : c) {
      if (x == o) n++;
    }
    return n;
  }

  /**
   * Checks the internal consistency of a block graph via the public API only. Fails with a message
   * that names the first broken invariant.
   */
  static void assertConsistent(MutableBlockControlFlowGraph g) {
    @SuppressWarnings("unchecked")
    Collection<MutableBasicBlock> blocks = (Collection<MutableBasicBlock>) g.getBlocks();
    Set<MutableBasicBlock> blockSet = Collections.newSetFromMap(new IdentityHashMap<>());
    blockSet.addAll(blocks);
    assertEquals(blocks.size(), blockSet.size(), "a block is listed twice in getBlocks()");

    Set<Stmt> seen = Collections.newSetFromMap(new IdentityHashMap<>());
    for (MutableBasicBlock b : blocks) {
      List<Stmt> stmts = b.getStmts();
      assertFalse(stmts.isEmpty(), "empty block in graph");
      for (int i = 0; i < stmts.size(); i++) {
        Stmt s = stmts.get(i);
        assertTrue(seen.add(s), "stmt '" + s + "' is in more than one block");
        assertSame(b, g.getBlockOf(s), "getBlockOf('" + s + "') is not its containing block");
        if (i < stmts.size() - 1) {
          assertTrue(
              s instanceof FallsThroughStmt && !(s instanceof BranchingStmt),
              "non-tail stmt '" + s + "' must fall through and not branch");
          assertEquals(
              Collections.singletonList(stmts.get(i + 1)),
              g.successors(s),
              "inner successor of '" + s + "'");
          assertEquals(1, g.outDegree(s));
          assertTrue(g.hasEdgeConnecting(s, stmts.get(i + 1)));
        }
        if (i > 0) {
          assertEquals(
              Collections.singletonList(stmts.get(i - 1)),
              g.predecessors(s),
              "inner predecessor of '" + s + "'");
          assertEquals(1, g.inDegree(s));
          assertTrue(
              g.exceptionalPredecessors(s).isEmpty(), "non-head '" + s + "' has exceptional preds");
        }
        Map<ClassType, Stmt> exc = g.exceptionalSuccessors(s);
        assertEquals(exc.size(), b.getExceptionalSuccessors().size());
        b.getExceptionalSuccessors()
            .forEach((t, h) -> assertSame(h.getHead(), exc.get(t), "exc succ of '" + s + "'"));
      }
    }
    assertEquals(seen.size(), g.getNodes().size(), "getNodes() size != stmts in blocks");
    for (Stmt s : g.getNodes()) {
      assertTrue(seen.contains(s), "getNodes() contains '" + s + "' which is in no block");
      assertTrue(g.containsNode(s));
    }

    for (MutableBasicBlock b : blocks) {
      Stmt tail = b.getTail();
      List<MutableBasicBlock> succs = b.getSuccessors();
      assertTrue(
          succs.size() <= tail.getExpectedSuccessorCount(),
          "block with tail '" + tail + "' has too many successors: " + succs.size());
      for (MutableBasicBlock s : succs) {
        assertTrue(blockSet.contains(s), "dangling successor block " + s + " of " + b);
      }
      for (MutableBasicBlock s : b.getExceptionalSuccessors().values()) {
        assertTrue(blockSet.contains(s), "dangling exceptional successor block " + s + " of " + b);
      }
      for (MutableBasicBlock p : b.getPredecessors()) {
        assertTrue(blockSet.contains(p), "dangling predecessor block " + p + " of " + b);
      }
      // stmt-level tail/head views agree with the block view
      List<Stmt> succHeads = new ArrayList<>();
      succs.forEach(s -> succHeads.add(s.getHead()));
      assertEquals(succHeads, g.successors(tail), "successors of tail '" + tail + "'");
      assertEquals(succs.size(), g.outDegree(tail));
      assertEquals(b.getPredecessors().size(), g.inDegree(b.getHead()));
      assertEquals(b.getPredecessors().size(), g.predecessors(b.getHead()).size());
    }

    // edge multiset: one predecessor entry per normal and per exceptional edge
    for (MutableBasicBlock target : blocks) {
      for (MutableBasicBlock src : blocks) {
        int expected =
            countIdentity(src.getSuccessors(), target)
                + countIdentity(src.getExceptionalSuccessors().values(), target);
        int actual = countIdentity(target.getPredecessors(), src);
        assertEquals(
            expected,
            actual,
            "predecessor entries of "
                + target
                + " for "
                + src
                + " (normal+exceptional edges) out of sync");
      }
    }

    Stmt start = g.getStartingStmt();
    if (start != null) {
      assertTrue(g.containsNode(start));
      assertSame(
          start, g.getBlockOf(start).getHead(), "starting stmt must be the head of its block");
      assertSame(g.getBlockOf(start), g.getStartingStmtBlock());
    }

    List<Stmt> ordered = g.getStmts();
    Set<Stmt> orderedSet = Collections.newSetFromMap(new IdentityHashMap<>());
    for (Stmt s : ordered) {
      assertTrue(orderedSet.add(s), "getStmts() lists '" + s + "' twice");
    }
    assertEquals(g.getNodes().size(), ordered.size(), "getStmts() does not list every stmt");
    if (start != null) {
      assertSame(start, ordered.get(0), "getStmts() must begin with the starting stmt");
    }
  }

  // ---------------------------------------------------------------------------------------------
  // stmt-level snapshot
  // ---------------------------------------------------------------------------------------------

  /** Stmt-level view of a graph: ordered successors and exceptional successors per stmt. */
  static final class Snapshot {
    final Map<Stmt, List<Stmt>> succ = new IdentityHashMap<>();
    final Map<Stmt, Map<ClassType, Stmt>> exc = new IdentityHashMap<>();
    Stmt start;

    static Snapshot of(ControlFlowGraph<?> g) {
      Snapshot s = new Snapshot();
      s.start = g.getStartingStmt();
      for (Stmt n : g.getNodes()) {
        s.succ.put(n, new ArrayList<>(g.successors(n)));
        s.exc.put(n, new HashMap<>(g.exceptionalSuccessors(n)));
      }
      return s;
    }

    /** identity-aware comparison with a readable diff */
    void assertSameAs(Snapshot other, String ctx) {
      assertSame(start, other.start, ctx + ": starting stmt");
      Set<Stmt> a = Collections.newSetFromMap(new IdentityHashMap<>());
      a.addAll(succ.keySet());
      Set<Stmt> b = Collections.newSetFromMap(new IdentityHashMap<>());
      b.addAll(other.succ.keySet());
      for (Stmt s : a) {
        assertTrue(b.contains(s), ctx + ": stmt '" + s + "' missing");
      }
      for (Stmt s : b) {
        assertTrue(a.contains(s), ctx + ": unexpected stmt '" + s + "'");
      }
      for (Stmt s : a) {
        List<Stmt> x = succ.get(s);
        List<Stmt> y = other.succ.get(s);
        boolean same = x.size() == y.size();
        for (int i = 0; same && i < x.size(); i++) {
          same = x.get(i) == y.get(i);
        }
        assertTrue(same, ctx + ": successors of '" + s + "' expected " + x + " but were " + y);
        assertEquals(exc.get(s).keySet(), other.exc.get(s).keySet(), ctx + ": exc types of " + s);
        for (Map.Entry<ClassType, Stmt> e : exc.get(s).entrySet()) {
          assertSame(
              e.getValue(),
              other.exc.get(s).get(e.getKey()),
              ctx + ": exc succ " + e.getKey() + " of '" + s + "'");
        }
      }
    }
  }

  /** linear chain s0 -> s1 -> ... -> sn, start = s0 */
  static MutableBlockControlFlowGraph chain(Stmt... stmts) {
    MutableBlockControlFlowGraph g = new MutableBlockControlFlowGraph();
    g.setStartingStmt(stmts[0]);
    for (int i = 0; i + 1 < stmts.length; i++) {
      g.putEdge((FallsThroughStmt) stmts[i], stmts[i + 1]);
    }
    return g;
  }
}
