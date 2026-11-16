package sootup.core.graph;

import java.util.*;
import java.util.function.Function;
import java.util.function.ToLongFunction;
import org.junit.jupiter.api.Test;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.Trap;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.expr.JEqExpr;
import sootup.core.jimple.common.ref.JCaughtExceptionRef;
import sootup.core.jimple.common.stmt.*;
import sootup.core.signatures.PackageName;
import sootup.core.signatures.SignatureInterner;
import sootup.core.types.ClassType;

/**
 * Manual benchmark for method-sized graphs (50..250 stmts) with branches, loops and traps, shaped
 * like bytecode-frontend output. Self-contained (public API only) so the same file runs against
 * older implementations. Not picked up by surefire's default includes; run with {@code
 * -Dtest=MutableBlockControlFlowGraphSmallMethodBenchmark}.
 */
public class MutableBlockControlFlowGraphSmallMethodBenchmark {

  static final StmtPositionInfo NOPOS = StmtPositionInfo.getNoStmtPositionInfo();
  static final int[] SIZES = {50, 100, 250};
  static final int GRAPHS = 400; // distinct random graphs per size
  static final int WARMUP = 4, ROUNDS = 10;

  static final class ExType extends ClassType {
    final String name;

    ExType(String name) {
      this.name = name;
    }

    @Override
    public String getFullyQualifiedName() {
      return "bench." + name;
    }

    @Override
    public String getClassName() {
      return name;
    }

    @Override
    public PackageName getPackageName() {
      return SignatureInterner.getPackageName("bench");
    }
  }

  static final ClassType[] EX = {new ExType("A"), new ExType("B"), new ExType("C")};
  static int counter;

  static JNopStmt nop() {
    return new JNopStmt(NOPOS);
  }

  static JIdentityStmt handler(ClassType t) {
    return Jimple.newIdentityStmt(
        Jimple.newLocal("ex" + counter++, t), new JCaughtExceptionRef(t), NOPOS);
  }

  /** input for initializeWith */
  static final class Method {
    final List<List<Stmt>> blocks = new ArrayList<>();
    final Map<BranchingStmt, List<Stmt>> succ = new HashMap<>();
    final List<Trap> traps = new ArrayList<>();
    final List<Stmt> body = new ArrayList<>();

    MutableBlockControlFlowGraph build() {
      MutableBlockControlFlowGraph g = new MutableBlockControlFlowGraph();
      g.initializeWith(blocks, succ, traps);
      return g;
    }
  }

