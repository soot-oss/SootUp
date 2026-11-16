package sootup.callgraph.invokedynamic;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2026 Markus Schmidt
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

import java.util.List;
import org.jspecify.annotations.NonNull;
import sootup.core.jimple.common.expr.JDynamicInvokeExpr;
import sootup.core.model.Body;
import sootup.core.model.SootMethod;
import sootup.core.views.View;

/** {@link DynamicInvokeResolver#withCreationSiteEdges()}: delegates, with the flag set. */
final class CreationSiteEdgesResolver implements DynamicInvokeResolver {

  private final DynamicInvokeResolver delegate;

  CreationSiteEdgesResolver(DynamicInvokeResolver delegate) {
    this.delegate = delegate;
  }

  @NonNull
  @Override
  public List<DynamicInvokeTarget> resolve(@NonNull JDynamicInvokeExpr expr) {
    return delegate.resolve(expr);
  }

  @Override
  public boolean creationSiteEdges() {
    return true;
  }

  @NonNull
  @Override
  public DynamicInvokeResolver withCreationSiteEdges() {
    return this;
  }

  @NonNull
  @Override
  public Body desugar(@NonNull SootMethod method, @NonNull Body body, @NonNull View view) {
    return delegate.desugar(method, body, view);
  }
}
