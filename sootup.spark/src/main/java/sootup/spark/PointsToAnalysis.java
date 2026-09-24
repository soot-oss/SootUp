package sootup.spark;

/*-
 * #%L
 * SootUp
 * %%
 * Copyright (C) 2002-2026 Ondrej Lhotak, Kadiray Karakaya and others
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.NonNull;
import org.graph4j.Digraph;
import org.graph4j.Edge;
import org.graph4j.EdgeIterator;
import sootup.core.jimple.common.Value;
import sootup.core.signatures.FieldSignature;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.Type;
import sootup.spark.node.AllocationNode;
import sootup.spark.node.InstanceFieldRefNode;
import sootup.spark.node.Node;

/**
 * Client-facing pointer analysis API over a SPARK {@link PAG}.
 *
 * <p>Construct via {@link #fromSolver(Solver)}. The factory triggers {@link Solver#solve()} (if the
 * PAG has not yet been built) and then runs an Andersen-style fixed-point propagation over the
 * allocation, assignment, store, and load edges of the PAG. After construction the following
 * queries are supported, all keyed by Jimple {@link Value} (a local, field ref, parameter ref,
 * {@code this}, etc.) plus the {@link MethodSignature} of the method that contains the value:
 *
 * <ul>
 *   <li>{@link #reachingObjects(Value, MethodSignature)} — the set of {@link AllocationNode}s the
 *       value may point to.
 *   <li>{@link #aliases(Value, MethodSignature)} — the set of PAG vertices whose points-to set
 *       intersects the query value's.
 *   <li>{@link #reachingTypes(Value, MethodSignature)} — the set of run-time {@link Type}s induced
 *       by the points-to set.
 * </ul>
 *
 * <p>Values that the PAG does not model (numeric constants, arithmetic expressions, etc.) yield an
 * empty result. The raw PAG-level points-to map is also exposed via {@link #getPointsToMap()} for
 * diagnostic purposes.
 */
public class PointsToAnalysis {

  private final Solver solver;
  private final NodeFactory nodeFactory;
  private final Map<Node, Set<AllocationNode>> pointsTo = new HashMap<>();
  private final Map<HeapKey, Set<AllocationNode>> heap = new HashMap<>();

  private PointsToAnalysis(Solver solver) {
    this.solver = solver;
    this.nodeFactory = new NodeFactory(solver.getSparkOptions());
  }

  /**
   * Builds a {@code PointsToAnalysis} over the PAG of {@code solver}. If the PAG has not yet been
   * built (i.e. has no vertices), {@link Solver#solve()} is invoked first.
   *
   * <p>In OTF mode the points-to and heap state has already been computed incrementally during
   * solving, so we adopt it directly instead of re-running the batch fixed-point.
   */
  static PointsToAnalysis fromSolver(@NonNull Solver solver) {
    if (solver.getPag().getDelegate().numVertices() == 0) {
      solver.solve();
    }
    PointsToAnalysis pta = new PointsToAnalysis(solver);
    if (solver.getSparkOptions().isOnFlyCallGraph() && solver.getIncrementalAnalysis() != null) {
      pta.adoptFrom(solver.getIncrementalAnalysis());
    } else {
      pta.propagate();
    }
    return pta;
  }

  private void adoptFrom(IncrementalPointsToAnalysis ipta) {
    pointsTo.putAll(ipta.getPointsTo());
    for (Map.Entry<IncrementalPointsToAnalysis.HeapKey, Set<AllocationNode>> e :
        ipta.getHeap().entrySet()) {
      heap.put(new HeapKey(e.getKey().obj(), e.getKey().field()), e.getValue());
    }
  }

  /**
   * Returns the set of allocation sites that {@code value} may point to within {@code
   * containingMethodSig}.
   *
   * <p>For an allocation expression the result is the singleton containing that allocation. For an
   * instance field reference {@code b.f} the result is the union of {@code heap[o, f]} over each
   * {@code o} in {@code reachingObjects(b)}. Values not modeled by the PAG yield an empty set.
   */
  public Set<AllocationNode> reachingObjects(
      @NonNull Value value, @NonNull MethodSignature containingMethodSig) {
    Optional<Node> node = nodeFactory.createNode(value, containingMethodSig);
    return node.map(this::reachingObjectsOfNode).orElse(Collections.emptySet());
  }

  /**
   * Returns the set of PAG vertices (other than the one for {@code value} itself) whose points-to
   * set shares at least one allocation with {@code reachingObjects(value, containingMethodSig)} —
   * i.e. nodes that may refer to a common run-time object.
   */
  public Set<Node> aliases(@NonNull Value value, @NonNull MethodSignature containingMethodSig) {
    Optional<Node> node = nodeFactory.createNode(value, containingMethodSig);
    return node.map(this::aliasesOfNode).orElse(Collections.emptySet());
  }

  /**
   * Returns the set of run-time types that the points-to set of {@code value} may induce within
   * {@code containingMethodSig}.
   */
  public Set<Type> reachingTypes(
      @NonNull Value value, @NonNull MethodSignature containingMethodSig) {
    Optional<Node> node = nodeFactory.createNode(value, containingMethodSig);
    return node.map(this::reachingTypesOfNode).orElse(Collections.emptySet());
  }

  /**
   * Read-only view of the raw PAG-level points-to map. Intended for diagnostics and debugging that
   * needs to walk every PAG vertex; prefer the {@link Value}-based query methods for analysis.
   */
  public Map<Node, Set<AllocationNode>> getPointsToMap() {
    return Collections.unmodifiableMap(pointsTo);
  }

