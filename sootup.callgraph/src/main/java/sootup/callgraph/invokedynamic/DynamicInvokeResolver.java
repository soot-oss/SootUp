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
import sootup.core.model.Body;
import sootup.core.model.SootMethod;

/**
 * Decides which methods an invokedynamic call site transfers control to. Shared by CHA, RTA, Spark
 * and Qilin (configured via {@code CallGraphConfigBuilder.dynamicInvokeResolver(...)}), so all
 * algorithms agree on which lambda bodies / method references are reachable.
 *
 * <p>A {@code LambdaMetafactory} implementation is reached through the {@link FunctionalObject} the
 * call site creates: calls on that object dispatch to it, with arguments and return value. Only
 * with {@link #withCreationSiteEdges()} is it additionally called from the invokedynamic statement
 * itself (for views that lack the code calling the lambda). All other targets are called from the
 * invokedynamic statement, see {@link #creationSiteTargets}.
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

  /** Whether lambda implementations are also called from their invokedynamic statement. */
  default boolean creationSiteEdges() {
    return false;
  }

  /** This resolver, plus edges from each invokedynamic statement to its lambda implementation. */
  @NonNull
  default DynamicInvokeResolver withCreationSiteEdges() {
    return new CreationSiteEdgesResolver(this);
  }

  /** Targets called from the invokedynamic statement itself (see class doc). */
  @NonNull
  default List<DynamicInvokeTarget> creationSiteTargets(@NonNull JDynamicInvokeExpr expr) {
    List<DynamicInvokeTarget> targets = resolve(expr);
    if (creationSiteEdges()) {
      return targets;
    }
    return targets.stream().filter(t -> !t.lambdaImplementation()).toList();
  }

  /**
   * Makes the implicit calls of other invokedynamics explicit (string concatenation's {@code
   * toString()}, records' {@code equals}/{@code hashCode}/{@code toString}). Identity by default.
   *
   * @return {@code body} itself if nothing changed, else a rewritten copy
   */
  @NonNull
  default Body desugar(@NonNull SootMethod method, @NonNull Body body) {
    return body;
  }
}
