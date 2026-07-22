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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.jspecify.annotations.NonNull;
import sootup.core.jimple.common.stmt.InvokableStmt;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.views.View;

/**
 * A {@link CallGraphScope} that applies the same filtering as {@link DefaultCallGraphScope}
 * (excluding calls originating from library classes), while additionally recording the signature of
 * every method whose calls were excluded, so callers can inspect what was pruned after the call
 * graph has been constructed, e.g. to diagnose why an expected method is missing from the call
 * graph.
 */
public class ExcludedCallsCollectingCallGraphScope extends DefaultCallGraphScope {

  private final Set<MethodSignature> visitedExcludedMethods = new HashSet<>();

  public ExcludedCallsCollectingCallGraphScope(@NonNull View view) {
    super(view);
  }

  @Override
  public Strategy includeCall(@NonNull SootMethod method, @NonNull InvokableStmt statement) {
    Strategy startegy = super.includeCall(method, statement);
    if (startegy == Strategy.IGNORE) {
      visitedExcludedMethods.add(method.getSignature());
    }
    return startegy;
  }

  /**
   * Returns the signatures of the methods whose calls were excluded from expansion so far.
   *
   * @return an unmodifiable view of the excluded method signatures
   */
  @NonNull
  public Set<MethodSignature> getVisitedExcludedMethods() {
    return Collections.unmodifiableSet(visitedExcludedMethods);
  }
}
