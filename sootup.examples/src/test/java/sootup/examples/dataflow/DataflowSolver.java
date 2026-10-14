package sootup.examples.dataflow;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import sootup.core.graph.ControlFlowGraph;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;

/**
 * A generic worklist-based solver for intra-procedural dataflow analyses.
 *
 * <p>Works for both forward and backward analyses: the direction is determined by {@link
 * DataflowAnalysis#isForward()}. You do not need to subclass this; just supply a concrete {@link
 * DataflowAnalysis} and call {@link #solve(Body)}.
 *
 * @param <F> the type of an analysis fact
 */
public class DataflowSolver<F> {

  private final DataflowAnalysis<F> analysis;
  private final Map<Stmt, F> inFacts = new HashMap<>();
  private final Map<Stmt, F> outFacts = new HashMap<>();

  public DataflowSolver(DataflowAnalysis<F> analysis) {
    this.analysis = analysis;
  }

  /** Run the analysis on {@code body} and populate the in/out fact maps. */
  public void solve(Body body) {
    ControlFlowGraph<?> cfg = body.getControlFlowGraph();
    // --8<-- [start:init]
    Collection<Stmt> nodes = cfg.getNodes();

    // Initialise every node with the "bottom" initial fact.
    for (Stmt node : nodes) {
      inFacts.put(node, analysis.newInitialFact());
      outFacts.put(node, analysis.newInitialFact());
    }

    // The boundary node gets the boundary fact (not the initial fact).
    if (analysis.isForward()) {
      Stmt entry = cfg.getStartingStmt();
      inFacts.put(entry, analysis.newBoundaryFact(body));
    } else {
      for (Stmt tail : cfg.getTails()) {
        outFacts.put(tail, analysis.newBoundaryFact(body));
      }
    }
    // --8<-- [end:init]

    // --8<-- [start:worklist-loop]
    // Seed the worklist with all nodes (conservative: assume every fact may change).
    Deque<Stmt> worklist = new ArrayDeque<>(nodes);

    while (!worklist.isEmpty()) {
      Stmt node = worklist.poll();

      if (analysis.isForward()) {
        // Forward: meet all predecessor OUT facts into this node's IN fact.
        F in = inFacts.get(node);
        for (Stmt pred : cfg.predecessors(node)) {
          analysis.meetInto(outFacts.get(pred), in);
        }
        // Apply the transfer function; if OUT changed, schedule successors.
        if (analysis.transferNode(node, in, outFacts.get(node))) {
          worklist.addAll(cfg.successors(node));
        }
      } else {
        // Backward: meet all successor IN facts into this node's OUT fact.
        F out = outFacts.get(node);
        List<Stmt> succs = cfg.successors(node);
        succs.addAll(cfg.exceptionalSuccessors(node).values());
        for (Stmt succ : succs) {
          analysis.meetInto(inFacts.get(succ), out);
        }
        // Apply the transfer function; if IN changed, schedule predecessors.
        if (analysis.transferNode(node, inFacts.get(node), out)) {
          worklist.addAll(cfg.predecessors(node));
        }
      }
    }
    // --8<-- [end:worklist-loop]
  }

  // --8<-- [start:result]
  /** The analysis fact that holds just before {@code stmt} executes. */
  public F getInFact(Stmt stmt) {
    return inFacts.get(stmt);
  }

  /** The analysis fact that holds just after {@code stmt} executes. */
  public F getOutFact(Stmt stmt) {
    return outFacts.get(stmt);
  }
  // --8<-- [end:result]
}
