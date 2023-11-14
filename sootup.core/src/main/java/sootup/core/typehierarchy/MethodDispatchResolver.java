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
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
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
}
