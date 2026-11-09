package sootup.examples.liveness;

import java.util.HashSet;
import java.util.Set;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.Value;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.examples.dataflow.DataflowAnalysis;

/**
 * Live Variable Analysis — a backward may-analysis.
 *
 * <p>A local variable is <em>live</em> before statement S if there exists at least one execution
 * path from S on which the variable is used before being redefined. Knowing which variables are
 * live is useful for register allocation, dead code elimination, and detecting
 * use-before-definition errors.
 *
 * <p>Transfer function (backward, so we compute IN from OUT):
 *
 * <pre>IN[S] = use(S) ∪ (OUT[S] − def(S))</pre>
 */
public class LiveVariableAnalysis implements DataflowAnalysis<Set<Local>> {

  @Override
  public boolean isForward() {
    return false; // backward: facts travel from exit toward entry
  }

  // --8<-- [start:boundary]
  @Override
  public Set<Local> newBoundaryFact(Body body) {
    // Nothing is live after the method returns.
    return new HashSet<>();
  }

  @Override
  public Set<Local> newInitialFact() {
    // Conservative start: assume nothing is live (we add facts as we discover uses).
    return new HashSet<>();
  }

  // --8<-- [end:boundary]

  // --8<-- [start:meet]
  @Override
  public void meetInto(Set<Local> fact, Set<Local> target) {
    // May analysis: a variable is live if it is live on at least one path → union.
    target.addAll(fact);
  }

  // --8<-- [end:meet]

  // --8<-- [start:transfer]
  @Override
  public boolean transferNode(Stmt stmt, Set<Local> in, Set<Local> out) {
    // We compute IN from OUT (backward direction).
    // IN[S] = use(S) ∪ (OUT[S] − def(S))
    Set<Local> newIn = new HashSet<>(out);

    if (stmt instanceof JAssignStmt) {
      JAssignStmt assign = (JAssignStmt) stmt;
      // Kill: remove the defined variable (it is no longer "needed" upstream).
      if (assign.getLeftOp() instanceof Local) {
        newIn.remove(assign.getLeftOp());
      }
      // Gen: add any locals used on the right-hand side.
      addLocals(assign.getRightOp(), newIn);
    } else {
      // For all other statements, gen the locals they use.
      for (Value use : stmt.getUses()) {
        if (use instanceof Local) {
          newIn.add((Local) use);
        }
      }
    }

    if (newIn.equals(in)) {
      return false; // unchanged — solver can stop propagating backward from here
    }
    in.clear();
    in.addAll(newIn);
    return true;
  }

  // --8<-- [end:transfer]

  private void addLocals(Value value, Set<Local> target) {
    if (value instanceof Local) {
      target.add((Local) value);
    } else {
      // Recursively collect locals from composite expressions (e.g. a + b).
      for (Value use : value.getUses()) {
        addLocals(use, target);
      }
    }
  }

  // --8<-- [start:result-query]
  /**
   * Convenience wrapper: run this analysis on {@code body} and return a solver whose {@code
   * getInFact(stmt)} gives the live variables just before each statement.
   */
  public static sootup.examples.dataflow.DataflowSolver<Set<Local>> analyze(Body body) {
    sootup.examples.dataflow.DataflowSolver<Set<Local>> solver =
        new sootup.examples.dataflow.DataflowSolver<>(new LiveVariableAnalysis());
    solver.solve(body);
    return solver;
  }
  // --8<-- [end:result-query]
}
