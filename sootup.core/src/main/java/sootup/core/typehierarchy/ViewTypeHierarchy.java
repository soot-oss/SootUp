package sootup.core.typehierarchy;
/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2019-2020 Christian Brüggemann
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

import com.google.common.base.Suppliers;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jgrapht.Graph;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sootup.core.model.SootClass;
import sootup.core.typehierarchy.ViewTypeHierarchy.ScanResult.Edge;
import sootup.core.typehierarchy.ViewTypeHierarchy.ScanResult.EdgeType;
import sootup.core.typehierarchy.ViewTypeHierarchy.ScanResult.Vertex;
import sootup.core.types.ClassType;
import sootup.core.views.View;

/**
 * Full documentation is in the <a
 * href="https://github.com/secure-software-engineering/soot-reloaded/wiki/Type-Hierarchy-Algorithm">wiki</a>.
 *
 * @author Christian Brüggemann
 */
public class ViewTypeHierarchy implements MutableTypeHierarchy {

  private static final Logger logger = LoggerFactory.getLogger(ViewTypeHierarchy.class);

  private final Supplier<ScanResult> lazyScanResult;
  private final ClassType objectClassType;
  private final Map<SymmetricKey, Set<ClassType>> lcaCache = new HashMap<>();

  /** to allow caching use Typehierarchy.fromView() to get/create the Typehierarchy. */
  public ViewTypeHierarchy(@Nonnull View view) {
    lazyScanResult = Suppliers.memoize(() -> scanView(view));
    objectClassType = view.getIdentifierFactory().getClassType("java.lang.Object");
  }

  @Nonnull
  @Override
  public Stream<ClassType> implementersOf(@Nonnull ClassType interfaceType) {
    Vertex vertex = lazyScanResult.get().typeToVertex.get(interfaceType);
    if (vertex == null) {
      throw new IllegalArgumentException("Could not find '" + interfaceType + "' in hierarchy.");
    }
    if (vertex instanceof ScanResult.ClassVertex) {
      throw new IllegalArgumentException("'" + interfaceType + "' is not an interface.");
    }
    return subtypesOf(interfaceType);
  }

  @Nonnull
  @Override
  public Stream<ClassType> subclassesOf(@Nonnull ClassType classType) {
    Vertex vertex = lazyScanResult.get().typeToVertex.get(classType);
    if (vertex == null) {
      throw new IllegalArgumentException("Could not find '" + classType + "' in hierarchy.");
    }
    if (vertex instanceof ScanResult.InterfaceVertex) {
      throw new IllegalArgumentException("'" + classType + "' is not a class.");
    }
    return subtypesOf(classType);
  }

  @Nonnull
  @Override
  public Stream<ClassType> subtypesOf(@Nonnull ClassType type) {
    ScanResult scanResult = lazyScanResult.get();
    Vertex vertex = scanResult.typeToVertex.get(type);
    if (vertex == null) {
      throw new IllegalArgumentException("Could not find '" + type + "' in hierarchy.");
    }

    // We now traverse the subgraph of the vertex to find all its subtypes
    return visitSubgraph(scanResult.graph, vertex, false);
  }

  @Nonnull
  @Override
  public Stream<ClassType> directSubtypesOf(@Nonnull ClassType type) {
    ScanResult scanResult = lazyScanResult.get();
    Vertex vertex = scanResult.typeToVertex.get(type);
    if (vertex == null) {
      throw new IllegalArgumentException("Could not find '" + type + "' in hierarchy.");
    }

    Graph<Vertex, Edge> graph = scanResult.graph;
    return vertex.directSubTypesOf(graph, vertex);
  }

  @Nonnull
  protected Stream<Vertex> superClassesOf(@Nonnull Vertex classVertex, boolean excludeSelf) {
    Iterator<Vertex> superclassIterator = new SuperClassVertexIterator(classVertex);

    if (excludeSelf) {
      // skip first element which is the classVertex
      superclassIterator.next();
    }

    return StreamSupport.stream(
        Spliterators.spliteratorUnknownSize(superclassIterator, Spliterator.NONNULL), false);
  }

