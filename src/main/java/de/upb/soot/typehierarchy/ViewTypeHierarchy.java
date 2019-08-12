package de.upb.soot.typehierarchy;

import com.google.common.base.Suppliers;
import com.ibm.wala.util.graph.labeled.SlowSparseNumberedLabeledGraph;
import de.upb.soot.core.AbstractClass;
import de.upb.soot.core.SootClass;
import de.upb.soot.frontends.AbstractClassSource;
import de.upb.soot.frontends.ResolveException;
import de.upb.soot.typehierarchy.ViewTypeHierarchy.ScanResult.ClassNode;
import de.upb.soot.typehierarchy.ViewTypeHierarchy.ScanResult.EdgeType;
import de.upb.soot.typehierarchy.ViewTypeHierarchy.ScanResult.InterfaceNode;
import de.upb.soot.typehierarchy.ViewTypeHierarchy.ScanResult.Node;
import de.upb.soot.types.JavaClassType;
import de.upb.soot.util.StreamUtils;
import de.upb.soot.views.View;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO Update wiki

/**
 * Full documentation is in the <a
 * href="https://github.com/secure-software-engineering/soot-reloaded/wiki/Type-Hierarchy-Algorithm">wiki</a>.
 */
class ViewTypeHierarchy implements TypeHierarchy {

  private static final Logger log = LoggerFactory.getLogger(ViewTypeHierarchy.class);

  private final Supplier<ScanResult> lazyScanResult = Suppliers.memoize(this::scanView);

  @Nonnull private final View view;

  ViewTypeHierarchy(@Nonnull View view) {
    this.view = view;
  }

  @Nonnull
  @Override
  public Set<JavaClassType> implementersOf(@Nonnull JavaClassType interfaceType) {
    ScanResult scanResult = lazyScanResult.get();
    InterfaceNode interfaceNode = scanResult.typeToInterfaceNode.get(interfaceType);

    Set<JavaClassType> implementers = new HashSet<>();
    // We now traverse the subgraph of interfaceNode to find all its subtypes
    visitSubgraph(
        scanResult.graph, interfaceNode, false, subnode -> implementers.add(subnode.type));
    return implementers;
  }

  @Nonnull
  @Override
  public Set<JavaClassType> subclassesOf(@Nonnull JavaClassType classType) {
    ScanResult scanResult = lazyScanResult.get();
    ClassNode classNode = scanResult.typeToClassNode.get(classType);

    Set<JavaClassType> subclasses = new HashSet<>();
    // We now traverse the subgraph of classNode to find all its subtypes
    visitSubgraph(scanResult.graph, classNode, false, subnode -> subclasses.add(subnode.type));
    return subclasses;
  }

  @Nonnull
  private List<ClassNode> superClassesOf(@Nonnull ClassNode classNode, boolean includingSelf) {
    ScanResult scanResult = lazyScanResult.get();
    SlowSparseNumberedLabeledGraph<Node, EdgeType> graph = scanResult.graph;

    List<ClassNode> superClasses = new ArrayList<>();
    if (includingSelf) {
      superClasses.add(classNode);
    }

    Iterator<? extends Node> superclassIterator =
        graph.getSuccNodes(classNode, EdgeType.ClassDirectlyExtends);
    while (superclassIterator.hasNext()) {
      Node superclass = superclassIterator.next();
      superClasses.add((ClassNode) superclass);
      superclassIterator = graph.getSuccNodes(superclass, EdgeType.ClassDirectlyExtends);
    }

    return superClasses;
  }

  private Stream<InterfaceNode> directlyImplementedInterfacesOf(ClassNode classNode) {
    SlowSparseNumberedLabeledGraph<Node, EdgeType> graph = lazyScanResult.get().graph;
    return StreamUtils.iteratorToStream(
            graph.getSuccNodes(classNode, EdgeType.ClassDirectlyImplements))
        .map(node -> (InterfaceNode) node);
  }

  private Stream<InterfaceNode> directlyExtendedInterfacesOf(InterfaceNode interfaceNode) {
    SlowSparseNumberedLabeledGraph<Node, EdgeType> graph = lazyScanResult.get().graph;
    return StreamUtils.iteratorToStream(
            graph.getSuccNodes(interfaceNode, EdgeType.InterfaceDirectlyExtends))
        .map(node -> (InterfaceNode) node);
  }

