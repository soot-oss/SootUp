package sootup.callgraph.scope;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2025 Markus Schmidt
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
import sootup.core.model.SootMethod;

/**
 * Controls which calls (edges) are expanded during call graph construction, at the pre-dispatch
 * checkpoint: invoked once per method that is expanded, <b>before</b> its body is loaded and before
 * dynamic dispatch is resolved for its statements. The returned {@link CallFilter} decides for the
 * whole method ({@link CallFilter#all()}, {@link CallFilter#none()}) or per invokable statement
 * ({@link CallFilter#of}). A method whose calls are all excluded still appears as a node in the
 * resulting call graph if it is reached as a target, it simply ends up with no outgoing edges.
 */
@FunctionalInterface
public interface CallResolver {

  /**
   * Decides which calls of {@code caller} are resolved and expanded.
   *
   * @param caller the method whose calls are about to be resolved
   * @return the calls to resolve; with {@link CallFilter#none()} the body of {@code caller} is not
   *     loaded
   */
  @NonNull CallFilter tryAdvanceMethod(@NonNull SootMethod caller);

  /** Returns a {@link CallResolver} that resolves every call. */
  @NonNull
  static CallResolver all() {
    return caller -> CallFilter.all();
  }
}
