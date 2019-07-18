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
    visitSubgraph(interfaceNode, false, subnode -> implementers.add(subnode.type));
    return implementers;
  }

  @Nonnull
  @Override
  public Set<JavaClassType> subclassesOf(@Nonnull JavaClassType classType) {
    ScanResult scanResult = lazyScanResult.get();
    ClassNode classNode = scanResult.typeToClassNode.get(classType);

    Set<JavaClassType> subclasses = new HashSet<>();
    visitSubgraph(classNode, false, subnode -> subclasses.add(subnode.type));
    return subclasses;
  }

  @Nonnull
  @Override
  public Set<JavaClassType> implementedInterfacesOf(@Nonnull JavaClassType classType) {
    return Stream.concat(Stream.of(classType), superClassesOf(classType).stream())
        .flatMap(type -> sootClassFor(type).getInterfaces().stream())
        .map(this::sootClassFor)
        .flatMap(this::selfAndImplementedInterfaces)
        .collect(Collectors.toSet());
  }

  @Nonnull
  private Stream<JavaClassType> selfAndImplementedInterfaces(SootClass iface) {
    return Stream.concat(Stream.of(iface.getType()), iface.getInterfaces().stream());
  }

  @Nullable
  @Override
  public JavaClassType superClassOf(@Nonnull JavaClassType classType) {
    return sootClassFor(classType).getSuperclass().orElse(null);
  }

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

  static class ScanResult {
    final Map<JavaClassType, ClassNode> typeToClassNode;
    final Map<JavaClassType, InterfaceNode> typeToInterfaceNode;

    private ScanResult(
        Map<JavaClassType, ClassNode> typeToClassNode,
        Map<JavaClassType, InterfaceNode> typeToInterfaceNode) {
      this.typeToClassNode = typeToClassNode;
      this.typeToInterfaceNode = typeToInterfaceNode;
    }

    static class Node {
      final JavaClassType type;

      Node(JavaClassType type) {
        this.type = type;
      }
    }

    static class InterfaceNode extends Node {
      final Set<ClassNode> directImplementers;
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

    static class ClassNode extends Node {
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
