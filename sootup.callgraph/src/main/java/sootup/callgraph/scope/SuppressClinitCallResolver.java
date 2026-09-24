package sootup.callgraph.scope;

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

import org.jspecify.annotations.NonNull;
import sootup.core.jimple.common.stmt.InvokableStmt;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.views.View;

/**
 * A {@link VirtualCallResolver} that drops every {@code <clinit>} candidate and delegates every
 * other candidate to a wrapped {@link VirtualCallResolver} (default: {@link
 * VirtualCallResolver#all()}).
 *
 * <p>Combined with {@code seedEntryPointClinits} on {@code AbstractCallGraphAlgorithm}, this
 * reproduces two of Soot/Qilin's classic static-initializer handling modes: paired with {@code
 * seedEntryPointClinits=true} it is equivalent to {@code FULL} (only entry points' own {@code
 * <clinit>}s are modeled); paired with {@code seedEntryPointClinits=false} it is equivalent to
 * {@code NONE} (no {@code <clinit>} call is modeled at all).
 */
public class SuppressClinitCallResolver implements VirtualCallResolver {

  @NonNull private final View view;
  @NonNull private final VirtualCallResolver delegate;

  public SuppressClinitCallResolver(@NonNull View view) {
    this(view, VirtualCallResolver.all());
  }

  public SuppressClinitCallResolver(@NonNull View view, @NonNull VirtualCallResolver delegate) {
    this.view = view;
    this.delegate = delegate;
  }

  @Override
  @NonNull
  public ExplorationVerdict tryAdvanceCall(
      @NonNull SootMethod caller,
      @NonNull MethodSignature callee,
      @NonNull InvokableStmt statement) {
    if (view.getIdentifierFactory().isStaticInitializerSubSignature(callee.getSubSignature())) {
      return ExplorationVerdict.STOP;
    }
    return delegate.tryAdvanceCall(caller, callee, statement);
  }
}
