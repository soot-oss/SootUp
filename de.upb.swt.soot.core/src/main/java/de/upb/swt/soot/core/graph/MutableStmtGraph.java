package de.upb.swt.soot.core.graph;

import de.upb.swt.soot.core.jimple.basic.Trap;
import de.upb.swt.soot.core.jimple.common.stmt.BranchingStmt;
import de.upb.swt.soot.core.jimple.common.stmt.JIfStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.jimple.javabytecode.stmt.JSwitchStmt;
import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * graph structure which keeps node and edge insertion order to store information about successive
 * stmts in edges. Ordered edges are needed because, this stores the target information of {@link
 * BranchingStmt}s so that in conditional branches (e.g. JSwicthStmt or JIfStmt ) we can associate
 * the i-th item with the i-th branch case. In a StmtGraph it is not allowed to have unconnected
 * Nodes.
 *
 * <pre>
 *  Stmt stmt1, stmt2;
 *  ...
 *  MutableStmtGraph graph = new MutableStmtGraph();
 *  graph.setEntryPoint(stmt1);
 *  graph.addNode(stmt1);
 *  graph.addNode(stmt2);
 *  graph.putEdge(stmt1, stmt2);
 * </pre>
 *
 * @author Markus Schmidt
 */
public class MutableStmtGraph extends StmtGraph {

  @Nonnull protected final ArrayList<List<Stmt>> predecessors;
  @Nonnull protected final ArrayList<List<Stmt>> successors;
  @Nonnull protected final Map<Stmt, Integer> stmtToIdx;

  @Nullable protected Stmt startingStmt;
  @Nonnull protected List<Trap> traps = Collections.emptyList();

  /** creates an empty instance of MutableStmtGraph */
  public MutableStmtGraph() {
    predecessors = new ArrayList<>();
    successors = new ArrayList<>();
    stmtToIdx = new HashMap<>();
  }

  /** creates a mutable copy(!) of originalStmtGraph */
  public MutableStmtGraph(@Nonnull StmtGraph originalStmtGraph) {
    setStartingStmt(originalStmtGraph.getStartingStmt());

    final Set<Stmt> nodes = originalStmtGraph.nodes();
    final int nodeSize = nodes.size();
    predecessors = new ArrayList<>(nodeSize);
    successors = new ArrayList<>(nodeSize);
    stmtToIdx = new HashMap<>(nodeSize);

    for (Stmt node : nodes) {
      int idx = addNode(node);

      final List<Stmt> pred = originalStmtGraph.predecessors(node);
      predecessors.set(idx, new ArrayList<>(pred));

      final List<Stmt> succ = originalStmtGraph.successors(node);
      successors.set(idx, new ArrayList<>(succ));
    }
  }

  public StmtGraph asUnmodifiableStmtGraph() {
    return new ForwardingStmtGraph(this);
  }

  public void setStartingStmt(@Nonnull Stmt firstStmt) {
    this.startingStmt = firstStmt;
  }

  public void setTraps(@Nonnull List<Trap> traps) {
    this.traps = traps;
  }

  @Override
  @Nonnull
  public List<Trap> getTraps() {
    return traps;
  }

  public Stmt getStartingStmt() {
    return startingStmt;
  }

  public int addNode(@Nonnull Stmt node) {
    final int idx = stmtToIdx.size();
    stmtToIdx.put(node, idx);

    predecessors.add(
        idx, new ArrayList<>(1)); // [ms] wastes an entry if its the TrapHandler or firststmt

    final int calculatedSuccessorSize;
    if (node instanceof JSwitchStmt) {
      calculatedSuccessorSize = ((JSwitchStmt) node).getValueCount();
    } else if (node instanceof JIfStmt) {
      calculatedSuccessorSize = 2;
    } else {
      calculatedSuccessorSize = 1;
    }

    successors.add(idx, new ArrayList<>(calculatedSuccessorSize));

    return idx;
  }

  public void removeNode(@Nonnull Stmt node) {
    int nodeIdx = existsNodeOrThrow(node);
    final List<Stmt> preds = predecessors.get(nodeIdx);
    if (preds != null) {
      preds.forEach(pred -> successors.get(existsNodeOrThrow(pred)).remove(node));
      predecessors.remove(nodeIdx);
    }
    final List<Stmt> succs = successors.get(nodeIdx);
    if (succs != null) {
      succs.forEach(succ -> predecessors.get(existsNodeOrThrow(succ)).remove(node));
      successors.remove(nodeIdx);
    }
    stmtToIdx.remove(nodeIdx);
  }

