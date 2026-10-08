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

import java.util.List;
import org.jspecify.annotations.NonNull;
import sootup.core.jimple.common.expr.JDynamicInvokeExpr;

/**
 * Decides which methods an invokedynamic call site transfers control to. Shared by CHA, RTA, Spark
 * and Qilin (configured via {@code CallGraphConfigBuilder.dynamicInvokeResolver(...)}), so all
 * algorithms agree on which lambda bodies / method references are reachable.
 *
 * <p>Edges to the returned targets are attributed to the invokedynamic statement itself, not to the
 * later functional-interface call site. Pointer analyses bind the call site's arguments (the
 * captured values) as {@link DynamicInvokeTarget#captureParameterIndex(int)} says; no return value
 * flows back, since the invokedynamic produces the functional object, not the target's result.
 */
@FunctionalInterface
public interface DynamicInvokeResolver {

  /**
   * @param expr the invokedynamic expression
   * @return the methods {@code expr} may transfer control to; empty if unresolved
   */
  @NonNull List<DynamicInvokeTarget> resolve(@NonNull JDynamicInvokeExpr expr);

  /** Resolves nothing: invokedynamic call sites get no call targets. */
  @NonNull
  static DynamicInvokeResolver none() {
    return NoDynamicInvokeResolver.INSTANCE;
  }

  /**
   * Every method referenced by a {@link sootup.core.jimple.common.constant.MethodHandle} bootstrap
   * argument is a target, e.g. the synthetic lambda body passed to {@code LambdaMetafactory}. The
   * default everywhere.
   */
  @NonNull
  static DynamicInvokeResolver bootstrapMethodHandles() {
    return BootstrapMethodHandleResolver.INSTANCE;
  }

  /** Whether this resolver is {@link #none()}. */
  default boolean isNone() {
    return this == NoDynamicInvokeResolver.INSTANCE;
  }
}
