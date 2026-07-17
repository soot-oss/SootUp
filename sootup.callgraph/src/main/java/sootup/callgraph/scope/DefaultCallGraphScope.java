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
import sootup.core.views.View;

/** The default {@link CallGraphScope}: excludes calls originating from library classes. */
public class DefaultCallGraphScope implements CallGraphScope {

  @NonNull private final View view;

  public DefaultCallGraphScope(@NonNull View view) {
    this.view = view;
  }

  @Override
  public boolean includeCall(@NonNull SootMethod method, @NonNull InvokableStmt statement) {
    return view.getClass(method.getDeclaringClassType())
        .map(sc -> !sc.isLibraryClass())
        .orElse(true);
  }
}
