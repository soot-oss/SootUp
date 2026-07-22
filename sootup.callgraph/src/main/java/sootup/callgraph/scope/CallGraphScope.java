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
import sootup.core.jimple.common.stmt.InvokableStmt;
import sootup.core.model.SootMethod;

/**
 * Controls which calls (edges) are expanded during call graph construction.
 *
 * <p>{@link #includeCall(SootMethod, InvokableStmt)} is invoked once per invokable statement of a
 * method that is expanded, before the call(s) caused by that statement are resolved. Excluding
 * every call originating from a method has the effect of pruning that method's expansion entirely:
 * the method still appears as a node in the resulting call graph if it is reached as a target, it
 * simply ends up with no outgoing edges, since none of its calls are resolved.
 */
public interface CallGraphScope {

  enum Strategy {
    EXPLORE_METHOD,
    STOP_AFTER_CALL,
    IGNORE
  }

  /**
   * Decides whether a call from {@code method} represented by {@code statement} shall be added to
   * the call graph. Default: accept everything.
   *
   * @param method the source (caller) method
   * @param statement the invokable statement causing the call
   * @return {@code true} if the call should be included in the call graph
   */
  default Strategy includeCall(@NonNull SootMethod method, @NonNull InvokableStmt statement) {
    return Strategy.EXPLORE_METHOD;
  }
}
