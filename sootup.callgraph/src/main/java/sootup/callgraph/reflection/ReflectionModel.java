package sootup.callgraph.reflection;

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

import org.jspecify.annotations.NonNull;
import sootup.core.model.Body;
import sootup.core.model.SootMethod;

/**
 * Makes reflective calls ({@code Method.invoke}, {@code Class.newInstance}, ...) explicit: returns
 * a body in which each resolved reflective call is preceded by plain Jimple stmts (allocations,
 * direct invokes, field accesses) modeling its effect. Consulted once per method whose body a call
 * graph algorithm inspects - pluggable like {@link sootup.callgraph.scope.CallResolver}.
 *
 * <p>Implementations must be deterministic per method: CHA, RTA, Spark and Qilin may ask for the
 * same method repeatedly (and Spark reuses a CHA graph built from the same model), so repeated
 * calls should return the same {@link Body} instance - see {@link AbstractReflectionModel}.
 */
@FunctionalInterface
public interface ReflectionModel {

  /**
   * @param method the method owning {@code body}
   * @param body the body to rewrite (not modified)
   * @return {@code body} itself if nothing reflective was resolved, else a rewritten copy
   */
  @NonNull Body resolve(@NonNull SootMethod method, @NonNull Body body);

  /** Model that resolves nothing - the default everywhere. */
  @NonNull
  static ReflectionModel none() {
    return NoReflectionModel.INSTANCE;
  }

  /** Whether this model is {@link #none()}. */
  default boolean isNone() {
    return this == NoReflectionModel.INSTANCE;
  }
}
