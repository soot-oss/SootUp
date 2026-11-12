package sootup.callgraph;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2026 Markus Schmidt and others
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

import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.NonNull;
import sootup.core.model.MethodModifier;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ArrayType;
import sootup.core.types.ClassType;
import sootup.core.types.Type;
import sootup.core.views.View;

/**
 * Resolution of signature-polymorphic methods (JVMS 2.9.3, 5.4.3.3), e.g. {@code
 * MethodHandle.invokeExact}, {@code VarHandle.get}, {@code MethodHandle.linkToStatic}.
 *
 * <p>Call site keeps its own descriptor, e.g. {@code invokeExact(String,String,int)int}; declared
 * method is {@code native varargs invokeExact(Object[])Object}. Plain sub-signature lookup misses.
 * Rule: declared in {@code java.lang.invoke.MethodHandle} or {@code VarHandle}, single {@code
 * Object[]} param, {@code ACC_VARARGS | ACC_NATIVE}, and only method of that name in the class.
 * Flags, not the package-private {@code @PolymorphicSignature} annotation.
 *
 * <p>Cost: callers ask only after a regular lookup missed. Non-holder classes rejected by two
 * string compares on already-stored names (no FQN allocation).
 */
public final class SignaturePolymorphicMethods {

  private static final String INVOKE_PACKAGE = "java.lang.invoke";

  private SignaturePolymorphicMethods() {}

  /** {@code java.lang.invoke.MethodHandle} or {@code java.lang.invoke.VarHandle}. */
  public static boolean isHolder(@NonNull ClassType type) {
    String name = type.getClassName();
    return (name.equals("MethodHandle") || name.equals("VarHandle"))
        && type.getPackageName().getName().equals(INVOKE_PACKAGE);
  }

  /** Whether {@code method} is signature polymorphic per JVMS 2.9.3. */
  public static boolean isSignaturePolymorphic(@NonNull SootMethod method) {
    return isHolder(method.getDeclaringClassType()) && hasPolymorphicShape(method);
  }

  /**
   * Resolves the declared signature-polymorphic method {@code callSite} refers to.
   *
   * @return declared method, or empty if {@code callSite} names no such method or the holder class
   *     is not in the view
   */
  @NonNull
  public static Optional<? extends SootMethod> resolve(
      @NonNull View view, @NonNull MethodSignature callSite) {
    return view.getClass(callSite.getDeclClassType())
        .flatMap(c -> resolveInHierarchy(view, c, callSite.getName()));
  }

  /**
   * Like {@link #resolve(SootClass, String)}, but walks superclasses from {@code start} (JVMS
   * 5.4.3.3 step 3): call-site owner may be a JDK-internal subclass, e.g. {@code
   * BoundMethodHandle.invokeBasic}.
   */
  @NonNull
  public static Optional<? extends SootMethod> resolveInHierarchy(
      @NonNull View view, @NonNull SootClass start, @NonNull String name) {
    SootClass c = start;
    while (c != null) {
      if (isHolder(c.getType())) {
        return resolve(c, name);
      }
      c = c.getSuperclass().flatMap(view::getClass).orElse(null);
    }
    return Optional.empty();
  }

  /**
   * Resolves signature-polymorphic method {@code name} declared in {@code holder}.
   *
   * @return declared method, or empty if {@code holder} is no holder class or has no such method
   */
  @NonNull
  public static Optional<? extends SootMethod> resolve(
      @NonNull SootClass holder, @NonNull String name) {
    if (!isHolder(holder.getType())) {
      return Optional.empty();
    }
    SootMethod found = null;
    for (SootMethod m : holder.getMethods()) {
      if (m.getName().equals(name)) {
        if (found != null) {
          // JVMS: must be the only method of that name
          return Optional.empty();
        }
        found = m;
      }
    }
    return found != null && hasPolymorphicShape(found) ? Optional.of(found) : Optional.empty();
  }

  private static boolean hasPolymorphicShape(@NonNull SootMethod m) {
    if (!m.isNative() || !MethodModifier.isVarargs(m.getModifiers())) {
      return false;
    }
    List<Type> params = m.getParameterTypes();
    if (params.size() != 1 || !(params.get(0) instanceof ArrayType arr)) {
      return false;
    }
    return arr.getDimension() == 1
        && arr.getBaseType() instanceof ClassType base
        && base.getClassName().equals("Object")
        && base.getPackageName().getName().equals("java.lang");
  }
}
