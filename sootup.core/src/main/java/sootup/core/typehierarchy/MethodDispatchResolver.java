package sootup.core.typehierarchy;
/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2019-2022 Christian Brüggemann, Jonas Klauke
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

import com.google.common.collect.Sets;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sootup.core.IdentifierFactory;
import sootup.core.frontend.ResolveException;
import sootup.core.jimple.common.expr.JSpecialInvokeExpr;
import sootup.core.model.Method;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.views.View;

public final class MethodDispatchResolver {

  private static final Logger logger = LoggerFactory.getLogger(MethodDispatchResolver.class);

  private MethodDispatchResolver() {}

  /**
   * Searches the view for classes that are subtypes of the class contained in the signature.
   * returns method signatures to all subtypes. Abstract methods are filtered the returned set can
   * contain signatures of not implemented methods.
   */
  @Nonnull
  public static Set<MethodSignature> resolveAllDispatches(
      View<? extends SootClass<?>> view, MethodSignature m) {
    TypeHierarchy hierarchy = view.getTypeHierarchy();

    return hierarchy.subtypesOf(m.getDeclClassType()).stream()
        .filter(
            classType -> {
              SootMethod sootMethod =
                  view.getMethod(
                          view.getIdentifierFactory()
                              .getMethodSignature(classType, m.getSubSignature()))
                      .orElse(null);
              // methods are kept that are not implemented or not abstract
              return sootMethod == null || !sootMethod.isAbstract();
            })
        .map(
            classType ->
                view.getIdentifierFactory().getMethodSignature(classType, m.getSubSignature()))
        .collect(Collectors.toSet());
  }

  /**
   * Searches the view for classes that implement or override the method <code>m</code> and returns
   * the set of method signatures that a method call could resolve to.
   */
  @Nonnull
  public static Stream<MethodSignature> resolveAbstractDispatch(
      View<? extends SootClass<?>> view, MethodSignature m) {
    TypeHierarchy hierarchy = view.getTypeHierarchy();

    return hierarchy.subtypesOf(m.getDeclClassType()).stream()
        .map(
            sootClass ->
                view.getMethod(
                    view.getIdentifierFactory().getMethodSignature(sootClass, m.getSubSignature())))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .filter(method -> !method.isAbstract())
        .map(Method::getSignature);
  }

  /**
   * Searches the view for classes that implement or override the method <code>m</code> and returns
   * the set of method signatures that a method call could resolve to within the given classes.
   */
  @Nonnull
  public static Set<MethodSignature> resolveAllDispatchesInClasses(
      View<? extends SootClass<?>> view, MethodSignature m, Set<ClassType> classes) {
    TypeHierarchy hierarchy = view.getTypeHierarchy();

    return hierarchy.subtypesOf(m.getDeclClassType()).stream()
        .map(
            subtype ->
                view.getClass(subtype)
                    .orElseThrow(
                        () ->
                            new ResolveException(
                                "Could not resolve " + subtype + ", but found it in hierarchy.")))
        .filter(c -> classes.contains(c.getType()))
        .map(sootClass -> sootClass.getMethod(m.getSubSignature()))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .filter(method -> !method.isAbstract())
        .map(Method::getSignature)
        .collect(Collectors.toSet());
  }

  /**
   * Resolves all dispatches of a given call filtered by a set of given classes
   *
   * <p>searches the view for classes that can override the method <code>m</code> and returns the
   * set of method signatures that a method call could resolve to within the given classes. All
   * filtered signatures are added to the given set <code>filteredSignatures</code>.
   *
   * @param view it contains all classes and their connections.
   * @param m it defines the actual invoked method signature.
   * @param classes the set of classes that define possible dispatch targets of method signatures
   * @param filteredSignatures the set of method signatures which is filled with filtered method
   *     signatures in the execution of this method.
   * @return a set of method signatures that a method call could resolve to within the given classes
   */
  @Nonnull
  public static Set<MethodSignature> resolveAllDispatchesInClasses(
      View<? extends SootClass<?>> view,
      MethodSignature m,
      Set<ClassType> classes,
      Set<MethodSignature> filteredSignatures) {

    Set<MethodSignature> allSignatures = resolveAllDispatches(view, m);
    Set<MethodSignature> signatureInClasses = Sets.newHashSet();
    allSignatures.forEach(
        methodSignature -> {
          if (classes.contains(methodSignature.getDeclClassType())) {
            signatureInClasses.add(methodSignature);
          } else {
            filteredSignatures.add(methodSignature);
          }
        });

    return signatureInClasses;
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
        && (called.getType().equals(potentialTarget.getType()) // return types are equal
            || hierarchy.isSubtype(called.getType(), potentialTarget.getType())); // covariant
  }

