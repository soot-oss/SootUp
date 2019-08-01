package de.upb.soot.typehierarchy;

import com.google.common.base.Suppliers;
import de.upb.soot.core.AbstractClass;
import de.upb.soot.core.SootClass;
import de.upb.soot.frontends.AbstractClassSource;
import de.upb.soot.frontends.ResolveException;
import de.upb.soot.typehierarchy.ViewTypeHierarchy.ScanResult.ClassNode;
import de.upb.soot.typehierarchy.ViewTypeHierarchy.ScanResult.InterfaceNode;
import de.upb.soot.typehierarchy.ViewTypeHierarchy.ScanResult.Node;
import de.upb.soot.types.JavaClassType;
import de.upb.soot.views.View;
import java.util.HashMap;
import java.util.HashSet;
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
    visitSubgraph(interfaceNode, false, subnode -> implementers.add(subnode.type));
    return implementers;
  }

  @Nonnull
  @Override
  public Set<JavaClassType> subclassesOf(@Nonnull JavaClassType classType) {
    ScanResult scanResult = lazyScanResult.get();
    ClassNode classNode = scanResult.typeToClassNode.get(classType);

    Set<JavaClassType> subclasses = new HashSet<>();
    // We now traverse the subgraph of classNode to find all its subtypes
    visitSubgraph(classNode, false, subnode -> subclasses.add(subnode.type));
    return subclasses;
  }

  @Nonnull
  @Override
  public Set<JavaClassType> implementedInterfacesOf(@Nonnull JavaClassType classType) {
    // We ascend from classType through its superclasses to java.lang.Object.
    // For each superclass, we take the interfaces they implement and merge
    // them together in a Set.
    return Stream.concat(Stream.of(classType), superClassesOf(classType).stream())
        .flatMap(type -> sootClassFor(type).getInterfaces().stream())
        .map(this::sootClassFor)
        .flatMap(this::selfAndImplementedInterfaces)
        .collect(Collectors.toSet());
  }

  @Nonnull
  private Stream<JavaClassType> selfAndImplementedInterfaces(SootClass iface) {
    // TODO Accelerate this using graph structure
    Set<JavaClassType> parentInterfaces = iface.getInterfaces();
    if (parentInterfaces.isEmpty()) {
      return Stream.of(iface.getType());
    }

    return Stream.concat(
        Stream.of(iface.getType()),
        parentInterfaces.stream()
            .flatMap(classType -> selfAndImplementedInterfaces(sootClassFor(classType))));
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
  private static void visitSubgraph(Node node, boolean includeSelf, Consumer<Node> visitor) {
    if (includeSelf) {
      visitor.accept(node);
    }
    if (node instanceof InterfaceNode) {
      ((InterfaceNode) node)
          .directImplementers.forEach(
              directImplementer -> visitSubgraph(directImplementer, true, visitor));
      ((InterfaceNode) node)
          .extendingInterfaces.forEach(
              extendingInterface -> visitSubgraph(extendingInterface, true, visitor));
    } else if (node instanceof ClassNode) {
      ((ClassNode) node)
          .directSubclasses.forEach(directSubclass -> visitSubgraph(directSubclass, true, visitor));
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

    view.getClassesStream()
        .filter(aClass -> aClass instanceof SootClass)
        .map(aClass -> (SootClass) aClass)
        .forEach(
            sootClass -> {
              if (sootClass.isInterface()) {
                InterfaceNode node =
                    typeToInterfaceNode.computeIfAbsent(sootClass.getType(), InterfaceNode::new);
                for (JavaClassType extendedInterface : sootClass.getInterfaces()) {
                  InterfaceNode extendedInterfaceNode =
                      typeToInterfaceNode.computeIfAbsent(extendedInterface, InterfaceNode::new);
                  extendedInterfaceNode.extendingInterfaces.add(node);
                }
              } else {
                ClassNode node =
                    typeToClassNode.computeIfAbsent(sootClass.getType(), ClassNode::new);
                for (JavaClassType implementedInterface : sootClass.getInterfaces()) {
                  InterfaceNode implementedInterfaceNode =
                      typeToInterfaceNode.computeIfAbsent(implementedInterface, InterfaceNode::new);
                  implementedInterfaceNode.directImplementers.add(node);
                }
                sootClass
                    .getSuperclass()
                    .ifPresent(
                        superClass -> {
                          ClassNode superClassNode =
                              typeToClassNode.computeIfAbsent(superClass, ClassNode::new);
                          superClassNode.directSubclasses.add(node);
                        });
              }
            });
    double runtimeMs = (System.nanoTime() - startNanos) / 1e6;
    log.info("Type hierarchy scan took " + runtimeMs + " ms");
    return new ScanResult(typeToClassNode, typeToInterfaceNode);
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

    /** Holds all nodes corresponding to classes (excluding interfaces). */
    final Map<JavaClassType, ClassNode> typeToClassNode;
    /** Holds all nodes corresponding to interfaces. */
    final Map<JavaClassType, InterfaceNode> typeToInterfaceNode;

    private ScanResult(
        Map<JavaClassType, ClassNode> typeToClassNode,
        Map<JavaClassType, InterfaceNode> typeToInterfaceNode) {
      this.typeToClassNode = typeToClassNode;
      this.typeToInterfaceNode = typeToInterfaceNode;
    }

    /**
     * @see ClassNode
     * @see InterfaceNode
     * @see #type
     */
    static class Node {
      final JavaClassType type;

      Node(JavaClassType type) {
        this.type = type;
      }
    }

    /**
     * @see #directImplementers
     * @see #extendingInterfaces
     */
    static class InterfaceNode extends Node {

      /** All classes implementing this interface directly, non-transitively. */
      final Set<ClassNode> directImplementers;

      /** All interfaces extending this interface directly, non-transitively. */
      final Set<InterfaceNode> extendingInterfaces;

      private InterfaceNode(
          JavaClassType type,
          Set<ClassNode> directImplementers,
          Set<InterfaceNode> extendingInterfaces) {
        super(type);
        this.directImplementers = directImplementers;
        this.extendingInterfaces = extendingInterfaces;
      }

      private InterfaceNode(JavaClassType type) {
        this(type, new HashSet<>(), new HashSet<>());
      }
    }

    /** @see #directSubclasses */
    static class ClassNode extends Node {

      /** All classes that directly extend this class, non-transitively. */
      final Set<ClassNode> directSubclasses;

      private ClassNode(JavaClassType type, Set<ClassNode> directSubclasses) {
        super(type);
        this.directSubclasses = directSubclasses;
      }

      private ClassNode(JavaClassType type) {
        this(type, new HashSet<>());
      }
    }
  }
}