  /**
   * ~n stmts: ~12% if (forward or backward target), ~3% goto, a tableswitch per ~120 stmts, one
   * return per ~60 stmts, one trap per ~70 stmts covering 8..30 stmts with a [handler, nop, goto]
   * handler block appended at the end.
   */
  static Method generate(int n, Random rnd) {
    Method m = new Method();
    Stmt[] s = new Stmt[n];
    for (int i = 0; i < n - 1; i++) {
      double p = rnd.nextDouble();
      if (i == 0 || i > n - 3) s[i] = nop();
      else if (p < 0.12)
        s[i] =
            new JIfStmt(new JEqExpr(IntConstant.getInstance(i), IntConstant.getInstance(0)), NOPOS);
      else if (p < 0.15) s[i] = new JGotoStmt(NOPOS);
      else if (p < 0.157) {
        List<IntConstant> keys = new ArrayList<>();
        for (int k = 0; k < 3; k++) keys.add(IntConstant.getInstance(k));
        s[i] =
            new sootup.core.jimple.javabytecode.stmt.JSwitchStmt(
                IntConstant.getInstance(i), keys, NOPOS);
      } else if (p < 0.173) s[i] = new JReturnVoidStmt(NOPOS);
      else s[i] = nop();
    }
    s[n - 1] = new JReturnVoidStmt(NOPOS);

    Set<Stmt> leaders = Collections.newSetFromMap(new IdentityHashMap<>());
    leaders.add(s[0]);
    for (int i = 0; i < n; i++) {
      if (s[i] instanceof BranchingStmt) {
        BranchingStmt b = (BranchingStmt) s[i];
        int k = s[i] instanceof JIfStmt ? 1 : s[i] instanceof JGotoStmt ? 1 : 4;
        List<Stmt> t = new ArrayList<>();
        for (int j = 0; j < k; j++) {
          // loops: ~30% backward; forward targets mostly short
          int tgt =
              rnd.nextDouble() < 0.3
                  ? Math.max(0, i - 1 - rnd.nextInt(Math.min(i, 40) + 1))
                  : Math.min(n - 1, i + 1 + rnd.nextInt(30));
          t.add(s[tgt]);
          leaders.add(s[tgt]);
        }
        m.succ.put(b, t);
      }
      if ((s[i] instanceof BranchingStmt || !(s[i] instanceof FallsThroughStmt)) && i + 1 < n)
        leaders.add(s[i + 1]);
    }

    List<Stmt> all = new ArrayList<>(Arrays.asList(s));
    int nTraps = Math.max(1, n / 70);
    for (int k = 0; k < nTraps; k++) {
      int len = 8 + rnd.nextInt(23);
      int a = 1 + rnd.nextInt(Math.max(1, n - len - 2));
      JIdentityStmt h = handler(EX[k % EX.length]);
      JNopStmt hn = nop();
      JGotoStmt hg = new JGotoStmt(NOPOS);
      m.succ.put(hg, Collections.singletonList(s[Math.min(n - 1, a + len)]));
      all.add(h);
      all.add(hn);
      all.add(hg);
      m.traps.add(new Trap(EX[k % EX.length], s[a], s[a + len], h));
      leaders.add(s[a]);
      leaders.add(s[a + len]);
      leaders.add(s[Math.min(n - 1, a + len)]);
      leaders.add(h);
    }

    List<Stmt> cur = new ArrayList<>();
    for (int i = 0; i < all.size(); i++) {
      Stmt x = all.get(i);
      if (leaders.contains(x) && !cur.isEmpty()) {
        m.blocks.add(cur);
        cur = new ArrayList<>();
      }
      cur.add(x);
      if (!(x instanceof FallsThroughStmt) || x instanceof BranchingStmt) {
        m.blocks.add(cur);
        cur = new ArrayList<>();
      }
    }
    if (!cur.isEmpty()) m.blocks.add(cur);
    m.body.addAll(all);
    return m;
  }

  static long sink;

  static final java.lang.management.ThreadMXBean THREADS =
      java.lang.management.ManagementFactory.getThreadMXBean();

  /** median CPU ns of this thread per graph (robust against other load on the machine) */
  static <T> double time(List<Method> ms, Function<Method, T> setup, ToLongFunction<T> op) {
    double[] r = new double[ROUNDS];
    for (int round = 0; round < WARMUP + ROUNDS; round++) {
      List<T> in = new ArrayList<>(ms.size());
      for (Method m : ms) in.add(setup.apply(m));
      System.gc();
      long t0 = THREADS.getCurrentThreadCpuTime();
      long acc = 0;
      for (T x : in) acc += op.applyAsLong(x);
      long dt = THREADS.getCurrentThreadCpuTime() - t0;
      sink += acc;
      if (round >= WARMUP) r[round - WARMUP] = (double) dt / ms.size();
    }
    Arrays.sort(r);
    return r[ROUNDS / 2];
  }

  static List<Stmt> nopsOf(MutableBlockControlFlowGraph g) {
    List<Stmt> l = new ArrayList<>();
    for (Stmt s : g.getNodes()) if (s instanceof JNopStmt) l.add(s);
    return l;
  }

  static List<Stmt> blockHeads(MutableBlockControlFlowGraph g) {
    List<Stmt> l = new ArrayList<>();
    for (BasicBlock<?> b : g.getBlocksSorted()) l.add(b.getHead());
    return l;
  }

  static final class Prep {
    final MutableBlockControlFlowGraph g;
    final List<Stmt> targets;

    Prep(MutableBlockControlFlowGraph g, List<Stmt> targets) {
      this.g = g;
      this.targets = targets;
    }
  }

  static long query(MutableBlockControlFlowGraph g) {
    long a = 0;
    for (Stmt s : g.getNodes()) {
      a += g.successors(s).size();
      a += g.predecessors(s).size();
      a += g.exceptionalSuccessors(s).size();
    }
    return a;
  }

