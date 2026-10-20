package sootup.callgraph.invokedynamic;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2026 Markus Schmidt
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
import java.util.Collections;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import sootup.core.jimple.common.Immediate;
import sootup.core.jimple.common.constant.MethodHandle;
import sootup.core.jimple.common.expr.JDynamicInvokeExpr;
import sootup.core.signatures.MethodSignature;

/**
 * Resolves an invokedynamic call site to every method referenced by a {@link MethodHandle} among
 * its bootstrap arguments. For {@code LambdaMetafactory} call sites that is the lambda body or
 * method reference, marked as {@link DynamicInvokeTarget#lambdaImplementation()} so pointer
 * analyses can bind captured values. Other bootstraps (e.g. {@code ObjectMethods} for records)
 * contribute their method handles as plain targets.
 */
public final class BootstrapMethodHandleResolver implements DynamicInvokeResolver {

  static final BootstrapMethodHandleResolver INSTANCE = new BootstrapMethodHandleResolver();

  private static final String LAMBDA_METAFACTORY = "java.lang.invoke.LambdaMetafactory";

  private BootstrapMethodHandleResolver() {}

  @NonNull
  @Override
  public List<DynamicInvokeTarget> resolve(@NonNull JDynamicInvokeExpr expr) {
    List<Immediate> args = expr.getBootstrapArgs();
    if (args.isEmpty()) {
      return Collections.emptyList();
    }
    MethodHandle implementation = lambdaImplementationHandle(expr);
    List<DynamicInvokeTarget> targets = new ArrayList<>(1);
    for (Immediate arg : args) {
      if (arg instanceof MethodHandle handle && handle.isMethodRef()) {
        targets.add(
            new DynamicInvokeTarget(
                (MethodSignature) handle.getReferenceSignature(),
                handle.getKind(),
                handle == implementation));
      }
    }
    return targets;
  }

  /** Whether {@code expr} is bootstrapped by {@code LambdaMetafactory.(alt)metafactory}. */
  public static boolean isLambdaMetafactory(@NonNull JDynamicInvokeExpr expr) {
    MethodSignature bsm = expr.getBootstrapMethodSignature();
    String name = bsm.getName();
    return bsm.getDeclClassType().getFullyQualifiedName().equals(LAMBDA_METAFACTORY)
        && (name.equals("metafactory") || name.equals("altMetafactory"));
  }

  /**
   * The implementation method handle of a {@code LambdaMetafactory} call site - its first {@link
   * MethodHandle} bootstrap argument - or {@code null} if {@code expr} is not one.
   */
  @Nullable
  public static MethodHandle lambdaImplementationHandle(@NonNull JDynamicInvokeExpr expr) {
    if (!isLambdaMetafactory(expr)) {
      return null;
    }
    for (Immediate arg : expr.getBootstrapArgs()) {
      if (arg instanceof MethodHandle handle) {
        return handle;
      }
    }
    return null;
  }
}
