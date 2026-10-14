package sootup.examples.dataflow;

import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;

/**
 * The five elements that define any intra-procedural dataflow analysis.
 *
 * <p>Implement this interface, pass it to {@link DataflowSolver}, and the solver handles the
 * worklist iteration. You only describe <em>what</em> the analysis computes; the solver handles
 * <em>how</em> the iteration converges.
 *
 * @param <F> the type of an analysis fact (e.g. {@code Set<Local>} or {@code Map<Local,Value>})
 */
// --8<-- [start:interface-full]
public interface DataflowAnalysis<F> {

  /** True for a forward analysis (entry → exit); false for backward (exit → entry). */
  boolean isForward();

  /**
   * The fact at the boundary node: the method entry for forward analyses, the method exit for
   * backward analyses. Represents what the analysis knows before examining any code.
   */
  F newBoundaryFact(Body body);

  /**
   * The fact used to initialise every non-boundary node at the start of the analysis. Typically the
   * "bottom" element of the lattice (e.g. empty set, empty map).
   */
  F newInitialFact();

  /**
   * Meet {@code fact} into {@code target} (i.e. {@code target = target ⊓ fact}). Mutates {@code
   * target} in place so the solver can reuse the same object across iterations at a join point,
   * avoiding repeated allocation.
   */
  void meetInto(F fact, F target);

  /**
   * Apply the transfer function for {@code stmt}: read {@code in}, write {@code out}.
   *
   * @return true if {@code out} changed (so the solver knows whether to propagate further)
   */
  boolean transferNode(Stmt stmt, F in, F out);
}
// --8<-- [end:interface-full]
