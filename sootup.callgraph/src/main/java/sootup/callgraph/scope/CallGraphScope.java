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
import sootup.core.model.SootClass;
import sootup.core.signatures.MethodSignature;

/**
 * Controls which classes/methods are expanded during call graph construction.
 *
 * <p>{@link #filter(SootClass, MethodSignature)} is invoked once per method signature popped from
 * the work list, before its outgoing calls are resolved. Note that the method signature is added as
 * a vertex to the call graph beforehand regardless of the filter result, so an excluded method
 * still appears as a node in the resulting call graph -- it simply has no outgoing edges, since its
 * body is never analyzed.
 */
public interface CallGraphScope {
  /**
   * Decides whether the given method should be excluded from call graph expansion.
   *
   * @param sc the declaring class of {@code ms}
   * @param ms the method signature under consideration
   * @return {@code true} if the method's calls should NOT be resolved/added to the call graph,
   *     {@code false} if it should be processed normally
   */
  boolean filter(@NonNull SootClass sc, @NonNull MethodSignature ms);
}
