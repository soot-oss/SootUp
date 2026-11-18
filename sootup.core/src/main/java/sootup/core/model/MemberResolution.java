package sootup.core.model;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2026 Hazem Gamal and others
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.jspecify.annotations.NonNull;
import sootup.core.signatures.FieldSignature;
import sootup.core.signatures.MethodSignature;
import sootup.core.signatures.MethodSubSignature;
import sootup.core.typehierarchy.TypeHierarchy;
import sootup.core.types.ClassType;
import sootup.core.views.View;

/**
 * Resolves method and field references to the class members they denote, following the Java Virtual
 * Machine Specification (JVMS 5.4.3.2 to 5.4.3.4). A reference such as {@code <B: void m()>} may
 * denote a member that is declared in a superclass or a superinterface of {@code B}.
 *
 * <p>The lookup only follows classes that are part of the view: the supertypes of a missing class
 * are unknown, so the search along that path ends there. Method resolution takes the
 * superinterfaces from the view's type hierarchy.
 */
public final class MemberResolution {

  private MemberResolution() {}

  /**
   * Resolves a method reference like the JVM: the method declared in the referenced class or its
   * superclasses (or the signature polymorphic method of that name, e.g. {@code
   * MethodHandle.invokeExact}) (for a reference to an interface: the interface, then the public
   * methods of {@code java.lang.Object}), otherwise a method of a superinterface. Among the
   * superinterface methods, which exclude private and static ones, the only non-abstract one of the
   * maximally specific methods is chosen, i.e. of those not overridden in a subinterface; if there
   * is none or more than one, a maximally specific one.
   *
   * @param view the view providing the classes
   * @param signature the method reference
   * @return the resolved method, or empty if no matching method is declared
   */
  @NonNull
  public static Optional<? extends SootMethod> resolveMethod(
      @NonNull View view, @NonNull MethodSignature signature) {
    MethodSubSignature subSignature = signature.getSubSignature();
    ClassType referenced = signature.getDeclClassType();
    Optional<? extends SootClass> referencedClass = view.getClass(referenced);
    if (!referencedClass.isPresent()) {
      return Optional.empty();
    }
    boolean interfaceReference = referencedClass.get().isInterface();

    // the class and its superclasses; for an interface: the interface and java.lang.Object
    ClassType current = referenced;
    Set<ClassType> visitedClasses = new HashSet<>();
    while (current != null && visitedClasses.add(current)) {
      Optional<? extends SootClass> clazz = view.getClass(current);
      if (!clazz.isPresent()) {
        break;
      }
      Optional<? extends SootMethod> method = clazz.get().getMethod(subSignature);
      boolean objectOfInterface = interfaceReference && !current.equals(referenced);
      if (method.isPresent()
          && (!objectOfInterface || (method.get().isPublic() && !method.get().isStatic()))) {
        return method;
      }
      // a signature polymorphic method matches any descriptor at the call site
      Optional<? extends SootMethod> polymorphic =
          SignaturePolymorphicMethods.resolve(clazz.get(), subSignature.getName());
      if (polymorphic.isPresent()) {
        return polymorphic;
      }
      current = clazz.get().getSuperclass().orElse(null);
    }

    // the superinterfaces: private and static methods are not inherited
    TypeHierarchy hierarchy = view.getTypeHierarchy();
    List<SootMethod> candidates =
        hierarchy
            .implementedInterfacesOf(referenced)
            .flatMap(i -> view.getClass(i).flatMap(c -> c.getMethod(subSignature)).stream())
            .filter(m -> !m.isPrivate() && !m.isStatic())
            .collect(Collectors.toList());
    // maximally specific: not declared in a superinterface of another candidate's interface
    List<SootMethod> maximallySpecific = new ArrayList<>();
    for (SootMethod candidate : candidates) {
      boolean overridden = false;
      for (SootMethod other : candidates) {
        if (other != candidate
            && hierarchy.isSubtype(
                candidate.getDeclaringClassType(), other.getDeclaringClassType())) {
          overridden = true;
          break;
        }
      }
      if (!overridden) {
        maximallySpecific.add(candidate);
      }
    }
    List<SootMethod> nonAbstract =
        maximallySpecific.stream().filter(m -> !m.isAbstract()).collect(Collectors.toList());
    if (nonAbstract.size() == 1) {
      return Optional.of(nonAbstract.get(0));
    }
    return maximallySpecific.stream().findFirst();
  }

  /**
   * Resolves a field reference: the field declared in the referenced class, otherwise in its
   * superinterfaces, otherwise in its superclass (recursively).
   *
   * @param view the view providing the classes
   * @param signature the field reference
   * @return the resolved field, or empty if no matching field is declared
   */
  @NonNull
  public static Optional<? extends SootField> resolveField(
      @NonNull View view, @NonNull FieldSignature signature) {
    return resolveField(view, signature, signature.getDeclClassType(), new HashSet<>());
  }

  private static Optional<? extends SootField> resolveField(
      View view, FieldSignature signature, ClassType type, Set<ClassType> visited) {
    if (!visited.add(type)) {
      return Optional.empty();
    }
    Optional<? extends SootClass> clazz = view.getClass(type);
    if (!clazz.isPresent()) {
      return Optional.empty();
    }
    Optional<? extends SootField> field = clazz.get().getField(signature.getSubSignature());
    if (field.isPresent()) {
      return field;
    }
    for (ClassType i : clazz.get().getInterfaces()) {
      Optional<? extends SootField> inInterface = resolveField(view, signature, i, visited);
      if (inInterface.isPresent()) {
        return inInterface;
      }
    }
    return clazz
        .get()
        .getSuperclass()
        .flatMap(superclass -> resolveField(view, signature, superclass, visited));
  }
}
