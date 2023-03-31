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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import sootup.core.frontend.ResolveException;
import sootup.core.jimple.common.expr.JSpecialInvokeExpr;
import sootup.core.model.Method;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.views.View;

public final class MethodDispatchResolver {
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
        .map(
            subtype ->
                view.getClass(subtype)
                    .orElseThrow(
                        () ->
                            new ResolveException(
                                "Could not resolve " + subtype + ", but found it in hierarchy.")))
        .filter(
            sootClass -> {
              SootMethod sootMethod = sootClass.getMethod(m.getSubSignature()).orElse(null);
              // method is not implemented or not abstract
              return sootMethod == null || !sootMethod.isAbstract();
            })
        .map(sootClass -> new MethodSignature(sootClass.getType(), m.getSubSignature()))
        .collect(Collectors.toSet());
  }

  /**
   * Searches the view for classes that implement or override the method <code>m</code> and returns
   * the set of method signatures that a method call could resolve to.
   */
  @Nonnull
  public static Set<MethodSignature> resolveAbstractDispatch(
      View<? extends SootClass<?>> view, MethodSignature m) {
    TypeHierarchy hierarchy = view.getTypeHierarchy();

    return hierarchy.subtypesOf(m.getDeclClassType()).stream()
        .map(
            subtype ->
                view.getClass(subtype)
                    .orElseThrow(
                        () ->
                            new ResolveException(
                                "Could not resolve " + subtype + ", but found it in hierarchy.")))
        .map(sootClass -> findConcreteMethodInSootClass(sootClass, m))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .filter(method -> !method.isAbstract())
        .map(Method::getSignature)
        .collect(Collectors.toSet());
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
        .map(sootClass -> findConcreteMethodInSootClass(sootClass, m))
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
    TypeHierarchy hierarchy = view.getTypeHierarchy();
    ClassType superClassType = m.getDeclClassType();
    SootClass<?> startClass = view.getClass(superClassType).orElse(null);
    ArrayList<SootClass<?>> classesInHierachyOrder = new ArrayList<>();

    // search concrete method in the class itself and its super classes
    do {
      ClassType finalSuperClassType = superClassType;
      SootClass<?> superClass =
          view.getClass(superClassType)
              .orElseThrow(
                  () ->
                      new ResolveException(
                          "Did not find class " + finalSuperClassType + " in View"));

      classesInHierachyOrder.add(superClass);

      SootMethod concreteMethod = findConcreteMethodInSootClass(superClass, m).orElse(null);
      if (concreteMethod != null && !concreteMethod.isAbstract()) {
        // found method is not abstract
        return Optional.of(concreteMethod.getSignature());
      }
      if (concreteMethod != null && concreteMethod.isAbstract()) {
        if (startClass.isAbstract()
            && !startClass.getType().equals(concreteMethod.getDeclaringClassType())) {
          // A not implemented method of an abstract class results into an abstract method
          return Optional.empty();
        }
        // found method is abstract and the startclass is not abstract
        throw new ResolveException(
            "Could not find concrete method for " + m + " because the method is abstract");
      }

      superClassType = hierarchy.superClassOf(superClassType);
    } while (superClassType != null);

    // No super class contains the implemented method, search the concrete method in interfaces
    // first collect all interfaces and super interfaces
    List<SootClass<?>> worklist =
        classesInHierachyOrder.stream()
            .flatMap(sootClass -> getSootClassesOfInterfaces(view, sootClass).stream())
            .collect(Collectors.toList());
    ArrayList<SootClass<?>> processedInterface = new ArrayList<>();
    ArrayList<SootMethod> possibleDefaultMethods = new ArrayList<>();
    while (!worklist.isEmpty()) {
      SootClass<?> currentInterface = worklist.remove(0);
      if (processedInterface.contains(currentInterface)) {
        // interface was already processed
        continue;
      }

      // add found default method to possibleDefaultMethods
      Optional<? extends SootMethod> concreteMethod =
          findConcreteMethodInSootClass(currentInterface, m);
      concreteMethod.ifPresent(possibleDefaultMethods::add);

      // if no default message is found search the default message in super interfaces
      if (!concreteMethod.isPresent()) {
        worklist.addAll(getSootClassesOfInterfaces(view, currentInterface));
      }
      processedInterface.add(currentInterface);
    }

    if (!possibleDefaultMethods.isEmpty()) {
      // the interfaces are sorted by hierarchy
      possibleDefaultMethods.sort(
          (interface1, interface2) -> {
            // interface1 is a sub-interface of interface2
            if (hierarchy.isSubtype(
                interface2.getDeclaringClassType(), interface1.getDeclaringClassType())) return -1;
            // interface1 is a super-interface of interface2
            if (hierarchy.isSubtype(
                interface1.getDeclaringClassType(), interface2.getDeclaringClassType())) return 1;
            // due to multiple inheritance in interfaces
            return 0;
          });
      // return the lowest element in the hierarchy
      return Optional.of(possibleDefaultMethods.get(0).getSignature());
    }
    throw new ResolveException("Could not find concrete method for " + m);
  }

  /**
   * Returns all SootClasses of interfaces that are implemented in the given SootClass
   *
   * <p>returns a list of all SootClass Objects of interfaces that are associated with the given
   * sootClass parameter. The ClassTypes of the interfaces are converted to SootClasses and not
   * contained Interfaces are filtered.
   *
   * @param view the view that contains all searched SootClasses
   * @param sootClass it contains the interfaces
   * @return a list of SootClasses of the interfaces of sootClass
   */
  private static List<SootClass<?>> getSootClassesOfInterfaces(
      View<? extends SootClass<?>> view, SootClass<?> sootClass) {
    return sootClass.getInterfaces().stream()
        .map(view::getClass)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toList());
  }

  /**
   * finds the concrete method in a SootClass
   *
   * <p>this method returns the concrete method of given method signature in a SootClass. Due to
   * covariant, the given method signature can differ from the concrete method at the return type
   * The method goes through all methods of the given SootClass and searches for a method which can
   * dispatch.
   *
   * @param sootClass The method is searched in this SootClass
   * @param methodSignature the signature of the searched method
   * @return an Optional Object that can contain the found concrete method in the given SootClass
   */
  private static Optional<? extends SootMethod> findConcreteMethodInSootClass(
      SootClass<?> sootClass, MethodSignature methodSignature) {
    return sootClass.getMethods().stream()
        .filter(
            potentialTarget ->
                methodSignature
                    .getSubSignature()
                    .equals(potentialTarget.getSignature().getSubSignature()))
        .findAny();
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
