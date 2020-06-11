package de.upb.swt.soot.core.graph;

import com.google.common.collect.ImmutableSet;
import com.google.common.graph.*;
import com.google.common.primitives.Ints;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import java.util.*;
import javax.annotation.Nonnull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * trivial Graph structure which keeps edge order
 *
 * @author Markus Schmidt
 */
public class StmtGraph implements MutableGraph<Stmt> {

  protected final HashMap<Stmt, List<Stmt>> predecessors = new HashMap<>();
  protected final Map<Stmt, List<Stmt>> successors = new HashMap<>();
  protected final List<Stmt> stmtList = new ArrayList<>();

  public StmtGraph() {}

  public boolean addNode(@Nonnull Stmt node) {
    boolean modify = !stmtList.contains(node);
    // if(modify) {
    stmtList.add(node);
    // }
    return modify;
  }

  @Override
  public boolean removeNode(Stmt node) {
    stmtList.remove(node);
    predecessors.remove(node);
    successors.remove(node);
    return true;
  }

  @Override
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
    /*if (!stmtList.contains(u)) {
      throw new IllegalArgumentException(
          "first parameter node " + u + " is not in the list of nodes.");
    }
    if (!stmtList.contains(v)) {
      throw new IllegalArgumentException(
          "second parameter node " + v + " is not in the list of nodes.");
    }
    */
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

  @Override
  @Nonnull
  public Set<Stmt> nodes() {
    return ImmutableSet.copyOf(stmtList);
  }

  @Override
  public Set<EndpointPair<Stmt>> edges() {
    return new AbstractSet<EndpointPair<Stmt>>() {
      @Override
      public Iterator<EndpointPair<Stmt>> iterator() {
        return StmtGraphIterator.of(StmtGraph.this);
      }

      @Override
      public int size() {
        return Ints.saturatedCast(stmtList.size());
      }

      @Override
      public boolean remove(Object o) {
        throw new UnsupportedOperationException();
      }

      // Mostly safe: We check contains(u) before calling successors(u), so we perform unsafe
      // operations only in weird cases like checking for an EndpointPair<ArrayList> in a
      // Graph<LinkedList>.
      @SuppressWarnings("unchecked")
      @Override
      public boolean contains(@Nullable Object obj) {
        if (!(obj instanceof EndpointPair)) {
          return false;
        }
        EndpointPair<?> endpointPair = (EndpointPair<?>) obj;
        final Set<Stmt> successors = successors((Stmt) endpointPair.nodeU());
        return successors != null && successors.contains(endpointPair.nodeV());
      }
    };
  }

  @Override
  public boolean isDirected() {
    return true;
  }

  @Override
  public boolean allowsSelfLoops() {
    // is this really possible?! possible optimization to turn it off
    return true;
  }

  @Override
  public ElementOrder<Stmt> nodeOrder() {
    return ElementOrder.insertion();
  }

  @Override
  public Set<Stmt> adjacentNodes(@Nonnull Stmt node) {
    final HashSet<Stmt> set = new HashSet<>();
    set.addAll(predecessors(node));
    set.addAll(successors(node));
    return set;
  }

  @Override
  public Set<Stmt> predecessors(@Nonnull Stmt node) {
    // TODO set property is already maintained -> more performant datastructure
    final List<Stmt> set = predecessors.get(node);
    if (set == null) {
      return Collections.emptySet();
    }
    return new LinkedHashSet<>(set);
  }

  @Override
  public Set<Stmt> successors(@Nonnull Stmt node) {
    // TODO set property is already maintained -> more performant datastructure
    final List<Stmt> set = successors.get(node);
    if (set == null) {
      return Collections.emptySet();
    }
    return new LinkedHashSet<>(set);
  }

  @Override
  @Nonnull
  public Set<EndpointPair<Stmt>> incidentEdges(@Nonnull Stmt node) {
    final Set<Stmt> predecessors = predecessors(node);
    final Set<Stmt> successors = successors(node);
    final HashSet<EndpointPair<Stmt>> incidents =
        new HashSet<>(predecessors.size() + successors.size());
    predecessors.forEach(pred -> incidents.add(EndpointPair.ordered(pred, node)));
    successors.forEach(succ -> incidents.add(EndpointPair.ordered(node, succ)));
    return incidents;
  }

  @Override
  public int degree(@Nonnull Stmt node) {
    return inDegree(node) + outDegree(node);
  }

  @Override
  public int inDegree(@Nonnull Stmt node) {
    return predecessors.get(node).size();
  }

  @Override
  public int outDegree(@Nonnull Stmt node) {
    return successors.get(node).size();
  }

  @Override
  public boolean hasEdgeConnecting(@Nonnull Stmt nodeU, @Nonnull Stmt nodeV) {
    final List<Stmt> stmts = successors.get(nodeU);
    return stmts != null && stmts.contains(nodeV);
  }
}
