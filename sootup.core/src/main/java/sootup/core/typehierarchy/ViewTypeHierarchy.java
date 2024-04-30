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
import org.jgrapht.Graph;
import org.jgrapht.graph.SimpleDirectedGraph;
import sootup.core.model.SootClass;
import sootup.core.typehierarchy.ViewTypeHierarchy.ScanResult.Edge;
import sootup.core.typehierarchy.ViewTypeHierarchy.ScanResult.EdgeType;
import sootup.core.typehierarchy.ViewTypeHierarchy.ScanResult.Vertex;
import sootup.core.typehierarchy.ViewTypeHierarchy.ScanResult.VertexType;
import sootup.core.types.ClassType;
import sootup.core.views.View;

/**
 * Full documentation is in the <a
 * href="https://github.com/secure-software-engineering/soot-reloaded/wiki/Type-Hierarchy-Algorithm">wiki</a>.
 *
 * @author Christian Brüggemann
 */
public class ViewTypeHierarchy implements MutableTypeHierarchy {

  private final Supplier<ScanResult> lazyScanResult;
  private final ClassType objectClassType;

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
    if (vertex.type != VertexType.Interface) {
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
    if (vertex.type != VertexType.Class) {
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

    Set<ClassType> subclasses = new HashSet<>();

    Graph<Vertex, Edge> graph = scanResult.graph;

    switch (vertex.type) {
      case Interface:
        graph.incomingEdgesOf(vertex).stream()
            .filter(
                edge ->
                    edge.type == EdgeType.ClassDirectlyImplements
                        || edge.type == EdgeType.InterfaceDirectlyExtends)
            .map(graph::getEdgeSource)
            .forEach(directSubclass -> subclasses.add(directSubclass.javaClassType));
        break;
      case Class:
        graph.incomingEdgesOf(vertex).stream()
            .filter(edge -> edge.type == EdgeType.ClassDirectlyExtends)
            .map(graph::getEdgeSource)
            .forEach(directSubclass -> subclasses.add(directSubclass.javaClassType));
        break;
      default:
        throw new AssertionError("Unknown vertex type!");
    }

    return subclasses.stream();
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
    if (vertex.type != VertexType.Class) {
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
    if (vertex.type != VertexType.Interface) {
      throw new IllegalArgumentException(interfaceType + " is not a class.");
    }
    return directExtendedInterfacesOf(vertex).map(v -> v.javaClassType);
  }

  @Override
  public boolean contains(ClassType type) {
    return lazyScanResult.get().typeToVertex.get(type) != null;
  }

  @Nonnull
  @Override
  public Stream<ClassType> implementedInterfacesOf(@Nonnull ClassType type) {
    ScanResult scanResult = lazyScanResult.get();
    Vertex vertex = scanResult.typeToVertex.get(type);

    if (vertex == null) {
      throw new IllegalArgumentException("Could not find " + type + " in this hierarchy.");
    }

    switch (vertex.type) {
      case Class:
        // We ascend from vertex through its superclasses to java.lang.Object.
        // For each superclass, we take the interfaces it implements and merge
        // them together in a Set.
        return superClassesOf(vertex, false)
            .flatMap(this::directImplementedInterfacesOf)
            .flatMap(this::selfAndImplementedInterfaces)
            .distinct();
      case Interface:
        return directExtendedInterfacesOf(vertex)
            .flatMap(this::selfAndImplementedInterfaces)
            .distinct();
      default:
        throw new AssertionError("Unexpected vertex type!");
    }
  }

  /**
   * Recursively obtains all interfaces this interface extends, including transitively extended
   * interfaces.
   */
  @Nonnull
  private Stream<ClassType> selfAndImplementedInterfaces(Vertex vertex) {
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
      if (classVertex.type == VertexType.Interface) {
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
    return vertex.type == VertexType.Interface;
  }

  public boolean isClass(@Nonnull ClassType type) {
    Vertex vertex = lazyScanResult.get().typeToVertex.get(type);
    if (vertex == null) {
      throw new IllegalArgumentException("Could not find '" + type + "' in hierarchy.");
    }
    return vertex.type == VertexType.Class;
  }

  /**
   * Visits the subgraph of the specified <code>vertex</code> and calls the <code>visitor</code> for
   * each vertex in the subgraph. If <code>includeSelf</code> is true, the <code>visitor</code> is
   * also called with the <code>vertex</code>.
   */
  private Stream<ClassType> visitSubgraph(
      Graph<Vertex, Edge> graph, Vertex vertex, boolean includeSelf) {
    Stream<ClassType> subgraph = includeSelf ? Stream.of(vertex.javaClassType) : Stream.empty();
    switch (vertex.type) {
      case Interface:
        return Stream.concat(
            subgraph,
            graph.incomingEdgesOf(vertex).stream()
                .filter(
                    edge ->
                        edge.type == EdgeType.ClassDirectlyImplements
                            || edge.type == EdgeType.InterfaceDirectlyExtends)
                .map(graph::getEdgeSource)
                .flatMap(directSubtype -> visitSubgraph(graph, directSubtype, true)));
      case Class:
        return Stream.concat(
            subgraph,
            graph.incomingEdgesOf(vertex).stream()
                .filter(edge -> edge.type == EdgeType.ClassDirectlyExtends)
                .map(graph::getEdgeSource)
                .flatMap(directSubclass -> visitSubgraph(graph, directSubclass, true)));
      default:
        throw new AssertionError("Unknown vertex type!");
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
  private ScanResult scanView(View view) {
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
    Vertex classVertex = new Vertex(type, VertexType.Class);
    graph.addVertex(classVertex);
    return classVertex;
  }

  @Nonnull
  private static Vertex createAndAddInterfaceVertex(Graph<Vertex, Edge> graph, ClassType type) {
    Vertex interfaceVertex = new Vertex(type, VertexType.Interface);
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

    enum VertexType {
      Class,
      Interface
    }

    /**
     * @see #javaClassType
     * @see #type
     */
    protected static class Vertex {
      @Nonnull final ClassType javaClassType;
      @Nonnull final VertexType type;

      Vertex(@Nonnull ClassType javaClassType, @Nonnull VertexType type) {
        this.javaClassType = javaClassType;
        this.type = type;
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
    @Nonnull private final Graph<Vertex, Edge> graph;
    @Nonnull private Optional<Vertex> classVertexItBase;

    public SuperClassVertexIterator(Vertex classVertex) {
      graph = lazyScanResult.get().graph;
      classVertexItBase = Optional.of(classVertex);
    }

    @Override
    public boolean hasNext() {
      return classVertexItBase.isPresent();
    }

    @Override
    public Vertex next() {
      Optional<Vertex> currentSuperClass = classVertexItBase;
      classVertexItBase = directSuperClassOf(classVertexItBase.get()).findAny();
      return currentSuperClass.get();
    }
  }
}
