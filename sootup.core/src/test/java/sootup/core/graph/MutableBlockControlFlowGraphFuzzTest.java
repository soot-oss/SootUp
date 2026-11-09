package sootup.core.graph;

import static org.junit.jupiter.api.Assertions.*;
import static sootup.core.graph.CfgTestKit.*;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import sootup.core.jimple.common.Trap;
import sootup.core.jimple.common.stmt.*;
import sootup.core.jimple.javabytecode.stmt.JSwitchStmt;
import sootup.core.types.ClassType;

/**
 * Model-based fuzzing of {@link MutableBlockControlFlowGraph}: a random program is loaded via
 * {@link MutableBlockControlFlowGraph#initializeWith}, then random mutations are applied to the
 * graph and to a trivial stmt-level reference model. After every step the graph must be
 * structurally consistent and equal to the model at stmt level.
 *
 * <p>Each op family has its own test so a failure points at the broken area. Failure messages
 * contain the seed and the op log for reproduction.
 */
public class MutableBlockControlFlowGraphFuzzTest {

  static final int SEEDS = 60;
  static final int STEPS = 40;

  enum Op {
    INSERT_BEFORE,
    INSERT_AFTER,
    REMOVE_KEEP_FLOW,
    REPLACE_NOP,
    REPLACE_GOTO_BY_NOP,
    REPLACE_IF_BY_NOP,
    REPLACE_NOP_BY_GOTO,
    REPLACE_BRANCH_SAME_ARITY,
    REDIRECT_BRANCH,
    SET_EDGES,
    ADD_EXC,
    REMOVE_EXC,
    CLEAR_EXC,
    REMOVE_FLOW_ALL
  }

  static final EnumSet<Op> INSERTS = EnumSet.of(Op.INSERT_BEFORE, Op.INSERT_AFTER);
  static final EnumSet<Op> REPLACES =
      EnumSet.of(
          Op.REPLACE_NOP,
          Op.REPLACE_GOTO_BY_NOP,
          Op.REPLACE_IF_BY_NOP,
          Op.REPLACE_NOP_BY_GOTO,
          Op.REPLACE_BRANCH_SAME_ARITY);
  static final EnumSet<Op> EDGES = EnumSet.of(Op.REDIRECT_BRANCH, Op.SET_EDGES);
  static final EnumSet<Op> EXCEPTIONAL =
      EnumSet.of(Op.ADD_EXC, Op.REMOVE_EXC, Op.CLEAR_EXC, Op.REMOVE_FLOW_ALL);

  static Stream<DynamicTest> perSeed(Consumer<Long> body) {
    return IntStream.range(0, SEEDS)
        .mapToObj(i -> 0xC0FFEEL * 31 + i)
        .map(
            seed ->
                DynamicTest.dynamicTest(
                    "seed " + seed,
                    () ->
                        assertTimeoutPreemptively(
                            java.time.Duration.ofSeconds(20), () -> body.accept(seed))));
  }

  // ===========================================================================================
  // tests

  @TestFactory
  Stream<DynamicTest> initializeWithMatchesProgram() {
    return perSeed(
        seed -> {
          new Run(seed, EnumSet.noneOf(Op.class)).run(0);
        });
  }

  @TestFactory
  Stream<DynamicTest> copiesMatchProgram() {
    return perSeed(
        seed -> {
          Run run = new Run(seed, EnumSet.noneOf(Op.class));
          run.run(0);
          run.checkCopies();
        });
  }

  @TestFactory
  Stream<DynamicTest> inserts() {
    return perSeed(
        seed -> {
          new Run(seed, INSERTS).run(STEPS);
        });
  }

  @TestFactory
  Stream<DynamicTest> removeKeepingFlow() {
    return perSeed(
        seed -> {
          new Run(seed, EnumSet.of(Op.REMOVE_KEEP_FLOW)).run(STEPS);
        });
  }

  @TestFactory
  Stream<DynamicTest> replaces() {
    return perSeed(
        seed -> {
          new Run(seed, REPLACES).run(STEPS);
        });
  }

  @TestFactory
  Stream<DynamicTest> edges() {
    return perSeed(
        seed -> {
          new Run(seed, EDGES).run(STEPS);
        });
  }

  @TestFactory
  Stream<DynamicTest> exceptionalEdges() {
    return perSeed(
        seed -> {
          new Run(seed, EXCEPTIONAL).run(STEPS);
        });
  }