  private Set<AllocationNode> reachingObjectsOfNode(Node node) {
    if (node instanceof AllocationNode a) {
      return Collections.singleton(a);
    }
    if (node instanceof InstanceFieldRefNode ifr) {
      Set<AllocationNode> bases = ptsOf(ifr.getBase());
      if (bases.isEmpty()) return Collections.emptySet();
      Set<AllocationNode> result = new LinkedHashSet<>();
      for (AllocationNode o : bases) {
        Set<AllocationNode> stored = heap.get(new HeapKey(o, ifr.getField()));
        if (stored != null) result.addAll(stored);
      }
      return Collections.unmodifiableSet(result);
    }
    return Collections.unmodifiableSet(ptsOf(node));
  }

  private Set<Node> aliasesOfNode(Node node) {
    Set<AllocationNode> targets = reachingObjectsOfNode(node);
    if (targets.isEmpty()) return Collections.emptySet();
    Set<Node> result = new LinkedHashSet<>();
    Digraph<Node, PAGEdge> pagGraph = solver.getPag().getDelegate();
    for (int vIdx : pagGraph.vertices()) {
      Node candidate = pagGraph.getVertexLabel(vIdx);
      if (candidate.equals(node)) continue;
      Set<AllocationNode> candPts = reachingObjectsOfNode(candidate);
      if (candPts.isEmpty()) continue;
      if (!Collections.disjoint(targets, candPts)) {
        result.add(candidate);
      }
    }
    return Collections.unmodifiableSet(result);
  }

  private Set<Type> reachingTypesOfNode(Node node) {
    Set<AllocationNode> objects = reachingObjectsOfNode(node);
    if (objects.isEmpty()) return Collections.emptySet();
    Set<Type> types = new LinkedHashSet<>();
    for (AllocationNode o : objects) types.add(o.getType());
    return Collections.unmodifiableSet(types);
  }

  private void propagate() {
    Digraph<Node, PAGEdge> g = solver.getPag().getDelegate();

    // Seed via ALLOCATION edges: pts(target) |= {source}
    EdgeIterator<PAGEdge> seedIt = g.edgeIterator();
    while (seedIt.hasNext()) {
      Edge<PAGEdge> edge = seedIt.next();
      PAGEdge e = edge.label();
      if (e.getEdgeType() == PAGEdge.EdgeType.ALLOCATION
          && g.getVertexLabel(edge.source()) instanceof AllocationNode a) {
        addPts(g.getVertexLabel(edge.target()), a);
      }
    }

    boolean changed = true;
    while (changed) {
      changed = false;
      EdgeIterator<PAGEdge> it = g.edgeIterator();
      while (it.hasNext()) {
        Edge<PAGEdge> edge = it.next();
        PAGEdge e = edge.label();
        Node src = g.getVertexLabel(edge.source());
        Node tgt = g.getVertexLabel(edge.target());
        switch (e.getEdgeType()) {
          case ASSIGNMENT -> changed |= unionInto(pointsTo, tgt, ptsOf(src));
          case STORE -> {
            if (tgt instanceof InstanceFieldRefNode ifr) {
              Set<AllocationNode> srcPts = ptsOf(src);
              if (!srcPts.isEmpty()) {
                for (AllocationNode o : ptsOf(ifr.getBase())) {
                  changed |= unionIntoHeap(o, ifr.getField(), srcPts);
                }
              }
            }
          }
          case LOAD -> {
            if (src instanceof InstanceFieldRefNode ifr) {
              // Snapshot: pts(ifr.getBase()) and pts(tgt) can be the same live set (e.g.
              // "x = x.f"), and unionInto below grows pts(tgt), so iterating the map-backed
              // set directly would throw ConcurrentModificationException.
              for (AllocationNode o : new ArrayList<>(ptsOf(ifr.getBase()))) {
                Set<AllocationNode> stored = heap.get(new HeapKey(o, ifr.getField()));
                if (stored != null && !stored.isEmpty()) {
                  changed |= unionInto(pointsTo, tgt, stored);
                }
              }
            }
          }
          case ALLOCATION -> {
            // already seeded
          }
        }
      }
    }
  }

  private Set<AllocationNode> ptsOf(Node n) {
    Set<AllocationNode> s = pointsTo.get(n);
    return s == null ? Collections.emptySet() : s;
  }

  private boolean addPts(Node n, AllocationNode a) {
    return pointsTo.computeIfAbsent(n, k -> new LinkedHashSet<>()).add(a);
  }

  private static boolean unionInto(
      Map<Node, Set<AllocationNode>> map, Node n, Set<AllocationNode> incoming) {
    if (incoming.isEmpty()) return false;
    return map.computeIfAbsent(n, k -> new LinkedHashSet<>()).addAll(incoming);
  }

  private boolean unionIntoHeap(
      AllocationNode obj, FieldSignature f, Set<AllocationNode> incoming) {
    if (incoming.isEmpty()) return false;
    return heap.computeIfAbsent(new HeapKey(obj, f), k -> new LinkedHashSet<>()).addAll(incoming);
  }

  private record HeapKey(AllocationNode obj, FieldSignature field) {
    private HeapKey {
      Objects.requireNonNull(obj);
      Objects.requireNonNull(field);
    }
  }
}