  protected Stream<Vertex> directImplementedInterfacesOf(@Nonnull Vertex classVertex) {
    Graph<Vertex, Edge> graph = lazyScanResult.get().graph;
    return graph.outgoingEdgesOf(classVertex).stream()
        .filter(edge -> edge.type == EdgeType.ClassDirectlyImplements)
        .map(graph::getEdgeTarget);
  }

  protected Stream<Vertex> directExtendedInterfacesOf(@Nonnull Vertex interfaceVertex) {
    Graph<Vertex, Edge> graph = lazyScanResult.get().graph;
    return graph.outgoingEdgesOf(interfaceVertex).stream()
        .filter(edge -> edge.type == EdgeType.InterfaceDirectlyExtends)
        .map(graph::getEdgeTarget);
  }

  protected Stream<Vertex> directSuperClassOf(@Nonnull Vertex classVertex) {
    Graph<Vertex, Edge> graph = lazyScanResult.get().graph;
    return graph.outgoingEdgesOf(classVertex).stream()
        .filter(edge -> edge.type == EdgeType.ClassDirectlyExtends)
        .map(graph::getEdgeTarget);
  }

  @Override
  public Stream<ClassType> directlyImplementedInterfacesOf(@Nonnull ClassType classType) {
    Vertex vertex = lazyScanResult.get().typeToVertex.get(classType);
    if (vertex == null) {
      throw new IllegalArgumentException("Could not find '" + classType + "' in hierarchy.");
    }
    if (vertex instanceof ScanResult.InterfaceVertex) {
      throw new IllegalArgumentException(classType + " is not a class.");
    }
    return directImplementedInterfacesOf(vertex).map(v -> v.javaClassType);
  }

  @Nonnull
  @Override
  public Stream<ClassType> directlyExtendedInterfacesOf(@Nonnull ClassType interfaceType) {
    Vertex vertex = lazyScanResult.get().typeToVertex.get(interfaceType);
    if (vertex == null) {
      throw new IllegalArgumentException("Could not find " + interfaceType + " in hierarchy.");
    }
    if (vertex instanceof ScanResult.ClassVertex) {
      throw new IllegalArgumentException(interfaceType + " is not an interface.");
    }
    return directExtendedInterfacesOf(vertex).map(v -> v.javaClassType);
  }

  @Override
  public boolean contains(ClassType type) {
    return lazyScanResult.get().typeToVertex.get(type) != null;
  }

  protected Set<Vertex> findAncestors(ClassType type) {
    Graph<Vertex, Edge> graph = lazyScanResult.get().graph;
    Vertex vertex = lazyScanResult.get().typeToVertex.get(type);
    if (vertex == null) {
      logger.warn("Could not find {} in this hierarchy!", type.toString());
      return Collections.emptySet();
    }
    Set<Vertex> ancestors = new HashSet<>();
    for (Edge edge : graph.outgoingEdgesOf(vertex)) {
      Vertex parent = graph.getEdgeTarget(edge);
      ancestors.add(parent);
      ancestors.addAll(findAncestors(parent.javaClassType));
    }
    return ancestors;
  }