  static final int PASSES = 2; // whole suite repeated, only the last pass printed (JIT warm)

  @Test
  void benchmark() {
    Map<String, double[]> res = null;
    for (int pass = 0; pass < PASSES; pass++) res = runPass();
    System.out.printf(
        "%-38s %10s %10s %10s   (median CPU ns per method; %d methods, %d warmup, %d rounds)%n",
        "op \\ stmts", SIZES[0], SIZES[1], SIZES[2], GRAPHS, WARMUP, ROUNDS);
    for (Map.Entry<String, double[]> e : res.entrySet())
      System.out.printf(
          "%-38s %10.0f %10.0f %10.0f%n",
          e.getKey(), e.getValue()[0], e.getValue()[1], e.getValue()[2]);
    System.out.println("sink " + sink);
  }

  static Map<String, double[]> runPass() {
    Map<String, double[]> res = new LinkedHashMap<>();
    for (int si = 0; si < SIZES.length; si++) {
      int n = SIZES[si];
      Random rnd = new Random(42 + n);
      List<Method> ms = new ArrayList<>();
      for (int i = 0; i < GRAPHS; i++) ms.add(generate(n, rnd));
      final int idx = si;
      Function<String, double[]> row = k -> res.computeIfAbsent(k, x -> new double[SIZES.length]);

      row.apply("initializeWith")[idx] = time(ms, m -> m, m -> m.build().getNodes().size());
      row.apply("copy ctor")[idx] =
          time(ms, Method::build, g -> new MutableBlockControlFlowGraph(g).getNodes().size());
      row.apply("getBlocksSorted")[idx] = time(ms, Method::build, g -> g.getBlocksSorted().size());
      row.apply("getStmts")[idx] = time(ms, Method::build, g -> g.getStmts().size());
      row.apply("succ+pred+excSucc of all stmts")[idx] =
          time(ms, Method::build, MutableBlockControlFlowGraphSmallMethodBenchmark::query);
      row.apply("replaceNode every nop")[idx] =
          time(
              ms,
              m -> {
                MutableBlockControlFlowGraph g = m.build();
                return new Prep(g, nopsOf(g));
              },
              p -> {
                for (Stmt s : p.targets) p.g.replaceNode(s, nop());
                return p.g.getNodes().size();
              });
      row.apply("removeNode(keepFlow) every nop")[idx] =
          time(
              ms,
              m -> {
                MutableBlockControlFlowGraph g = m.build();
                return new Prep(g, nopsOf(g));
              },
              p -> {
                for (Stmt s : p.targets) p.g.removeNode(s, true);
                return p.g.getNodes().size();
              });
      row.apply("insertBefore every block head")[idx] =
          time(
              ms,
              m -> {
                MutableBlockControlFlowGraph g = m.build();
                return new Prep(g, blockHeads(g));
              },
              p -> {
                for (Stmt h : p.targets) {
                  if (h == p.g.getStartingStmt() || h instanceof JIdentityStmt) continue;
                  p.g.insertBefore(
                      h, Collections.singletonList(nop()), p.g.exceptionalSuccessors(h));
                }
                return p.g.getNodes().size();
              });
      row.apply("pipeline (copy,replace,insert,remove)")[idx] =
          time(
              ms,
              Method::build,
              g0 -> {
                MutableBlockControlFlowGraph g = new MutableBlockControlFlowGraph(g0);
                long a = g.getBlocksSorted().size();
                List<Stmt> nops = nopsOf(g);
                List<Stmt> fresh = new ArrayList<>();
                for (int i = 0; i < nops.size(); i += 3) {
                  JNopStmt x = nop();
                  g.replaceNode(nops.get(i), x);
                  fresh.add(x);
                }
                a += g.getBlocksSorted().size();
                for (Stmt h : blockHeads(g)) {
                  if (h == g.getStartingStmt() || h instanceof JIdentityStmt) continue;
                  g.insertBefore(h, Collections.singletonList(nop()), g.exceptionalSuccessors(h));
                }
                a += g.getBlocksSorted().size();
                for (int i = 0; i < fresh.size(); i += 2) g.removeNode(fresh.get(i), true);
                a += g.getBlocksSorted().size() + g.getStmts().size();
                return a;
              });
    }
    return res;
  }
}
