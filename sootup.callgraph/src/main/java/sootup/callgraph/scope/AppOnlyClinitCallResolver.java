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
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.views.View;

/**
 * A {@link VirtualCallResolver} that drops {@code <clinit>} candidates declared on a library class
 * (see {@link SootClass#isLibraryClass()}) and delegates every other candidate - including {@code
 * <clinit>}s of application classes - to a wrapped {@link VirtualCallResolver} (default: {@link
 * VirtualCallResolver#all()}).
 *
 * <p>Paired with {@code seedEntryPointClinits=false} on {@code AbstractCallGraphAlgorithm}, this is
 * equivalent to Soot/Qilin's classic {@code APP} static-initializer handling mode: like {@code
 * ON_THE_FLY}, every {@code <clinit>} triggered during traversal is modeled as it's discovered,
 * except those belonging to library classes.
 */
public class AppOnlyClinitCallResolver implements VirtualCallResolver {

  @NonNull private final View view;
  @NonNull private final VirtualCallResolver delegate;

  public AppOnlyClinitCallResolver(@NonNull View view) {
    this(view, VirtualCallResolver.all());
  }

  public AppOnlyClinitCallResolver(@NonNull View view, @NonNull VirtualCallResolver delegate) {
    this.view = view;
    this.delegate = delegate;
  }

  @Override
  @NonNull
  public ExplorationVerdict tryAdvanceCall(
      @NonNull SootMethod caller,
      @NonNull MethodSignature callee,
      @NonNull InvokableStmt statement) {
    if (isLibraryClinit(callee)) {
      return ExplorationVerdict.STOP;
    }
    return delegate.tryAdvanceCall(caller, callee, statement);
  }

  private boolean isLibraryClinit(@NonNull MethodSignature callee) {
    if (!view.getIdentifierFactory().isStaticInitializerSubSignature(callee.getSubSignature())) {
      return false;
    }
    return view.getClass(callee.getDeclClassType()).map(SootClass::isLibraryClass).orElse(false);
  }
}
