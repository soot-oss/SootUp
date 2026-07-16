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
import sootup.core.model.SootClass;
import sootup.core.signatures.MethodSignature;

/**
 * A {@link CallGraphScope} that applies the same filtering as {@link DefaultCallGraphScope}
 * (excluding library classes), while additionally recording every excluded method signature so
 * callers can inspect what was pruned after the call graph has been constructed, e.g. to diagnose
 * why an expected method is missing from the call graph.
 */
public class ExcludedCallsCollectingCallGraphScope implements CallGraphScope {
  private final Set<MethodSignature> visitedExcludedMethods = new HashSet<>();

  @Override
  public boolean filter(@NonNull SootClass sc, @NonNull MethodSignature ms) {
    boolean isExcluded = sc.isLibraryClass();
    if (isExcluded) {
      visitedExcludedMethods.add(ms);
    }
    return isExcluded;
  }

  /**
   * Returns the method signatures that were excluded from expansion so far.
   *
   * @return an unmodifiable view of the excluded method signatures
   */
  @NonNull
  public Set<MethodSignature> getVisitedExcludedMethods() {
    return Collections.unmodifiableSet(visitedExcludedMethods);
  }
}