  /**
   * This algorithm is implementation of the algorithm
   * https://www.baeldung.com/cs/lowest-common-ancestor-acyclic-graph
   */
  @Override
  public Collection<ClassType> getLowestCommonAncestors(ClassType a, ClassType b) {
    // search in cache
    SymmetricKey pair = new SymmetricKey(a, b);
    Set<ClassType> lcas = lcaCache.get(pair);
    if (lcas != null) {
      return lcas;
    }

    Graph<Vertex, Edge> graph = lazyScanResult.get().graph;
    Set<Vertex> ancestorsOfA = findAncestors(a);
    Set<Vertex> ancestorsOfB = findAncestors(b);
    lcas = new HashSet<>();

    if (ancestorsOfA.isEmpty() || ancestorsOfB.isEmpty()) {
      lcas.add(objectClassType);
      lcaCache.put(pair, lcas);
      return lcas;
    }
    // ancestorsOfA contains now common ancestors of a and b
    ancestorsOfA.retainAll(ancestorsOfB);
    boolean notLca = false;
    for (Vertex ca : ancestorsOfA) {
      Set<Edge> incomingEdges = graph.incomingEdgesOf(ca);
      for (Edge ie : incomingEdges) {
        if (ancestorsOfA.contains(graph.getEdgeSource(ie))) {
          notLca = true;
          break;
        }
      }
      if (notLca) {
        notLca = false;
      } else {
        lcas.add(ca.javaClassType);
      }
    }
    if (lcas.isEmpty()) {
      lcas = Collections.singleton(objectClassType);
    }
    lcaCache.put(pair, lcas);
    return lcas;
  }

  @Nonnull
  @Override
  public Stream<ClassType> implementedInterfacesOf(@Nonnull ClassType type) {
    ScanResult scanResult = lazyScanResult.get();
    Vertex vertex = scanResult.typeToVertex.get(type);

    if (vertex == null) {
      throw new IllegalArgumentException("Could not find " + type + " in this hierarchy.");
    }

    if (vertex instanceof ScanResult.ClassVertex) {
      // We ascend from vertex through its superclasses to java.lang.Object.
      // For each superclass, we take the interfaces it implements and merge
      // them together in a Set.
      return superClassesOf(vertex, false)
          .flatMap(this::directImplementedInterfacesOf)
          .flatMap(this::selfAndImplementedInterfaces)
          .distinct();
    } else {
      return directExtendedInterfacesOf(vertex)
          .flatMap(this::selfAndImplementedInterfaces)
          .distinct();
    }
  }

  /**
   * Recursively obtains all interfaces this interface extends, including transitively extended
   * interfaces.
   */
  @Nonnull
  protected Stream<ClassType> selfAndImplementedInterfaces(Vertex vertex) {
    ScanResult scanResult = lazyScanResult.get();
    Graph<Vertex, Edge> graph = scanResult.graph;

    Stream<Vertex> extendedInterfaces =
        graph.outgoingEdgesOf(vertex).stream()
            .filter(edge -> edge.type == EdgeType.InterfaceDirectlyExtends)
            .map(graph::getEdgeTarget);

    return Stream.concat(
        Stream.of(vertex.javaClassType),
        extendedInterfaces.flatMap(this::selfAndImplementedInterfaces));
  }

  @Nonnull
  @Override
  public Optional<ClassType> superClassOf(@Nonnull ClassType classType) {
    ScanResult scanResult = lazyScanResult.get();
    Vertex classVertex = scanResult.typeToVertex.get(classType);
    if (classVertex == null) {
      throw new IllegalArgumentException("Could not find '" + classType + "' in the view.");
    }
    if (objectClassType.equals(classType)) {
      return Optional.empty();
    }
    Optional<ClassType> superclassOpt =
        directSuperClassOf(classVertex).findAny().map(v -> v.javaClassType);

    if (superclassOpt.isPresent()) {
      return superclassOpt;
    } else {
      if (classVertex instanceof ScanResult.InterfaceVertex) {
        return Optional.of(objectClassType);
      }
      return Optional.empty();
    }
  }

  @Override
  public boolean isInterface(@Nonnull ClassType type) {
    Vertex vertex = lazyScanResult.get().typeToVertex.get(type);
    if (vertex == null) {
      throw new IllegalArgumentException("Could not find '" + type + "' in hierarchy.");
    }
    return vertex instanceof ScanResult.InterfaceVertex;
  }

  public boolean isClass(@Nonnull ClassType type) {
    Vertex vertex = lazyScanResult.get().typeToVertex.get(type);
    if (vertex == null) {
      throw new IllegalArgumentException("Could not find '" + type + "' in hierarchy.");
    }
    return vertex instanceof ScanResult.ClassVertex;
  }

