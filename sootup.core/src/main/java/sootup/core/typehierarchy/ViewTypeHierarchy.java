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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.jgrapht.Graph;
import org.jgrapht.graph.SimpleDirectedGraph;
import sootup.core.frontend.ResolveException;
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

  private final Supplier<ScanResult> lazyScanResult = Suppliers.memoize(this::scanView);

  @Nonnull private final View<? extends SootClass<?>> view;

  /** to allow caching use Typehierarchy.fromView() to get/create the Typehierarchy. */
  public ViewTypeHierarchy(@Nonnull View<? extends SootClass<?>> view) {
    this.view = view;
  }

  @Nonnull
  @Override
  public Set<ClassType> implementersOf(@Nonnull ClassType interfaceType) {
    Vertex vertex = lazyScanResult.get().typeToVertex.get(interfaceType);
    if (vertex == null) {
      throw new ResolveException("Could not find " + interfaceType + " in hierarchy.");
    }
    if (vertex.type != VertexType.Interface) {
      throw new IllegalArgumentException(interfaceType + " is not an interface.");
    }
    return subtypesOf(interfaceType);
  }

  @Nonnull
  @Override
  public Set<ClassType> subclassesOf(@Nonnull ClassType classType) {
    Vertex vertex = lazyScanResult.get().typeToVertex.get(classType);
    if (vertex == null) {
      throw new ResolveException("Could not find " + classType + " in hierarchy.");
    }
    if (vertex.type != VertexType.Class) {
      throw new IllegalArgumentException(classType + " is not a class.");
    }
    return subtypesOf(classType);
  }

  @Nonnull
  @Override
  public Set<ClassType> subtypesOf(@Nonnull ClassType type) {
    ScanResult scanResult = lazyScanResult.get();
    Vertex vertex = scanResult.typeToVertex.get(type);
    if (vertex == null) {
      throw new ResolveException("Could not find " + type + " in hierarchy.");
    }

    Set<ClassType> subclasses = new HashSet<>();
    // We now traverse the subgraph of the vertex to find all its subtypes
    visitSubgraph(
        scanResult.graph, vertex, false, subvertex -> subclasses.add(subvertex.javaClassType));
    return subclasses;
  }

  @Nonnull
  @Override
  public Set<ClassType> directSubtypesOf(@Nonnull ClassType type) {
    ScanResult scanResult = lazyScanResult.get();
    Vertex vertex = scanResult.typeToVertex.get(type);
    if (vertex == null) {
      throw new ResolveException("Could not find " + type + " in hierarchy.");
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

    return subclasses;
  }

  @Nonnull
  public List<Vertex> superClassesOf(@Nonnull Vertex classVertex, boolean includingSelf) {
    ScanResult scanResult = lazyScanResult.get();
    Graph<Vertex, Edge> graph = scanResult.graph;

    List<Vertex> superClasses = new ArrayList<>();
    if (includingSelf) {
      superClasses.add(classVertex);
    }

    Optional<Vertex> superClass =
        graph.outgoingEdgesOf(classVertex).stream()
            .filter(edge -> edge.type == EdgeType.ClassDirectlyExtends)
            .map(graph::getEdgeTarget)
            .findAny();
    while (superClass.isPresent()) {
      superClasses.add(superClass.get());
      superClass =
          graph.outgoingEdgesOf(superClass.get()).stream()
              .filter(edge -> edge.type == EdgeType.ClassDirectlyExtends)
              .map(graph::getEdgeTarget)
              .findAny();
    }

    return superClasses;
  }

  public Stream<Vertex> directlyImplementedInterfacesOf(@Nonnull Vertex classVertex) {
    Graph<Vertex, Edge> graph = lazyScanResult.get().graph;
    return graph.outgoingEdgesOf(classVertex).stream()
        .filter(edge -> edge.type == EdgeType.ClassDirectlyImplements)
        .map(graph::getEdgeTarget);
  }

  public Stream<Vertex> directlyExtendedInterfacesOf(@Nonnull Vertex interfaceVertex) {
    Graph<Vertex, Edge> graph = lazyScanResult.get().graph;
    return graph.outgoingEdgesOf(interfaceVertex).stream()
        .filter(edge -> edge.type == EdgeType.InterfaceDirectlyExtends)
        .map(graph::getEdgeTarget);
  }

  public Stream<Vertex> directSuperClassOf(@Nonnull Vertex classVertex) {
    Graph<Vertex, Edge> graph = lazyScanResult.get().graph;
    return graph.outgoingEdgesOf(classVertex).stream()
        .filter(edge -> edge.type == EdgeType.ClassDirectlyExtends)
        .map(graph::getEdgeTarget);
  }

  public Set<ClassType> directlyImplementedInterfacesOf(@Nonnull ClassType classType) {
    Vertex vertex = lazyScanResult.get().typeToVertex.get(classType);
    if (vertex == null) {
      throw new IllegalStateException("Could not find '" + classType + "' in hierarchy.");
    }
    if (vertex.type != VertexType.Class) {
      throw new IllegalArgumentException(classType + " is not a class.");
    }
    return directlyImplementedInterfacesOf(vertex)
        .map(v -> v.javaClassType)
        .collect(Collectors.toSet());
  }

  @Nonnull
  public Set<ClassType> directlyExtendedInterfacesOf(@Nonnull ClassType interfaceType) {
    Vertex vertex = lazyScanResult.get().typeToVertex.get(interfaceType);
    if (vertex == null) {
      throw new IllegalStateException("Could not find " + interfaceType + " in hierarchy.");
    }
    if (vertex.type != VertexType.Interface) {
      throw new IllegalArgumentException(interfaceType + " is not a class.");
    }
    return directlyExtendedInterfacesOf(vertex)
        .map(v -> v.javaClassType)
        .collect(Collectors.toSet());
  }

  /**
   * method exists for completeness - superClassOf() / which is basically SootClass.getSuperClass()
   * should be more performant.
   */
  @Nullable
  @Deprecated
  public ClassType directSuperClassOf(@Nonnull ClassType classType) {
    Vertex vertex = lazyScanResult.get().typeToVertex.get(classType);
    if (vertex == null) {
      throw new IllegalStateException("Could not find " + classType + " in hierarchy.");
    }
    Graph<Vertex, Edge> graph = lazyScanResult.get().graph;
    List<Vertex> list =
        graph.outgoingEdgesOf(vertex).stream()
            .filter(edge -> edge.type == EdgeType.ClassDirectlyExtends)
            .map(graph::getEdgeTarget)
            .collect(Collectors.toList());

    if (list.isEmpty()) {
      /* is java.lang.Object */
      return null;
    } else if (list.size() > 1) {
      throw new RuntimeException(classType + "cannot have multiple superclasses");
    } else {
      return list.get(0).javaClassType;
    }
  }

  @Nonnull
  @Override
  public Set<ClassType> implementedInterfacesOf(@Nonnull ClassType type) {
    ScanResult scanResult = lazyScanResult.get();
    Vertex vertex = scanResult.typeToVertex.get(type);

    if (vertex == null) {
      throw new IllegalStateException("Could not find " + type + " in hierarchy for view " + view);
    }

    switch (vertex.type) {
      case Class:
        // We ascend from vertex through its superclasses to java.lang.Object.
        // For each superclass, we take the interfaces it implements and merge
        // them together in a Set.
        List<Vertex> superClasses = superClassesOf(vertex, true);
        return superClasses.stream()
            .flatMap(this::directlyImplementedInterfacesOf)
            .flatMap(this::selfAndImplementedInterfaces)
            .collect(Collectors.toSet());
      case Interface:
        return directlyExtendedInterfacesOf(vertex)
            .flatMap(this::selfAndImplementedInterfaces)
            .collect(Collectors.toSet());
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

  @Nullable
  @Override
  public ClassType superClassOf(@Nonnull ClassType classType) {
    return sootClassFor(classType).getSuperclass().orElse(null);
  }

  public boolean isInterface(@Nonnull ClassType type) {
    Vertex vertex = lazyScanResult.get().typeToVertex.get(type);
    if (vertex == null) {
      throw new RuntimeException("Could not find " + type + " in hierarchy.");
    }
    return vertex.type == VertexType.Interface;
  }

  public boolean isClass(@Nonnull ClassType type) {
    Vertex vertex = lazyScanResult.get().typeToVertex.get(type);
    if (vertex == null) {
      throw new RuntimeException("Could not find " + type + " in hierarchy.");
    }
    return vertex.type == VertexType.Class;
  }
  /**
   * Visits the subgraph of the specified <code>vertex</code> and calls the <code>visitor</code> for
   * each vertex in the subgraph. If <code>includeSelf</code> is true, the <code>visitor</code> is
   * also called with the <code>vertex</code>.
   */
  private static void visitSubgraph(
      Graph<Vertex, Edge> graph, Vertex vertex, boolean includeSelf, Consumer<Vertex> visitor) {
    if (includeSelf) {
      visitor.accept(vertex);
    }
    switch (vertex.type) {
      case Interface:
        graph.incomingEdgesOf(vertex).stream()
            .filter(
                edge ->
                    edge.type == EdgeType.ClassDirectlyImplements
                        || edge.type == EdgeType.InterfaceDirectlyExtends)
            .map(graph::getEdgeSource)
            .forEach(directSubtype -> visitSubgraph(graph, directSubtype, true, visitor));
        break;
      case Class:
        graph.incomingEdgesOf(vertex).stream()
            .filter(edge -> edge.type == EdgeType.ClassDirectlyExtends)
            .map(graph::getEdgeSource)
            .forEach(directSubclass -> visitSubgraph(graph, directSubclass, true, visitor));
        break;
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
  private ScanResult scanView() {
    Map<ClassType, Vertex> typeToVertex = new HashMap<>();
    Graph<Vertex, Edge> graph = new SimpleDirectedGraph<>(null, null, false);

    view.getClasses().forEach(sootClass -> addSootClassToGraph(sootClass, typeToVertex, graph));
    return new ScanResult(typeToVertex, graph);
  }

  private static void addSootClassToGraph(
      SootClass<?> sootClass, Map<ClassType, Vertex> typeToVertex, Graph<Vertex, Edge> graph) {
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

  @Nonnull
  private SootClass<?> sootClassFor(@Nonnull ClassType classType) {
    return view.getClassOrThrow(classType);
  }

  @Override
  public void addType(@Nonnull SootClass<?> sootClass) {
    ScanResult scanResult = lazyScanResult.get();
    addSootClassToGraph(sootClass, scanResult.typeToVertex, scanResult.graph);
  }

  /** Holds a vertex for each {@link ClassType} encountered during the scan. */
  static class ScanResult {

    enum VertexType {
      Class,
      Interface
    }

    /**
     * @see #javaClassType
     * @see #type
     */
    static class Vertex {
      @Nonnull final ClassType javaClassType;
      @Nonnull final VertexType type;

      Vertex(@Nonnull ClassType javaClassType, @Nonnull VertexType type) {
        this.javaClassType = javaClassType;
        this.type = type;
      }
    }

    enum EdgeType {
      /** Edge to an interface vertex this interface extends directly, non-transitively. */
      InterfaceDirectlyExtends,
      /** Edge to an interface extending this interface directly, non-transitively. */
      ClassDirectlyImplements,
      /** Edge to a class this class is directly subclassed by, non-transitively. */
      ClassDirectlyExtends
    }

    /** @see #type */
    static class Edge {
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
}
