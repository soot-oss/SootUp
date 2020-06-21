package de.upb.swt.soot.core.graph;

import de.upb.swt.soot.core.jimple.common.stmt.JIfStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.jimple.javabytecode.stmt.JSwitchStmt;
import java.util.*;
import javax.annotation.Nonnull;

/**
 * trivial Graph structure which keeps node and edge insertion order
 *
 * @author Markus Schmidt
 */
public class MutableStmtGraph extends StmtGraph {

  @Nonnull protected final Map<Stmt, List<Stmt>> predecessors = new HashMap<>();
  @Nonnull protected final Map<Stmt, List<Stmt>> successors = new HashMap<>();
  @Nonnull protected final Set<Stmt> stmtList = new LinkedHashSet<>();

  public MutableStmtGraph() {}

  public static MutableStmtGraph copyOf(@Nonnull StmtGraph stmtGraph) {
    final MutableStmtGraph graph = new MutableStmtGraph();
    graph.setEntryPoint(stmtGraph.getEntryPoint());

    for (Stmt node : stmtGraph.nodes()) {
      graph.addNode(node);

      final List<Stmt> pred = stmtGraph.predecessors(node);
      graph.predecessors.put(node, new ArrayList(pred));

      final List<Stmt> succ = stmtGraph.successors(node);
      graph.successors.put(node, new ArrayList<>(succ));
    }

    return graph;
  }

  public StmtGraph asUnmodifiableStmtGraph() {
    StmtGraph ref = this;
    return new StmtGraph() {
      @Override
      public Stmt getEntryPoint() {
        return ref.getEntryPoint();
      }

      @Nonnull
      @Override
      public Set<Stmt> nodes() {
        return ref.nodes();
      }

      @Nonnull
      @Override
      public List<Stmt> adjacentNodes(@Nonnull Stmt node) {
        return ref.adjacentNodes(node);
      }

      @Nonnull
      @Override
      public List<Stmt> predecessors(@Nonnull Stmt node) {
        return ref.predecessors(node);
      }

      @Nonnull
      @Override
      public List<Stmt> successors(@Nonnull Stmt node) {
        return ref.successors(node);
      }

      @Override
      public int degree(@Nonnull Stmt node) {
        return ref.degree(node);
      }

      @Override
      public int inDegree(@Nonnull Stmt node) {
        return ref.inDegree(node);
      }

      @Override
      public int outDegree(@Nonnull Stmt node) {
        return ref.outDegree(node);
      }

      @Override
      public boolean hasEdgeConnecting(@Nonnull Stmt nodeU, @Nonnull Stmt nodeV) {
        return ref.hasEdgeConnecting(nodeU, nodeV);
      }
    };
  }

  public void setEntryPoint(@Nonnull Stmt firstStmt) {
    this.entrypoint = firstStmt;
  }

  public Stmt getEntryPoint() {
    return entrypoint;
  }

  public boolean addNode(@Nonnull Stmt node) {
    boolean modify = !stmtList.contains(node);
    if (modify) {
      stmtList.add(node);
    }
    return modify;
  }

  public boolean removeNode(@Nonnull Stmt node) {
    if (stmtList.remove(node)) {
      predecessors.get(node).forEach(pred -> successors.get(pred).remove(node));
      predecessors.remove(node);
      successors.get(node).forEach(succ -> predecessors.get(succ).remove(node));
      successors.remove(node);
      return true;
    }
    return false;
  }

  public boolean removeEdge(@Nonnull Stmt from, @Nonnull Stmt to) {

    if (!stmtList.contains(from) || !stmtList.contains(to)) {
      return false;
    }

    final List<Stmt> pred = predecessors.get(to);
    boolean modified = false;
    if (pred != null) {
      pred.remove(from);
      modified = true;
    }
    final List<Stmt> succ = successors.get(from);
    if (succ != null) {
      succ.remove(to);
      modified = true;
    }
    return modified;
  }

  public boolean putEdge(@Nonnull Stmt from, @Nonnull Stmt to) {

    if (!stmtList.contains(from) || !stmtList.contains(to)) {
      return false;
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

    if (predictedSuccessorSize != 1) {
      final List<Stmt> succ =
          successors.computeIfAbsent(from, key -> new ArrayList<>(predictedSuccessorSize));
      succ.add(to);
    } else {
      successors.put(from, Collections.singletonList(to));
    }

    return true;
  }

  @Override
  @Nonnull
  public Set<Stmt> nodes() {
    return Collections.unmodifiableSet(stmtList);
  }

  @Override
  @Nonnull
  public List<Stmt> adjacentNodes(@Nonnull Stmt node) {
    final List<Stmt> pred = predecessors.get(node);
    final List<Stmt> succ = successors.get(node);
    final ArrayList<Stmt> set =
        new ArrayList<>((pred == null ? 0 : pred.size()) + (succ == null ? 0 : succ.size()));
    set.addAll(pred);
    set.addAll(succ);
    return set;
  }

  @Override
  @Nonnull
  public List<Stmt> predecessors(@Nonnull Stmt node) {
    final List<Stmt> stmts = predecessors.get(node);
    if (stmts == null) {
      return Collections.emptyList();
    }
    return Collections.unmodifiableList(stmts);
  }

  @Override
  @Nonnull
  public List<Stmt> successors(@Nonnull Stmt node) {
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
    final List<Stmt> stmts = predecessors.get(node);
    return stmts == null ? 0 : stmts.size();
  }

  @Override
  public int outDegree(@Nonnull Stmt node) {
    final List<Stmt> stmts = successors.get(node);
    return stmts == null ? 0 : stmts.size();
  }

  @Override
  public boolean hasEdgeConnecting(@Nonnull Stmt from, @Nonnull Stmt to) {
    final List<Stmt> stmts = successors.get(from);
    return stmts != null && stmts.contains(to);
  }
}