  /**
   * Searches for the signature of the method that is the concrete implementation of <code>m</code>.
   * This is done by checking each superclass and the class itself for whether it contains the
   * concrete implementation.
   */
  @Nonnull
  public static Optional<MethodSignature> resolveConcreteDispatch(
      View<? extends SootClass<?>> view, MethodSignature m) {
    Optional<? extends SootMethod> methodOp = findConcreteMethod(view, m);
    if (methodOp.isPresent()) {
      SootMethod method = methodOp.get();
      if (method.isAbstract()) {
        return Optional.empty();
      }
      return Optional.of(method.getSignature());
    }
    return Optional.empty();
  }

  /**
   * searches the method object in the given hierarchy
   *
   * @param view it contains all classes
   * @param sig the signature of the searched method
   * @return the found method object, or null if the method was not found.
   */
  public static Optional<? extends SootMethod> findConcreteMethod(
      @Nonnull View<? extends SootClass<?>> view, @Nonnull MethodSignature sig) {
    IdentifierFactory identifierFactory = view.getIdentifierFactory();
    Optional<? extends SootMethod> startMethod = view.getMethod(sig);
    if (startMethod.isPresent()) {
      return startMethod;
    }
    TypeHierarchy typeHierarchy = view.getTypeHierarchy();

    List<ClassType> superClasses = typeHierarchy.superClassesOf(sig.getDeclClassType());
    for (ClassType superClassType : superClasses) {
      Optional<? extends SootMethod> method =
          view.getMethod(
              identifierFactory.getMethodSignature(superClassType, sig.getSubSignature()));
      if (method.isPresent()) {
        return method;
      }
    }
    Set<ClassType> interfaces = typeHierarchy.implementedInterfacesOf(sig.getDeclClassType());
    // interface1 is a sub-interface of interface2
    // interface1 is a super-interface of interface2
    // due to multiple inheritance in interfaces
    final HierarchyComparator hierarchyComparator = new HierarchyComparator(view);
    Optional<? extends SootMethod> defaultMethod =
        interfaces.stream()
            .map(
                classType ->
                    view.getMethod(
                        identifierFactory.getMethodSignature(classType, sig.getSubSignature())))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .min(
                (m1, m2) ->
                    hierarchyComparator.compare(
                        m1.getDeclaringClassType(), m2.getDeclaringClassType()));
    if (defaultMethod.isPresent()) {
      return defaultMethod;
    }
    logger.warn(
        "Could not find \""
            + sig.getSubSignature()
            + "\" in "
            + sig.getDeclClassType().getClassName()
            + " and in its superclasses and interfaces");
    return Optional.empty();
  }

  /**
   * Resolves the actual method called by the <code>specialInvokeExpr</code> that is contained by
   * <code>container</code>.
   */
  public static MethodSignature resolveSpecialDispatch(
      View<? extends SootClass<?>> view,
      JSpecialInvokeExpr specialInvokeExpr,
      MethodSignature container) {
    MethodSignature specialMethodSig = specialInvokeExpr.getMethodSignature();
    if (specialMethodSig.getSubSignature().getName().equals("<init>")) {
      return specialMethodSig;
    }

    SootMethod specialMethod =
        view.getClass(specialMethodSig.getDeclClassType())
            .flatMap(cl -> cl.getMethod(specialMethodSig.getSubSignature()))
            .orElse(null);
    if (specialMethod != null && specialMethod.isPrivate()) {
      return specialMethodSig;
    }

    if (view.getTypeHierarchy()
        .isSubtype(container.getDeclClassType(), specialMethodSig.getDeclClassType())) {
      return resolveConcreteDispatch(view, specialMethodSig).orElse(null);
    }

    return specialMethodSig;
  }
}
