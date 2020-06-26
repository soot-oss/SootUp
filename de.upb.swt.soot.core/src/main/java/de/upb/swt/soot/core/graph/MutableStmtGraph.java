package de.upb.swt.soot.core.graph;

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
 * <p>TODO: where and how its used
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

  @Nonnull protected final Map<Stmt, List<Stmt>> predecessors = new HashMap<>();
  @Nonnull protected final Map<Stmt, List<Stmt>> successors = new HashMap<>();
  @Nonnull protected final Set<Stmt> stmtList = new LinkedHashSet<>();

  @Nullable protected Stmt startingStmt;

  public MutableStmtGraph() {}

  public static MutableStmtGraph copyOf(@Nonnull StmtGraph originalStmtGraph) {
    final MutableStmtGraph copiedGraph = new MutableStmtGraph();
    copiedGraph.setStartingStmt(originalStmtGraph.getStartingStmt());

    for (Stmt node : originalStmtGraph.nodes()) {
      copiedGraph.addNode(node);

      final List<Stmt> pred = originalStmtGraph.predecessors(node);
      copiedGraph.predecessors.put(node, new ArrayList<>(pred));

      final List<Stmt> succ = originalStmtGraph.successors(node);
      copiedGraph.successors.put(node, new ArrayList<>(succ));
    }

    return copiedGraph;
  }

  public StmtGraph asUnmodifiableStmtGraph() {
    StmtGraph graphRef = this;
    return new StmtGraph() {
      @Nonnull
      @Override
      public Stmt getStartingStmt() {
        return graphRef.getStartingStmt();
      }

      @Nonnull
      @Override
      public Set<Stmt> nodes() {
        return graphRef.nodes();
      }

      @Nonnull
      @Override
      public List<Stmt> predecessors(@Nonnull Stmt node) {
        return graphRef.predecessors(node);
      }

      @Nonnull
      @Override
      public List<Stmt> successors(@Nonnull Stmt node) {
        return graphRef.successors(node);
      }

      @Override
      public int degree(@Nonnull Stmt node) {
        return graphRef.degree(node);
      }

      @Override
      public int inDegree(@Nonnull Stmt node) {
        return graphRef.inDegree(node);
      }

      @Override
      public int outDegree(@Nonnull Stmt node) {
        return graphRef.outDegree(node);
      }

      @Override
      public boolean hasEdgeConnecting(@Nonnull Stmt nodeU, @Nonnull Stmt nodeV) {
        return graphRef.hasEdgeConnecting(nodeU, nodeV);
      }
    };
  }

  public void setStartingStmt(@Nonnull Stmt firstStmt) {
    this.startingStmt = firstStmt;
  }

  public Stmt getStartingStmt() {
    return startingStmt;
  }

  public void addNode(@Nonnull Stmt node) {
    stmtList.add(node);
  }

  public void removeNode(@Nonnull Stmt node) {
    if (stmtList.remove(node)) {
      predecessors
          .getOrDefault(node, Collections.emptyList())
          .forEach(pred -> successors.get(pred).remove(node));
      predecessors.remove(node);
      successors
          .getOrDefault(node, Collections.emptyList())
          .forEach(succ -> predecessors.get(succ).remove(node));
      successors.remove(node);
    }
  }

  private void existsNodeOrThrow(@Nonnull Stmt node) {
    if (!containsNode(node)) {
      addNode(node);
      throw new RuntimeException("'" + node + "' is currently not a Node in this StmtGraph.");
    }
  }

  public boolean containsNode(@Nonnull Stmt node) {
    return stmtList.contains(node);
  }

  public void removeEdge(@Nonnull Stmt from, @Nonnull Stmt to) {
    existsNodeOrThrow(from);
    existsNodeOrThrow(to);

    final List<Stmt> pred = predecessors.get(to);
    if (pred != null) {
      pred.remove(from);
      if (degree(to) == 0) {
        stmtList.remove(to);
      }
    }
    final List<Stmt> succ = successors.get(from);
    if (succ != null) {
      succ.remove(to);
      if (degree(from) == 0) {
        stmtList.remove(from);
      }
    }
  }

  public void setEdges(@Nonnull Stmt from, @Nonnull List<Stmt> targets) {
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
    successors(from).forEach(succ -> predecessors.get(succ).remove(from));

    for (Stmt target : targets) {
      final List<Stmt> pred = predecessors.computeIfAbsent(target, key -> new ArrayList<>(1));
      pred.add(from);
    }

    successors.put(from, targets);
  }

  public void putEdge(@Nonnull Stmt from, @Nonnull Stmt to) {
    if (from == to) {
      throw new RuntimeException("A Stmt can't flow to itself.");
    }
    if (!containsNode(from)) {
      addNode(from);
    }
    if (!containsNode(to)) {
      addNode(to);
    }

    final List<Stmt> pred = predecessors.computeIfAbsent(to, key -> new ArrayList<>(1));
    pred.add(from);

    final int predictedSuccessorSize;
    if (from instanceof JSwitchStmt) {
      predictedSuccessorSize = ((JSwitchStmt) from).getValueCount();
    } else if (from instanceof JIfStmt) {
      predictedSuccessorSize = 2;
    } else {
      predictedSuccessorSize = 1;
    }

    final List<Stmt> succ =
        successors.computeIfAbsent(from, key -> new ArrayList<>(predictedSuccessorSize));
    succ.add(to);
  }

  @Override
  @Nonnull
  public Set<Stmt> nodes() {
    return Collections.unmodifiableSet(stmtList);
  }

  @Nonnull
  public List<Stmt> adjacentNodes(@Nonnull Stmt node) {
    existsNodeOrThrow(node);
    final List<Stmt> pred = predecessors.get(node);
    final List<Stmt> succ = successors.get(node);
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
    existsNodeOrThrow(node);
    final List<Stmt> stmts = predecessors.get(node);
    if (stmts == null) {
      return Collections.emptyList();
    }
    return Collections.unmodifiableList(stmts);
  }

  @Override
  @Nonnull
  public List<Stmt> successors(@Nonnull Stmt node) {
    existsNodeOrThrow(node);
    final List<Stmt> stmts = successors.get(node);
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
    existsNodeOrThrow(node);
    final List<Stmt> stmts = predecessors.get(node);
    return stmts == null ? 0 : stmts.size();
  }

  @Override
  public int outDegree(@Nonnull Stmt node) {
    existsNodeOrThrow(node);
    final List<Stmt> stmts = successors.get(node);
    return stmts == null ? 0 : stmts.size();
  }

  @Override
  public boolean hasEdgeConnecting(@Nonnull Stmt from, @Nonnull Stmt to) {
    final List<Stmt> stmts = successors.get(from);
    return stmts != null && stmts.contains(to);
  }
}
