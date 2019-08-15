package de.upb.soot.typehierarchy;

import de.upb.soot.core.AbstractClass;
import de.upb.soot.core.Method;
import de.upb.soot.core.SootMethod;
import de.upb.soot.frontends.AbstractClassSource;
import de.upb.soot.frontends.ResolveException;
import de.upb.soot.jimple.common.expr.JSpecialInvokeExpr;
import de.upb.soot.signatures.MethodSignature;
import de.upb.soot.types.JavaClassType;
import de.upb.soot.views.View;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

public final class MethodDispatchResolver {
  private MethodDispatchResolver() {}

  /**
   * Searches the view for classes that implement or override the method <code>m</code> and returns
   * the set of method signatures that a method call could resolve to.
   */
  @Nonnull
  public static Set<MethodSignature> resolveAbstractDispatch(View view, MethodSignature m) {
    TypeHierarchy hierarchy = view.typeHierarchy();

    return hierarchy.subtypesOf(m.getDeclClassType()).stream()
        .map(
            subtype ->
                view.getClass(subtype)
                    .orElseThrow(
                        () ->
                            new ResolveException(
                                "Could not resolve " + subtype + ", but found it in hierarchy.")))
        .map(subtype -> subtype.getMethod(m.getSubSignature()))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .filter(method -> method instanceof SootMethod && !((SootMethod) method).isAbstract())
        .map(Method::getSignature)
        .collect(Collectors.toSet());
  }

  /**
   * Searches for the signature of the method that is the concrete implementation of <code>m</code>.
   * This is done by checking each superclass and the class itself for whether it contains the
   * concrete implementation.
   */
  @Nonnull
  public static MethodSignature resolveConcreteDispatch(View view, MethodSignature m) {
    TypeHierarchy hierarchy = view.typeHierarchy();
    JavaClassType superClassType = m.getDeclClassType();
    do {
      JavaClassType finalSuperClassType = superClassType;
      AbstractClass<? extends AbstractClassSource> superClass =
          view.getClass(superClassType)
              .orElseThrow(
                  () ->
                      new ResolveException(
                          "Did not find class " + finalSuperClassType + " in View"));

      Method concreteMethod = superClass.getMethod(m.getSubSignature()).orElse(null);
      if (concreteMethod instanceof SootMethod && !((SootMethod) concreteMethod).isAbstract()) {
        return concreteMethod.getSignature();
      }

      superClassType = hierarchy.superClassOf(superClassType);
    } while (superClassType != null);

    throw new ResolveException("Could not find concrete method for " + m);
  }

  /**
   * Resolves the actual method called by the <code>specialInvokeExpr</code> that is contained by
   * <code>container</code>.
   */
  @Nonnull
  public static MethodSignature resolveSpecialDispatch(
      View view, JSpecialInvokeExpr specialInvokeExpr, MethodSignature container) {
    MethodSignature specialMethodSig = specialInvokeExpr.getMethodSignature();
    if (specialMethodSig.getSubSignature().getName().equals("<init>")) {
      return specialMethodSig;
    }

    Method specialMethod =
        view.getClass(specialMethodSig.getDeclClassType())
            .flatMap(cl -> cl.getMethod(specialMethodSig))
            .orElse(null);
    if (specialMethod instanceof SootMethod && ((SootMethod) specialMethod).isPrivate()) {
      return specialMethodSig;
    }

    if (view.typeHierarchy()
        .isSubtype(container.getDeclClassType(), specialMethodSig.getDeclClassType())) {
      return resolveConcreteDispatch(view, specialMethodSig);
    }

    return specialMethodSig;
  }
}
