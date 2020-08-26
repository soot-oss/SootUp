package de.upb.swt.soot.core.graph;

import de.upb.swt.soot.core.jimple.basic.Trap;
import de.upb.swt.soot.core.jimple.common.stmt.*;
import de.upb.swt.soot.core.jimple.javabytecode.stmt.JSwitchStmt;
import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** @author Markus Schmidt */
// [ms] possible performance improvement: on instantiation assign: Collection.singleTonList()
// directly and not on demand -> change type of sucessors/predecessors to List<Stmt> this removes
// additional checks in successor()/predecessor()
public class ImmutableStmtGraph extends StmtGraph {
  @Nonnull private final Object[] successors;
  @Nonnull private final Object[] predecessors;
  @Nonnull private final Map<Stmt, Integer> nodeToIndex;

  @Nullable private final Stmt startingStmt;
  @Nonnull private final List<Trap> traps;

  /** creates an immutable copy of the given stmtGraph. */
  private ImmutableStmtGraph(StmtGraph originalStmtGraph) {
    final Set<Stmt> nodes = originalStmtGraph.nodes();
    final int nodeSize = nodes.size();
    nodeToIndex = new HashMap<>(nodeSize);
    predecessors = new Object[nodeSize];
    successors = new Object[nodeSize];

    startingStmt = originalStmtGraph.getStartingStmt();
    traps = new ArrayList<>(originalStmtGraph.getTraps());

    int idx = 0;
    for (Stmt node : nodes) {
      nodeToIndex.put(node, idx);

      final List<Stmt> pred = originalStmtGraph.predecessors(node);
      switch (pred.size()) {
        case 0:
          predecessors[idx] = Collections.emptyList();
          break;
        case 1:
          predecessors[idx] = pred.get(0);
          break;
        default:
          predecessors[idx] = Collections.unmodifiableList(new ArrayList<>(pred));
      }

      final List<Stmt> succ = originalStmtGraph.successors(node);
      switch (succ.size()) {
        case 0:
          successors[idx] = Collections.emptyList();
          break;
        case 1:
          successors[idx] = succ.get(0);
          break;
        default:
          successors[idx] = Collections.unmodifiableList(new ArrayList<>(succ));
      }
      idx++;
    }
  }

  /**
   * creates an immutable copy of the given stmtGraph if necessary and validates the starting Stmt
   */
  public static ImmutableStmtGraph copyOf(StmtGraph stmtGraph) {
    if (stmtGraph instanceof ImmutableStmtGraph) {
      return (ImmutableStmtGraph) stmtGraph;
    }

    if (stmtGraph.nodes().size() > 0) {
      final Stmt startingStmt = stmtGraph.getStartingStmt();
      if (startingStmt == null) {
        throw new RuntimeException("The starting Stmt must be set.");
      }
      if (!stmtGraph.nodes().contains(startingStmt)) {
        throw new RuntimeException(
            "The starting Stmt '" + startingStmt + "' must exist in the StmtGraph.");
      }
    }
    return new ImmutableStmtGraph(stmtGraph);
  }

  @Override
  public Stmt getStartingStmt() {
    return startingStmt;
  }

  @Nonnull
  @Override
  public Set<Stmt> nodes() {
    return Collections.unmodifiableSet(nodeToIndex.keySet());
  }

  @Override
  public boolean containsNode(@Nonnull Stmt node) {
    return nodeToIndex.containsKey(node);
  }

  @Nonnull
  @Override
  public List<Stmt> predecessors(@Nonnull Stmt node) {
    final Integer idx = nodeToIndex.get(node);
    if (predecessors[idx] instanceof Stmt) {
      final List<Stmt> stmts = Collections.singletonList((Stmt) predecessors[idx]);
      predecessors[idx] = stmts;
      return stmts;
    }
    return (List<Stmt>) predecessors[idx];
  }

  @Nonnull
  @Override
  public List<Stmt> successors(@Nonnull Stmt node) {
    final Integer idx = nodeToIndex.get(node);
    if (successors[idx] instanceof Stmt) {
      final List<Stmt> stmts = Collections.singletonList((Stmt) successors[idx]);
      successors[idx] = stmts;
      return stmts;
    }
    return (List<Stmt>) successors[idx];
  }

  @Override
  public int inDegree(@Nonnull Stmt node) {
    return predecessors(node).size();
  }

  @Override
  public int outDegree(@Nonnull Stmt node) {
    // TODO: [ms]: check if this is faster: successors( node ).size();
    return successorsOfANode(node);
  }

  private int successorsOfANode(@Nonnull Stmt node) {
    if (node instanceof JIfStmt) {
      return 2;
    } else if (node instanceof JSwitchStmt) {
      return ((JSwitchStmt) node).getValueCount();
    } else if (node instanceof JReturnVoidStmt
        || node instanceof JReturnStmt
        || node instanceof JThrowStmt) {
      return 0;
    } else {
      return 1;
    }
  }

  @Override
  public boolean hasEdgeConnecting(@Nonnull Stmt source, @Nonnull Stmt target) {
    return successors(source).contains(target);
  }

  @Nonnull
  @Override
  public List<Trap> getTraps() {
    return Collections.unmodifiableList(traps);
  }
}