  @Nonnull
  @Override
  public Set<JavaClassType> implementedInterfacesOf(@Nonnull JavaClassType type) {
    ScanResult scanResult = lazyScanResult.get();
    ClassNode classNode = scanResult.typeToClassNode.get(type);
    if (classNode != null) {
      // We ascend from classNode through its superclasses to java.lang.Object.
      // For each superclass, we take the interfaces it implements and merge
      // them together in a Set.
      List<ClassNode> superClasses = superClassesOf(classNode, true);
      return superClasses.stream()
          .flatMap(this::directlyImplementedInterfacesOf)
          .flatMap(this::selfAndImplementedInterfaces)
          .collect(Collectors.toSet());
    }

    InterfaceNode interfaceNode = scanResult.typeToInterfaceNode.get(type);
    if (interfaceNode != null) {
      return directlyExtendedInterfacesOf(interfaceNode)
          .flatMap(this::selfAndImplementedInterfaces)
          .collect(Collectors.toSet());
    }

    throw new ResolveException("Could not find " + type + " in hierarchy for view " + view);
  }

  /**
   * Recursively obtains all interfaces this interface extends, including transitively extended
   * interfaces.
   */
  @Nonnull
  private Stream<JavaClassType> selfAndImplementedInterfaces(InterfaceNode node) {
    ScanResult scanResult = lazyScanResult.get();
    SlowSparseNumberedLabeledGraph<Node, EdgeType> graph = scanResult.graph;

    Stream<? extends Node> extendedInterfaces =
        StreamUtils.iteratorToStream(graph.getSuccNodes(node, EdgeType.InterfaceDirectlyExtends));

    return Stream.concat(
        Stream.of(node.type),
        extendedInterfaces.flatMap(
            extendedInterface -> selfAndImplementedInterfaces((InterfaceNode) extendedInterface)));
  }

  @Nullable
  @Override
  public JavaClassType superClassOf(@Nonnull JavaClassType classType) {
    return sootClassFor(classType).getSuperclass().orElse(null);
  }

  /**
   * Visits the subgraph of the specified <code>node</code> and calls the <code>visitor</code> for
   * each node in the subgraph. If <code>includeSelf</code> is true, the <code>visitor</code> is
   * also called with the <code>node</code>.
   */
  private static void visitSubgraph(
      SlowSparseNumberedLabeledGraph<Node, EdgeType> graph,
      Node node,
      boolean includeSelf,
      Consumer<Node> visitor) {
    if (includeSelf) {
      visitor.accept(node);
    }
    if (node instanceof InterfaceNode) {
      graph
          .getPredNodes(node, EdgeType.ClassDirectlyImplements)
          .forEachRemaining(
              directImplementer -> visitSubgraph(graph, directImplementer, true, visitor));
      graph
          .getPredNodes(node, EdgeType.InterfaceDirectlyExtends)
          .forEachRemaining(directExtender -> visitSubgraph(graph, directExtender, true, visitor));
    } else if (node instanceof ClassNode) {
      graph
          .getPredNodes(node, EdgeType.ClassDirectlyExtends)
          .forEachRemaining(directSubclass -> visitSubgraph(graph, directSubclass, true, visitor));
    } else {
      throw new AssertionError("Unknown node type!");
    }
  }