  @TestFactory
  Stream<DynamicTest> everything() {
    return perSeed(
        seed -> {
          Run run = new Run(seed, EnumSet.allOf(Op.class));
          run.run(STEPS);
          run.checkCopies();
        });
  }

  // ===========================================================================================
  // reference model

  static final class Model {
    final Map<Stmt, Stmt[]> succ = new IdentityHashMap<>();
    final Map<Stmt, Map<ClassType, Stmt>> exc = new IdentityHashMap<>();
    Stmt start;

    void add(Stmt s) {
      succ.put(s, new Stmt[s.getExpectedSuccessorCount()]);
      exc.put(s, new HashMap<>());
    }

    void remove(Stmt s) {
      succ.remove(s);
      exc.remove(s);
    }

    void redirectAll(Stmt from, Stmt to) {
      for (Stmt[] targets : succ.values()) {
        for (int i = 0; i < targets.length; i++) {
          if (targets[i] == from) targets[i] = to;
        }
      }
    }

    /** replace stmt identity, keep all edges */
    void rename(Stmt old, Stmt neu) {
      Stmt[] t = succ.remove(old);
      Stmt[] nt = new Stmt[neu.getExpectedSuccessorCount()];
      System.arraycopy(t, 0, nt, 0, Math.min(t.length, nt.length));
      succ.put(neu, nt);
      exc.put(neu, exc.remove(old));
      redirectAll(old, neu);
      for (Map<ClassType, Stmt> m : exc.values()) {
        m.replaceAll((k, v) -> v == old ? neu : v);
      }
      if (start == old) start = neu;
    }

    /** succ[0] of a falling-through stmt, i.e. the stmt it falls into; null otherwise */
    Stmt fallsInto(Stmt s) {
      return s instanceof FallsThroughStmt ? succ.get(s)[0] : null;
    }

    /**
     * true if a new fall-through edge from -> to keeps the code linearizable: "to" has no other
     * fall-through predecessor and the fall-through chain starting at "to" does not reach "from".
     */
    boolean canFallThrough(Stmt from, Stmt to) {
      for (Stmt p : succ.keySet()) {
        if (p != from && fallsInto(p) == to) return false;
      }
      Set<Stmt> seen = Collections.newSetFromMap(new IdentityHashMap<>());
      for (Stmt c = to; c != null && seen.add(c); c = fallsInto(c)) {
        if (c == from) return false;
      }
      return true;
    }

    List<Stmt> preds(Stmt s) {
      List<Stmt> p = new ArrayList<>();
      succ.forEach(
          (from, ts) -> {
            for (Stmt t : ts) if (t == s) p.add(from);
          });
      return p;
    }

    boolean isExceptionalTarget(Stmt s) {
      for (Map<ClassType, Stmt> m : exc.values()) {
        if (m.containsValue(s)) return true;
      }
      return false;
    }

    Snapshot snapshot() {
      Snapshot s = new Snapshot();
      s.start = start;
      succ.forEach(
          (k, v) -> {
            List<Stmt> l = new ArrayList<>();
            for (Stmt t : v) if (t != null) l.add(t);
            s.succ.put(k, l);
          });
      exc.forEach((k, v) -> s.exc.put(k, new HashMap<>(v)));
      return s;
    }
  }

  // ===========================================================================================
  // one fuzz run

  static final class Run {
    final long seed;
    final Random rnd;
    final EnumSet<Op> ops;
    final Model model = new Model();
    final MutableBlockControlFlowGraph g = new MutableBlockControlFlowGraph();
    final List<String> log = new ArrayList<>();
    final List<Stmt> names = new ArrayList<>(); // stable index for log output

    Run(long seed, EnumSet<Op> ops) {
      this.seed = seed;
      this.rnd = new Random(seed);
      this.ops = ops;
    }

    String n(Stmt s) {
      int i = -1;
      for (int k = 0; k < names.size(); k++) if (names.get(k) == s) i = k;
      if (i < 0) {
        names.add(s);
        i = names.size() - 1;
      }
      return "s" + i + "(" + s + ")";
    }

    void fail(String msg, Throwable cause) {
      throw new AssertionError(
          "seed=" + seed + " " + msg + "\nop log:\n  " + String.join("\n  ", log), cause);
    }

