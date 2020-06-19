package de.upb.swt.soot.core.graph;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import java.util.*;
import javax.annotation.Nonnull;

/**
 * trivial Graph structure which keeps node and edge insertion order
 *
 * @author Markus Schmidt
 */
public class MutableStmtGraph extends StmtGraph {

  protected final Map<Stmt, List<Stmt>> predecessors = new HashMap<>();
  protected final Map<Stmt, List<Stmt>> successors = new HashMap<>();
  protected final List<Stmt> stmtList = new ArrayList<>();

  public MutableStmtGraph() {}

  public static MutableStmtGraph copyOf(StmtGraph stmtGraph) {
    final MutableStmtGraph graph = new MutableStmtGraph();

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
    final int indexOf = stmtList.indexOf(node);
    if (indexOf > -1) {
      stmtList.remove(indexOf);
      predecessors.remove(node);
      successors.remove(node);
      return true;
    }
    return false;
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

    final List<Stmt> pred = predecessors.computeIfAbsent(v, key -> new ArrayList<>());
    pred.add(u);

    final List<Stmt> succ = successors.computeIfAbsent(u, key -> new ArrayList<>());
    succ.add(v);

    return true;
  }

  @Override
  @Nonnull
  public Set<Stmt> nodes() {
    return ImmutableSet.copyOf(stmtList);
  }

  @Override
  @Nonnull
  public List<Stmt> adjacentNodes(@Nonnull Stmt node) {
    final List<Stmt> predecessors = predecessors(node);
    final List<Stmt> successors = successors(node);
    final ArrayList<Stmt> set =
        new ArrayList<>(
            (predecessors == null ? 0 : predecessors.size())
                + (successors == null ? 0 : successors.size()));
    set.addAll(predecessors);
    set.addAll(successors);
    return set;
  }

  @Override
  @Nonnull
  public List<Stmt> predecessors(@Nonnull Stmt node) {
    final List<Stmt> stmts = predecessors.get(node);
    if (stmts == null) {
      return Collections.emptyList();
    }
    return ImmutableList.copyOf(stmts);
  }

  @Override
  @Nonnull
  public List<Stmt> successors(@Nonnull Stmt node) {
    final List<Stmt> stmts = successors.get(node);
    if (stmts == null) {
      return Collections.emptyList();
    }
    return ImmutableList.copyOf(stmts);
  }

  @Override
  public int degree(@Nonnull Stmt node) {
    return inDegree(node) + outDegree(node);
  }

  @Override
  public int inDegree(@Nonnull Stmt node) {
    final List<Stmt> stmts = predecessors.get(node);
    return stmts == null ? 0 : stmts.size();
  }

  @Override
  public int outDegree(@Nonnull Stmt node) {
    final List<Stmt> stmts = successors.get(node);
    return stmts == null ? 0 : stmts.size();
  }

  @Override
  public boolean hasEdgeConnecting(@Nonnull Stmt nodeU, @Nonnull Stmt nodeV) {
    final List<Stmt> stmts = successors.get(nodeU);
    return stmts != null && stmts.contains(nodeV);
  }
}
