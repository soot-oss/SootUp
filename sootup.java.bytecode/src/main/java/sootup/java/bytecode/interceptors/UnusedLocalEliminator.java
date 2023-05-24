package sootup.java.bytecode.interceptors;
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
import javax.annotation.Nonnull;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.transform.BodyInterceptor;
import sootup.core.views.View;

/**
 * A BodyTransformer that removes all unused local variables from a given Body.
 *
 * @author Marcus Nachtigall
 */
public class UnusedLocalEliminator implements BodyInterceptor {

  /**
   * Removes unused local variables from the List of Stmts of the given {@link Body}. Complexity is
   * linear with respect to the statements.
   *
   * @param builder the BodyBuilder.
   */
  @Override
  public void interceptBody(@Nonnull Body.BodyBuilder builder, @Nonnull View<?> view) {

    Set<Local> locals = new LinkedHashSet<>();

    // Traverse statements copying all used uses and defs
    for (Stmt stmt : builder.getStmtGraph()) {
      for (Value value : stmt.getUsesAndDefs()) {
        if (value instanceof Local) {
          Local local = (Local) value;
          locals.add(local);
        }
      }
    }

    builder.setLocals(locals);
  }
}