    void run(int steps) {
      try {
        buildProgram();
      } catch (Throwable t) {
        fail("building program failed", t);
      }
      check("after initializeWith");
      if (ops.isEmpty()) return;
      List<Op> opList = new ArrayList<>(ops);
      int done = 0;
      int attempts = 0;
      while (done < steps && attempts++ < steps * 20) {
        Op op = opList.get(rnd.nextInt(opList.size()));
        boolean applied;
        try {
          applied = apply(op);
        } catch (AssertionError e) {
          throw e;
        } catch (Throwable t) {
          fail("graph threw on a valid " + op, t);
          return;
        }
        if (applied) {
          done++;
          check("after " + log.get(log.size() - 1));
        }
      }
    }

    void check(String ctx) {
      try {
        assertConsistent(g);
        model.snapshot().assertSameAs(Snapshot.of(g), ctx);
        for (Stmt s : model.succ.keySet()) {
          if (model.isExceptionalTarget(s)) continue; // preds() also lists exceptional preds
          List<Stmt> expected = model.preds(s);
          List<Stmt> actual = g.predecessors(s);
          assertEquals(
              expected.size(), actual.size(), ctx + ": predecessor count of '" + n(s) + "'");
          for (Stmt p : expected) {
            assertTrue(
                actual.stream().anyMatch(x -> x == p),
                ctx + ": predecessor '" + n(p) + "' of '" + n(s) + "' missing");
          }
        }
      } catch (AssertionError e) {
        fail(ctx, e);
      } catch (Throwable t) {
        fail(ctx + " (checker crashed)", t);
      }
    }

    void checkCopies() {
      try {
        Snapshot expected = model.snapshot();
        MutableBlockControlFlowGraph copy = new MutableBlockControlFlowGraph(g);
        assertConsistent(copy);
        expected.assertSameAs(Snapshot.of(copy), "mutable copy");
        expected.assertSameAs(
            Snapshot.of(new ImmutableBlockControlFlowGraph(g)), "immutable snapshot");
      } catch (AssertionError e) {
        fail("copy", e);
      } catch (Throwable t) {
        fail("copy crashed", t);
      }
    }

    // ---------------------------------------------------------------------------------------
    // program generation

    void buildProgram() {
      int n = 6 + rnd.nextInt(20);
      ClassType[] types = {EX_A, EX_B};
      Stmt[] stmts = new Stmt[n];
      Set<Integer> handlerIdx = new TreeSet<>();
      Map<Integer, ClassType> handlerType = new HashMap<>();
      int handlers = rnd.nextInt(3);
      for (int k = 0; k < handlers; k++) {
        int i = 1 + rnd.nextInt(n - 2); // never the last stmt, it becomes a return
        if (handlerIdx.add(i)) handlerType.put(i, types[rnd.nextInt(types.length)]);
      }
      for (int i = 0; i < n; i++) {
        if (handlerIdx.contains(i)) {
          stmts[i] = handler(handlerType.get(i));
          continue;
        }
        int r = rnd.nextInt(100);
        boolean last = i == n - 1;
        if (r < 55) stmts[i] = last ? ret() : nop();
        else if (r < 65) stmts[i] = gotoStmt();
        else if (r < 77) stmts[i] = last ? gotoStmt() : ifStmt();
        else if (r < 85) stmts[i] = switchStmt(1 + rnd.nextInt(2));
        else if (r < 95) stmts[i] = ret();
        else stmts[i] = throwStmt();
      }
      if (stmts[n - 1] instanceof FallsThroughStmt) stmts[n - 1] = ret();
      for (Stmt s : stmts) {
        model.add(s);
        n(s);
      }
      model.start = stmts[0];

      Set<Integer> leaders = new TreeSet<>();
      leaders.add(0);
      leaders.addAll(handlerIdx);
      Map<BranchingStmt, List<Stmt>> successorMap = new HashMap<>();
      for (int i = 0; i < n; i++) {
        Stmt s = stmts[i];
        Stmt[] t = model.succ.get(s);
        if (s instanceof JIfStmt) {
          int target = rnd.nextInt(n);
          t[0] = stmts[i + 1];
          t[1] = stmts[target];
          successorMap.put((BranchingStmt) s, list(stmts[target]));
          leaders.add(target);
        } else if (s instanceof BranchingStmt) {
          List<Stmt> targets = new ArrayList<>();
          for (int k = 0; k < t.length; k++) {
            int target = rnd.nextInt(n);
            t[k] = stmts[target];
            targets.add(stmts[target]);
            leaders.add(target);
          }
          successorMap.put((BranchingStmt) s, targets);
        } else if (s instanceof FallsThroughStmt) {
          t[0] = stmts[i + 1];
        }
        if ((s.branches() || !(s instanceof FallsThroughStmt)) && i + 1 < n) leaders.add(i + 1);
      }

      // non-overlapping traps per type, handler must be a handler stmt of that type
      List<Trap> traps = new ArrayList<>();
      for (ClassType type : types) {
        List<Integer> hs = new ArrayList<>();
        handlerType.forEach(
            (i, t) -> {
              if (t == type) hs.add(i);
            });
        if (hs.isEmpty() || n < 3) continue;
        int pos = 0;
        int ranges = 1 + rnd.nextInt(2);
        for (int k = 0; k < ranges && pos < n - 2; k++) {
          int b = pos + rnd.nextInt(Math.max(1, (n - 1 - pos) / 2));
          int e = b + 1 + rnd.nextInt(Math.max(1, n - 1 - b));
          if (e > n - 1) e = n - 1;
          if (e <= b) break;
          Stmt h = stmts[hs.get(rnd.nextInt(hs.size()))];
          traps.add(new Trap(type, stmts[b], stmts[e], h));
          for (int i = b; i < e; i++) model.exc.get(stmts[i]).put(type, h);
          leaders.add(b);
          leaders.add(e);
          pos = e;
        }
      }
      // block boundaries where the trap coverage changes are already leaders (b, e)

      List<List<Stmt>> blocks = new ArrayList<>();
      List<Integer> ls = new ArrayList<>(leaders);
      for (int k = 0; k < ls.size(); k++) {
        int from = ls.get(k);
        int to = k + 1 < ls.size() ? ls.get(k + 1) : n;
        blocks.add(new ArrayList<>(Arrays.asList(stmts).subList(from, to)));
      }
      StringBuilder sb = new StringBuilder("program:");
      for (List<Stmt> b : blocks) {
        sb.append("\n    [");
        for (Stmt s : b) sb.append(n(s)).append(" ");
        sb.append("]");
      }
      for (Trap t : traps) {
        sb.append("\n    trap ")
            .append(t.getExceptionType())
            .append(" ")
            .append(n(t.getBeginStmt()))
            .append(" .. ")
            .append(n(t.getEndStmt()))
            .append(" -> ")
            .append(n(t.getHandlerStmt()));
      }
      successorMap.forEach(
          (k, v) -> {
            sb.append("\n    ").append(n(k)).append(" -> ");
            v.forEach(x -> sb.append(n(x)).append(" "));
          });
      log.add(sb.toString());
      g.initializeWith(blocks, successorMap, traps);
    }

