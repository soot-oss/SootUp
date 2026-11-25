package sootup.core.graph;

import static sootup.core.graph.CfgTestKit.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import sootup.core.jimple.common.stmt.FallsThroughStmt;
import sootup.core.jimple.common.stmt.JReturnVoidStmt;
import sootup.core.jimple.common.stmt.Stmt;

/**
 * Manual scaling benchmark: prints time for n and 2n. ratio ~2 = linear, ~4 = quadratic. Run with
 * {@code -Djunit.jupiter.conditions.deactivate=org.junit.*DisabledCondition}.
 */
@Disabled("manual benchmark")
public class MutableBlockControlFlowGraphScalingBenchmark {

  static final int N = 20_000;

  static List<FallsThroughStmt> nops(int n) {
    List<FallsThroughStmt> l = new ArrayList<>(n);
    for (int i = 0; i < n; i++) l.add(nop());
    return l;
  }

  static MutableBlockControlFlowGraph straightLine(List<FallsThroughStmt> s) {
    MutableBlockControlFlowGraph g = new MutableBlockControlFlowGraph();
    g.setStartingStmt(s.get(0));
    for (int i = 0; i + 1 < s.size(); i++) g.putEdge(s.get(i), s.get(i + 1));
    JReturnVoidStmt r = ret();
    g.putEdge(s.get(s.size() - 1), r);
    return g;
  }

  static void measure(String name, IntFunction<Runnable> setup) {
    long[] t = new long[2];
    for (int warm = 0; warm < 2; warm++) {
      for (int k = 0; k < 2; k++) {
        Runnable r = setup.apply(N << k);
        long t0 = System.nanoTime();
        r.run();
        t[k] = System.nanoTime() - t0;
      }
    }
    System.out.printf(
        "%-45s n=%6d %8.1f ms | 2n %8.1f ms | ratio %.2f%n",
        name, N, t[0] / 1e6, t[1] / 1e6, (double) t[1] / t[0]);
  }

  @Test
  void scaling() {
    measure(
        "build straight line via putEdge",
        n -> {
          List<FallsThroughStmt> s = nops(n);
          return () -> straightLine(s);
        });
    measure(
        "removeNode(head, keepFlow) until empty",
        n -> {
          List<FallsThroughStmt> s = nops(n);
          MutableBlockControlFlowGraph g = straightLine(s);
          return () -> {
            for (Stmt x : s) g.removeNode(x, true);
          };
        });
    measure(
        "removeNode(tail-1, keepFlow) bottom-up",
        n -> {
          List<FallsThroughStmt> s = nops(n);
          MutableBlockControlFlowGraph g = straightLine(s);
          return () -> {
            for (int i = s.size() - 1; i >= 0; i--) g.removeNode(s.get(i), true);
          };
        });
    measure(
        "insertBefore(middle) n/10 times",
        n -> {
          List<FallsThroughStmt> s = nops(n);
          MutableBlockControlFlowGraph g = straightLine(s);
          return () -> {
            for (int i = 0; i < n / 10; i++) g.insertBefore(s.get(n / 2), nop());
          };
        });
    measure(
        "addExceptionalEdge on every 50th stmt",
        n -> {
          List<FallsThroughStmt> s = nops(n);
          MutableBlockControlFlowGraph g = straightLine(s);
          Stmt h = handler(EX_A);
          return () -> {
            for (int i = 1; i < n - 1; i += 50) g.addExceptionalEdge(s.get(i), EX_A, h);
          };
        });
    measure(
        "successors()+predecessors() of every stmt x5",
        n -> {
          List<FallsThroughStmt> s = nops(n);
          MutableBlockControlFlowGraph g = straightLine(s);
          for (int i = 1; i < n - 1; i += 50) g.addExceptionalEdge(s.get(i), EX_A, handler(EX_A));
          return () -> {
            for (int k = 0; k < 5; k++)
              for (Stmt x : s) {
                g.successors(x);
                g.predecessors(x);
                g.exceptionalSuccessors(x);
              }
          };
        });
    measure(
        "replaceNode + getStmts() n/20 times",
        n -> {
          List<FallsThroughStmt> s = nops(n);
          MutableBlockControlFlowGraph g = straightLine(s);
          for (int i = 1; i < n - 1; i += 50) g.addExceptionalEdge(s.get(i), EX_A, handler(EX_A));
          return () -> {
            for (int i = 0; i < n / 20; i++) {
              Stmt old = s.get(i * 20 + 2);
              FallsThroughStmt neu = nop();
              g.replaceNode(old, neu);
              s.set(i * 20 + 2, neu);
              g.getStmts();
            }
          };
        });
  }
}
