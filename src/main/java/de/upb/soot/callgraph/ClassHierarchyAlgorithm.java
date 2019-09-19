package de.upb.soot.callgraph;

import de.upb.soot.core.AbstractClass;
import de.upb.soot.core.Method;
import de.upb.soot.core.Modifier;
import de.upb.soot.core.SootMethod;
import de.upb.soot.frontends.AbstractClassSource;
import de.upb.soot.jimple.common.expr.AbstractInvokeExpr;
import de.upb.soot.signatures.MethodSignature;
import de.upb.soot.signatures.MethodSubSignature;
import de.upb.soot.typehierarchy.MethodDispatchResolver;
import de.upb.soot.typehierarchy.TypeHierarchy;
import de.upb.soot.types.JavaClassType;
import de.upb.soot.views.View;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

/**
 * This class implements CHA (Class Hierarchy Algorithm)
 *
 * @author Markus Schmidt
 * @author Christian Brüggemann
 * @author Ben Hermann
 */
public class ClassHierarchyAlgorithm extends AbstractCallGraphAlgorithm {
  @Nonnull private final View view;
  @Nonnull private final TypeHierarchy hierarchy;

  public ClassHierarchyAlgorithm(@Nonnull View view, @Nonnull TypeHierarchy hierarchy) {
    this.view = view;
    this.hierarchy = hierarchy;
  }

  @Nonnull
  @Override
  public CallGraph initialize(@Nonnull List<MethodSignature> entryPoints) {
    return constructCompleteCallGraph(view, entryPoints);
  }

  @Nonnull
  @Override
  public CallGraph addClass(@Nonnull CallGraph oldCallGraph, @Nonnull JavaClassType classType) {
    MutableCallGraph updated = oldCallGraph.copy();

    AbstractClass<? extends AbstractClassSource> clazz = view.getClassOrThrow(classType);
    Set<MethodSignature> newMethodSignatures =
        clazz.getMethods().stream().map(Method::getSignature).collect(Collectors.toSet());

    if (newMethodSignatures.stream().anyMatch(oldCallGraph::containsMethod)) {
      throw new IllegalArgumentException("CallGraph already contains methods from " + classType);
    }

    // Step 1: Add edges from the new methods to other methods
    Deque<MethodSignature> workList = new ArrayDeque<>(newMethodSignatures);
    Set<MethodSignature> processed = new HashSet<>(oldCallGraph.getMethodSignatures());
    processWorkList(view, workList, processed, updated);

    // Step 2: Add edges from old methods to methods overridden in the new class
    List<JavaClassType> superClasses = hierarchy.superClassesOf(classType);
    Set<JavaClassType> implementedInterfaces = hierarchy.implementedInterfacesOf(classType);
    Stream<JavaClassType> superTypes =
        Stream.concat(superClasses.stream(), implementedInterfaces.stream());

    Set<MethodSubSignature> newMethodSubSigs =
        newMethodSignatures.stream()
            .map(MethodSignature::getSubSignature)
            .collect(Collectors.toSet());

    superTypes
        .map(view::getClassOrThrow)
        .flatMap(superType -> superType.getMethods().stream())
        .map(Method::getSignature)
        .filter(
            superTypeMethodSig -> newMethodSubSigs.contains(superTypeMethodSig.getSubSignature()))
        .forEach(
            overriddenMethodSig -> {
              //noinspection OptionalGetWithoutIsPresent (We know this exists)
              MethodSignature overridingMethodSig =
                  clazz.getMethod(overriddenMethodSig.getSubSignature()).get().getSignature();

              for (MethodSignature callingMethodSig : oldCallGraph.callsTo(overriddenMethodSig)) {
                updated.addCall(callingMethodSig, overridingMethodSig);
              }
            });

    return updated;
  }

  @Override
  @Nonnull
  protected Stream<MethodSignature> resolveCall(SootMethod method, AbstractInvokeExpr invokeExpr) {
    MethodSignature targetMethodSignature = invokeExpr.getMethodSignature();

    if (Modifier.isStatic(targetMethodSignature.getModifiers())) {
      return Stream.of(targetMethodSignature);
    } else {
      return MethodDispatchResolver.resolveAbstractDispatch(view, targetMethodSignature).stream();
    }
  }
}
