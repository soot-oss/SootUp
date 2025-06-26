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
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.google.common.collect.Iterators;
import com.google.common.collect.Streams;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.graph4j.Digraph;
import org.graph4j.GraphBuilder;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sootup.core.model.SootClass;
import sootup.core.typehierarchy.ViewTypeHierarchy.ScanResult.Edge;
import sootup.core.typehierarchy.ViewTypeHierarchy.ScanResult.EdgeType;
import sootup.core.types.ClassType;
import sootup.core.views.View;

/**
 * Documentation about how to use it is available <a
 * href="https://soot-oss.github.io/SootUp/latest/typehierarchy/">here</a>.
 *
 * @author Christian Brüggemann
 */
public class ViewTypeHierarchy implements MutableTypeHierarchy {

  private static final Logger logger = LoggerFactory.getLogger(ViewTypeHierarchy.class);

  private final Supplier<ScanResult> lazyScanResult;
  private final ClassType objectClassType;
  private final Map<SymmetricKey, Set<ClassType>> lcaCache = new HashMap<>();

  /** to allow caching use Typehierarchy.fromView() to get/create the Typehierarchy. */
  public ViewTypeHierarchy(@NonNull View view) {
    lazyScanResult = Suppliers.memoize(() -> scanView(view));
    objectClassType = view.getIdentifierFactory().getClassType("java.lang.Object");
  }

  @NonNull
  @Override
  public Stream<ClassType> implementersOf(@NonNull ClassType interfaceType) {
    int vertex = lazyScanResult.get().graph.findVertex(interfaceType);
    if (vertex < 0) {
      throw new IllegalArgumentException("Could not find '" + interfaceType + "' in hierarchy.");
    }
    if (!lazyScanResult.get().isInterface.get(vertex)) {
      throw new IllegalArgumentException("'" + interfaceType + "' is not an interface.");
    }
    return subtypesOf(interfaceType);
  }

  @NonNull
  @Override
  public Stream<ClassType> subclassesOf(@NonNull ClassType classType) {
    int vertex = lazyScanResult.get().graph.findVertex(classType);
    if (vertex < 0) {
      throw new IllegalArgumentException("Could not find '" + classType + "' in hierarchy.");
    }
    if (lazyScanResult.get().isInterface.get(vertex)) {
      throw new IllegalArgumentException("'" + classType + "' is not a class.");
    }
    return subtypesOf(classType);
  }

  @NonNull
  @Override
  public Stream<ClassType> subinterfacesOf(@NonNull ClassType interfaceType) {
    Digraph<ClassType, Edge> graph = lazyScanResult.get().graph;
    int vertex = graph.findVertex(interfaceType);
    if (vertex < 0) {
      throw new IllegalArgumentException("Could not find '" + interfaceType + "' in hierarchy.");
    }
    return visitInterfaceSubgraph(graph, interfaceType, false);
  }

  @NonNull
  @Override
  public Stream<ClassType> subtypesOf(@NonNull ClassType type) {
    Digraph<ClassType, Edge> graph = lazyScanResult.get().graph;
    int vertex = graph.findVertex(type);
    if (vertex < 0) {
      throw new IllegalArgumentException("Could not find '" + type + "' in hierarchy.");
    }

    // We now traverse the subgraph of the vertex to find all its subtypes
    return visitSubgraph(graph, vertex, false);
  }

  @NonNull
  @Override
  public Stream<ClassType> directSubtypesOf(@NonNull ClassType type) {
    ScanResult scanResult = lazyScanResult.get();
    int vertex = lazyScanResult.get().graph.findVertex(type);
    if (vertex < 0) {
      throw new IllegalArgumentException("Could not find '" + type + "' in hierarchy.");
    }

    Digraph<ClassType, Edge> graph = scanResult.graph;
      if (lazyScanResult.get().isInterface.get(vertex)) {
        // interfacetype
        return Arrays.stream(graph.incomingEdgesTo(vertex))
                .map(edge -> graph.getEdgeLabel(edge.source(), edge.target()))
                .filter(edge -> edge.type == EdgeType.ClassDirectlyExtends)
                .map(edge -> graph.getVertexLabel(graph.findEdge(edge).source()))
                .distinct();
      } else {
        // classtype
        return Arrays.stream(graph.incomingEdgesTo(vertex))
                .map(edge -> graph.getEdgeLabel(edge.source(), edge.target()))
                .filter(
                        edge ->
                                edge.type == EdgeType.ClassDirectlyImplements
                                        || edge.type == EdgeType.InterfaceDirectlyExtends)
                .map(edge -> graph.getVertexLabel(graph.findEdge(edge).source()))
                .distinct();
      }

  }