    // ---------------------------------------------------------------------------------------
    // ops

    <T> T pick(List<T> l) {
      return l.isEmpty() ? null : l.get(rnd.nextInt(l.size()));
    }

    ClassType handlerType(Stmt h) {
      return (ClassType) ((JIdentityStmt) h).getRightOp().getType();
    }

    int idx(Stmt s) {
      n(s);
      for (int k = 0; k < names.size(); k++) if (names.get(k) == s) return k;
      throw new IllegalStateException();
    }

    List<Stmt> nodes() {
      List<Stmt> l = new ArrayList<>(model.succ.keySet());
      l.sort(Comparator.comparingInt(this::idx)); // deterministic order
      return l;
    }

    List<Stmt> nodesWhere(java.util.function.Predicate<Stmt> p) {
      List<Stmt> l = new ArrayList<>();
      for (Stmt s : nodes()) if (p.test(s)) l.add(s);
      return l;
    }

    List<Stmt> handlerStmts() {
      return nodesWhere(CfgTestKit::isHandler);
    }

    Map<ClassType, Stmt> randomTrapMap(Stmt like) {
      int r = rnd.nextInt(3);
      if (r == 0) return new HashMap<>(model.exc.get(like));
      Map<ClassType, Stmt> m = new HashMap<>();
      if (r == 1) {
        for (Stmt h : handlerStmts()) {
          if (rnd.nextBoolean()) m.put(handlerType(h), h);
        }
      }
      return m;
    }

    List<FallsThroughStmt> newNops() {
      int k = 1 + rnd.nextInt(3);
      List<FallsThroughStmt> l = new ArrayList<>();
      for (int i = 0; i < k; i++) {
        JNopStmt s = nop();
        n(s);
        l.add(s);
      }
      return l;
    }

    String names(List<? extends Stmt> l) {
      StringBuilder sb = new StringBuilder("[");
      for (Stmt s : l) sb.append(n(s)).append(" ");
      return sb.append("]").toString();
    }