  /**
   * Visits the subgraph of the specified <code>vertex</code> and calls the <code>visitor</code> for
   * each vertex in the subgraph. If <code>includeSelf</code> is true, the <code>visitor</code> is
   * also called with the <code>vertex</code>.
   */
  private Stream<ClassType> visitSubgraph(
      Graph<Vertex, Edge> graph, Vertex vertex, boolean includeSelf) {
    Stream<ClassType> subgraph = includeSelf ? Stream.of(vertex.javaClassType) : Stream.empty();
    if (vertex instanceof ScanResult.InterfaceVertex) {
      return Stream.concat(
          subgraph,
          graph.incomingEdgesOf(vertex).stream()
              .filter(
                  edge ->
                      edge.type == EdgeType.ClassDirectlyImplements
                          || edge.type == EdgeType.InterfaceDirectlyExtends)
              .map(graph::getEdgeSource)
              .flatMap(directSubtype -> visitSubgraph(graph, directSubtype, true)));
    } else {
      return Stream.concat(
          subgraph,
          graph.incomingEdgesOf(vertex).stream()
              .filter(edge -> edge.type == EdgeType.ClassDirectlyExtends)
              .map(graph::getEdgeSource)
              .flatMap(directSubclass -> visitSubgraph(graph, directSubclass, true)));
    }
  }

  /**
   * This method scans the view by iterating over its classes and creating a graph vertex for each
   * one. When a class is encountered that extends another one or implements an interface, the graph
   * vertex of the extended class or implemented interface is connected to the vertex of the
   * subtype.
   *
   * <p>We distinguish between interface and class vertices, as interfaces may have direct
   * implementers as well as other interfaces that extend them.
   *
   * <p>In the graph structure, a type is only connected to its direct subtypes.
   */
  private ScanResult scanView(@Nonnull View view) {
    Map<ClassType, Vertex> typeToVertex = new HashMap<>();
    Graph<Vertex, Edge> graph = new SimpleDirectedGraph<>(null, null, false);

    view.getClasses().forEach(sootClass -> addSootClassToGraph(sootClass, typeToVertex, graph));
    return new ScanResult(typeToVertex, graph);
  }

  private static void addSootClassToGraph(
      SootClass sootClass, Map<ClassType, Vertex> typeToVertex, Graph<Vertex, Edge> graph) {
    if (sootClass.isInterface()) {
      Vertex vertex =
          typeToVertex.computeIfAbsent(
              sootClass.getType(), type -> createAndAddInterfaceVertex(graph, type));
      for (ClassType extendedInterface : sootClass.getInterfaces()) {
        Vertex extendedInterfaceVertex =
            typeToVertex.computeIfAbsent(
                extendedInterface, type -> createAndAddInterfaceVertex(graph, type));
        graph.addEdge(vertex, extendedInterfaceVertex, new Edge(EdgeType.InterfaceDirectlyExtends));
      }
    } else {
      Vertex vertex =
          typeToVertex.computeIfAbsent(
              sootClass.getType(), type -> createAndAddClassVertex(graph, type));
      for (ClassType implementedInterface : sootClass.getInterfaces()) {
        Vertex implementedInterfaceVertex =
            typeToVertex.computeIfAbsent(
                implementedInterface, type -> createAndAddInterfaceVertex(graph, type));
        graph.addEdge(
            vertex, implementedInterfaceVertex, new Edge(EdgeType.ClassDirectlyImplements));
      }
      sootClass
          .getSuperclass()
          .ifPresent(
              superClass -> {
                Vertex superClassVertex =
                    typeToVertex.computeIfAbsent(
                        superClass, type -> createAndAddClassVertex(graph, type));
                graph.addEdge(vertex, superClassVertex, new Edge(EdgeType.ClassDirectlyExtends));
              });
    }
  }

  @Nonnull
  private static Vertex createAndAddClassVertex(Graph<Vertex, Edge> graph, ClassType type) {
    Vertex classVertex = new ScanResult.ClassVertex(type);
    graph.addVertex(classVertex);
    return classVertex;
  }