  @NonNull
  protected Stream<ClassType> superClassesOf(@NonNull ClassType classVertex, boolean excludeSelf) {
    Iterator<ClassType> superclassIterator = new SuperClassVertexIterator(classVertex);

    if (excludeSelf) {
      // skip first element which is the classVertex
      superclassIterator.next();
    }

    return StreamSupport.stream(
        Spliterators.spliteratorUnknownSize(superclassIterator, Spliterator.NONNULL), false);
  }

  protected Stream<ClassType> directExtendedInterfacesOf( int interfaceVertex) {
    Digraph<ClassType, Edge> graph = lazyScanResult.get().graph;
    return Arrays.stream(graph.outgoingEdgesFrom(interfaceVertex))
        .map(edge -> graph.getEdgeLabel(edge.source(), edge.target()))
        .filter(edge -> edge.type == EdgeType.InterfaceDirectlyExtends)
        .map(edge -> graph.getVertexLabel(graph.findEdge(edge).target()));
  }

  protected Stream<ClassType> directSuperClassOf(int classVertex) {
    Digraph<ClassType, Edge> graph = lazyScanResult.get().graph;
    return Arrays.stream(graph.outgoingEdgesFrom(classVertex))
            .map(edge -> graph.getEdgeLabel(edge.source(), edge.target()))
            .filter(edge -> edge.type == EdgeType.ClassDirectlyExtends)
            .map(edge -> graph.getVertexLabel(graph.findEdge(edge).target()));
  }

  @Override
  public Stream<ClassType> directlyImplementedInterfacesOf(@NonNull ClassType classType) {
    int vertex = lazyScanResult.get().graph.findVertex(classType);
    if (vertex < 0 ) {
      throw new IllegalArgumentException("Could not find '" + classType + "' in hierarchy.");
    }
    if (lazyScanResult.get().isInterface.get(vertex)) {
      throw new IllegalArgumentException(classType + " is not a class.");
    }
    Digraph<ClassType, Edge> graph = lazyScanResult.get().graph;
    return Arrays.stream(graph.outgoingEdgesFrom(vertex))
        .map(edge -> graph.getEdgeLabel(edge.source(), edge.target()))
        .filter(edge -> edge.type == EdgeType.ClassDirectlyImplements)
        .map(edge -> graph.getVertexLabel(graph.findEdge(edge).target()));
  }

  @NonNull
  @Override
  public Stream<ClassType> directlyExtendedInterfacesOf(@NonNull ClassType interfaceType) {
    int vertex = lazyScanResult.get().graph.findVertex(interfaceType);
    if (vertex < 0 ) {
      throw new IllegalArgumentException("Could not find " + interfaceType + " in hierarchy.");
    }
    if (!lazyScanResult.get().isInterface.get(vertex)) {
      throw new IllegalArgumentException(interfaceType + " is not an interface.");
    }
    return directExtendedInterfacesOf(vertex);
  }

  @Override
  public boolean contains(ClassType type) {
    return lazyScanResult.get().graph.findVertex(type) != -1;
  }