    String trapStr(Map<ClassType, Stmt> m) {
      StringBuilder sb = new StringBuilder("{");
      m.forEach((k, v) -> sb.append(k).append("->").append(n(v)).append(" "));
      return sb.append("}").toString();
    }

    void chainInModel(List<FallsThroughStmt> l, Map<ClassType, Stmt> traps, Stmt last) {
      for (int i = 0; i < l.size(); i++) {
        model.add(l.get(i));
        model.exc.get(l.get(i)).putAll(traps);
      }
      for (int i = 0; i + 1 < l.size(); i++) model.succ.get(l.get(i))[0] = l.get(i + 1);
      model.succ.get(l.get(l.size() - 1))[0] = last;
    }

    boolean apply(Op op) {
      switch (op) {
        case INSERT_BEFORE:
          {
            Stmt e = pick(nodesWhere(s -> !isHandler(s)));
            if (e == null) return false;
            List<FallsThroughStmt> l = newNops();
            Map<ClassType, Stmt> traps = randomTrapMap(e);
            log.add("insertBefore(" + n(e) + ", " + names(l) + ", " + trapStr(traps) + ")");
            g.insertBefore(e, l, traps);
            model.redirectAll(e, l.get(0));
            chainInModel(l, traps, e);
            if (model.start == e) model.start = l.get(0);
            return true;
          }
        case INSERT_AFTER:
          {
            Stmt e =
                pick(
                    nodesWhere(
                        s ->
                            s instanceof FallsThroughStmt
                                && !(s instanceof JIfStmt)
                                && model.succ.get(s)[0] != null));
            if (e == null) return false;
            List<FallsThroughStmt> l = newNops();
            Map<ClassType, Stmt> traps = randomTrapMap(e);
            log.add("insertAfter(" + n(e) + ", " + names(l) + ", " + trapStr(traps) + ")");
            g.insertAfter(e, l, traps);
            Stmt old = model.succ.get(e)[0];
            model.succ.get(e)[0] = l.get(0);
            chainInModel(l, traps, old);
            return true;
          }
        case REMOVE_KEEP_FLOW:
          {
            Stmt s =
                pick(
                    nodesWhere(
                        x ->
                            x instanceof JNopStmt
                                && model.succ.get(x)[0] != null
                                && model.succ.get(x)[0] != x));
            if (s == null) return false;
            log.add("removeNode(" + n(s) + ", keepFlow)");
            g.removeNode(s, true);
            Stmt succ = model.succ.get(s)[0];
            model.remove(s);
            model.redirectAll(s, succ);
            if (model.start == s) model.start = succ;
            return true;
          }
        case REPLACE_NOP:
          {
            Stmt s = pick(nodesWhere(x -> x instanceof JNopStmt));
            if (s == null) return false;
            JNopStmt neu = nop();
            log.add("replaceNode(" + n(s) + ", " + n(neu) + ")");
            g.replaceNode(s, neu);
            model.rename(s, neu);
            return true;
          }
        case REPLACE_GOTO_BY_NOP:
          {
            Stmt s =
                pick(
                    nodesWhere(
                        x ->
                            x instanceof JGotoStmt
                                && model.succ.get(x)[0] != null
                                && model.canFallThrough(x, model.succ.get(x)[0])));
            if (s == null) return false;
            JNopStmt neu = nop();
            log.add("replaceNode(" + n(s) + ", " + n(neu) + ")");
            g.replaceNode(s, neu);
            model.rename(s, neu);
            return true;
          }
        case REPLACE_IF_BY_NOP:
          {
            Stmt s = pick(nodesWhere(x -> x instanceof JIfStmt && model.succ.get(x)[0] != null));
            if (s == null) return false;
            JNopStmt neu = nop();
            log.add("replaceNode(" + n(s) + ", " + n(neu) + ")");
            g.replaceNode(s, neu);
            model.rename(s, neu);
            return true;
          }
        case REPLACE_NOP_BY_GOTO:
          {
            Stmt s = pick(nodesWhere(x -> x instanceof JNopStmt && model.succ.get(x)[0] != null));
            if (s == null) return false;
            JGotoStmt neu = gotoStmt();
            log.add("replaceNode(" + n(s) + ", " + n(neu) + ")");
            g.replaceNode(s, neu);
            model.rename(s, neu);
            return true;
          }
        case REPLACE_BRANCH_SAME_ARITY:
          {
            Stmt s = pick(nodesWhere(x -> x instanceof BranchingStmt));
            if (s == null) return false;
            Stmt neu;
            if (s instanceof JGotoStmt) neu = gotoStmt();
            else if (s instanceof JIfStmt) neu = ifStmt();
            else neu = switchStmt(((JSwitchStmt) s).getValueCount() - 1);
            log.add("replaceNode(" + n(s) + ", " + n(neu) + ")");
            g.replaceNode(s, neu);
            model.rename(s, neu);
            return true;
          }
        case REDIRECT_BRANCH:
          {
            Stmt b = pick(nodesWhere(x -> x instanceof BranchingStmt));
            if (b == null) return false;
            Stmt[] ts = model.succ.get(b);
            List<Integer> nonNull = new ArrayList<>();
            for (int i = b instanceof JIfStmt ? 1 : 0; i < ts.length; i++)
              if (ts[i] != null && !(b instanceof JIfStmt && ts[i] == ts[0])) nonNull.add(i);
            if (nonNull.isEmpty()) return false;
            Stmt old = ts[pick(nonNull)];
            Stmt target = pick(nodes());
            List<Integer> expectedIdx = new ArrayList<>();
            for (int i = 0; i < ts.length; i++) if (ts[i] == old) expectedIdx.add(i);
            log.add("redirect(" + n(b) + ": " + n(old) + " => " + n(target) + ")");
            List<Integer> idx = g.removeEdge(b, old);
            assertEquals(expectedIdx, idx, "seed=" + seed + " removeEdge indices");
            for (int i : idx) {
              ts[i] = null;
            }
            check("after removeEdge(" + n(b) + ", " + n(old) + ")");
            for (int i : idx) {
              g.putEdge((BranchingStmt) b, i, target);
              ts[i] = target;
            }
            return true;
          }
        case SET_EDGES:
          {
            Stmt b = pick(nodesWhere(x -> x instanceof BranchingStmt));
            if (b == null) return false;
            List<Stmt> targets = new ArrayList<>();
            List<Stmt> all = nodes();
            for (int i = 0; i < b.getExpectedSuccessorCount(); i++) targets.add(pick(all));
            if (b instanceof JIfStmt) {
              // keep the fall-through edge, only the branch target is free
              if (model.succ.get(b)[0] == null) return false;
              targets.set(0, model.succ.get(b)[0]);
            }
            log.add("setEdges(" + n(b) + ", " + names(targets) + ")");
            g.setEdges((BranchingStmt) b, targets);
            Stmt[] ts = model.succ.get(b);
            for (int i = 0; i < ts.length; i++) ts[i] = targets.get(i);
            return true;
          }
        case ADD_EXC:
          {
            Stmt s = pick(nodes());
            Stmt h = pick(handlerStmts());
            if (s == null || h == null) return false;
            ClassType type = handlerType(h);
            log.add("addExceptionalEdge(" + n(s) + ", " + type + ", " + n(h) + ")");
            g.addExceptionalEdge(s, type, h);
            model.exc.get(s).put(type, h);
            return true;
          }
        case REMOVE_EXC:
          {
            Stmt s = pick(nodesWhere(x -> !model.exc.get(x).isEmpty()));
            if (s == null) return false;
            ClassType type = pick(new ArrayList<>(model.exc.get(s).keySet()));
            log.add("removeExceptionalEdge(" + n(s) + ", " + type + ")");
            g.removeExceptionalEdge(s, type);
            model.exc.get(s).remove(type);
            return true;
          }
        case CLEAR_EXC:
          {
            Stmt s = pick(nodesWhere(x -> !model.exc.get(x).isEmpty()));
            if (s == null) return false;
            log.add("clearExceptionalEdges(" + n(s) + ")");
            g.clearExceptionalEdges(s);
            model.exc.get(s).clear();
            return true;
          }
        case REMOVE_FLOW_ALL:
          {
            Stmt s = pick(nodesWhere(x -> !model.exc.get(x).isEmpty()));
            if (s == null) return false;
            ClassType type = pick(new ArrayList<>(model.exc.get(s).keySet()));
            Stmt h = model.exc.get(s).get(type);
            log.add("removeExceptionalFlowFromAllBlocks(" + type + ", " + n(h) + ")");
            g.removeExceptionalFlowFromAllBlocks(type, h);
            for (Map<ClassType, Stmt> m : model.exc.values()) m.remove(type, h);
            return true;
          }
        default:
          throw new IllegalStateException();
      }
    }
  }
}