  private int existsNodeOrThrow(@Nonnull Stmt node) {
    final Integer idx = stmtToIdx.get(node);
    if (idx == null) {
      addNode(node);
      throw new RuntimeException("'" + node + "' is currently not a Node in this StmtGraph.");
    }
    return idx;
  }

  public boolean containsNode(@Nonnull Stmt node) {
    return stmtToIdx.containsKey(node);
  }

  public void removeEdge(@Nonnull Stmt from, @Nonnull Stmt to) {
    int fromIdx = existsNodeOrThrow(from);
    int toIdx = existsNodeOrThrow(to);

    final List<Stmt> pred = predecessors.get(toIdx);
    if (pred != null) {
      pred.remove(from);
      if (degree(to) == 0) {
        stmtToIdx.remove(to);
      }
    }
    final List<Stmt> succ = successors.get(fromIdx);
    if (succ != null) {
      succ.remove(to);
      if (degree(from) == 0) {
        stmtToIdx.remove(from);
      }
    }
  }

  public void setEdges(@Nonnull Stmt from, @Nonnull List<Stmt> targets) {
    int fromIdx = existsNodeOrThrow(from);
    targets.forEach(
        node -> {
          if (!containsNode(node)) {
            if (from == node) {
              throw new RuntimeException("A Stmt can't flow to itself.");
            }
            addNode(node);
          }
        });

    // cleanup existing edges before replacing it with the new list with successors
    successors(from).forEach(succ -> predecessors.get(existsNodeOrThrow(succ)).remove(from));

    for (Stmt target : targets) {
      predecessors.get(existsNodeOrThrow(target)).add(from);
    }

    successors.add(fromIdx, targets);
  }

  public void putEdge(@Nonnull Stmt from, @Nonnull Stmt to) {
    if (from == to) {
      throw new RuntimeException("A Stmt can't flow to itself.");
    }

    Integer fromIdx = stmtToIdx.get(from);
    if (fromIdx == null) {
      fromIdx = addNode(from);
    }

    Integer toIdx = stmtToIdx.get(to);
    if (toIdx == null) {
      toIdx = addNode(to);
    }

    predecessors.get(toIdx).add(from);
    successors.get(fromIdx).add(to);
  }

  @Override
  @Nonnull
  public Set<Stmt> nodes() {
    return Collections.unmodifiableSet(stmtToIdx.keySet());
  }

  @Nonnull
  public List<Stmt> adjacentNodes(@Nonnull Stmt node) {
    int nodeIdx = existsNodeOrThrow(node);
    final List<Stmt> pred = predecessors.get(nodeIdx);
    final List<Stmt> succ = successors.get(nodeIdx);
    final int predSize = (pred == null ? 0 : pred.size());
    final int succSize = (succ == null ? 0 : succ.size());
    final int degree = predSize + succSize;
    if (degree > 0) {
      final List<Stmt> list = new ArrayList<>(degree);
      if (predSize > 0) {
        list.addAll(pred);
      }
      if (succSize > 0) {
        list.addAll(succ);
      }
      return list;
    } else {
      return Collections.emptyList();
    }
  }

  @Override
  @Nonnull
  public List<Stmt> predecessors(@Nonnull Stmt node) {
    int nodeIdx = existsNodeOrThrow(node);
    final List<Stmt> stmts = predecessors.get(nodeIdx);
    if (stmts == null) {
      return Collections.emptyList();
    }
    return Collections.unmodifiableList(stmts);
  }

  @Override
  @Nonnull
  public List<Stmt> successors(@Nonnull Stmt node) {
    int nodeIdx = existsNodeOrThrow(node);
    final List<Stmt> stmts = successors.get(nodeIdx);
    if (stmts == null) {
      return Collections.emptyList();
    }
    return Collections.unmodifiableList(stmts);
  }

  @Override
  public int degree(@Nonnull Stmt node) {
    return inDegree(node) + outDegree(node);
  }

  @Override
  public int inDegree(@Nonnull Stmt node) {
    int nodeIdx = existsNodeOrThrow(node);
    final List<Stmt> stmts = predecessors.get(nodeIdx);
    return stmts == null ? 0 : stmts.size();
  }

  @Override
  public int outDegree(@Nonnull Stmt node) {
    int nodeIdx = existsNodeOrThrow(node);
    final List<Stmt> stmts = successors.get(nodeIdx);
    return stmts == null ? 0 : stmts.size();
  }

  @Override
  public boolean hasEdgeConnecting(@Nonnull Stmt from, @Nonnull Stmt to) {
    int fromIdx = existsNodeOrThrow(from);
    final List<Stmt> stmts = successors.get(fromIdx);
    return stmts != null && stmts.contains(to);
  }
}