  @Nonnull
  private static Vertex createAndAddInterfaceVertex(Graph<Vertex, Edge> graph, ClassType type) {
    Vertex interfaceVertex = new ScanResult.InterfaceVertex(type);
    graph.addVertex(interfaceVertex);
    return interfaceVertex;
  }

  @Override
  public void addType(@Nonnull SootClass sootClass) {
    ScanResult scanResult = lazyScanResult.get();
    addSootClassToGraph(sootClass, scanResult.typeToVertex, scanResult.graph);
  }

  /** Holds a vertex for each {@link ClassType} encountered during the scan. */
  protected static class ScanResult {

    /** @see #javaClassType */
    protected abstract static class Vertex {
      @Nonnull final ClassType javaClassType;

      private Vertex(@Nonnull ClassType javaClassType) {
        this.javaClassType = javaClassType;
      }

      public abstract Stream<ClassType> directSubTypesOf(Graph<Vertex, Edge> graph, Vertex vertex);
    }

    private static class InterfaceVertex extends Vertex {
      public InterfaceVertex(ClassType javaClassType) {
        super(javaClassType);
      }

      public Stream<ClassType> directSubTypesOf(Graph<Vertex, Edge> graph, Vertex vertex) {
        return graph.incomingEdgesOf(vertex).stream()
            .filter(
                edge ->
                    edge.type == EdgeType.ClassDirectlyImplements
                        || edge.type == EdgeType.InterfaceDirectlyExtends)
            .map(graph::getEdgeSource)
            .map(directSubclass -> directSubclass.javaClassType)
            .distinct();
      }
    }

    private static class ClassVertex extends Vertex {
      public ClassVertex(ClassType javaClassType) {
        super(javaClassType);
      }

      @Override
      public Stream<ClassType> directSubTypesOf(Graph<Vertex, Edge> graph, Vertex vertex) {
        return graph.incomingEdgesOf(vertex).stream()
            .filter(edge -> edge.type == EdgeType.ClassDirectlyExtends)
            .map(graph::getEdgeSource)
            .map(directSubclass -> directSubclass.javaClassType)
            .distinct();
      }
    }

    protected enum EdgeType {
      /** Edge to an interface vertex this interface extends directly, non-transitively. */
      InterfaceDirectlyExtends,
      /** Edge to an interface extending this interface directly, non-transitively. */
      ClassDirectlyImplements,
      /** Edge to a class this class is directly subclassed by, non-transitively. */
      ClassDirectlyExtends
    }

    /** @see #type */
    protected static class Edge {
      @Nonnull final EdgeType type;

      Edge(@Nonnull EdgeType type) {
        this.type = type;
      }
    }

    /** Holds the vertex for each type. */
    @Nonnull final Map<ClassType, Vertex> typeToVertex;

    @Nonnull final Graph<Vertex, Edge> graph;

    private ScanResult(
        @Nonnull Map<ClassType, Vertex> typeToVertex, @Nonnull Graph<Vertex, Edge> graph) {
      this.typeToVertex = typeToVertex;
      this.graph = graph;
    }
  }

  private class SuperClassVertexIterator implements Iterator<Vertex> {
    @Nullable private Vertex classVertexItBase;

    public SuperClassVertexIterator(@Nonnull Vertex classVertex) {
      classVertexItBase = classVertex;
    }

    @Override
    public boolean hasNext() {
      return classVertexItBase != null;
    }

    @Override
    public Vertex next() {
      if (classVertexItBase == null) {
        throw new NoSuchElementException("Iterator is already iterated.");
      }
      Vertex currentSuperClass = classVertexItBase;
      classVertexItBase = directSuperClassOf(classVertexItBase).findAny().orElse(null);
      return currentSuperClass;
    }
  }

  static class SymmetricKey extends ImmutablePair<ClassType, ClassType> {
    public SymmetricKey(ClassType left, ClassType right) {
      super(left, right);
    }

    @Override
    public int hashCode() {
      return Objects.hash(getKey()) + Objects.hash(getValue());
    }
  }
}
