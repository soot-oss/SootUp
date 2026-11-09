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
 * Controls which calls (edges) are expanded during call graph construction, at the pre-dispatch
 * checkpoint: invoked once per invokable statement of a method that is expanded, <b>before</b>
 * dynamic dispatch is resolved for that statement. No specific callee is known yet, so only {@link
 * ExplorationVerdict#EXPLORE_METHOD} vs. anything else matters here: {@link
 * ExplorationVerdict#STOP_AFTER_CALL} and {@link ExplorationVerdict#STOP} are equivalent at this
 * checkpoint (both mean "do not resolve this statement's call(s)"). Excluding every statement of a
 * method has the effect of pruning that method's expansion entirely: the method still appears as a
 * node in the resulting call graph if it is reached as a target, it simply ends up with no outgoing
 * edges, since none of its calls are resolved.
 */
@FunctionalInterface
public interface CallResolver {

  /**
   * Decides whether the call(s) triggered by {@code statement} in {@code caller} should be resolved
   * and expanded at all.
   *
   * @param caller the source (caller) method
   * @param statement the invokable statement causing the call
   * @return the verdict for this statement; only {@link ExplorationVerdict#EXPLORE_METHOD} vs.
   *     non-{@link ExplorationVerdict#EXPLORE_METHOD} is significant here
   */
  @NonNull ExplorationVerdict tryAdvance(
      @NonNull SootMethod caller, @NonNull InvokableStmt statement);

  /** Returns a {@link CallResolver} that explores every statement. */
  @NonNull
  static CallResolver all() {
    return (caller, statement) -> ExplorationVerdict.EXPLORE_METHOD;
  }
}
