package de.upb.swt.soot.callgraph.typehierarchy;
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
import de.upb.swt.soot.core.frontend.AbstractClassSource;
import de.upb.swt.soot.core.frontend.ResolveException;
import de.upb.swt.soot.core.jimple.common.expr.JSpecialInvokeExpr;
import de.upb.swt.soot.core.model.AbstractClass;
import de.upb.swt.soot.core.model.Method;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.core.views.View;
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
    TypeHierarchy hierarchy = TypeHierarchy.fromView(view);

    return hierarchy.subtypesOf(m.getDeclClassType()).stream()
        .map(
            subtype ->
                view.getClass(subtype)
                    .orElseThrow(
                        () ->
                            new ResolveException(
                                "Could not resolve " + subtype + ", but found it in hierarchy.")))
        .flatMap(abstractClass -> abstractClass.getMethods().stream())
        .filter(potentialTarget -> canDispatch(m, potentialTarget.getSignature(), hierarchy))
        .filter(method -> !((SootMethod) method).isAbstract())
        .map(Method::getSignature)
        .collect(Collectors.toSet());
  }

  /**
   * Searches the view for classes that implement or override the method <code>m</code> and returns
   * the set of method signatures that a method call could resolve to within the given classes.
   */
  @Nonnull
  public static Set<MethodSignature> resolveAbstractDispatchInClasses(
      View view, MethodSignature m, Set<ClassType> classes) {
    TypeHierarchy hierarchy = TypeHierarchy.fromView(view);

    return hierarchy.subtypesOf(m.getDeclClassType()).stream()
        .map(
            subtype ->
                view.getClass(subtype)
                    .orElseThrow(
                        () ->
                            new ResolveException(
                                "Could not resolve " + subtype + ", but found it in hierarchy.")))
        .filter(c -> classes.contains(c.getType()))
        .flatMap(abstractClass -> abstractClass.getMethods().stream())
        .filter(potentialTarget -> canDispatch(m, potentialTarget.getSignature(), hierarchy))
        .filter(method -> !((SootMethod) method).isAbstract())
        .map(Method::getSignature)
        .collect(Collectors.toSet());
  }

  /**
   * <b>Warning!</b> Assumes that for an abstract dispatch, <code>potentialTarget</code> is declared
   * in the same or a subtype of the declaring class of <code>called</code>.
   *
   * <p>For a concrete dispatch, assumes that <code>potentialTarget</code> is declared in the same
   * or a supertype of the declaring class of <code>called</code>.
   *
   * @return Whether name and parameters are equal and the return type of <code>potentialTarget
   *     </code> is compatible with the return type of <code>called</code>.
   */
  public static boolean canDispatch(
      MethodSignature called, MethodSignature potentialTarget, TypeHierarchy hierarchy) {
    return called.getName().equals(potentialTarget.getName())
        && called.getParameterTypes().equals(potentialTarget.getParameterTypes())
        && (called.getType().equals(potentialTarget.getType())
            || hierarchy.isSubtype(called.getType(), potentialTarget.getType()));
  }

  /**
   * Searches for the signature of the method that is the concrete implementation of <code>m</code>.
   * This is done by checking each superclass and the class itself for whether it contains the
   * concrete implementation.
   */
  @Nonnull
  public static MethodSignature resolveConcreteDispatch(View view, MethodSignature m) {
    TypeHierarchy hierarchy = TypeHierarchy.fromView(view);

    ClassType superClassType = m.getDeclClassType();
    do {
      ClassType finalSuperClassType = superClassType;
      AbstractClass<? extends AbstractClassSource> superClass =
          view.getClass(superClassType)
              .orElseThrow(
                  () ->
                      new ResolveException(
                          "Did not find class " + finalSuperClassType + " in View"));

      Method concreteMethod =
          superClass.getMethods().stream()
              .filter(potentialTarget -> canDispatch(m, potentialTarget.getSignature(), hierarchy))
              .findAny()
              .orElse(null);
      if (!((SootMethod) concreteMethod).isAbstract()) {
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
    if (((SootMethod) specialMethod).isPrivate()) {
      return specialMethodSig;
    }

    if (TypeHierarchy.fromView(view)
        .isSubtype(container.getDeclClassType(), specialMethodSig.getDeclClassType())) {
      return resolveConcreteDispatch(view, specialMethodSig);
    }

    return specialMethodSig;
  }
}
