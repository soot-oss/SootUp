package sootup.examples.constprop;

import java.util.HashMap;
import java.util.Map;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.Value;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.expr.AbstractBinopExpr;
import sootup.core.jimple.common.expr.JAddExpr;
import sootup.core.jimple.common.expr.JDivExpr;
import sootup.core.jimple.common.expr.JMulExpr;
import sootup.core.jimple.common.expr.JSubExpr;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.examples.dataflow.DataflowAnalysis;
import sootup.examples.dataflow.DataflowSolver;

/**
 * Constant Propagation — a forward may-analysis over a three-valued lattice.
 *
 * <p>For each local variable at each program point, we track one of three lattice values:
 *
 * <ul>
 *   <li>{@link sootup.examples.constprop.Value#getUndef()} — not yet seen (bottom of lattice)
 *   <li>{@link sootup.examples.constprop.Value#makeConstant(int)} — known to hold exactly this
 *       integer
 *   <li>{@link sootup.examples.constprop.Value#getNAC()} — Not A Constant; could be anything (top)
 * </ul>
 *
 * <p>Scope: only {@code int}-typed assignments. Method calls, field accesses, and non-integer
 * expressions conservatively produce {@code NAC}.
 */
public class ConstantPropagation
    implements DataflowAnalysis<Map<Local, sootup.examples.constprop.Value>> {

  @Override
  public boolean isForward() {
    return true;
  }

  // --8<-- [start:boundary]
  @Override
  public Map<Local, sootup.examples.constprop.Value> newBoundaryFact(Body body) {
    // Parameters are unknown at the call site → NAC.
    // All other locals are absent from the map, which means UNDEF.
    Map<Local, sootup.examples.constprop.Value> fact = new HashMap<>();
    for (Local param : body.getParameterLocals()) {
      fact.put(param, sootup.examples.constprop.Value.getNAC());
    }
    return fact;
  }

  @Override
  public Map<Local, sootup.examples.constprop.Value> newInitialFact() {
    return new HashMap<>(); // empty map = all variables UNDEF
  }

  // --8<-- [end:boundary]

  // --8<-- [start:meet-value]
  @Override
  public void meetInto(
      Map<Local, sootup.examples.constprop.Value> fact,
      Map<Local, sootup.examples.constprop.Value> target) {
    // For every variable in fact, meet its value into target.
    for (Map.Entry<Local, sootup.examples.constprop.Value> entry : fact.entrySet()) {
      Local local = entry.getKey();
      sootup.examples.constprop.Value incoming = entry.getValue();
      sootup.examples.constprop.Value existing =
          target.getOrDefault(local, sootup.examples.constprop.Value.getUndef());
      target.put(local, existing.meet(incoming));
    }
  }

  // --8<-- [end:meet-value]

  // --8<-- [start:transfer]
  @Override
  public boolean transferNode(
      Stmt stmt,
      Map<Local, sootup.examples.constprop.Value> in,
      Map<Local, sootup.examples.constprop.Value> out) {

    // Copy IN into OUT as the default (identity for non-assignment statements).
    Map<Local, sootup.examples.constprop.Value> newOut = new HashMap<>(in);

    if (stmt instanceof JAssignStmt) {
      JAssignStmt assign = (JAssignStmt) stmt;
      Value lhs = assign.getLeftOp();
      Value rhs = assign.getRightOp();

      if (lhs instanceof Local) {
        Local lhsLocal = (Local) lhs;
        sootup.examples.constprop.Value result = evaluate(rhs, in);
        newOut.put(lhsLocal, result);
      }
    }
    // (Other statement types: identity — OUT = IN, already handled by the copy above.)

    if (newOut.equals(out)) {
      return false;
    }
    out.clear();
    out.putAll(newOut);
    return true;
  }

  // --8<-- [end:transfer]

  /** Evaluate {@code expr} given the current facts {@code in}. */
  private sootup.examples.constprop.Value evaluate(
      Value expr, Map<Local, sootup.examples.constprop.Value> in) {
    if (expr instanceof IntConstant) {
      return sootup.examples.constprop.Value.makeConstant(((IntConstant) expr).getValue());
    }
    if (expr instanceof Local) {
      return in.getOrDefault((Local) expr, sootup.examples.constprop.Value.getUndef());
    }
    if (expr instanceof AbstractBinopExpr) {
      AbstractBinopExpr binop = (AbstractBinopExpr) expr;
      sootup.examples.constprop.Value v1 = evaluate(binop.getOp1(), in);
      sootup.examples.constprop.Value v2 = evaluate(binop.getOp2(), in);
      if (!v1.isConstant() || !v2.isConstant()) {
        return sootup.examples.constprop.Value.getNAC();
      }
      int a = v1.getConstant(), b = v2.getConstant();
      if (expr instanceof JAddExpr) return sootup.examples.constprop.Value.makeConstant(a + b);
      if (expr instanceof JSubExpr) return sootup.examples.constprop.Value.makeConstant(a - b);
      if (expr instanceof JMulExpr) return sootup.examples.constprop.Value.makeConstant(a * b);
      if (expr instanceof JDivExpr) {
        if (b == 0) return sootup.examples.constprop.Value.getUndef(); // division by zero
        return sootup.examples.constprop.Value.makeConstant(a / b);
      }
    }
    return sootup.examples.constprop.Value.getNAC(); // conservative fallback
  }

  // --8<-- [start:result-query]
  /** Convenience wrapper: run constant propagation on {@code body} and return the solver. */
  public static DataflowSolver<Map<Local, sootup.examples.constprop.Value>> analyze(Body body) {
    DataflowSolver<Map<Local, sootup.examples.constprop.Value>> solver =
        new DataflowSolver<>(new ConstantPropagation());
    solver.solve(body);
    return solver;
  }
  // --8<-- [end:result-query]
}
