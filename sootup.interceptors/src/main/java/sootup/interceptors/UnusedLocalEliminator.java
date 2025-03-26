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
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.transform.BodyInterceptor;
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
   * Collects all used Locals.
   *
   * <p>Removes unused local variables from the List of Stmts of the given {@link Body}. Complexity
   * is linear with respect to the statements.
   *
   * @param builder the BodyBuilder.
   */
  @Override
  public void interceptBody(Body.@NonNull BodyBuilder builder, @NonNull View view) {

    // recreate Set of Locals from Stmts
    Set<Local> locals = new LinkedHashSet<>();

    // traverse statements copying all used uses and defs
    for (Stmt stmt : builder.getStmtGraph().getNodes()) {
      stmt.getUsesAndDefs()
          .filter(value -> value instanceof Local)
          .forEach(
              value -> {
                Local local = (Local) value;
                locals.add(local);
              });
    }

    builder.setLocals(locals);
  }
}
