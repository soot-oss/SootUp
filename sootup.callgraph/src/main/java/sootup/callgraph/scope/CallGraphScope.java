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
import sootup.core.signatures.MethodSignature;

/**
 * Controls which calls (edges) are expanded during call graph construction.
 *
 * <p>There are two checkpoints:
 *
 * <ul>
 *   <li>{@link #tryAdvance(SootMethod, InvokableStmt)} is invoked once per invokable statement of a
 *       method that is expanded, <b>before</b> dynamic dispatch is resolved for that statement. No
 *       specific callee is known yet, so only {@link ExplorationVerdict#EXPLORE_METHOD} vs.
 *       anything else matters here: {@link ExplorationVerdict#STOP_AFTER_CALL} and {@link
 *       ExplorationVerdict#STOP} are equivalent at this checkpoint (both mean "do not resolve this
 *       statement's call(s)"). Excluding every statement of a method has the effect of pruning
 *       that method's expansion entirely: the method still appears as a node in the resulting call
 *       graph if it is reached as a target, it simply ends up with no outgoing edges, since none
 *       of its calls are resolved.
 *   <li>{@link #advanceCall(SootMethod, MethodSignature, InvokableStmt)} is invoked once per
 *       dynamic-dispatch candidate <b>after</b> {@code resolveCall} has resolved it for a
 *       statement that passed the first checkpoint. Here all three verdicts are distinct: {@link
 *       ExplorationVerdict#EXPLORE_METHOD} admits the edge and expands the callee, {@link
 *       ExplorationVerdict#STOP_AFTER_CALL} admits the edge but does not expand the callee via
 *       this edge, and {@link ExplorationVerdict#STOP} drops the edge entirely.
 * </ul>
 */
public interface CallGraphScope {

  enum ExplorationVerdict {
    EXPLORE_METHOD,
    STOP_AFTER_CALL,
    STOP
  }

  /**
   * Decides whether the call(s) triggered by {@code statement} in {@code caller} should be
   * resolved and expanded at all. Invoked once per invokable statement of a method being expanded,
   * before dynamic dispatch is resolved for that statement. Default: explore everything.
   *
   * @param caller the source (caller) method
   * @param statement the invokable statement causing the call
   * @return the verdict for this statement; only {@link ExplorationVerdict#EXPLORE_METHOD} vs.
   *     non-{@link ExplorationVerdict#EXPLORE_METHOD} is significant here
   */
  default ExplorationVerdict tryAdvance(@NonNull SootMethod caller, @NonNull InvokableStmt statement) {
    return ExplorationVerdict.EXPLORE_METHOD;
  }

  /**
   * Decides whether a single already-resolved dynamic-dispatch candidate should be admitted as an
   * edge in the call graph, and whether the callee should be expanded via this edge. Invoked once
   * per candidate returned by {@code resolveCall} for a statement that passed {@link
   * #tryAdvance(SootMethod, InvokableStmt)}.
   *
   * <p>{@code callee} is intentionally a bare {@link MethodSignature}, not a resolved {@link
   * SootMethod}: most implementations only need identity/name/declaring-class information already
   * present on the signature. Implementations that need method-level metadata (e.g. whether the
   * target is abstract or a library class) should resolve it lazily via their own {@link
   * sootup.core.views.View} reference, the same way {@link DefaultCallGraphScope} already holds
   * one — this avoids forcing a lookup for every dynamic-dispatch candidate when most scopes don't
   * need one. Default: include and expand everything.
   *
   * @param caller the caller method
   * @param callee the resolved dynamic-dispatch candidate's signature
   * @param statement the invokable statement causing the call
   * @return the verdict for this edge
   */
  default ExplorationVerdict advanceCall(
      @NonNull SootMethod caller,
      @NonNull MethodSignature callee,
      @NonNull InvokableStmt statement) {
    return ExplorationVerdict.EXPLORE_METHOD;
  }
}
