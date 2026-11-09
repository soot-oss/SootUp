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
 * Controls which calls (edges) are expanded during call graph construction, at the post-dispatch
 * checkpoint: invoked once per dynamic-dispatch candidate <b>after</b> {@code resolveCall} has
 * resolved it for a statement that passed the {@link CallResolver} checkpoint. Here all three
 * {@link ExplorationVerdict} verdicts are distinct: {@link ExplorationVerdict#EXPLORE_METHOD}
 * admits the edge and expands the callee, {@link ExplorationVerdict#STOP_AFTER_CALL} admits the
 * edge but does not expand the callee via this edge, and {@link ExplorationVerdict#STOP} drops the
 * edge entirely.
 */
@FunctionalInterface
public interface VirtualCallResolver {

  /**
   * Decides whether a single already-resolved dynamic-dispatch candidate should be admitted as an
   * edge in the call graph, and whether the callee should be expanded via this edge.
   *
   * <p>{@code callee} is intentionally a bare {@link MethodSignature}, not a resolved {@link
   * SootMethod}: most implementations only need identity/name/declaring-class information already
   * present on the signature. Implementations that need method-level metadata (e.g. whether the
   * target is abstract or a library class) should resolve it lazily via their own {@link
   * sootup.core.views.View} reference, the same way {@link DefaultCallResolver} already holds one —
   * this avoids forcing a lookup for every dynamic-dispatch candidate when most resolvers don't
   * need one.
   *
   * @param caller the caller method
   * @param callee the resolved dynamic-dispatch candidate's signature
   * @param statement the invokable statement causing the call
   * @return the verdict for this edge
   */
  @NonNull ExplorationVerdict tryAdvanceCall(
      @NonNull SootMethod caller,
      @NonNull MethodSignature callee,
      @NonNull InvokableStmt statement);

  /** Returns a {@link VirtualCallResolver} that admits and expands every candidate. */
  @NonNull
  static VirtualCallResolver all() {
    return (caller, callee, statement) -> ExplorationVerdict.EXPLORE_METHOD;
  }
}
