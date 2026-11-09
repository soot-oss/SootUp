package sootup.interceptors;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2020 Raja Vallée-Rai, Marcus Nachtigall and others
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
 *
 */
import java.util.LinkedHashSet;
import java.util.Set;
import org.jspecify.annotations.NonNull;
import sootup.core.interceptor.BodyInterceptor;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.views.View;

/**
 * The UnusedLocalEliminator removes any unused locals from the method. TODO: ms: it basically
 * collects all locals and assigns the collection of Locals to the Body... i.e. its use replaces the
 * need of a validator and the manual assignment of the Locals to the body.
 *
 * @author Marcus Nachtigall
 */
public class UnusedLocalEliminator implements BodyInterceptor {

  /**
   * Removes unused locals while retaining the builder's local-chain order.
   *
   * <p>Removes unused local variables from the List of Stmts of the given {@link Body}. Complexity
   * is linear with respect to the statements.
   *
   * @param builder the BodyBuilder.
   */
  @Override
  public void interceptBody(Body.@NonNull BodyBuilder builder, @NonNull View view) {

    // - Traverse builder.getStmts() rather than getControlFlowGraph().getNodes() to maintain
    // deterministic
    //   linear statement evaluation order instead of unpredictable CFG graph node iteration order.
    // - Filter builder.getLocals() to retain existing local variable chain ordering. Rebuilding the
    // local set
    //    from scratch based on statement uses.
    // - Append newly referenced but undeclared locals at the end deterministically.
    Set<Local> referencedLocals = new LinkedHashSet<>();
    for (Stmt stmt : builder.getStmts()) {
      stmt.getUsesAndDefs().stream()
          .filter(value -> value instanceof Local)
          .map(value -> (Local) value)
          .forEach(referencedLocals::add);
    }

    Set<Local> retainedLocals = new LinkedHashSet<>();
    for (Local local : builder.getLocals()) {
      if (referencedLocals.remove(local)) {
        retainedLocals.add(local);
      }
    }
    // Interceptors may introduce a referenced local through a statement before registering it on
    // the builder. Keep such locals deterministically after the established chain.
    retainedLocals.addAll(referencedLocals);
    builder.setLocals(retainedLocals);
  }
}
