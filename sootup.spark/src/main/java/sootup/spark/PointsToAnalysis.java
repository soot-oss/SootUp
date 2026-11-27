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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.NonNull;
import org.graph4j.Digraph;
import org.graph4j.Edge;
import org.graph4j.EdgeIterator;
import sootup.core.jimple.common.Value;
import sootup.core.model.MemberResolution;
import sootup.core.model.SootField;
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
 *   <li>{@link #reachingObjects(Set, FieldSignature)} — the set of {@link AllocationNode}s that a
 *       field of given objects may point to.
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

  /**
   * With {@code SparkOptions#isSimplifySccs()}, the node that stands for each other node of its
   * strongly connected component of assignment edges. A node that is not a key stands for itself.
   */
  private final Map<Node, Node> representatives = new HashMap<>();

  private PointsToAnalysis(Solver solver) {
    this.solver = solver;
    this.nodeFactory = new NodeFactory(solver.getSparkOptions(), solver.getView());
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
   * Returns the set of allocation sites that the given field of the given objects may point to,
   * i.e. the union of {@code heap[o, field]} over each {@code o} in {@code objects}. Together with
   * {@link #reachingObjects(Value, MethodSignature)}, this follows chains of field accesses such as
   * {@code b.f.g}.
   *
   * <p>In field-based mode ({@code SparkOptions#isIgnoreBaseObjects()}) the field is one global
   * variable, so the result is what it points to, whatever the objects, as in Soot.
   *
   * @param objects the allocation sites whose field is queried
   * @param field the field, also as referenced through a subclass of the class that declares it
   * @return the allocation sites that the field may point to
   */
  public Set<AllocationNode> reachingObjects(
      @NonNull Set<AllocationNode> objects, @NonNull FieldSignature field) {
    FieldSignature declared =
        MemberResolution.resolveField(solver.getView(), field)
            .<FieldSignature>map(SootField::getSignature)
            .orElse(field);
    if (solver.getSparkOptions().isIgnoreBaseObjects()) {
      return new LinkedHashSet<>(ptsOf(nodeFactory.globalField(declared)));
    }
    Set<AllocationNode> result = new LinkedHashSet<>();
    for (AllocationNode o : objects) {
      Set<AllocationNode> stored = heap.get(new HeapKey(o, declared));
      if (stored != null) {
        result.addAll(stored);
      }
    }
    return result;
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
    if (solver.getSparkOptions().isSimplifySccs()) {
      collapseAssignmentCycles(g);
    }

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
          case ASSIGNMENT -> {
            if (!representative(src).equals(representative(tgt))) {
              changed |= unionInto(tgt, ptsOf(src));
            }
          }
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
                  changed |= unionInto(tgt, stored);
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
    // every node of a collapsed component shares the set of its representative
    representatives.forEach(
        (node, representative) -> {
          Set<AllocationNode> pts = pointsTo.get(representative);
          if (pts != null) {
            pointsTo.put(node, pts);
          }
        });
  }

  /**
   * Lets one node stand for each strongly connected component of assignment edges, like Soot's
   * {@code SCCCollapser}: the nodes of such a cycle always get the same points-to set, so it is
   * propagated once instead of around the cycle. Soot merges only nodes of the same type, unless
   * {@code ignore-types-for-sccs} is set, because it filters points-to sets by the declared type of
   * each node; this propagation does not filter them, so collapsing does not change any result.
   */
  private void collapseAssignmentCycles(Digraph<Node, PAGEdge> g) {
    Map<Integer, List<Integer>> successors = new HashMap<>();
    EdgeIterator<PAGEdge> it = g.edgeIterator();
    while (it.hasNext()) {
      Edge<PAGEdge> edge = it.next();
      if (edge.label().getEdgeType() == PAGEdge.EdgeType.ASSIGNMENT
          && edge.source() != edge.target()) {
        successors.computeIfAbsent(edge.source(), v -> new ArrayList<>()).add(edge.target());
      }
    }

    // Tarjan's algorithm, with an explicit stack of (vertex, next successor) frames
    Map<Integer, Integer> index = new HashMap<>();
    Map<Integer, Integer> lowLink = new HashMap<>();
    Deque<Integer> component = new ArrayDeque<>();
    Set<Integer> onComponentStack = new HashSet<>();
    Deque<int[]> frames = new ArrayDeque<>();
    for (int root : successors.keySet()) {
      if (index.containsKey(root)) {
        continue;
      }
      frames.push(enter(root, index, lowLink, component, onComponentStack));
      while (!frames.isEmpty()) {
        int[] frame = frames.peek();
        int v = frame[0];
        List<Integer> out = successors.getOrDefault(v, List.of());
        if (frame[1] < out.size()) {
          int w = out.get(frame[1]++);
          if (!index.containsKey(w)) {
            frames.push(enter(w, index, lowLink, component, onComponentStack));
          } else if (onComponentStack.contains(w)) {
            lowLink.put(v, Math.min(lowLink.get(v), index.get(w)));
          }
          continue;
        }
        frames.pop();
        if (!frames.isEmpty()) {
          int parent = frames.peek()[0];
          lowLink.put(parent, Math.min(lowLink.get(parent), lowLink.get(v)));
        }
        if (lowLink.get(v).equals(index.get(v))) {
          Node representative = g.getVertexLabel(v);
          int w;
          do {
            w = component.pop();
            onComponentStack.remove(w);
            if (w != v) {
              representatives.put(g.getVertexLabel(w), representative);
            }
          } while (w != v);
        }
      }
    }
  }

  private static int[] enter(
      int v,
      Map<Integer, Integer> index,
      Map<Integer, Integer> lowLink,
      Deque<Integer> component,
      Set<Integer> onComponentStack) {
    int number = index.size();
    index.put(v, number);
    lowLink.put(v, number);
    component.push(v);
    onComponentStack.add(v);
    return new int[] {v, 0};
  }

  private Node representative(Node n) {
    if (representatives.isEmpty()) {
      // no cycles collapsed: each node stands for itself
      return n;
    }
    return representatives.getOrDefault(n, n);
  }

  private Set<AllocationNode> ptsOf(Node n) {
    Set<AllocationNode> s = pointsTo.get(representative(n));
    return s == null ? Collections.emptySet() : s;
  }

  private boolean addPts(Node n, AllocationNode a) {
    return pointsTo.computeIfAbsent(representative(n), k -> new LinkedHashSet<>()).add(a);
  }

  private boolean unionInto(Node n, Set<AllocationNode> incoming) {
    if (incoming.isEmpty()) return false;
    return pointsTo.computeIfAbsent(representative(n), k -> new LinkedHashSet<>()).addAll(incoming);
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
