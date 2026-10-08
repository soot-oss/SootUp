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

import java.util.Collections;
import java.util.List;
import org.jspecify.annotations.NonNull;
import sootup.core.jimple.common.expr.JDynamicInvokeExpr;

/** {@link DynamicInvokeResolver} that leaves every invokedynamic call site unresolved. */
final class NoDynamicInvokeResolver implements DynamicInvokeResolver {

  static final NoDynamicInvokeResolver INSTANCE = new NoDynamicInvokeResolver();

  private NoDynamicInvokeResolver() {}

  @NonNull
  @Override
  public List<DynamicInvokeTarget> resolve(@NonNull JDynamicInvokeExpr expr) {
    return Collections.emptyList();
  }
}
