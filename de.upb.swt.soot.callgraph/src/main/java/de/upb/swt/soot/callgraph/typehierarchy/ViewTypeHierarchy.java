package de.upb.swt.soot.callgraph.typehierarchy;

import com.google.common.base.Suppliers;
import de.upb.swt.soot.callgraph.typehierarchy.ViewTypeHierarchy.ScanResult.Edge;
import de.upb.swt.soot.callgraph.typehierarchy.ViewTypeHierarchy.ScanResult.EdgeType;
import de.upb.swt.soot.callgraph.typehierarchy.ViewTypeHierarchy.ScanResult.Vertex;
import de.upb.swt.soot.callgraph.typehierarchy.ViewTypeHierarchy.ScanResult.VertexType;
import de.upb.swt.soot.core.frontend.AbstractClassSource;
import de.upb.swt.soot.core.frontend.ResolveException;
import de.upb.swt.soot.core.model.AbstractClass;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.core.views.View;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Full documentation is in the <a
 * href="https://github.com/secure-software-engineering/soot-reloaded/wiki/Type-Hierarchy-Algorithm">wiki</a>.
 *
 * @author Christian Brüggemann
 */
public class ViewTypeHierarchy implements MutableTypeHierarchy {

  private static final Logger log = LoggerFactory.getLogger(ViewTypeHierarchy.class);

  private final Supplier<ScanResult> lazyScanResult = Suppliers.memoize(this::scanView);

  @Nonnull private final View view;

  public ViewTypeHierarchy(@Nonnull View view) {
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
      throw new IllegalArgumentException(interfaceType + " is not an interface");
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
      throw new IllegalArgumentException(classType + " is not a class");
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
  private List<Vertex> superClassesOf(@Nonnull Vertex classVertex, boolean includingSelf) {
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

  private Stream<Vertex> directlyImplementedInterfacesOf(Vertex classVertex) {
    Graph<Vertex, Edge> graph = lazyScanResult.get().graph;
    return graph.outgoingEdgesOf(classVertex).stream()
        .filter(edge -> edge.type == EdgeType.ClassDirectlyImplements)
        .map(graph::getEdgeTarget);
  }

  private Stream<Vertex> directlyExtendedInterfacesOf(Vertex interfaceVertex) {
    Graph<Vertex, Edge> graph = lazyScanResult.get().graph;
    return graph.outgoingEdgesOf(interfaceVertex).stream()
        .filter(edge -> edge.type == EdgeType.InterfaceDirectlyExtends)
        .map(graph::getEdgeTarget);
  }

  @Nonnull
  @Override
  public Set<ClassType> implementedInterfacesOf(@Nonnull ClassType type) {
    ScanResult scanResult = lazyScanResult.get();
    Vertex vertex = scanResult.typeToVertex.get(type);

    if (vertex == null) {
      throw new ResolveException("Could not find " + type + " in hierarchy for view " + view);
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
    long startNanos = System.nanoTime();
    Map<ClassType, Vertex> typeToVertex = new HashMap<>();
    Graph<Vertex, Edge> graph = new SimpleDirectedGraph<>(null, null, false);

    view.getClassesStream()
        .filter(aClass -> aClass instanceof SootClass)
        .map(aClass -> (SootClass) aClass)
        .forEach(sootClass -> addSootClassToGraph(sootClass, typeToVertex, graph));
    double runtimeMs = (System.nanoTime() - startNanos) / 1e6;
    log.info("Type hierarchy scan took " + runtimeMs + " ms");
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

  @Nonnull
  private SootClass sootClassFor(@Nonnull ClassType classType) {
    AbstractClass<? extends AbstractClassSource> aClass =
        view.getClass(classType)
            .orElseThrow(
                () -> new ResolveException("Could not find " + classType + " in view " + view));
    if (!(aClass instanceof SootClass)) {
      throw new ResolveException("" + classType + " is not a regular Java class");
    }
    return (SootClass) aClass;
  }

  @Override
  public void addType(SootClass sootClass) {
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