  /**
   * This method scans the view by iterating over its classes and creating a graph node for each
   * one. When a class is encountered that extends another one or implements an interface, the graph
   * node of the extended class or implemented interface is connected to the node of the subtype.
   *
   * <p>We distinguish between interface and class nodes, as interfaces may have direct implementers
   * as well as other interfaces that extend them.
   *
   * <p>In the graph structure, a type is only connected to its direct subtypes.
   */
  private ScanResult scanView() {
    long startNanos = System.nanoTime();
    Map<JavaClassType, ScanResult.ClassNode> typeToClassNode = new HashMap<>();
    Map<JavaClassType, ScanResult.InterfaceNode> typeToInterfaceNode = new HashMap<>();
    SlowSparseNumberedLabeledGraph<Node, EdgeType> graph =
        new SlowSparseNumberedLabeledGraph<>(EdgeType.NoMeaning);

    view.getClassesStream()
        .filter(aClass -> aClass instanceof SootClass)
        .map(aClass -> (SootClass) aClass)
        .forEach(
            sootClass -> {
              if (sootClass.isInterface()) {
                InterfaceNode node =
                    typeToInterfaceNode.computeIfAbsent(
                        sootClass.getType(),
                        type -> {
                          InterfaceNode interfaceNode = new InterfaceNode(type);
                          graph.addNode(interfaceNode);
                          return interfaceNode;
                        });
                for (JavaClassType extendedInterface : sootClass.getInterfaces()) {
                  InterfaceNode extendedInterfaceNode =
                      typeToInterfaceNode.computeIfAbsent(
                          extendedInterface,
                          type -> {
                            InterfaceNode interfaceNode = new InterfaceNode(type);
                            graph.addNode(interfaceNode);
                            return interfaceNode;
                          });
                  graph.addEdge(node, extendedInterfaceNode, EdgeType.InterfaceDirectlyExtends);
                }
              } else {
                ClassNode node =
                    typeToClassNode.computeIfAbsent(
                        sootClass.getType(),
                        type -> {
                          ClassNode classNode = new ClassNode(type);
                          graph.addNode(classNode);
                          return classNode;
                        });
                for (JavaClassType implementedInterface : sootClass.getInterfaces()) {
                  // TODO This looks messy
                  InterfaceNode implementedInterfaceNode =
                      typeToInterfaceNode.computeIfAbsent(
                          implementedInterface,
                          type -> {
                            InterfaceNode interfaceNode = new InterfaceNode(type);
                            graph.addNode(interfaceNode);
                            return interfaceNode;
                          });
                  graph.addEdge(node, implementedInterfaceNode, EdgeType.ClassDirectlyImplements);
                }
                sootClass
                    .getSuperclass()
                    .ifPresent(
                        superClass -> {
                          ClassNode superClassNode =
                              typeToClassNode.computeIfAbsent(
                                  superClass,
                                  type -> {
                                    ClassNode classNode = new ClassNode(type);
                                    graph.addNode(classNode);
                                    return classNode;
                                  });
                          graph.addEdge(node, superClassNode, EdgeType.ClassDirectlyExtends);
                        });
              }
            });
    double runtimeMs = (System.nanoTime() - startNanos) / 1e6;
    log.info("Type hierarchy scan took " + runtimeMs + " ms");
    return new ScanResult(typeToClassNode, typeToInterfaceNode, graph);
  }

  @Nonnull
  private SootClass sootClassFor(@Nonnull JavaClassType classType) {
    AbstractClass<? extends AbstractClassSource> aClass =
        view.getClass(classType)
            .orElseThrow(
                () -> new ResolveException("Could not find " + classType + " in view " + view));
    if (!(aClass instanceof SootClass)) {
      throw new ResolveException("" + classType + " is not a regular Java class");
    }
    return (SootClass) aClass;
  }

  /** Holds a node for each {@link JavaClassType} encountered during the scan. */
  static class ScanResult {

    // TODO Probably don't need two types each here?
    enum EdgeType {
      /** Edge to an interface node this interface extends directly, non-transitively. */
      InterfaceDirectlyExtends,
      /** Edge to an interface extending this interface directly, non-transitively. */
      ClassDirectlyImplements,
      /** Edge to a class this class is directly subclassed by, non-transitively. */
      ClassDirectlyExtends,
      /** Used as the default edge label. */
      NoMeaning
    }

    /** Holds all nodes corresponding to classes (excluding interfaces). */
    @Nonnull final Map<JavaClassType, ClassNode> typeToClassNode;
    /** Holds all nodes corresponding to interfaces. */
    @Nonnull final Map<JavaClassType, InterfaceNode> typeToInterfaceNode;

    @Nonnull final SlowSparseNumberedLabeledGraph<Node, EdgeType> graph;

    private ScanResult(
        @Nonnull Map<JavaClassType, ClassNode> typeToClassNode,
        @Nonnull Map<JavaClassType, InterfaceNode> typeToInterfaceNode,
        @Nonnull SlowSparseNumberedLabeledGraph<Node, EdgeType> graph) {
      this.typeToClassNode = typeToClassNode;
      this.typeToInterfaceNode = typeToInterfaceNode;
      this.graph = graph;
    }

    /**
     * @see ClassNode
     * @see InterfaceNode
     * @see #type
     */
    static class Node {
      @Nonnull final JavaClassType type;

      Node(@Nonnull JavaClassType type) {
        this.type = type;
      }
    }

    static class InterfaceNode extends Node {

      private InterfaceNode(@Nonnull JavaClassType type) {
        super(type);
      }
    }

    static class ClassNode extends Node {

      private ClassNode(@Nonnull JavaClassType type) {
        super(type);
      }
    }
  }
}