  protected Set<ClassType> findAncestors(ClassType type) {
    Digraph<ClassType, Edge> graph = lazyScanResult.get().graph;
    int vertex = lazyScanResult.get(). graph.findVertex(type);
    if (vertex < 0 ) {
      logger.debug("Could not find {} in this hierarchy!", type.toString());
      return Collections.emptySet();
    }
    Set<ClassType> ancestors = new HashSet<>();
    Set<Edge> outgoingEdgesFrom =
        Arrays.stream(graph.outgoingEdgesFrom(vertex))
            .map(edge -> graph.getEdgeLabel(edge.source(), edge.target()))
            .collect(Collectors.toSet());
    for (Edge edge : outgoingEdgesFrom) {
      ClassType parent = graph.getVertexLabel(graph.findEdge(edge).target());
      ancestors.add(parent);
      ancestors.addAll(findAncestors(parent));
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

    Digraph<ClassType, Edge> graph = lazyScanResult.get().graph;
    Set<ClassType> ancestorsOfA = findAncestors(a);
    Set<ClassType> ancestorsOfB = findAncestors(b);
    lcas = new HashSet<>();

    if (ancestorsOfA.isEmpty() || ancestorsOfB.isEmpty()) {
      lcas.add(objectClassType);
      lcaCache.put(pair, lcas);
      return lcas;
    }
    // ancestorsOfA contains now common ancestors of a and b
    ancestorsOfA.retainAll(ancestorsOfB);
    boolean notLca = false;
    for (ClassType ca : ancestorsOfA) {
      int vertex = graph.findVertex(ca);
      assert vertex > -1;
      Set<Edge> incomingEdges =
          Arrays.stream(graph.incomingEdgesTo(vertex))
              .map(edge -> graph.getEdgeLabel(edge.source(), edge.target()))
              .collect(Collectors.toSet());
      for (Edge ie : incomingEdges) {
        if (ancestorsOfA.contains(graph.getVertexLabel(graph.findEdge(ie).source()))) {
          notLca = true;
          break;
        }
      }
      if (notLca) {
        notLca = false;
      } else {
        lcas.add(ca);
      }
    }
    if (lcas.isEmpty()) {
      lcas = Collections.singleton(objectClassType);
    }
    lcaCache.put(pair, lcas);
    return lcas;
  }

  @NonNull
  @Override
  public Stream<ClassType> implementedInterfacesOf(@NonNull ClassType type) {
    ScanResult scanResult = lazyScanResult.get();
    int vertex = scanResult.graph.findVertex(type);

    if (vertex < 0) {
      throw new IllegalArgumentException("Could not find " + type + " in this hierarchy.");
    }

    if (!lazyScanResult.get().isInterface.get(vertex)) {
      // We ascend from vertex through its superclasses to java.lang.Object.
      // For each superclass, we take the interfaces it implements and merge
      // them together in a Set.
      return superClassesOf(type, false)
          .flatMap((ClassType classVertex) -> {
            Digraph<ClassType, Edge> graph = lazyScanResult.get().graph;
            return Arrays.stream(graph.outgoingEdgesFrom(graph.findVertex((ClassType) classVertex)))
                .map(edge -> graph.getEdgeLabel(edge.source(), edge.target()))
                .filter(edge -> edge.type == EdgeType.ClassDirectlyImplements)
                .map(edge -> graph.getVertexLabel(graph.findEdge(edge).target()));
          })
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
  @NonNull
  protected Stream<ClassType> selfAndImplementedInterfaces(ClassType type) {
    ScanResult scanResult = lazyScanResult.get();
    Digraph<ClassType, Edge> graph = scanResult.graph;
    int vertex = graph.findVertex(type);

    Stream<ClassType> extendedInterfaces =
        Arrays.stream(graph.outgoingEdgesFrom(vertex))
            .map(edge -> graph.getEdgeLabel(edge.source(), edge.target()))
            .filter(edge -> edge.type == EdgeType.InterfaceDirectlyExtends)
            .map(edge -> graph.getVertexLabel(graph.findEdge(edge).target()));

    return Stream.concat(
        Stream.of(type),
        extendedInterfaces.flatMap(this::selfAndImplementedInterfaces));
  }

  @NonNull
  @Override
  public Optional<ClassType> superClassOf(@NonNull ClassType classType) {
    ScanResult scanResult = lazyScanResult.get();
    int classVertex = scanResult.graph.findVertex(classType);
    if (classVertex < 0) {
      throw new IllegalArgumentException("Could not find '" + classType + "' in the view.");
    }
    if (objectClassType.equals(classType)) {
      return Optional.empty();
    }
    Optional<ClassType> superclassOpt =
        directSuperClassOf(classVertex).findAny();

    if (superclassOpt.isPresent()) {
      return superclassOpt;
    } else {
      if (lazyScanResult.get().isInterface.get(classVertex)) {
        return Optional.of(objectClassType);
      }
      return Optional.empty();
    }
  }

  @Override
  public boolean isInterface(@NonNull ClassType type) {
    int vertex = lazyScanResult.get().graph.findVertex(type);
    if (vertex < 0) {
      throw new IllegalArgumentException("Could not find '" + type + "' in hierarchy.");
    }
    return lazyScanResult.get().isInterface.get(vertex);
  }

  public boolean isClass(@NonNull ClassType type) {
    int vertex = lazyScanResult.get().graph.findVertex(type);
    if (vertex < 0) {
      throw new IllegalArgumentException("Could not find '" + type + "' in hierarchy.");
    }
    return !lazyScanResult.get().isInterface.get(vertex);
  }

  /**
   * Visits the subgraph of the specified <code>vertex</code> and calls the <code>visitor</code> for
   * each vertex in the subgraph. If <code>includeSelf</code> is true, the <code>visitor</code> is
   * also called with the <code>vertex</code>.
   */
  private Stream<ClassType> visitSubgraph(
      Digraph<ClassType, Edge> graph, int vertex, boolean includeSelf) {
    assert vertex > -1;

    Stream<ClassType> subgraph = includeSelf ? Stream.of(graph.getVertexLabel(vertex)) : Stream.empty();
    if (lazyScanResult.get().isInterface.get(vertex)) {
      org.graph4j.Edge[] incomingEdges = graph.incomingEdgesTo(vertex);
      return Stream.concat(
          subgraph,
          Arrays.stream(incomingEdges)
              .map(edge -> {
                  if(edge == null){
                    System.out.println("vertex");
                    System.out.println(vertex);

                    System.out.println("incomingEdges");
                    System.out.println(incomingEdges);
                    System.out.println(Arrays.toString(incomingEdges));

                    System.out.println("vertices");

                    System.out.println(
                            Arrays.toString(graph.vertices())
                    );

                    System.out.println("edges");

                    System.out.println(
                            Arrays.toString(graph.edges())
                    );

                  }
                  return graph.getEdgeLabel(edge.source(), edge.target());
              })
              .filter(
                  edge ->
                      edge.type == EdgeType.ClassDirectlyImplements
                          || edge.type == EdgeType.InterfaceDirectlyExtends)
              .map(edge -> graph.findEdge(edge).source())
              .flatMap(directSubtype -> visitSubgraph(graph, directSubtype, true)));
    } else {
      return Stream.concat(
          subgraph,
          Arrays.stream(graph.incomingEdgesTo(vertex))
              .map(edge -> graph.getEdgeLabel(edge.source(), edge.target()))
              .filter(edge -> edge.type == EdgeType.ClassDirectlyExtends)
              .map(edge -> graph.findEdge(edge).source())
              .flatMap(directSubclass -> visitSubgraph(graph, directSubclass, true)));
    }
  }

  /**
   * Visits the subgraph of the specified <code>vertex</code> and calls the <code>visitor</code> for
   * each vertex in the subgraph that is an interface. If <code>includeSelf</code> is true, the
   * <code>visitor</code> is also called with the <code>vertex</code>.
   */
  private Stream<ClassType> visitInterfaceSubgraph(
      Digraph<ClassType, Edge> graph, ClassType vertex, boolean includeSelf) {
    Stream<ClassType> subgraph = includeSelf ? Stream.of(vertex) : Stream.empty();
    return Stream.concat(
        subgraph,
        Arrays.stream(graph.incomingEdgesTo(graph.findVertex(vertex)))
            .map(edge -> graph.getEdgeLabel(edge.source(), edge.target()))
            .filter(edge -> edge.type == EdgeType.InterfaceDirectlyExtends)
            .map(edge -> graph.getVertexLabel(graph.findEdge(edge).source()))
            .flatMap(directSubtype -> visitInterfaceSubgraph(graph, directSubtype, true)));
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
  private ScanResult scanView(@NonNull View view) {
    ScanResult scanResult = new ScanResult();
    view.getClasses().forEach(sootClass -> scanResult.handleSootClass(sootClass));
    return scanResult;
  }

  @Override
  public void addType(@NonNull SootClass sootClass) {
    ScanResult scanResult = lazyScanResult.get();
    scanResult.handleSootClass(sootClass);
  }

  /** Holds a vertex for each {@link ClassType} encountered during the scan. */
  protected static class ScanResult {

    protected enum EdgeType {
      /** Edge to an interface vertex this interface extends directly, non-transitively. */
      InterfaceDirectlyExtends,
      /** Edge to an interface extending this interface directly, non-transitively. */
      ClassDirectlyImplements,
      /** Edge to a class this class is directly subclassed by, non-transitively. */
      ClassDirectlyExtends
    }

    /**
     * @see #type
     */
    protected static class Edge {
      @NonNull final EdgeType type;

      Edge(@NonNull EdgeType type) {
        this.type = type;
      }
    }

    @NonNull final Digraph<ClassType, Edge> graph = GraphBuilder.empty().buildDirectedMultigraph();
    @NonNull final BitSet isInterface = new BitSet();

    private ScanResult() {

    }

    private void handleSootClass(SootClass sootClass) {
      ClassType type = sootClass.getType();
      int typeVertex;
      if (sootClass.isInterface()) {
        typeVertex = createOrGetInterfaceVertex(graph, type);
        for (ClassType extendedInterface : sootClass.getInterfaces()) {
          int extendedInterfaceVertex = createOrGetInterfaceVertex(graph, extendedInterface);
          graph.addLabeledEdge(typeVertex, extendedInterfaceVertex, new Edge(EdgeType.InterfaceDirectlyExtends));
        }
      } else {
        typeVertex = createOrGetClassVertex(graph, type);
        for (ClassType implementedInterface : sootClass.getInterfaces()) {
          int implementsInterfaceVertex = createOrGetInterfaceVertex(graph, implementedInterface);
          graph.addLabeledEdge(typeVertex, implementsInterfaceVertex, new Edge(EdgeType.ClassDirectlyImplements));
        }
        sootClass
                .getSuperclass()
                .ifPresent(
                        superClass -> {
                          int superclassVertex = createOrGetClassVertex(graph, superClass);
                          graph.addLabeledEdge(typeVertex, superclassVertex, new Edge(EdgeType.ClassDirectlyExtends));
                        });
      }
    }

    private int createOrGetClassVertex(Digraph<ClassType, Edge> graph, ClassType type) {
      int vertex = graph.findVertex(type);
      if (vertex < 0) {
        int classVertex = graph.addLabeledVertex(type);
        isInterface.set(classVertex,false);
        return classVertex;
      }
      return vertex;
    }

    private int createOrGetInterfaceVertex(Digraph<ClassType, Edge> graph, ClassType type) {
      int vertex = graph.findVertex(type);
      if (vertex < 0) {
        int interfaceVertex = graph.addLabeledVertex(type);
        isInterface.set(interfaceVertex, true);
        return interfaceVertex;
      }
      return vertex;
    }

  }


  private class SuperClassVertexIterator implements Iterator<ClassType> {
    @Nullable private ClassType classVertexItBase;

    public SuperClassVertexIterator(@NonNull ClassType classVertex) {
      classVertexItBase = classVertex;
    }

    @Override
    public boolean hasNext() {
      return classVertexItBase != null;
    }

    @Override
    public ClassType next() {
      if (classVertexItBase == null) {
        throw new NoSuchElementException("Iterator is already iterated.");
      }
      ClassType currentSuperClass = classVertexItBase;
      classVertexItBase = directSuperClassOf(lazyScanResult.get().graph.findVertex(classVertexItBase)).findAny().orElse(null);
      return currentSuperClass;
    }
  }

  static class SymmetricKey extends ImmutablePair<ClassType, ClassType> {
    public SymmetricKey(ClassType left, ClassType right) {
      super(left, right);
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == this) {
        return true;
      }
      if (!(obj instanceof Map.Entry)) {
        return false;
      }
      Map.Entry<?, ?> other = (Map.Entry) obj;
      return (Objects.equals(this.getKey(), other.getKey())
              && Objects.equals(this.getValue(), other.getValue()))
          || (Objects.equals(this.getKey(), other.getValue())
              && Objects.equals(this.getValue(), other.getKey()));
    }

    @Override
    public int hashCode() {
      return Objects.hash(getKey()) + Objects.hash(getValue());
    }
  }
}
