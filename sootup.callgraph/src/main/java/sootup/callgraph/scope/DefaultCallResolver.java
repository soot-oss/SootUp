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
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.views.View;

/** The default {@link CallResolver}: excludes calls originating from library classes. */
public class DefaultCallResolver implements CallResolver {

  @NonNull private final View view;

  public DefaultCallResolver(@NonNull View view) {
    this.view = view;
  }

  @Override
  public ExplorationVerdict tryAdvance(
      @NonNull SootMethod method, @NonNull InvokableStmt statement) {
    return verdictFor(method);
  }

  /** Excludes the calls of library methods as a whole, so that their bodies are not loaded. */
  @Override
  @NonNull
  public ExplorationVerdict tryAdvanceMethod(@NonNull SootMethod method) {
    return verdictFor(method);
  }

  @NonNull
  private ExplorationVerdict verdictFor(@NonNull SootMethod method) {
    return view.getClass(method.getDeclaringClassType())
        .filter(SootClass::isLibraryClass)
        .map(sc -> ExplorationVerdict.STOP_AFTER_CALL)
        .orElse(ExplorationVerdict.EXPLORE_METHOD);
  }
}
