package de.upb.swt.soot.core.graph;

import com.google.common.collect.ImmutableSet;
import com.google.common.graph.*;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import java.util.*;
import javax.annotation.Nonnull;

/**
 * trivial Graph structure which keeps node and edge insertion order
 *
 * @author Markus Schmidt
 */
public class StmtGraph {

  protected final Map<Stmt, List<Stmt>> predecessors = new HashMap<>();
  protected final Map<Stmt, List<Stmt>> successors = new HashMap<>();
  protected final List<Stmt> stmtList = new ArrayList<>();

  public StmtGraph() {}

  public static StmtGraph copyOf(StmtGraph stmtGraph) {
    final StmtGraph graph = new StmtGraph();

    for (Stmt node : stmtGraph.nodes()) {
      graph.addNode(node);
      stmtGraph.successors(node).forEach(target -> graph.putEdge(node, target));
    }

    return graph;
  }

  public boolean addNode(@Nonnull Stmt node) {
    // [ms] contains is expensive!
    boolean modify = !stmtList.contains(node);
    if (modify) {
      stmtList.add(node);
    }
    return modify;
  }

  public boolean removeNode(Stmt node) {
    stmtList.remove(node);
    predecessors.remove(node);
    successors.remove(node);
    return true;
  }

  public boolean removeEdge(Stmt nodeU, Stmt nodeV) {
    final List<Stmt> pred = predecessors.get(nodeV);
    boolean modified = false;
    if (pred != null) {
      pred.remove(nodeU);
      modified = true;
    }
    final List<Stmt> succ = successors.get(nodeU);
    if (succ != null) {
      succ.remove(nodeV);
      modified = true;
    }
    return modified;
  }

  public boolean putEdge(@Nonnull Stmt u, @Nonnull Stmt v) {
    // maintain set property
    if (hasEdgeConnecting(u, v)) {
      return false;
    }
    final List<Stmt> pred = predecessors.computeIfAbsent(v, key -> new ArrayList<>());
    pred.add(u);

    final List<Stmt> succ = successors.computeIfAbsent(u, key -> new ArrayList<>());
    succ.add(v);

    return true;
  }

  @Nonnull
  public Set<Stmt> nodes() {
    return ImmutableSet.copyOf(stmtList);
  }

  public boolean isDirected() {
    return true;
  }

  public boolean allowsSelfLoops() {
    return false;
  }

  @Nonnull
  public Set<Stmt> adjacentNodes(@Nonnull Stmt node) {
    final HashSet<Stmt> set = new HashSet<>();
    set.addAll(predecessors(node));
    set.addAll(successors(node));
    return set;
  }

  @Nonnull
  public Set<Stmt> predecessors(@Nonnull Stmt node) {
    final List<Stmt> set = predecessors.get(node);
    if (set == null) {
      return Collections.emptySet();
    }
    return new LinkedHashSet<>(set);
  }

  @Nonnull
  public Set<Stmt> successors(@Nonnull Stmt node) {
    final List<Stmt> set = successors.get(node);
    if (set == null) {
      return Collections.emptySet();
    }
    return new LinkedHashSet<>(set);
  }

  public int degree(@Nonnull Stmt node) {
    return inDegree(node) + outDegree(node);
  }

  public int inDegree(@Nonnull Stmt node) {
    return predecessors.get(node).size();
  }

  public int outDegree(@Nonnull Stmt node) {
    return successors.get(node).size();
  }

  public boolean hasEdgeConnecting(@Nonnull Stmt nodeU, @Nonnull Stmt nodeV) {
    final List<Stmt> stmts = successors.get(nodeU);
    return stmts != null && stmts.contains(nodeV);
  }
}
