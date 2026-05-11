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

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.NonNull;
import org.jgrapht.Graph;
import sootup.core.signatures.FieldSignature;
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
 * queries are supported:
 *
 * <ul>
 *   <li>{@link #reachingObjects(Node)} — the set of {@link AllocationNode}s a node may point to.
 *   <li>{@link #aliases(Node)} — the set of other PAG nodes whose points-to set intersects the
 *       query node's.
 *   <li>{@link #reachingTypes(Node)} — the set of run-time {@link Type}s induced by the points-to
 *       set.
 * </ul>
 */
public class PointsToAnalysis {

  private final Solver solver;
  private final Map<Node, Set<AllocationNode>> pointsTo = new HashMap<>();
  private final Map<HeapKey, Set<AllocationNode>> heap = new HashMap<>();

  private PointsToAnalysis(Solver solver) {
    this.solver = solver;
  }

  /**
   * Builds a {@code PointsToAnalysis} over the PAG of {@code solver}. If the PAG has not yet been
   * built (i.e. has no vertices), {@link Solver#solve()} is invoked first.
   */
  public static PointsToAnalysis fromSolver(@NonNull Solver solver) {
    if (solver.getPag().getDelegate().vertexSet().isEmpty()) {
      solver.solve();
    }
    PointsToAnalysis pta = new PointsToAnalysis(solver);
    pta.propagate();
    return pta;
  }

  /**
   * Returns the set of allocation sites that {@code node} may point to.
   *
   * <p>For an {@link AllocationNode} the result is the singleton containing that allocation. For an
   * {@link InstanceFieldRefNode} {@code b.f} the result is the union of {@code heap[o, f]} over
   * each {@code o} in {@code reachingObjects(b)}.
   */
  public Set<AllocationNode> reachingObjects(@NonNull Node node) {
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

  /**
   * Returns the set of nodes (other than {@code node} itself) whose points-to set shares at least
   * one allocation with {@code reachingObjects(node)} — i.e. nodes that may refer to a common
   * run-time object. Only PAG vertices are considered as candidates.
   */
  public Set<Node> aliases(@NonNull Node node) {
    Set<AllocationNode> targets = reachingObjects(node);
    if (targets.isEmpty()) return Collections.emptySet();
    Set<Node> result = new LinkedHashSet<>();
    for (Node candidate : solver.getPag().getDelegate().vertexSet()) {
      if (candidate.equals(node)) continue;
      Set<AllocationNode> candPts = reachingObjects(candidate);
      if (candPts.isEmpty()) continue;
      if (!Collections.disjoint(targets, candPts)) {
        result.add(candidate);
      }
    }
    return Collections.unmodifiableSet(result);
  }

  /** Returns the set of run-time types that {@code node}'s points-to set may induce. */
  public Set<Type> reachingTypes(@NonNull Node node) {
    Set<AllocationNode> objects = reachingObjects(node);
    if (objects.isEmpty()) return Collections.emptySet();
    Set<Type> types = new LinkedHashSet<>();
    for (AllocationNode o : objects) types.add(o.getType());
    return Collections.unmodifiableSet(types);
  }

  private void propagate() {
    Graph<Node, PAGEdge> g = solver.getPag().getDelegate();

    // Seed via ALLOCATION edges: pts(target) |= {source}
    for (PAGEdge e : g.edgeSet()) {
      if (e.getEdgeType() == PAGEdge.EdgeType.ALLOCATION
          && g.getEdgeSource(e) instanceof AllocationNode a) {
        addPts(g.getEdgeTarget(e), a);
      }
    }

    boolean changed = true;
    while (changed) {
      changed = false;
      for (PAGEdge e : g.edgeSet()) {
        Node src = g.getEdgeSource(e);
        Node tgt = g.getEdgeTarget(e);
        switch (e.getEdgeType()) {
          case ASSIGNMENT -> changed |= unionInto(pointsTo, tgt, ptsOf(src));
          case STORE -> {
            // src -> ifr : for each o in pts(ifr.base), heap[o, ifr.field] |= pts(src)
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
            // ifr -> tgt : for each o in pts(ifr.base), pts(tgt) |= heap[o, ifr.field]
            if (src instanceof InstanceFieldRefNode ifr) {
              for (AllocationNode o : ptsOf(ifr.getBase())) {
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
